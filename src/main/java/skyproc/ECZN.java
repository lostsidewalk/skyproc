package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Plutoman101
 */
public class ECZN extends MajorRecord {

    // Static prototypes and definitions
    static final SubPrototype ECZNproto = new SubPrototype(MajorRecord.majorProto) {
        @Override
        protected void addRecords() {
            add(new DATA());
        }
    };

    /**
     * Creates a new ECZN record.
     */
    ECZN() {
        super();
        subRecords.setPrototype(ECZNproto);
    }

    // Enums

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("ECZN");
    }

    // Common Functions

    @Override
    Record getNew() {
        return new ECZN();
    }

    // Get/set
    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(ECZNFlags flag) {
        return getDATA().flags.get(flag.value);
    }

    /**
     * @param flag
     * @param on
     */
    public void set(ECZNFlags flag, boolean on) {
        getDATA().flags.set(flag.value, on);
    }


    public FormID getLocation() {
        return getDATA().location;
    }

    /**
     * @param location
     */
    public void setLocation(FormID location) {
        getDATA().location = location;
    }


    public int getMaxLevel() {
        return getDATA().maxLevel;
    }

    /**
     * @param maxLevel
     */
    public void setMaxLevel(int maxLevel) {
        getDATA().maxLevel = maxLevel;
    }


    public int getMinLevel() {
        return getDATA().minLevel;
    }

    /**
     * @param minLevel
     */
    public void setMinLevel(int minLevel) {
        getDATA().minLevel = minLevel;
    }


    public FormID getOwner() {
        return getDATA().owner;
    }

    /**
     * @param owner
     */
    public void setOwner(FormID owner) {
        getDATA().owner = owner;
    }


    public int getRank() {
        return getDATA().rank;
    }

    /**
     * @param rank
     */
    public void setRank(int rank) {
        getDATA().rank = rank;
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        ECZN e = this;
        if (!(no == null && bo == null && (no instanceof ECZN) && (bo instanceof ECZN))) {
            final ECZN ne = (ECZN) no;
            final ECZN be = (ECZN) bo;
            SubRecords sList = e.subRecords;
            SubRecords nsList = ne.subRecords;
            SubRecords bsList = be.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return e;
    }


    public enum ECZNFlags {


        NeverResets(0),

        MatchPCBelowMin(1),

        DisableCombatBoundary(2);
        final int value;

        ECZNFlags(int value) {
            this.value = value;
        }
    }

    static final class DATA extends SubRecord implements Serializable {

        final LFlags flags = new LFlags(1);
        private FormID owner = new FormID();
        private FormID location = new FormID();
        private int rank = 0;
        private int minLevel = 0;
        private int maxLevel = 0;
        private boolean valid = true;

        DATA() {
            super();
            valid = false;
        }

        @Override
        SubRecord getNew(String type) {
            return new DATA();
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);

            owner.parseData(in, srcMod);
            location.parseData(in, srcMod);
            if (!in.isDone()) {
                rank = in.extractInt(1);
                minLevel = in.extractInt(1);
                flags.set(in.extract(1));
                maxLevel = in.extractInt(1);
            }
            logMod(srcMod, "", "DATA record: ");
            logMod(srcMod, "", "  " + "Owner: " + owner.getFormStr() + ", Location: " + location.getFormStr());
            logMod(srcMod, "", "  " + "Required Rank: " + rank + ", Minimum Level: " + minLevel);
            logMod(srcMod, "", "  " + "Max Level: " + maxLevel + ", Flags: " + flags);

            valid = true;
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            if (isValid()) {
                owner.export(out);
                location.export(out);
                out.write(rank, 1);
                out.write(minLevel, 1);
                out.write(flags.export(), 1);
                out.write(maxLevel, 1);
            }
        }

        @Override
        boolean isValid() {
            return valid;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            if (isValid()) {
                return 12;
            } else {
                return 0;
            }
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(2);
            out.add(owner);
            out.add(location);
            return out;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }
    }
}
