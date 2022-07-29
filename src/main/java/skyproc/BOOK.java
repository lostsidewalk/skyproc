package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.ActorValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class BOOK extends MajorRecordDescription {

    static final SubPrototype BOOKprototype = new SubPrototype(MajorRecordDescription.descProto) {
        @Override
        protected void addRecords() {
            add(new ScriptPackage());
            add(new SubData("OBND", new byte[12]));
            reposition("FULL");
            add(new Model());
            add(new SubString("ICON"));
            add(new SubString("MICO"));
            reposition("DESC");
            add(new DestructionData());
            add(new SubForm("YNAM"));
            add(new SubForm("ZNAM"));
            add(new KeywordSet());
            add(new DATA());
            add(new SubForm("INAM"));
            SubStringPointer cnam = new SubStringPointer("CNAM", SubStringPointer.Files.DLSTRINGS);
            cnam.forceExport = true;
            add(cnam);
        }
    };

    // Common Functions
    BOOK() {
        super();
        subRecords.setPrototype(BOOKprototype);
    }

    //Enums

    @Override
    Record getNew() {
        return new BOOK();
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("BOOK");
    }


    public ScriptPackage getScriptPackage() {
        return subRecords.getScripts();
    }

    /**
     * @return
     * @deprecated use getModelData()
     */
    public String getModel() {
        return subRecords.getModel().getFileName();
    }

    //Get/Set

    /**
     * @param path
     * @deprecated use getModelData()
     */
    public void setModel(String path) {
        subRecords.getModel().setFileName(path);
    }


    public String getInventoryImage() {
        return subRecords.getSubString("ICON").print();
    }

    /**
     * @param path
     */
    public void setInventoryImage(String path) {
        subRecords.setSubString("ICON", path);
    }


    public String getMessageImage() {
        return subRecords.getSubString("MICO").print();
    }

    /**
     * @param path
     */
    public void setMessageImage(String path) {
        subRecords.setSubString("MICO", path);
    }


    public FormID getPickupSound() {
        return subRecords.getSubForm("YNAM").getForm();
    }

    /**
     * @param sound
     */
    public void setPickupSound(FormID sound) {
        subRecords.setSubForm("YNAM", sound);
    }


    public FormID getDropSound() {
        return subRecords.getSubForm("ZNAM").getForm();
    }

    /**
     * @param sound
     */
    public void setDropSound(FormID sound) {
        subRecords.setSubForm("ZNAM", sound);
    }


    public KeywordSet getKeywordSet() {
        return subRecords.getKeywords();
    }

    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }


    public int getValue() {
        return getDATA().value;
    }

    /**
     * @param gold
     */
    public void setValue(int gold) {
        getDATA().value = gold;
    }


    public float getWeight() {
        return getDATA().weight;
    }

    /**
     * @param weight
     */
    public void setWeight(float weight) {
        getDATA().weight = weight;
    }

    /**
     * @param flag
     * @param on
     */
    public void set(BookFlag flag, boolean on) {
        getDATA().flags.set(flag.ordinal(), on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(BookFlag flag) {
        return getDATA().flags.get(flag.ordinal());
    }


    public ActorValue getTeachesAV() {
        return getDATA().teachesAV;
    }

    /**
     * @param val
     */
    public void setTeachesAV(ActorValue val) {
        getDATA().teachesAV = val;
    }


    public FormID getTeachesSpell() {
        return getDATA().teachesSpell;
    }

    /**
     * @param spell
     */
    public void setTeachesSpell(FormID spell) {
        getDATA().teachesSpell = spell;
    }


    public FormID getInventoryArt() {
        return subRecords.getSubForm("INAM").getForm();
    }

    /**
     * @param id
     */
    public void setInventoryArt(FormID id) {
        subRecords.setSubForm("INAM", id);
    }

    @Override
    public String getDescription() {
        return subRecords.getSubStringPointer("CNAM").print();
    }

    @Override
    public void setDescription(String description) {
        subRecords.setSubStringPointer("CNAM", description);
    }


    public String getText() {
        return subRecords.getSubStringPointer("DESC").print();
    }

    /**
     * @param text
     */
    public void setText(String text) {
        subRecords.setSubStringPointer("DESC", text);
    }

    /**
     * @return List of the AltTextures applied.
     * @deprecated use getModelData()
     */
    public ArrayList<AltTextures.AltTexture> getAltTextures() {
        return subRecords.getModel().getAltTextures();
    }


    public Model getModelData() {
        return subRecords.getModel();
    }


    public enum BookFlag {


        TeachesSkill,

        CantBeTaken,

        TeachesSpell,

        Read
    }

    // Static prototypes and definitions
    static final class DATA extends SubRecord {

        final LFlags flags = new LFlags(4);
        ActorValue teachesAV = ActorValue.AbsorbChance;
        FormID teachesSpell = new FormID();
        int value = 0;
        float weight = 0;

        DATA() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(flags.export());
            if (flags.get(BookFlag.TeachesSkill.ordinal())) {
                out.write(ActorValue.value(teachesAV));
            } else if (flags.get(BookFlag.TeachesSpell.ordinal())) {
                teachesSpell.export(out);
            } else {
                out.write(-1);
            }
            out.write(value);
            out.write(weight);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            flags.set(in.extract(4));
            if (flags.get(BookFlag.TeachesSpell.ordinal())) {
                teachesSpell.parseData(in, srcMod);
            } else {
                teachesAV = ActorValue.value(in.extractInt(4));
            }
            value = in.extractInt(4);
            weight = in.extractFloat();
        }

        @Override
        SubRecord getNew(String type) {
            return new DATA();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 16;
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(1);
            out.add(teachesSpell);
            return out;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }
    }
}
