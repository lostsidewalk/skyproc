package skyproc;

import java.util.ArrayList;

/**
 * @author Justin Swanson
 */
public class VTYP extends MajorRecord {

    static final SubPrototype VTYPprototype = new SubPrototype(MajorRecord.majorProto) {
        @Override
        protected void addRecords() {
            add(new SubFlag("DNAM", 1));
        }
    };

    // Enums

    // Common Functions
    VTYP() {
        super();
        subRecords.setPrototype(VTYPprototype);
    }

    /**
     * @param edid
     */
    public VTYP(String edid) {
        this();
        originateFromPatch(edid);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("VTYP");
    }

    @Override
    String getFormArrayStr(Boolean master) {
        return super.getFormArrayStr(master);
    }

    @Override
    Record getNew() {
        return new VTYP();
    }

    /**
     * @param flag
     * @param on
     */
    public void set(VoiceTypeFlag flag, boolean on) {
        subRecords.setSubFlag("DNAM", flag.ordinal(), on);
    }

    // Get/Set

    /**
     * @param flag
     * @return
     */
    public boolean get(VoiceTypeFlag flag) {
        return subRecords.getSubFlag("DNAM").is(flag.ordinal());
    }

    /**
     *
     */
    public enum VoiceTypeFlag {
        /**
         *
         */
        AllowDefaultDialogue,
        /**
         *
         */
        Female,
    }
}
