package skyproc;

import lev.*;
import skyproc.SPGlobal.Language;
import skyproc.SubStringPointer.Files;
import skyproc.exceptions.BadMod;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.gui.SPProgressBarPlug;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.DataFormatException;

import static skyproc.SPImporter.extractHeaderInfo;

/**
 * A mod is a collection of GRUPs which contain records. Mods are used to create
 * patches by creating an empty one, adding records, and then calling its export
 * function.
 *
 * @author Justin Swanson
 */
public class Mod implements Comparable, Iterable<GRUP> {

    final TES4 tes = new TES4();
    ModListing modInfo;
    final Map<GRUP_TYPE, GRUP> GRUPs = new EnumMap<>(GRUP_TYPE.class);
    LInChannel input;
    Language language = Language.English;
    final Map<ModListing, Integer> masterMap = new HashMap<>();
    final Map<SubStringPointer.Files, Map<Integer, Integer>> stringLocations = new EnumMap<>(SubStringPointer.Files.class);
    final Map<SubStringPointer.Files, LImport> stringStreams = new EnumMap<>(SubStringPointer.Files.class);
    private final ArrayList<String> outStrings = new ArrayList<>();
    private final ArrayList<String> outDLStrings = new ArrayList<>();
    private final ArrayList<String> outILStrings = new ArrayList<>();

    /**
     * Creates an empty Mod with the name and master flag set to match info.
     *
     * @param info ModListing object containing name and master flag.
     * @see ModListing
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Mod(ModListing info) {
        init(info);
        SPDatabase.add(this);
    }

    /**
     * Creates an empty Mod with the name and master flag set to parameters.
     *
     * @param name   String to set the Mod name to.
     * @param master Sets the Mod's master flag (which appends ".esp" and ".esm"
     *               to the modname as appropriate)
     */
    public Mod(String name, Boolean master) {
        this(new ModListing(name, master));
    }

    @SuppressWarnings("LeakingThisInConstructor")
    Mod(ModListing info, ByteBuffer headerInfo) throws Exception {
        this(info, true);
        SPGlobal.logMod(this, "MOD", "Parsing header");
        if (!headerInfo.hasRemaining()) {
            throw new BadMod(info.print() + " did not have a TES4 header.");
        }
        tes.parseData(headerInfo, this);
        if (tes.flags.get(Mod_Flags.MASTER.value) && (!info.getMasterTag())) {
            info.setFalseMaster(true);
        }
    }

    Mod(ModListing info, boolean temp) {
        init(info);
    }

    void deleteStringsFiles() {
        File STRINGS = new File(genStringsPath(SubStringPointer.Files.STRINGS));
        File DLSTRINGS = new File(genStringsPath(SubStringPointer.Files.STRINGS));
        File ILSTRINGS = new File(genStringsPath(SubStringPointer.Files.STRINGS));
        if (STRINGS.exists()) {
            STRINGS.delete();
        }
        if (DLSTRINGS.exists()) {
            DLSTRINGS.delete();
        }
        if (ILSTRINGS.exists()) {
            ILSTRINGS.delete();
        }
    }

    void addGRUP(MajorRecord r) {
        GRUPs.put(GRUP_TYPE.valueOf(r.getType()), new GRUP(r));
    }

    void addGRUPrecursive(MajorRecord r) {
        GRUPs.put(GRUP_TYPE.valueOf(r.getType()), new GRUPRecursive(r));
    }

    final void init(ModListing info) {
        this.modInfo = info;
        this.setFlag(Mod_Flags.MASTER, info.getMasterTag());
        this.setFlag(Mod_Flags.STRING_TABLED, false);
        stringLocations.put(SubStringPointer.Files.STRINGS, new TreeMap<>());
        stringLocations.put(SubStringPointer.Files.DLSTRINGS, new TreeMap<>());
        stringLocations.put(SubStringPointer.Files.ILSTRINGS, new TreeMap<>());
        addGRUP(new GMST());
        addGRUP(new KYWD());
        addGRUP(new TXST());
        addGRUP(new GLOB());
        addGRUP(new FACT());
        addGRUP(new HDPT());
        addGRUP(new RACE());
        addGRUP(new MGEF());
        addGRUP(new ENCH());
        addGRUP(new SPEL());
        addGRUP(new SCRL());
        addGRUP(new ARMO());
        addGRUP(new BOOK());
        addGRUP(new CONT());
        addGRUP(new INGR());
        addGRUP(new MISC());
        addGRUP(new ALCH());
        addGRUP(new COBJ());
        addGRUP(new PROJ());
        addGRUP(new STAT());
        addGRUP(new WEAP());
        addGRUP(new AMMO());
        addGRUP(new NPC_());
        addGRUP(new LVLN());
        addGRUP(new LVLI());
        addGRUP(new WTHR());
        addGRUPrecursive(new DIAL());
        addGRUP(new QUST());
        addGRUP(new IMGS());
        addGRUP(new FLST());
        addGRUP(new PERK());
        addGRUP(new VTYP());
        addGRUP(new AVIF());
        addGRUP(new ARMA());
        addGRUP(new ECZN());
        addGRUP(new LGTM());
        addGRUP(new DLBR());
        addGRUP(new DLVW());
        addGRUP(new OTFT());
    }

    /**
     * Returns the ModListing associated with the nth master of this mod.
     * Changing values on the returned ModListing will affect the mod it is tied
     * to.
     *
     * @param i The index of the master to return.
     * @return The ModListing object associated with the Nth master
     */
    public ModListing getNthMaster(int i) {
        if (tes.getMasters().size() > i && i >= 0) {
            return tes.getMasters().get(i);
        } else {
            return getInfo();
        }
    }

    /**
     * @return The number of masters in the mod.
     */
    public int numMasters() {
        return tes.getMasters().size();
    }

    /**
     * @return True if no GRUP in the mod has any records.
     */
    public boolean isEmpty() {
        for (GRUP g : GRUPs.values()) {
            if (g.numRecords() > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if the esp file exists for this mod in the data folder.
     */
    public boolean exists() {
        File f = new File(SPGlobal.pathToDataFixed + getInfo().print());
        return f.exists();
    }

    FormID getNextID() {
        return new FormID(tes.getHEDR().nextID++, getInfo());
    }

    void resetNextIDcounter() {
        tes.getHEDR().nextID = Mod.HEDR.firstAvailableID;
    }

    void addMaster(ModListing input) {
        if (!getInfo().equals(input)) {
            masterMap.clear();
            tes.addMaster(input);
        }
    }

    void closeStreams() {
        input.close();
        for (LImport c : stringStreams.values()) {
            c.close();
        }
    }

    /**
     * @param query      FormID to look in the mod for.
     * @param grup_types Types of GRUPs to look in. (Optional - searches all if
     *                   left blank)
     * @return The Major Record with the query FormID, or null if not found.
     */
    public MajorRecord getMajor(FormID query, GRUP_TYPE... grup_types) {
        if (query != null && query.getMaster() != null) {
            if (grup_types.length == 0) {
                grup_types = GRUPs.keySet().toArray(new GRUP_TYPE[0]);
            }
            for (GRUP_TYPE g : grup_types) {
                if (!GRUP_TYPE.internal(g)) {
                    GRUP grup = GRUPs.get(g);
                    if (grup != null) {
                        MajorRecord mr = grup.get(query);
                        if (mr != null) {
                            return mr;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param edid       EDID to look in the mod for.
     * @param grup_types Types of GRUPs to look in. (Optional - searches all if
     *                   left blank)
     * @return The Major Record with the query FormID, or null if not found.
     */
    public MajorRecord getMajor(String edid, GRUP_TYPE... grup_types) {
        if (edid != null) {
            if (grup_types.length == 0) {
                grup_types = GRUPs.keySet().toArray(new GRUP_TYPE[0]);
            }
            for (GRUP_TYPE g : grup_types) {
                GRUP grup = GRUPs.get(g);
                if (grup != null) {
                    MajorRecord mr = grup.get(edid);
                    if (mr != null) {
                        return mr;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Makes a copy of the Major Record and loads it into the mod, giving a new
     * Major Record a FormID originating from the mod. This function also
     * automatically adds the new copied record to the mod. This makes two
     * separate records independent of each other.<br><br>
     * <p>
     * CONSISTENCY NOTE: This functions appends "_DUP" to the end of the EDID,
     * and then a random number if that EDID already exists. It is suggested you
     * only use this function if you are only making one duplicate of the
     * record. For multiple duplicates, use the version with a specified EDID,
     * for better consistencyFile results<br><br>
     * <p>
     * COMPILER NOTE: The record returned can only be determined by the compiler
     * to be a Major Record. You must cast it yourself to be the correct type of
     * major record.<br> ex. NPC_ newNPC = (NPC_) myPatch.makeCopy(otherNPC);
     *
     * @param m Major Record to make a copy of and add to the mod.
     * @return The copied record.
     */
    MajorRecord makeCopy(MajorRecord m) {
        m = m.copyOf(this);
        GRUPs.get(GRUP_TYPE.valueOf(m.getType())).addRecord(m);
        return m;
    }

    /**
     * Makes a copy of the Major Record and loads it into the mod, giving a new
     * Major Record a FormID originating from the mod. This function also
     * automatically adds the new copied record to the mod. This makes two
     * separate records independent of each other.<br><br>
     * <p>
     * COMPILER NOTE: The record returned can only be determined by the compiler
     * to be a Major Record. You must cast it yourself to be the correct type of
     * major record.<br> ex. NPC_ newNPC = (NPC_) myPatch.makeCopy(otherNPC);
     *
     * @param m       Major Record to make a copy of and add to the mod.
     * @param newEDID EDID to assign to the new record. Make sure it's unique.
     * @return The copied record, or null if either parameter is null.
     */
    public MajorRecord makeCopy(MajorRecord m, String newEDID) {
        if (m == null || newEDID == null) {
            return null;
        }
        m = m.copyOf(this, newEDID);
        GRUPs.get(GRUP_TYPE.valueOf(m.getType())).addRecord(m);
        return m;
    }

    /**
     * This function requires there to be a GlobalDB set, as it adds the
     * necessary masters from it.<br><br>
     *
     * @param g GRUP to make a copy of.
     * @return ArrayList of duplicated Major Records.
     */
    public ArrayList<MajorRecord> makeCopy(GRUP g) {
        ArrayList<MajorRecord> out = new ArrayList<>();
        for (Object o : g) {
            MajorRecord m = (MajorRecord) o;
            out.add(makeCopy(m));
        }
        return out;
    }

    /**
     * @return An arraylist containing the GRUP_TYPEs of non-empty GRUPs.
     */
    public ArrayList<GRUP_TYPE> getContainedTypes() {
        ArrayList<GRUP_TYPE> out = new ArrayList<>();
        for (GRUP g : GRUPs.values()) {
            if (!g.isEmpty()) {
                out.add(g.getContainedType());
            }
        }
        return out;
    }

    /**
     * This function requires there to be a GlobalDB set, as it adds the
     * necessary masters from it.
     *
     * @param m Major Record to add as an override.
     */
    public void addRecord(MajorRecord m) {
        GRUPs.get(GRUP_TYPE.valueOf(m.getType())).addRecord(m);
    }

    /**
     * Prints each GRUP in the mod to the asynchronous log.
     */
    public void print() {

        if (!getMastersStrings().isEmpty()) {
            SPGlobal.logMod(this, getName(), "=======================================================================");
            SPGlobal.logMod(this, getName(), "======================= Printing Mod Masters ==========================");
            SPGlobal.logMod(this, getName(), "=======================================================================");
            for (String s : getMastersStrings()) {
                SPGlobal.logMod(this, getName(), s);
            }
        }
        for (GRUP g : GRUPs.values()) {
            g.toString();
        }
        SPGlobal.logMod(this, getName(), "------------------------  DONE PRINTING -------------------------------");
    }


    @Override
    public String toString() {
        return getName();
    }

    public ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> tmp = new ArrayList<>();
        for (GRUP g : GRUPs.values()) {
            tmp.addAll(g.allFormIDs());
        }
        ArrayList<FormID> out = new ArrayList<>(tmp.size());
        for (FormID id : tmp) {
            if (id != null) {
                out.add(id);
            } else {
                SPGlobal.logError(this.toString(), "AllFormIDs return null formid reference.");
            }
        }
        return out;
    }

    void openStringStreams() {
        if (this.isFlag(Mod_Flags.STRING_TABLED)) {
            for (Files f : SubStringPointer.Files.values()) {
                addStream(stringStreams, f);
            }
        }
    }

    void addStream(Map<SubStringPointer.Files, LImport> streams, SubStringPointer.Files file) {
        String stringPath = SPImporter.getStringFilePath(this, language, file);
        File stringFile = new File(SPGlobal.pathToDataFixed + stringPath);
        if (stringFile.isFile()) {
            streams.put(file, new LInChannel(stringFile));
            return;
        } else if (BSA.hasBSA(getInfo())) {
            BSA bsa = BSA.getBSA(getInfo());
            if (bsa.hasFile(stringPath)) {
                LByteChannel stream = new LByteChannel();
                stream.openStream(bsa.getFile(stringPath));
                streams.put(file, stream);
            }
            return;
        } else {
            //check for plugin loaded BSA has strings file
            for (BSA theBSA : BSA.getPluginBSAs()) {
                theBSA.loadFolders();
                if (!theBSA.hasFile(stringPath)) {
                    continue;
                }
                LByteChannel stream = new LByteChannel();
                stream.openStream(theBSA.getFile(stringPath));
                streams.put(file, stream);
                return;
            }
            // check for resource loaded BSA with strings file
            for (BSA theBSA : BSA.getResourceBSAa()) {
                theBSA.loadFolders();
                if (!theBSA.hasFile(stringPath)) {
                    continue;
                }
                LByteChannel stream = new LByteChannel();
                stream.openStream(theBSA.getFile(stringPath));
                streams.put(file, stream);
                return;
            }
        }

        SPGlobal.logMod(this, getName(), "No strings file for " + file);
    }

    /**
     * @return The names of all the masters of the mod.
     * @see ModListing
     */
    public ArrayList<String> getMastersStrings() {
        ArrayList<String> out = new ArrayList<>();
        for (ModListing m : tes.getMasters()) {
            out.add(m.print());
        }
        return out;
    }

    /**
     * Returns the ModListings of all the masters of the mod. Note that changing
     * any ModListings will have an effect on their associated mods.
     *
     * @return The ModListings of all the masters of the mod.
     * @see ModListing
     */
    public ArrayList<ModListing> getMasters() {
        ArrayList<ModListing> out = new ArrayList<>();
        for (ModListing m : tes.getMasters()) {
            out.add(m);
        }
        return out;
    }

    /**
     * @return the Map of GRUPs contained in the record.
     */
    public Map<GRUP_TYPE, GRUP> getGRUPs() {
        return GRUPs;
    }

    /**
     * Clears all GRUPS in the Mod except for the GRUPs specified in the
     * parameter.
     *
     * @param grup_type Any amount of GRUPs to keep, separated by commas
     */
    public void keep(GRUP_TYPE... grup_type) {
        ArrayList<GRUP_TYPE> grups = new ArrayList<>(Arrays.asList(grup_type));
        for (GRUP g : GRUPs.values()) {
            if (!grups.contains(g.getContainedType())) {
                g.clear();
            }
        }
    }

    /**
     * This function will merge all GRUPs from the rhs mod into the calling
     * mod.<br> Any records already in the mod will be overwritten by the
     * version from rhs.<br><br> NOTE: Merging will NOT add records from a mod
     * with a matching ModListing. This is to prevent patches taking in
     * information from old versions of themselves.
     *
     * @param rhs        Mod whose GRUPs to add in.
     * @param grup_types Any amount of GRUPs to merge in, separated by commas.
     *                   Leave this empty if you want all GRUPs merged.
     */
    public void addAsOverrides(Mod rhs, GRUP_TYPE... grup_types) {
        if (!this.equals(rhs)) {
            if (grup_types.length == 0) {
                grup_types = GRUPs.keySet().toArray(new GRUP_TYPE[0]);
            }
            ArrayList<GRUP_TYPE> grups = new ArrayList<>(Arrays.asList(grup_types));
            for (GRUP_TYPE t : grups) {
                GRUP g = GRUPs.get(t);
                if (g != null) {
                    g.merge(rhs.GRUPs.get(t));
                }
            }
        }
    }

    /**
     * Iterates through each mod in the ArrayList, in order, and merges them in
     * one by one.<br> This means any conflicting records within the list will
     * end up with the last mod's version.<br><br> NOTE: Merging will NOT add
     * records from a mod with a matching ModListing. This is to prevent patches
     * taking in information from old versions of themselves.
     *
     * @param in         ArrayList of mods to merge in.
     * @param grup_types Any amount of GRUPs to merge in, separated by commas.
     *                   Leave this empty if you want all GRUPs merged.
     */
    public void addAsOverrides(ArrayList<Mod> in, GRUP_TYPE... grup_types) {
        for (Mod m : in) {
            addAsOverrides(m, grup_types);
        }
    }

    /**
     * NOTE: To sort the mods in load order, use a TreeSet.<br> Iterates through
     * each mod in the Set and merges them in one by one.<br> This means any
     * conflicting records within the set will end up with the last mod's
     * version.<br><br> NOTE: Merging will NOT add records from a mod with a
     * matching ModListing. This is to prevent patches taking in information
     * from old versions of themselves.
     *
     * @param in         Set of mods to merge in.
     * @param grup_types Any amount of GRUPs to merge in, separated by commas.
     *                   Leave this empty if you want all GRUPs merged.
     */
    public void addAsOverrides(Collection<Mod> in, GRUP_TYPE... grup_types) {
        for (Mod m : in) {
            addAsOverrides(m, grup_types);
        }
    }

    /**
     * Iterates through each mod in the SPDatabase, in load order, and merges
     * them in one by one.<br> This means any conflicting records within the
     * database will end up with the last mod's version.<br><br> NOTE: Merging
     * will NOT add records from a mod with a matching ModListing. This is to
     * prevent patches taking in information from old versions of themselves.
     *
     * @param db         The SPDatabase to merge in.
     * @param grup_types Any amount of GRUPs to merge in, separated by commas.
     *                   Leave this empty if you want all GRUPs merged.
     */
    public void addAsOverrides(SPDatabase db, GRUP_TYPE... grup_types) {
        addAsOverrides(SPDatabase.modLookup.values(), grup_types);
    }

    /**
     * @return The number of records contained in all the GRUPs in the mod.
     */
    public int numRecords() {
        int sum = 0;
        for (GRUP g : GRUPs.values()) {
            if (g.numRecords() != 0) {
                sum += g.numRecords() + 1;
            }
        }
        return sum;
    }

    /**
     * @return All Major Records from the mod.
     */
    public ArrayList<MajorRecord> getRecords() {
        ArrayList<MajorRecord> out = new ArrayList<>();
        for (GRUP g : GRUPs.values()) {
            out.addAll(g.getRecords());
        }
        return out;
    }

    /**
     * @param id FormID to look for.
     * @return True if mod contains parameter id.
     */
    public boolean contains(FormID id) {
        for (GRUP g : GRUPs.values()) {
            if (g.contains(id)) {
                return true;
            }
        }
        return false;
    }

    int getMasterIndex(ModListing in) {
        Integer out = masterMap.get(in);
        if (out != null) {
            return out;
        } else {
            ArrayList<ModListing> masters = getMasters();
            int i;
            for (i = 0; i < masters.size(); i++) {
                if (masters.get(i).equals(in)) {
                    return i;
                }
            }
            masterMap.put(in, i);
            return i;
        }
    }

    /**
     * Exports the mod to the path designated by SPGlobal.pathToDataFixed.
     *
     * @throws IOException If there are any unforseen disk errors exporting the
     *                     data.
     * @throws BadRecord   If duplicate EDIDs are found in the mod. This has been
     *                     deemed an unacceptable mod format, and is thrown to promote the
     *                     investigation and elimination of duplicate EDIDs.
     * @see SPGlobal
     */
    public void export() throws IOException, BadRecord {
        export(SPGlobal.pathToDataFixed);
    }

    /**
     * Exports the mod to the path given by the parameter. (You shouldn't
     * include the mod name in the string)
     *
     * @param path
     * @throws IOException
     * @throws BadRecord   If duplicate EDIDs are found in the mod. This has been
     *                     deemed an unacceptable mod format, and is thrown to promote the
     *                     investigation and elimination of duplicate EDIDs.
     */
    public void export(String path) throws IOException, BadRecord {
        File tmp = new File(SPGlobal.pathToInternalFiles + "tmp.esp");
        if (tmp.isFile()) {
            tmp.delete();
        }
        File dest = new File(path + getName());
        File backup = new File(SPGlobal.pathToInternalFiles + getName() + ".bak");
        if (backup.isFile()) {
            backup.delete();
        }
        export(tmp);

        Ln.moveFile(dest, backup, false);
        Ln.moveFile(tmp, dest, false);
    }

    void export(File outPath) throws IOException, BadRecord {
        SPGlobal.logMain("Mod Export", "Exporting " + this);

        ModExporter out = new ModExporter(outPath, this);

        // check leveled lists don't have more than 255 entries
        // and reduces and split as needed
        GRUP<LVLI> lvli = GRUPs.get(GRUP_TYPE.LVLI);
        validateListEntries(lvli);
        GRUP<LVLN> lvln = GRUPs.get(GRUP_TYPE.LVLN);
        validateListEntries(lvln);

        ArrayList<GRUP> exportGRUPs = new ArrayList<>();

        // Progress Bar Setup
        for (GRUP g : GRUPs.values()) {
            if (!g.isEmpty()) {
                exportGRUPs.add(g);
            }
        }
        SPProgressBarPlug.reset();
        SPProgressBarPlug.setMax(exportGRUPs.size() + 6, "Exporting " + this);
        ArrayList<FormID> allForms = allFormIDs();

        // Add all mods that contained any of the FormIDs used.
        SPProgressBarPlug.setStatusNumbered("Adding Masters From Records");
        if (SPGlobal.getAllModsAsMasters()) {
            ArrayList<ModListing> addedMods = SPImporter.getActiveModList();
            for (ModListing m : addedMods) {
                addMaster(m);
            }
        } else {
            Set<ModListing> addedMods = new HashSet<>();
            for (FormID ID : allForms) {
                if (!ID.isNull()) {
                    ModListing master = ID.getMaster();
                    if (!addedMods.contains(master)) {
                        addMaster(master);
                        addedMods.add(master);
                    }
                }
            }
            SPProgressBarPlug.incrementBar();

            // Go through each record, and add all mods that reference that record
            // Just to symbolize that they "had part" in the patch
            // And help encourage repatching when mods are removed.
            SPProgressBarPlug.setStatusNumbered("Adding Masters From Contributors");
            if (!SPGlobal.mergeMode) {
                for (GRUP<MajorRecord> g : this) {
                    for (MajorRecord major : g) {
                        FormID id = major.getForm();
                        for (Mod mod : SPDatabase.getImportedMods()) {
                            if (mod.contains(id) && !addedMods.contains(mod.getInfo())
                                    && !mod.equals(SPGlobal.getGlobalPatch())) {
                                addMaster(mod.getInfo());
                                addedMods.add(mod.getInfo());
                            }
                        }
                    }
                }
            }
        }
        SPProgressBarPlug.incrementBar();

        // Sort masters to match load order
        sortMasters();
        SPProgressBarPlug.incrementBar();

        // Export Header
        tes.setNumRecords(numRecords());
        if (SPGlobal.logging()) {
            SPGlobal.logSync(this.getName(), "Exporting " + tes.getHEDR().numRecords + " records.");
            SPGlobal.logSync(this.getName(), "Masters: ");
            int i = 0;
            for (String s : this.getMastersStrings()) {
                SPGlobal.logSync(this.getName(), "   " + Ln.printHex(i++) + "  " + s);
            }
        }

        tes.export(out);

        // Export GRUPs
        for (GRUP g : exportGRUPs) {
            SPProgressBarPlug.setStatusNumbered("Exporting " + this + ": " + g.getContainedType());
            g.export(out);
            SPProgressBarPlug.incrementBar();
        }

        // Export or clean up STRINGS files
        if (this.isFlag(Mod_Flags.STRING_TABLED)) {
            SPProgressBarPlug.setStatusNumbered("Exporting " + this + ": STRINGS files");
            exportStringsFile(outStrings, SubStringPointer.Files.STRINGS);
            exportStringsFile(outDLStrings, SubStringPointer.Files.DLSTRINGS);
            exportStringsFile(outILStrings, SubStringPointer.Files.ILSTRINGS);
            SPProgressBarPlug.incrementBar();
        } else {
            deleteStringsFiles();
        }
        out.close();

        // Check if any duplicate EDIDS or FormIDS
        SPProgressBarPlug.setStatusNumbered("Checking for Duplicates");
        Map<String, MajorRecord> edids = new HashMap<>();
        Set<FormID> IDs = new HashSet<>();
        boolean bad = false;
        for (GRUP g : GRUPs.values()) {
            for (Object o : g.listRecords) {
                MajorRecord m = (MajorRecord) o;
                if (edids.containsKey(m.getEDID())
                        && (m.getFormMaster().equals(SPGlobal.getGlobalPatch().modInfo)
                        || edids.get(m.getEDID()).getFormMaster().equals(SPGlobal.getGlobalPatch().modInfo))) {
                    SPGlobal.logError("EDID Check", "Error! Duplicate EDID " + m);
                    SPGlobal.logError("EDID Check", "    With: " + edids.get(m.getEDID()));
                    bad = true;
                } else {
                    edids.put(m.getEDID(), m);
                }
                if (IDs.contains(m.getForm())) {
                    SPGlobal.logError("FormID Check", "Error! Duplicate FormID " + m);
                    bad = true;
                } else {
                    IDs.add(m.getForm());
                }
            }
        }
        SPProgressBarPlug.incrementBar();
        if (bad) {
            throw new BadRecord("Duplicate EDIDs or FormIDs.  Check logs for a listing.");
        }

        SPProgressBarPlug.setStatusNumbered("Validating Record Lengths");
        // Validate all record lengths are correct
        if (!NiftyFunc.validateRecordLengths(outPath, 1)) {
            SPGlobal.logError("Record Length Check", "Record lengths were off.");
            throw new BadRecord("Record lengths are off.");
        }
        SPProgressBarPlug.incrementBar();

        SPProgressBarPlug.setStatusNumbered("Exporting Consistency File");
        if (Consistency.automaticExport) {
            Consistency.export();
        }
        SPProgressBarPlug.incrementBar();
    }

    /**
     * Exports the master list of this mod to "Files/Last Masterlist.txt"<br>
     * Used for checking if patches are needed.
     *
     * @param path
     * @throws IOException
     */
    public void exportMasterList(String path) throws IOException {
        File masterListTmp = new File(SPGlobal.pathToInternalFiles + "Last Masterlist Temp.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(masterListTmp))) {
            for (ModListing m : this.getMasters()) {
                writer.write(m.toString() + "\n");
            }
        }

        File masterList = new File(path);
        if (masterList.isFile()) {
            masterList.delete();
        }

        Ln.moveFile(masterListTmp, masterList, false);
    }

    int addOutString(String in, SubStringPointer.Files file) {
        switch (file) {
            case DLSTRINGS:
                return addOutString(in, outDLStrings);
            case ILSTRINGS:
                return addOutString(in, outILStrings);
            default:
                return addOutString(in, outStrings);
        }
    }

    int addOutString(String in, ArrayList<String> list) {
        if (!list.contains(in)) {
            list.add(in);
        }
        return list.indexOf(in) + 1;  // To prevent indexing starting at 0
    }

    String genStringsPath(SubStringPointer.Files file) {
        return SPGlobal.pathToDataFixed + "Strings/" + getNameNoSuffix() + "_" + SPGlobal.language + "." + file;
    }

    void sortMasters() {
        SubList<ModListing, ModListing> sublist = tes.getMasters();
        for (ModListing m : sublist.collection) {
            if (!(m.master || m.falseMaster)) {
                try {
                    RecordFileChannel inputChannel = new RecordFileChannel(SPGlobal.pathToDataFixed + m.print());
                    Mod plugin = new Mod(m, extractHeaderInfo(inputChannel)); // sets false master flag for the ModListing
                } catch (Exception e) {
                    if (SPGlobal.logging()) {
                        SPGlobal.logSync("Sort Masters", "Could not read mod file: " + m.print() + ", Sorting based on extension.");
                    }
                }
            }
        }
        sublist.sort();
    }

    void exportStringsFile(ArrayList<String> list, SubStringPointer.Files file) throws IOException {
        int stringLength = 0;
        for (String s : list) {
            stringLength += s.length() + 1;

        }
        int outLength = stringLength + 8 * list.size() + 8;
        LOutFile out = new LOutFile(genStringsPath(file));

        out.write(list.size());
        out.write(stringLength);

        int i = 1;  // To prevent indexing starting at 0
        int offset = 0;
        for (String s : list) {
            out.write(i++);
            out.write(offset);
            offset += s.length() + 1;
        }

        for (String s : list) {
            out.write(s);
            out.write(0, 1);  // Null terminator
        }

        list.clear();
        out.close();
    }

    void validateListEntries(GRUP g) {
        ArrayList<MajorRecord> records = new ArrayList<>(g.getRecords());
        for (MajorRecord record : records) {
            LeveledRecord leveledRec = (LeveledRecord) record;
            if (leveledRec != null) {
                if (leveledRec.numEntries() > 255) {
                    leveledRec.splitEntries();
                }
            }
        }
    }

    /**
     * Sets the author name of the mod.
     *
     * @return The author of the mod
     */
    public String getAuthor() {
        return tes.getAuthor();
    }

    /**
     * Sets the author name of the mod.
     *
     * @param in Your name here.
     */
    public void setAuthor(String in) {
        tes.setAuthor(in);
    }

    /**
     * Sets the description of the mod.
     *
     * @return The description of the mod
     */
    public String getDescription() {
        return tes.getDescription();
    }

    /**
     * Sets the description of the mod.
     *
     * @param in the description.
     */
    public void setDescription(String in) {
        tes.setDescription(in);
    }

    void parseData(String type, LImport data) throws Exception {
        GRUPs.get(GRUP_TYPE.valueOf(type)).parseData(data, this);
    }

    /**
     * Returns whether the given flag is on or off. <br> <br> An example use of
     * this function is as follows: <br> boolean isaMasterMod =
     * myMod.isFlag(Mod_Flags.MASTER);
     *
     * @param flag Mod_Flags enum to check.
     * @return True if the given flag is on in the mod.
     * @see Mod_Flags
     */
    public boolean isFlag(Mod_Flags flag) {
        return tes.flags.get(flag.value);
    }

    /**
     * Sets the given flag in the mod. <br> <br> An example of using this
     * function to set its master flag is as follows: <br>
     * myMod.setFlag(Mod_Flags.MASTER, true);
     *
     * @param flag Mod_Flags enum to set.
     * @param on   What to set the flag to.
     * @see Mod_Flags
     */
    public final void setFlag(Mod_Flags flag, boolean on) {
        tes.flags.set(flag.value, on);
        if (flag == Mod_Flags.MASTER) {
            getInfo().setMasterTag(on);
        }
    }

    // Get Set

    /**
     * @return The GRUP containing Leveled List records.
     * @see GRUP
     */
    public GRUP<LVLN> getLeveledCreatures() {
        return GRUPs.get(GRUP_TYPE.LVLN);
    }

    /**
     * @return The GRUP containing NPC records.
     * @see GRUP
     */
    public GRUP<NPC_> getNPCs() {
        return GRUPs.get(GRUP_TYPE.NPC_);
    }

    /**
     * @return The GRUP containing Quest records.
     * @see GRUP
     */
    public GRUP<QUST> getQuests() {
        return GRUPs.get(GRUP_TYPE.QUST);
    }

    /**
     * @return The GRUP containing Image Space records.
     * @see GRUP
     */
    public GRUP<IMGS> getImageSpaces() {
        return GRUPs.get(GRUP_TYPE.IMGS);
    }

    /**
     * @return The GRUP containing Perk records.
     * @see GRUP
     */
    public GRUP<PERK> getPerks() {
        return GRUPs.get(GRUP_TYPE.PERK);
    }

    /**
     * @return The GRUP containing Spell records.
     * @see GRUP
     */
    public GRUP<SPEL> getSpells() {
        return GRUPs.get(GRUP_TYPE.SPEL);
    }

    /**
     * @return The GRUP containing Race records
     * @see GRUP
     */
    public GRUP<RACE> getRaces() {
        return GRUPs.get(GRUP_TYPE.RACE);
    }

    /**
     * @return The GRUP containing Armor records
     * @see GRUP
     */
    public GRUP<ARMO> getArmors() {
        return GRUPs.get(GRUP_TYPE.ARMO);
    }

    /**
     * @return The GRUP containing Armature records
     * @see GRUP
     */
    public GRUP<ARMA> getArmatures() {
        return GRUPs.get(GRUP_TYPE.ARMA);
    }

    /**
     * @return The GRUP containing Texture records
     * @see GRUP
     */
    public GRUP<TXST> getTextureSets() {
        return GRUPs.get(GRUP_TYPE.TXST);
    }

    /**
     * @return The GRUP containing Weapon records
     * @see GRUP
     */
    public GRUP<WEAP> getWeapons() {
        return GRUPs.get(GRUP_TYPE.WEAP);
    }

    /**
     * @return The GRUP containing Keyword records
     * @see GRUP
     */
    public GRUP<KYWD> getKeywords() {
        return GRUPs.get(GRUP_TYPE.KYWD);
    }

    /**
     * @return The GRUP containing Keyword records
     * @see GRUP
     */
    public GRUP<FLST> getFormLists() {
        return GRUPs.get(GRUP_TYPE.FLST);
    }

    /**
     * @return The GRUP containing Magic Effect records
     * @see GRUP
     */
    public GRUP<MGEF> getMagicEffects() {
        return GRUPs.get(GRUP_TYPE.MGEF);
    }

    /**
     * @return The GRUP containing Alchemy records
     * @see GRUP
     */
    public GRUP<ALCH> getAlchemy() {
        return GRUPs.get(GRUP_TYPE.ALCH);
    }

    /**
     * @return The GRUP containing Ingredient records
     * @see GRUP
     */
    public GRUP<INGR> getIngredients() {
        return GRUPs.get(GRUP_TYPE.INGR);
    }

    /**
     * @return The GRUP containing Ammo records
     * @see GRUP
     */
    public GRUP<AMMO> getAmmo() {
        return GRUPs.get(GRUP_TYPE.AMMO);
    }

    /**
     * @return The GRUP containing Faction records
     * @see GRUP
     */
    public GRUP<FACT> getFactions() {
        return GRUPs.get(GRUP_TYPE.FACT);
    }

    /**
     * @return The GRUP containing Game Setting records
     * @see GRUP
     */
    public GRUP<GMST> getGameSettings() {
        return GRUPs.get(GRUP_TYPE.GMST);
    }

    /**
     * @return The GRUP containing Enchantments records
     * @see GRUP
     */
    public GRUP<ENCH> getEnchantments() {
        return GRUPs.get(GRUP_TYPE.ENCH);
    }

    /**
     * @return The GRUP containing Leveled Items records
     * @see GRUP
     */
    public GRUP<LVLI> getLeveledItems() {
        return GRUPs.get(GRUP_TYPE.LVLI);
    }

    /**
     * @return The GRUP containing ActorValue records
     * @see GRUP
     */
    public GRUP<AVIF> getActorValues() {
        return GRUPs.get(GRUP_TYPE.AVIF);
    }

    /**
     * @return The GRUP containing Encounter Zone records
     * @see GRUP
     */
    public GRUP<ECZN> getEncounterZones() {
        return GRUPs.get(GRUP_TYPE.ECZN);
    }

    /**
     * @return The GRUP containing Global records
     * @see GRUP
     */
    public GRUP<GLOB> getGlobals() {
        return GRUPs.get(GRUP_TYPE.GLOB);
    }

    /**
     * @return The GRUP containing Constructible Object records
     * @see GRUP
     */
    public GRUP<COBJ> getConstructibleObjects() {
        return GRUPs.get(GRUP_TYPE.COBJ);
    }

    /**
     * @return The GRUP containing Misc Object records
     * @see GRUP
     */
    public GRUP<MISC> getMiscObjects() {
        return GRUPs.get(GRUP_TYPE.MISC);
    }

    /**
     * @return The GRUP containing outfit records
     * @see GRUP
     */
    public GRUP<OTFT> getOutfits() {
        return GRUPs.get(GRUP_TYPE.OTFT);
    }

    /**
     * @return The GRUP containing head part records
     * @see GRUP
     */
    public GRUP<HDPT> getHeadParts() {
        return GRUPs.get(GRUP_TYPE.HDPT);
    }

    /**
     * @return The GRUP containing projectile records
     * @see GRUP
     */
    public GRUP<PROJ> getProjectiles() {
        return GRUPs.get(GRUP_TYPE.PROJ);
    }

    /**
     * @return The GRUP containing lighting template records
     * @see GRUP
     */
    public GRUP<LGTM> getLightingTemplates() {
        return GRUPs.get(GRUP_TYPE.LGTM);
    }

    /**
     * @return The GRUP containing Book records
     * @see GRUP
     */
    public GRUP<BOOK> getBooks() {
        return GRUPs.get(GRUP_TYPE.BOOK);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<WTHR> getWeathers() {
        return GRUPs.get(GRUP_TYPE.WTHR);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<DIAL> getDialogs() {
        return GRUPs.get(GRUP_TYPE.DIAL);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<DLBR> getDialogBranches() {
        return GRUPs.get(GRUP_TYPE.DLBR);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<DLVW> getDialogViews() {
        return GRUPs.get(GRUP_TYPE.DLVW);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<CONT> getContainers() {
        return GRUPs.get(GRUP_TYPE.CONT);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<VTYP> getVoiceTypes() {
        return GRUPs.get(GRUP_TYPE.VTYP);
    }

    /**
     * @return
     * @see GRUP
     */
    public GRUP<STAT> getStatics() {
        return GRUPs.get(GRUP_TYPE.STAT);
    }

    /**
     * @return the GRUP containing scroll records
     * @see GRUP
     */
    public GRUP<SCRL> getScrolls() {
        return GRUPs.get(GRUP_TYPE.SCRL);
    }

    /**
     * @return The name of the mod (including suffix)
     */
    public String getName() {
        return modInfo.print();
    }

    /**
     * @return The ModListing object associated with the mod.
     * @see ModListing
     */
    public ModListing getInfo() {
        return modInfo;
    }

    /**
     * @return The name of the mod (without suffix)
     */
    public String getNameNoSuffix() {
        return getName().substring(0, getName().indexOf(".es"));
    }

    /**
     * A compare function used for sorting Mods in load order. <br> .esp > .esm
     * <br> later date > earlier date <br>
     *
     * @param o Another Mod object
     * @return 1: Mod is later in the load order <br> -1: Mod is earlier in the
     * load order <br> 0: Mod is equal in load order (should not happen)
     */
    @Override
    public int compareTo(Object o) {
        Mod rhs = (Mod) o;
        return this.getInfo().compareTo(rhs.getInfo());
    }

    /**
     * An equals function that compares only mod name. It does NOT compare mod
     * contents.
     *
     * @param obj
     * @return True if obj is a mod with the same name and suffix (Skyrim.esm ==
     * Skyrim.esm)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ModListing) {
            return getInfo().equals(obj);
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mod other = (Mod) obj;
        return this.getName().equalsIgnoreCase(other.getName());
    }

    /**
     * @param id
     */
    public void remove(FormID id) {
        for (GRUP g : GRUPs.values()) {
            if (g.contains(id)) {
                g.removeRecord(id);
            }
        }
    }

    /**
     * A custom hash function that takes the mod header into account for better
     * hashing.
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.modInfo != null ? this.modInfo.hashCode() : 0);
        return hash;
    }

    /**
     * @return An iterator over all the GRUPs in the mod.
     */
    @Override
    public Iterator<GRUP> iterator() {
        return GRUPs.values().iterator();
    }

    /**
     * The various flags found in the Mod header.
     */
    public enum Mod_Flags {

        /**
         * Master flag determines whether a mod is labeled as a master plugin
         * and receives a ".esm" suffix.
         */
        MASTER(0),
        /**
         * String Tabled flag determines whether names and descriptions are
         * stored inside the mod itself, or in external STRINGS files. <br><br>
         * <p>
         * In general, it is suggested to disable this flag for most patches, as
         * the only benefit of external STRINGS files is easy multi-language
         * support. Skyproc can offer this same multilanguage support without
         * external STRINGS files. See SPGlobal.language for more information.
         *
         * @see SPGlobal
         */
        STRING_TABLED(7);
        final int value;

        Mod_Flags(int in) {
            value = in;
        }
    }

    // Internal Classes
    static class TES4 extends Record {

        static final SubPrototype TES4proto = new SubPrototype() {
            @Override
            protected void addRecords() {
                add(new HEDR());
                add(SubString.getNew("CNAM", true));
                add(new SubList<>(new ModListing(), true));
                add(SubString.getNew("SNAM", true));
                add(new SubFormArray("ONAM", 0));
                add(new SubData("SCRN"));
                add(new SubData("INTV"));
                add(new SubData("INCC"));
            }
        };
        private final static byte[] defaultINTV = Ln.parseHexString("C5 26 01 00", 4);
        final SubRecordsDerived subRecords = new SubRecordsDerived(TES4proto);
        private final LFlags flags = new LFlags(4);
        private int fluff1 = 0;
        private int fluff2 = 0;
        private int fluff3 = 0;

        TES4() {
        }

        TES4(LShrinkArray in) throws Exception {
            this();
            parseData(in, null);
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, BadParameter, DataFormatException {
            super.parseData(in, srcMod);
            flags.set(in.extract(4));
            fluff1 = Ln.arrayToInt(in.extractInts(4));
            fluff2 = Ln.arrayToInt(in.extractInts(4));
            fluff3 = Ln.arrayToInt(in.extractInts(4));
            subRecords.importSubRecords(in, srcMod);
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        public String toString() {
            return "HEDR";
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("TES4");
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(flags.export(), 4);
            out.write(fluff1);
            out.write(fluff2);
            out.write(fluff3);
            subRecords.export(out);
        }

        void addMaster(ModListing mod) {
            subRecords.getSubList("MAST").add(mod);
        }

        void clearMasters() {
            subRecords.getSubList("MAST").clear();
        }

        SubList<ModListing, ModListing> getMasters() {
            return subRecords.getSubList("MAST");
        }

        String getAuthor() {
            return subRecords.getSubString("CNAM").print();
        }

        void setAuthor(String in) {
            subRecords.getSubString("CNAM").setString(in);
        }

        String getDescription() {
            return subRecords.getSubString("SNAM").print();
        }

        void setDescription(String in) {
            subRecords.getSubString("SNAM").setString(in);
        }

        @Override
        int getFluffLength() {
            return 16;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return subRecords.length(isStringTabled);
        }

        @Override
        int getSizeLength() {
            return 4;
        }

        HEDR getHEDR() {
            return (HEDR) subRecords.get("HEDR");
        }

        int getNumRecords() {
            return getHEDR().numRecords;
        }

        void setNumRecords(int num) {
            getHEDR().setRecords(num);
        }

        @Override
        Record getNew() {
            return new TES4();
        }

        @Override
        public String print() {
            return toString();
        }
    }

    static class HEDR extends SubRecord<HEDR> {

        static final int firstAvailableID = 0xD62;  // first available ID on empty CS plugins
        byte[] version;
        int numRecords;
        int nextID;

        HEDR() {
            super();
            clear();
        }

        HEDR(LShrinkArray in) throws Exception {
            this();
            parseData(in, null);
        }

        void setRecords(int num) {
            numRecords = num;
        }

        int numRecords() {
            return numRecords;
        }

        void addRecords(int num) {
            setRecords(numRecords() + num);
        }

        int nextID() {
            return nextID++;
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(version);
            out.write(numRecords);
            out.write(nextID);
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, BadParameter, DataFormatException {
            super.parseData(in, srcMod);
            version = in.extract(4);
            numRecords = in.extractInt(4);
            nextID = in.extractInt(4);
        }

        @Override
        SubRecord getNew(String type) {
            return new HEDR();
        }

        final public void clear() {
            version = Ln.parseHexString("D7 A3 70 3F", 4);
            numRecords = 0;
            nextID = firstAvailableID;
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 12;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("HEDR");
        }
    }
}
