package skyproc;

import lev.LImport;
import skyproc.AltTextures.AltTexture;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.Gender;
import skyproc.genenums.Perspective;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * Armature records (pieces of armor)
 *
 * @author Justin Swanson
 */
public class ARMA extends MajorRecord {

    // Static prototypes and definitions
    static final SubPrototype ARMAprototype = new SubPrototype(MajorRecord.majorProto) {

        @Override
        protected void addRecords() {
            add(new BodyTemplate());
            add(new SubForm("RNAM"));
            add(new DNAM());
            // Third Person
            // Male
            add(SubString.getNew("MOD2", true));
            add(new SubList<>(new SubData("MO2T")));
            add(new AltTextures("MO2S"));
            // Female
            add(SubString.getNew("MOD3", true));
            add(new SubList<>(new SubData("MO3T")));
            add(new AltTextures("MO3S"));
            // First person
            // Male
            add(SubString.getNew("MOD4", true));
            add(new SubList<>(new SubData("MO4T")));
            add(new AltTextures("MO4S"));
            // Female
            add(SubString.getNew("MOD5", true));
            add(new SubList<>(new SubData("MO5T")));
            add(new AltTextures("MO5S"));
            add(new SubForm("NAM0"));
            add(new SubForm("NAM1"));
            add(new SubForm("NAM2"));
            add(new SubForm("NAM3"));
            add(new SubList<>(new SubForm("MODL")));
            add(new SubForm("SNDD"));
            add(new SubForm("ONAM"));
        }
    };

    /**
     * Armature Major Record
     */
    ARMA() {
        super();
        subRecords.setPrototype(ARMAprototype);
    }

    // Common Functions

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("ARMA");
    }

    @Override
    Record getNew() {
        return new ARMA();
    }

    // Get/set
    String getModelPathType(Gender gender, Perspective perspective) {
        switch (gender) {
            case MALE:
                switch (perspective) {
                    case THIRD_PERSON:
                        return "MOD2";
                    case FIRST_PERSON:
                        return "MOD4";
                }
            case FEMALE:
                switch (perspective) {
                    case THIRD_PERSON:
                        return "MOD3";
                    case FIRST_PERSON:
                        return "MOD5";
                }
            default:
                return "MOD2";
        }
    }

    /**
     * @param path        Path of the .nif file to assign.
     * @param gender      The gender to assign this model path to.
     * @param perspective Perspective to assign this model path to.
     */
    public void setModelPath(String path, Gender gender, Perspective perspective) {
        subRecords.setSubString(getModelPathType(gender, perspective), path);
    }

    /**
     * @param gender      The gender of the desired model path to query.
     * @param perspective The perspective of the model path to query.
     * @return The model path of the specified gender/perspective. Empty string
     * if a model path does not exist for specified parameters.
     */
    public String getModelPath(Gender gender, Perspective perspective) {
        return subRecords.getSubString(getModelPathType(gender, perspective)).print();
    }

    String getAltTexType(Gender gender, Perspective perspective) {
        switch (gender) {
            case MALE:
                switch (perspective) {
                    case THIRD_PERSON:
                        return "MO2S";
                    case FIRST_PERSON:
                        return "MO4S";
                }
            default:
                switch (perspective) {
                    case THIRD_PERSON:
                        return "MO3S";
                    default:
                        return "MO5S";
                }
        }
    }

    /**
     * Returns the set of AltTextures applied to a specified gender and
     * perspective.
     *
     * @param gender      Gender of the AltTexture set to query.
     * @param perspective Perspective of the AltTexture set to query.
     * @return List of the AltTextures applied to the gender/perspective.
     */
    public ArrayList<AltTexture> getAltTextures(Gender gender, Perspective perspective) {
        AltTextures t = (AltTextures) subRecords.get(getAltTexType(gender, perspective));
        return t.altTextures;
    }

    /**
     * @param rhs         Other ARMA record.
     * @param gender      Gender of the pack to compare.
     * @param perspective Perspective of the pack to compare
     * @return true if:<br> Both sets are empty.<br> or <br> Each set contains
     * matching Alt Textures with the same name and TXST formID reference, in
     * the same corresponding indices.
     */
    public boolean equalAltTextures(ARMA rhs, Gender gender, Perspective perspective) {
        return AltTextures.equal(getAltTextures(gender, perspective), rhs.getAltTextures(gender, perspective));
    }


    public FormID getRace() {
        return subRecords.getSubForm("RNAM").getForm();
    }

    /**
     * @param race
     */
    public void setRace(FormID race) {
        subRecords.setSubForm("RNAM", race);
    }

    /**
     * @param skin
     * @param gender
     */
    public void setSkinTexture(FormID skin, Gender gender) {
        switch (gender) {
            case MALE:
                subRecords.setSubForm("NAM0", skin);
                return;
            case FEMALE:
                subRecords.setSubForm("NAM1", skin);
        }
    }

    /**
     * @param gender
     * @return
     */
    public FormID getSkinTexture(Gender gender) {
        switch (gender) {
            case MALE:
                return subRecords.getSubForm("NAM0").getForm();
            default:
                return subRecords.getSubForm("NAM1").getForm();
        }
    }

    /**
     * @param swapList
     * @param gender
     */
    public void setSkinSwap(FormID swapList, Gender gender) {
        switch (gender) {
            case MALE:
                subRecords.getSubForm("NAM2").setForm(swapList);
                return;
            case FEMALE:
                subRecords.getSubForm("NAM3").setForm(swapList);
        }
    }

    /**
     * @param gender
     * @return
     */
    public FormID getSkinSwap(Gender gender) {
        switch (gender) {
            case MALE:
                return subRecords.getSubForm("NAM2").getForm();
            default:
                return subRecords.getSubForm("NAM3").getForm();
        }
    }

    /**
     * @param addRace
     */
    public void addAdditionalRace(FormID addRace) {
        subRecords.getSubList("MODL").add(addRace);
    }

    /**
     * @param addRace
     */
    public void removeAdditionalRace(FormID addRace) {
        subRecords.getSubList("MODL").remove(addRace);
    }


    public ArrayList<FormID> getAdditionalRaces() {
        return subRecords.getSubList("MODL").toPublic();
    }


    public void clearAdditionalRaces() {
        subRecords.getSubList("MODL").clear();
    }


    public FormID getFootstepSound() {
        return subRecords.getSubForm("SNDD").getForm();
    }

    /**
     * @param footstep
     */
    public void setFootstepSound(FormID footstep) {
        subRecords.setSubForm("SNDD", footstep);
    }

    DNAM getDNAM() {
        return (DNAM) subRecords.get("DNAM");
    }

    /**
     * @param priority
     * @param gender
     */
    public void setPriority(int priority, Gender gender) {
        switch (gender) {
            case MALE:
                getDNAM().malePriority = priority;
                return;
            case FEMALE:
                getDNAM().femalePriority = priority;
        }
    }

    /**
     * @param gender
     * @return
     */
    public int getPriority(Gender gender) {
        switch (gender) {
            case MALE:
                return getDNAM().malePriority;
            default:
                return getDNAM().femalePriority;
        }
    }


    public int getDetectionSoundValue() {
        return getDNAM().detectionSoundValue;
    }

    /**
     * @param value
     */
    public void setDetectionSoundValue(int value) {
        getDNAM().detectionSoundValue = value;
    }


    public float getWeaponAdjust() {
        return getDNAM().weaponAdjust;
    }

    /**
     * @param adjust
     */
    public void setWeaponAdjust(float adjust) {
        getDNAM().weaponAdjust = adjust;
    }


    public BodyTemplate getBodyTemplate() {
        return subRecords.getBodyTemplate();
    }

    /*
     * SkyBash functions.
     */
    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        ARMA a = this;
        if (!(no == null && bo == null && (no instanceof ARMA) && (bo instanceof ARMA))) {
            final ARMA na = (ARMA) no;
            final ARMA ba = (ARMA) bo;
            SubRecords sList = a.subRecords;
            SubRecords nsList = na.subRecords;
            SubRecords bsList = ba.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return a;
    }

    static final class DNAM extends SubRecord {

        int malePriority;
        int femalePriority;
        byte[] unknown;
        int detectionSoundValue;
        byte[] unknown2;
        float weaponAdjust;

        DNAM() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(malePriority, 1);
            out.write(femalePriority, 1);
            out.write(unknown, 4);
            out.write(detectionSoundValue, 1);
            out.write(unknown2, 1);
            out.write(weaponAdjust);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            malePriority = in.extractInt(1);
            femalePriority = in.extractInt(1);
            unknown = in.extract(4);
            detectionSoundValue = in.extractInt(1);
            unknown2 = in.extract(1);
            weaponAdjust = in.extractFloat();
            logMod(srcMod, "", "M-Priority: " + malePriority + ", F-Priority: " + femalePriority + ", DetectionValue: " + detectionSoundValue + ", weaponAdjust: " + weaponAdjust);
        }

        @Override
        SubRecord getNew(String type) {
            return new DNAM();
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
            return Record.getTypeList("DNAM");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            DNAM d = this;
            if (!(no == null && bo == null && (no instanceof DNAM) && (bo instanceof DNAM))) {
                final DNAM nd = (DNAM) no;
                final DNAM bd = (DNAM) bo;
                Merger.merge(d.detectionSoundValue, nd.detectionSoundValue, bd.detectionSoundValue, getType(), "detection sound value");
                Merger.merge(d.malePriority, nd.malePriority, bd.malePriority, getType(), "male priority");
                Merger.merge(d.femalePriority, nd.femalePriority, bd.femalePriority, getType(), "female priority");
                Merger.merge(d.unknown, nd.unknown, bd.unknown, getType(), "unknown");
                Merger.merge(d.unknown2, nd.unknown2, bd.unknown2, getType(), "unknown");
                Merger.merge(d.weaponAdjust, nd.weaponAdjust, bd.weaponAdjust, getType(), "weapon adjust");
            }
            return d;
        }
    }
}
