package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.ArmorType;
import skyproc.genenums.FirstPersonFlags;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * A internal structure found in many major records representing body setups.
 * The use depends on the context of the major record it is inside of.
 *
 * @author Justin Swanson
 */
public class BodyTemplate extends SubShell {

    static final SubPrototype BODTproto = new SubPrototype() {
        @Override
        protected void addRecords() {
            add(new BodyTemplateMain("BODT"));
            add(new BodyTemplateMain("BOD2"));
        }
    };

    BodyTemplate() {
        super(BODTproto);
    }

    BodyTemplateMain getMain() {
        BodyTemplateMain b = (BodyTemplateMain) subRecords.get("BODT");
        if (!(b.isValid())) {
            b = (BodyTemplateMain) subRecords.get("BOD2");
        }
        return b;
    }

    @Override
    SubRecord getNew(String type) {
        return new BodyTemplate();
    }

    /**
     * @param type
     * @param flag
     * @param on
     */
    public void set(BodyTemplateType type, FirstPersonFlags flag, boolean on) {
        BodyTemplateMain main = getMain();
        if (flag == FirstPersonFlags.NONE) {
            main.bodyParts.clear();
        } else {
            main.bodyParts.set(flag.ordinal(), on);
        }
        main.valid = true;

    }

    /**
     * @param type
     * @param part
     * @return
     */
    public boolean get(BodyTemplateType type, FirstPersonFlags part) {
        BodyTemplateMain main = getMain();
        main.valid = true;
        if (part == FirstPersonFlags.NONE) {
            return main.bodyParts.isZeros();
        } else {
            return main.bodyParts.get(part.ordinal());
        }
    }

    /**
     * @param flag
     * @param on
     */
    public void set(GeneralFlags flag, boolean on) {
        BodyTemplateMain main = getMain();
        main.valid = true;
        main.flags.set(flag.value, on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(GeneralFlags flag) {
        BodyTemplateMain main = getMain();
        main.valid = true;
        return main.flags.get(flag.value);
    }

    /**
     * @param type
     * @param armorType
     */
    public void setArmorType(BodyTemplateType type, ArmorType armorType) {
        BodyTemplateMain main = getMain();
        main.valid = true;
        main.armorType = armorType;
    }

    /**
     * @param type
     * @return
     */
    public ArmorType getArmorType(BodyTemplateType type) {
        BodyTemplateMain main = getMain();
        main.valid = true;
        return main.armorType;
    }

    void makeBod2(MajorRecord owner) {
        BodyTemplateMain main = getMain();
        if (main.isBODT()) {
            if (main.flags.get(GeneralFlags.NonPlayable.value)) {
                owner.set(MajorRecord.MajorFlags.NonPlayable, true);
            }
            BodyTemplateMain bod2 = (BodyTemplateMain) subRecords.get("BOD2");
            bod2.bodyParts = main.bodyParts;
            bod2.armorType = main.armorType;
            if (bod2.armorType == null) {
                bod2.armorType = ArmorType.CLOTHING;
            }
            bod2.valid = main.valid;
            subRecords.remove("BODT");
            subRecords.add(new BodyTemplateMain("BODT"));
        }
    }


    public enum GeneralFlags {


        ModulatesVoice(0),

        NonPlayable(4);
        final int value;

        GeneralFlags(int val) {
            value = val;
        }
    }


    public enum BodyTemplateType {


        Normal("BODT"),

        Biped("BOD2");
        final String type;

        BodyTemplateType(String in) {
            type = in;
        }
    }

    static class BodyTemplateMain extends SubRecordTyped {

        LFlags bodyParts = new LFlags(4);
        LFlags flags = new LFlags(4);
        ArmorType armorType = null;
        boolean valid = false;

        BodyTemplateMain(String type) {
            super(type);
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(bodyParts.export(), 4);
            if (isBODT()) {
                out.write(flags.export(), 4);
            }
            if (armorType != null) {
                out.write(armorType.ordinal());
            }
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            bodyParts = new LFlags(in.extract(4));
            if (isBODT()) {
                flags = new LFlags(in.extract(4));
            }
            if (!in.isDone()) {
                armorType = ArmorType.values()[in.extractInt(4)];
            }
            valid = true;
        }

        @Override
        SubRecord getNew(String type) {
            return new BodyTemplateMain(type);
        }

        boolean isBODT() {
            return "BODT".equals(getType());
        }

        @Override
        boolean isValid() {
            return valid;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            int len = 4;
            if (isBODT()) {
                len += 4;
            }
            if (armorType != null) {
                len += 4;
            }
            return len;
        }
    }

}