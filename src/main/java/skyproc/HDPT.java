package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class HDPT extends MajorRecordNamed {

    // Static prototypes and definitions
    static final SubPrototype HDPTproto = new SubPrototype(MajorRecordNamed.namedProto) {
        @Override
        protected void addRecords() {
            add(new Model());
            add(new HDPT_Flags("DATA"));
            add(new SubInt("PNAM"));
            add(new SubList<>(new SubForm("HNAM")));
            add(new SubList<>(new SubShell(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(new SubInt("NAM0"));
                    add(SubString.getNew("NAM1", true));
                }
            })));
            add(new SubForm("CNAM"));
            add(new SubForm("TNAM"));
            add(new SubForm("RNAM"));
        }
    };

    // Common Functions
    HDPT() {
        super();
        subRecords.setPrototype(HDPTproto);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("HDPT");
    }

    @Override
    Record getNew() {
        return new HDPT();
    }

    /**
     * @return
     * @deprecated use getModelData()
     */
    public String getModel() {
        return subRecords.getModel().getFileName();
    }

    // Get/Set

    /**
     * @param path
     * @deprecated use getModelData()
     */
    public void setModel(String path) {
        subRecords.getModel().setFileName(path);
    }


    public ArrayList<FormID> getHeadParts() {
        return subRecords.getSubList("HNAM").toPublic();
    }

    /**
     * @param id
     */
    public void addHeadPart(FormID id) {
        subRecords.getSubList("HNAM").add(id);
    }

    /**
     * @param id
     */
    public void removeHeadPart(FormID id) {
        subRecords.getSubList("HNAM").remove(id);
    }


    public void clearHeadParts() {
        subRecords.getSubList("HNAM").clear();
    }


    public FormID getBaseTexture() {
        return subRecords.getSubForm("TNAM").getForm();
    }

    /**
     * @param txst
     */
    public void setBaseTexture(FormID txst) {
        subRecords.setSubForm("TNAM", txst);
    }


    public FormID getResourceList() {
        return subRecords.getSubForm("RNAM").getForm();
    }

    /**
     * @param id
     */
    public void setResourceList(FormID id) {
        subRecords.setSubForm("RNAM", id);
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

    /**
     * @param flag HeadPartFlag to check
     * @return value of flag
     */
    public boolean getHeadPartFlag(skyproc.genenums.HeadPartFlags flag) {
        HDPT_Flags h = (HDPT_Flags) subRecords.get("DATA");
        return h.flags.get(flag.ordinal());
    }

    /**
     * @param flag HeadPartFlag to set
     * @param on   value of flag
     */
    public void setHeadPartFlag(skyproc.genenums.HeadPartFlags flag, boolean on) {
        HDPT_Flags h = (HDPT_Flags) subRecords.get("DATA");
        h.flags.set(flag.ordinal(), on);
    }

    /**
     * @return HDPT_Type
     */
    public HDPT_Type getHDPT_Type() {
        int i = subRecords.getSubInt("PNAM").get();
        return HDPT_Type.values()[i];
    }

    /**
     * @param t HDPT_Type to set record to
     */
    public void setHDPT_Type(HDPT_Type t) {
        int i = t.ordinal();
        subRecords.getSubInt("PNAM").set(i);
    }


    public enum HDPT_Type {
        Misc,
        Face,
        Eyes,
        Hair,
        Facial_Hair,
        Scar,
        Eyebrows
    }

    static class HDPT_Flags extends SubRecordTyped {

        LFlags flags = new LFlags(1);

        HDPT_Flags(String type) {
            super(type);
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(flags.export(), 1);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            flags = new LFlags(in.extract(1));
        }

        @Override
        SubRecord getNew(String type) {
            return new HDPT_Flags(type);
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 1;
        }
    }

}
