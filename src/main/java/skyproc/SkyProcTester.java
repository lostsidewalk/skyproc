package skyproc;

import lev.Ln;
import lev.debug.LDebug;
import lombok.extern.slf4j.Slf4j;
import skyproc.exceptions.BadRecord;
import skyproc.gui.SPDefaultGUI;
import skyproc.gui.SPProgressBarPlug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static skyproc.SPImporter.importActiveMods;

/**
 * @author Justin Swanson
 */
@Slf4j
public class SkyProcTester {

    static ArrayList<FormID> badIDs;
    //    static GRUP_TYPE[] types = {GRUP_TYPE.DIAL};
    static GRUP_TYPE[] types = GRUP_TYPE.values();
    static boolean streaming = false;
    static ArrayList<GRUP_TYPE> skip = new ArrayList<>(List.of(GRUP_TYPE.BOOK));

    /**
     * @param test
     */
    public static void runTests(int test) {
        setSkyProcGlobal();
        badIDs = new ArrayList<>();
        ModListing skyrim = new ModListing("Skyrim.esm");
        badIDs.add(new FormID("018A45", skyrim));  //RiverwoodZone
        badIDs.add(new FormID("00001E", skyrim));  //NoZoneZone
        SPGlobal.testing = true;
        SPDefaultGUI gui = new SPDefaultGUI("Tester Program", "A tester program meant to flex SkyProc.");
        try {
            switch (test) {
                case 1:
                    validateAll();
                    break;
                case 2:
                    importTest();
                    break;
                case 3:
                    copyTest();
                    break;
            }
            gui.finished();
        } catch (Exception e) {
            log.error("EXCEPTION THROWN: {}", e.getMessage());
            gui.finished();
            SPGlobal.logException(e);
        }
        LDebug.wrapUp();
    }

    private static void validateAll() throws Exception {
        ModTestPackage[] mods = {
                new ModTestPackage("Skyrim.esm", "Skyrim.esm", "Update.esm"),
                new ModTestPackage("Dawnguard.esm", "Skyrim.esm", "Update.esm", "Dawnguard.esm"),
                new ModTestPackage("Dragonborn.esm", "Skyrim.esm", "Update.esm", "Dragonborn.esm")
        };
        SPGlobal.checkMissingMasters = false;
        for (ModTestPackage p : mods) {
            if (!validate(p)) {
                break;
            }
        }
        log.info("TESTING COMPLETE");
    }

    private static boolean validate(ModTestPackage p) throws Exception {

        SubStringPointer.shortNull = false;

        FormID.allIDs.clear();

        SPProgressBarPlug.reset();
        SPProgressBarPlug.setMax(types.length);

        boolean exportPass = true;
        boolean idPass = true;
        for (GRUP_TYPE g : types) {
            if (!GRUP_TYPE.unfinished(g) && !GRUP_TYPE.internal(g) && !skip.contains(g)) {
                for (ModListing m : p.importList) {
                    SPImporter.importMod(m, SPGlobal.pathToDataFixed, g);
                }
                if (!test(g, p)) {
                    SPProgressBarPlug.setStatus("FAILED: " + g);
                    exportPass = false;
                    break;
                }
                SPProgressBarPlug.setStatus("Validating DONE");
                for (FormID id : FormID.allIDs) {
                    if (!id.isNull() && id.getMaster() == null && !badIDs.contains(id)) {
                        log.warn("A bad id: " + id);
                        log.warn("Some FormIDs were unstandardized!");
                        return false;
                    }
                }
                SPGlobal.reset();
            }
        }

        SPGlobal.reset();
        return exportPass && idPass;
    }

    private static boolean test(GRUP_TYPE type, ModTestPackage p) throws IOException {
        log.info("Testing {} in {}", type, p.main);
        SPProgressBarPlug.setStatus("Validating " + type);
        SPProgressBarPlug.pause(true);

        boolean passed = true;
        Mod patch = new Mod(new ModListing("Test.esp"));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.addAsOverrides(SPDatabase.getMod(p.main), type);
        // Test to see if stream has been prematurely imported
        if (SPGlobal.streamMode && type != GRUP_TYPE.NPC_) {
            GRUP g = patch.GRUPs.get(type);
            if (!g.listRecords.isEmpty()) {
                MajorRecord m = (MajorRecord) g.listRecords.get(0);
                if (m.subRecords.map.size() > 2) {
                    log.warn("Premature streaming occurred: {}", m);
                    return false;
                }
            }
        }
        // Remove known bad ids
        for (FormID f : badIDs) {
            patch.remove(f);
        }
        patch.setAuthor("Leviathan1753");
        for (ModListing depend : p.importList) {
            patch.addMaster(depend);
        }
        try {
            patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
        } catch (BadRecord ex) {
            SPGlobal.logException(ex);
            log.error("Records lengths were off.");
        }
        passed = passed && NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + "Test.esp", 10);
        File validF = new File("Validation Files/" + type.toString() + "_" + p.main.printNoSuffix() + ".esp");
        if (validF.isFile()) {
            passed = Ln.validateCompare(SPGlobal.pathToDataFixed + "Test.esp", validF.getPath(), 10) && passed;
        } else {
            log.error("Didn't have a source file to validate bytes to.");
        }

        SPProgressBarPlug.pause(false);
        SPProgressBarPlug.incrementBar();
        return passed;
    }

    private static boolean copyTest() throws IOException, BadRecord {
        SPProgressBarPlug.pause(true);

        boolean passed = true;
        Mod merger = new Mod(new ModListing("tmpMerge.esp"));
        merger.addAsOverrides(SPGlobal.getDB());
        for (FormID f : badIDs) {
            merger.remove(f);
        }

        Mod patch = new Mod(new ModListing("Test.esp"));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.setAuthor("Leviathan1753");

        for (GRUP g : merger) {
            for (Object o : g) {
                MajorRecord m = (MajorRecord) o;
                m.copyOf(patch);
            }
        }

        patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
        passed = passed && NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + "Test.esp", 10);

        SPProgressBarPlug.pause(false);
        SPProgressBarPlug.incrementBar();
        return passed;
    }

    /**
     *
     */
    public static void importTest() {
        try {
            importActiveMods();
            Mod patch = new Mod(new ModListing("Test.esp"));
            patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
            patch.addAsOverrides(SPGlobal.getDB());
            patch.allFormIDs();
        } catch (Exception e) {
            SPGlobal.logException(e);
        }
    }

    /**
     *
     */
    public static void parseEmbeddedScripts() {
        try {
            EmbeddedScripts.generateEnums();
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
    }

    private static void setSkyProcGlobal() {
        SPGlobal.createGlobalLog();
        LDebug.timeElapsed = true;
        SPGlobal.streamMode = streaming;
        SPGlobal.logging(true);
        SPGlobal.setGlobalPatch(new Mod(new ModListing("Test", false)));
    }

    private static class ModTestPackage {
        ModListing main;
        ModListing[] importList;

        public ModTestPackage(String main, String... list) {
            this.main = new ModListing(main);
            importList = new ModListing[list.length];
            for (int i = 0; i < list.length; i++) {
                importList[i] = new ModListing(list[i]);
            }
        }
    }
}
