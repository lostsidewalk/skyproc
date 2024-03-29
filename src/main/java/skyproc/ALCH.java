package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Alchemy Records
 *
 * @author Justin Swanson
 */
public class ALCH extends MagicItem {

    // Static prototypes and definitions
    static final SubPrototype ALCHproto = new SubPrototype(MagicItem.magicItemProto) {

        @Override
        protected void addRecords() {
            remove("DESC");
            add(new Model());
            add(new DestructionData());
            add(SubString.getNew("ICON", true));
            add(SubString.getNew("MICO", true));
            add(new SubForm("YNAM"));
            add(new SubForm("ZNAM"));
            add(new SubForm("ETYP"));
            add(new SubFloat("DATA"));
            add(new ENIT());
            reposition("EFID");
        }
    };

    // Common Functions
    ALCH() {
        super();
        subRecords.setPrototype(ALCHproto);
    }

    // Enums

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("ALCH");
    }

    @Override
    Record getNew() {
        return new ALCH();
    }

    /**
     * @return
     * @deprecated use getModelData()
     */
    public String getModel() {
        return subRecords.getModel().getFileName();
    }

    /**
     * @param groundModel
     * @deprecated use getModelData()
     */
    public void setModel(String groundModel) {
        subRecords.getModel().setFileName(groundModel);
    }

    // Get / set


    public FormID getPickupSound() {
        return subRecords.getSubForm("YNAM").getForm();
    }

    /**
     * @param pickupSound
     */
    public void setPickupSound(FormID pickupSound) {
        subRecords.setSubForm("YNAM", pickupSound);
    }


    public FormID getDropSound() {
        return subRecords.getSubForm("ZNAM").getForm();
    }

    /**
     * @param dropSound
     */
    public void setDropSound(FormID dropSound) {
        subRecords.setSubForm("ZNAM", dropSound);
    }

    ENIT getEnit() {
        return (ENIT) subRecords.get("ENIT");
    }


    public int getValue() {
        return getEnit().value;
    }

    /**
     * @param value
     */
    public void setValue(int value) {
        getEnit().value = value;
    }

    /**
     * @param flag
     * @param on
     */
    public void set(ALCHFlag flag, boolean on) {
        getEnit().flags.set(flag.value, on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(ALCHFlag flag) {
        return getEnit().flags.get(flag.value);
    }


    public FormID getAddiction() {
        return getEnit().addiction;
    }

    /**
     * @param addiction
     */
    public void setAddiction(FormID addiction) {
        getEnit().addiction = addiction;
    }


    public FormID getUseSound() {
        return getEnit().useSound;
    }

    /**
     * @param useSound
     */
    public void setUseSound(FormID useSound) {
        getEnit().useSound = useSound;
    }


    public float getWeight() {
        return subRecords.getSubFloat("DATA").get();
    }

    /**
     * @param weight
     */
    public void setWeight(float weight) {
        subRecords.setSubFloat("DATA", weight);
    }


    public String getInventoryIcon() {
        return subRecords.getSubString("ICON").print();
    }

    /**
     * @param filename
     */
    public void setInventoryIcon(String filename) {
        subRecords.setSubString("ICON", filename);
    }


    public String getMessageIcon() {
        return subRecords.getSubString("MICO").print();
    }

    /**
     * @param filename
     */
    public void setMessageIcon(String filename) {
        subRecords.setSubString("MICO", filename);
    }


    public FormID getEquipType() {
        return subRecords.getSubForm("ETYP").getForm();
    }

    /**
     * @param equipType
     */
    public void setEquipType(FormID equipType) {
        subRecords.setSubForm("ETYP", equipType);
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

    /*
     * SkyBash functions.
     */
    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        ALCH a = this;
        if (!(no == null && bo == null && (no instanceof ALCH) && (bo instanceof ALCH))) {
            final ALCH na = (ALCH) no;
            final ALCH ba = (ALCH) bo;
            SubRecords sList = a.subRecords;
            SubRecords nsList = na.subRecords;
            SubRecords bsList = ba.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return a;
    }


    public enum ALCHFlag {


        ManualCalc(0),

        Food(1),

        Medicine(16),

        Poison(17);
        final int value;

        ALCHFlag(int in) {
            value = in;
        }
    }

    static final class ENIT extends SubRecord {

        int value = 0;
        LFlags flags = new LFlags(4);
        FormID addiction = new FormID();
        byte[] addictionChance = new byte[4];
        FormID useSound = new FormID();

        ENIT() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(value);
            out.write(flags.export());
            addiction.export(out);
            out.write(addictionChance, 4);
            useSound.export(out);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            value = in.extractInt(4);
            flags.set(in.extract(4));
            addiction.parseData(in, srcMod);
            addictionChance = in.extract(4);
            useSound.parseData(in, srcMod);
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(2);
            out.add(addiction);
            out.add(useSound);
            return out;
        }

        @Override
        SubRecord getNew(String type) {
            return new ENIT();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 20;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("ENIT");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            ENIT e = this;
            if (!(no == null && bo == null && (no instanceof ENIT) && (bo instanceof ENIT))) {
                final ENIT ne = (ENIT) no;
                final ENIT be = (ENIT) bo;
                Merger.merge(e.value, ne.value, be.value, getType(), "value");
                e.flags = Merger.merge(e.flags, ne.flags, be.flags, getType());
                Merger.merge(e.addictionChance, ne.addictionChance, be.addictionChance, getType(), "addiction chance");
                e.addiction.merge(ne.addiction, be.addiction, getType());
                e.useSound.merge(ne.useSound, be.useSound, getType());
            }
            return e;
        }
    }
}
