package skyproc.exceptions;

/**
 * @author Justin Swanson
 */
public class NotFound extends Exception {


    public NotFound() {
    }

    /**
     * @param msg
     */
    public NotFound(String msg) {
        super(msg);
    }
}
