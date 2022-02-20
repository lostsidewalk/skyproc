package skyproc.exceptions;

/**
 * @author Justin Swanson
 */
public class BadMod extends Exception {

    /**
     *
     */
    public BadMod() {
    }

    /**
     * @param msg
     */
    public BadMod(String msg) {
        super(msg);
    }
}