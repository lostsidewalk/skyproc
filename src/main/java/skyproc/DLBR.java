package skyproc;

import java.util.ArrayList;

/**
 * @author Justin Swanson
 */
public class DLBR extends MajorRecord {

    static final SubPrototype DLBRprototype = new SubPrototype(MajorRecord.majorProto) {

        @Override
        protected void addRecords() {
            add(new SubForm("QNAM"));
            add(new SubForm("TNAM"));
            forceExport("TNAM");
            add(new SubData("DNAM"));
            add(new SubForm("SNAM"));
        }
    };

    DLBR() {
        super();
        subRecords.setPrototype(DLBRprototype);
    }

    @Override
    Record getNew() {
        return new DLBR();
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("DLBR");
    }


    public FormID getQuest() {
        return subRecords.getSubForm("QNAM").getForm();
    }

    /**
     * @param quest
     */
    public void setQuest(FormID quest) {
        subRecords.setSubForm("QNAM", quest);
    }


    public FormID getStartingTopic() {
        return subRecords.getSubForm("SNAM").getForm();
    }

    /**
     * @param dialog
     */
    public void setStartingTopic(FormID dialog) {
        subRecords.setSubForm("SNAM", dialog);
    }
}
