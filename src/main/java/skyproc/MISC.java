package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class MISC extends MajorRecordNamed {

    // Static prototypes and definitions
    static final SubPrototype MISCproto = new SubPrototype(MajorRecordNamed.namedProto) {

        @Override
        protected void addRecords() {
            after(new ScriptPackage(), "EDID");
            add(new SubData("OBND", new byte[12]));
            reposition("FULL");
            add(new Model());
            add(SubString.getNew("ICON", true));
            add(new SubForm("YNAM"));
            add(new SubForm("ZNAM"));
            add(new KeywordSet());
            add(new DATA());
            add(SubString.getNew("MICO", true));
            add(new DestructionData());
        }
    };

    // Common Functions
    MISC() {
        super();
        subRecords.setPrototype(MISCproto);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("MISC");
    }

    @Override
    Record getNew() {
        return new MISC();
    }

    /**
     * @return
     * @deprecated use getModelData()
     */
    public String getModel() {
        return subRecords.getModel().getFileName();
    }

    /**
     * @param path
     * @deprecated use getModelData()
     */
    public void setModel(String path) {
        subRecords.getModel().setFileName(path);
    }

    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }


    public int getValue() {
        return getDATA().value;
    }

    /**
     * @param value
     */
    public void setValue(int value) {
        getDATA().value = value;
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


    public KeywordSet getKeywordSet() {
        return subRecords.getKeywords();
    }


    public ScriptPackage getScriptPackage() {
        return subRecords.getScripts();
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

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        MISC e = this;
        if (!(no == null && bo == null && (no instanceof MISC) && (bo instanceof MISC))) {
            final MISC ne = (MISC) no;
            final MISC be = (MISC) bo;
            SubRecords sList = e.subRecords;
            SubRecords nsList = ne.subRecords;
            SubRecords bsList = be.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return e;
    }

    static class DATA extends SubRecord {

        int value = 0;
        float weight = 0;

        DATA() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(value);
            out.write(weight);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            value = in.extractInt(4);
            weight = in.extractFloat();
            if (SPGlobal.logMods) {
                logMod(srcMod, "", "Setting DATA:    Weight: " + weight);
            }
        }

        @Override
        SubRecord getNew(String type) {
            return new MISC.DATA();
        }

        @Override
        int getContentLength(ModExporter out) {
            return 8;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }
    }
}
