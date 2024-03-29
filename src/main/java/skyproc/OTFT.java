package skyproc;

import java.util.ArrayList;

/**
 * @author Justin Swanson
 */
public class OTFT extends MajorRecord {

    // Static prototypes and definitions
    static final SubPrototype OTFTproto = new SubPrototype(MajorRecord.majorProto) {

        @Override
        protected void addRecords() {
            add(new SubFormArray("INAM", 0));
        }
    };

    // Common Functions
    OTFT() {
        super();
        subRecords.setPrototype(OTFTproto);
    }

    /**
     * @param uniqueEDID
     */
    public OTFT(String uniqueEDID) {
        this();
        originateFromPatch(uniqueEDID);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("OTFT");
    }

    @Override
    Record getNew() {
        return new OTFT();
    }

    // Get/Set


    public ArrayList<FormID> getInventoryList() {
        return subRecords.get("INAM").allFormIDs();
    }

    /**
     * @param item
     */
    public void addInventoryItem(FormID item) {
        subRecords.getSubFormArray("INAM").add(item);
    }

    /**
     * @param item
     */
    public void removeInventoryItem(FormID item) {
        subRecords.getSubFormArray("INAM").remove(item);
    }


    public void clearInventoryItems() {
        subRecords.getSubFormArray("INAM").clear();
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        OTFT e = this;
        if (!(no == null && bo == null && (no instanceof OTFT) && (bo instanceof OTFT))) {
            final OTFT ne = (OTFT) no;
            final OTFT be = (OTFT) bo;
            SubRecords sList = e.subRecords;
            SubRecords nsList = ne.subRecords;
            SubRecords bsList = be.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return e;
    }
}
