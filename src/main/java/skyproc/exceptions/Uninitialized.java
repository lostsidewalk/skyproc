package skyproc.exceptions;

/**
 * @author Justin Swanson
 */
public class Uninitialized extends Exception {


    public Uninitialized() {
    }

    /**
     * @param msg
     */
    public Uninitialized(String msg) {
        super(msg);
    }

}
