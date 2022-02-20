package skyproc.exceptions;

/**
 * @author Justin Swanson
 */
public class BadRecord extends Exception {

    /**
     *
     */
    public BadRecord() {
    }

    /**
     * @param msg
     */
    public BadRecord(String msg) {
        super(msg);
    }
}
