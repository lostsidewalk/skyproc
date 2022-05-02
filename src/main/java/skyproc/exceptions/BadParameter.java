package skyproc.exceptions;

/**
 * @author Justin Swanson
 */
public class BadParameter extends Exception {


    public BadParameter() {
    }

    /**
     * @param msg
     */
    public BadParameter(String msg) {
        super(msg);
    }
}