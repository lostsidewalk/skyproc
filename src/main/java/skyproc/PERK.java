package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * A record fully describing and specifying a perk, including level up perks, as
 * well as more obscure hidden perks related to quests and NPCs.
 *
 * @author Justin Swanson
 */
public class PERK extends MajorRecordDescription {

    // Static prototypes and definitions
    static final SubPrototype PERKproto = new SubPrototype(MajorRecordDescription.descProto) {

        @Override
        protected void addRecords() {
            after(new ScriptPackage(new PERKScriptFragments()), "EDID");
            add(new SubList<>(new Condition()));
            add(new DATA());
            add(new SubForm("NNAM"));
            add(SubString.getNew("ICON", true));
            add(new SubList<>(new PRKEPackage(new SubPrototype() {

                @Override
                protected void addRecords() {
                    add(new SubData("PRKE"));
                    add(new PRKEComplexSubPackage(new SubPrototype() {

                        @Override
                        protected void addRecords() {
                            add(new SubData("DATA"));
                            add(new SubList<>(new SubShell(new SubPrototype() {

                                @Override
                                protected void addRecords() {
                                    add(new SubData("PRKC"));
                                    add(new SubList<>(new Condition()));
                                }
                            })));
                            add(new SubData("EPFT"));
                            add(new SubStringPointer("EPF2", SubStringPointer.Files.STRINGS));
                            add(new SubData("EPF3"));
                            add(new SubData("EPFD"));
                        }
                    }));
                    add(new SubData("PRKF"));
                    forceExport("PRKF");
                }
            })));
        }
    };

    // Common Functions
    PERK() {
        super();
        subRecords.setPrototype(PERKproto);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("PERK");
    }

    @Override
    Record getNew() {
        return new PERK();
    }


    public ScriptPackage getScriptPackage() {
        return subRecords.getScripts();
    }


    public ArrayList<Condition> getConditions() {
        return subRecords.getSubList("CTDA").toPublic();
    }

    /**
     * @param c
     */
    public void addCondition(Condition c) {
        subRecords.getSubList("CTDA").add(c);
    }

    /**
     * @param c
     */
    public void removeCondition(Condition c) {
        subRecords.getSubList("CTDA").remove(c);
    }


    public FormID getNextPerk() {
        return subRecords.getSubForm("NNAM").getForm();
    }

    static class DATA extends SubRecord {

        boolean trait;
        int level;
        int ranks;
        boolean playable;
        boolean hidden;

        DATA() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(trait, 1);
            out.write(level, 1);
            out.write(ranks, 1);
            out.write(playable, 1);
            out.write(hidden, 1);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            trait = in.extractBool(1);
            level = in.extractInt(1);
            ranks = in.extractInt(1);
            playable = in.extractBool(1);
            hidden = in.extractBool(1);
            logMod(srcMod, "", "Trait: " + trait + ", level: " + level + ", ranks: " + ranks + ", playable: " + playable + ", hidden: " + hidden);
        }

        @Override
        SubRecord getNew(String type) {
            return new DATA();
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 5;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }
    }

    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }

    public void setTrait(boolean trait) {
        getDATA().trait = trait;
    }

    public boolean getTrait() {
        return getDATA().trait;
    }

    public void setLevel(int level) {
        getDATA().level = level;
    }

    public int getLevel() {
        return getDATA().level;
    }

    public void setRanks(int ranks) {
        getDATA().ranks = ranks;
    }

    public int getRanks() {
        return getDATA().ranks;
    }

    public void setPlayable(boolean playable) {
        getDATA().playable = playable;
    }

    public boolean getPlayable() {
        return getDATA().playable;
    }

    public void setHidden(boolean hidden) {
        getDATA().hidden = hidden;
    }

    public boolean getHidden() {
        return getDATA().hidden;
    }

    // Get/Set

    /**
     * @param perk
     */
    public void setNextPerk(FormID perk) {
        subRecords.setSubForm("NNAM", perk);
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        PERK p = this;
        if (!(no == null && bo == null && (no instanceof PERK) && (bo instanceof PERK))) {
            final PERK np = (PERK) no;
            final PERK bp = (PERK) bo;
            SubRecords sList = p.subRecords;
            SubRecords nsList = np.subRecords;
            SubRecords bsList = bp.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return p;
    }

    // Enums
    enum PerkType {

        QUEST, ABILITY, COMPLEX
    }

    static class PRKEPackage extends SubShellBulkType {

        PRKEPackage(SubPrototype proto) {
            super(proto, false);
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            SubData PRKE = subRecords.getSubData("PRKE");
            PRKE.parseData(PRKE.extractRecordData(in), srcMod);
            PerkType perkType = PerkType.values()[subRecords.getSubData("PRKE").getData()[0]];
            switch (perkType) {
                case QUEST:
                    subRecords.remove("DATA");
                    subRecords.add(new SubFormData("DATA"));
                    break;
                case ABILITY:
                    subRecords.remove("DATA");
                    subRecords.add(new SubForm("DATA"));
                    break;
            }
            super.parseData(in, srcMod);
        }

        @Override
        SubRecord getNew(String type) {
            return new PRKEPackage(getPrototype());
        }

        @Override
        boolean isValid() {
            return subRecords.get("PRKE").isValid() && subRecords.get("DATA").isValid();
        }
    }

    static class PRKEComplexSubPackage extends SubShell {

        PRKEComplexSubPackage(SubPrototype proto) {
            super(proto);
        }

        @Override
        SubRecord getNew(String type) {
            return new PRKEComplexSubPackage(getPrototype());
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            switch (getNextType(in)) {
                case "EPFT":
                    SubData EPFT = subRecords.getSubData("EPFT");
                    EPFT.parseData(in, srcMod);
                    if (EPFT.toInt() >= 3 && EPFT.toInt() <= 5) {
                        subRecords.remove("EPFD");
                        subRecords.add(new SubForm("EPFD"));
                    }
                    return;
            }
            super.parseData(in, srcMod);
        }

        @Override
        boolean isValid() {
            return true;
        }
    }

    static class PERKScriptFragments extends SubRecord {

        byte unknown = 0;
        final StringNonNull fragmentFile = new StringNonNull();
        final ArrayList<PERKScriptFragment> fragments = new ArrayList<>();
        boolean valid = false;

        @Override
        void parseData(LImport in, Mod srcMod) {
            unknown = in.extract(1)[0];
            fragmentFile.set(in.extractString(in.extractInt(2)));
            int numFrag = in.extractInt(2);
            for (int i = 0; i < numFrag; i++) {
                PERKScriptFragment frag = new PERKScriptFragment();
                frag.parseData(in, srcMod);
                fragments.add(frag);
            }
            valid = true;
        }

        @Override
        void export(ModExporter out) throws IOException {
            if (!valid) {
                return;
            }
            out.write(unknown, 1);
            fragmentFile.export(out);
            out.write(fragments.size(), 2);
            for (PERKScriptFragment frag : fragments) {
                frag.export(out);
            }
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            if (!valid) {
                return 0;
            }
            int len = 3;
            len += fragmentFile.getTotalLength(isStringTabled);
            for (PERKScriptFragment frag : fragments) {
                len += frag.getContentLength(isStringTabled);
            }
            return len;
        }

        @Override
        SubRecord getNew(String type) {
            return new PERKScriptFragments();
        }

        @Override
        ArrayList<String> getTypes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class PERKScriptFragment {

        int index = 0;
        byte[] unknown = new byte[3];
        final StringNonNull scriptName = new StringNonNull();
        final StringNonNull fragmentName = new StringNonNull();

        void parseData(LImport in, Mod srcMod) {
            index = in.extractInt(2);
            unknown = in.extract(3);
            scriptName.set(in.extractString(in.extractInt(2)));
            fragmentName.set(in.extractString(in.extractInt(2)));
        }

        void export(ModExporter out) throws IOException {
            out.write(index, 2);
            out.write(unknown);
            scriptName.export(out);
            fragmentName.export(out);
        }

        int getContentLength(boolean isStringTabled) {
            return 5 + scriptName.getTotalLength(isStringTabled)
                    + fragmentName.getTotalLength(isStringTabled);
        }
    }
}
