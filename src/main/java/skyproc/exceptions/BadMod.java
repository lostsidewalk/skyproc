package skyproc.exceptions;

import skyproc.ModListing;

/**
 * @author Justin Swanson
 */
public class BadMod extends Exception {

    private final ModListing failedMod;

    /**
     * @param msg
     */
    public BadMod(String msg, Throwable cause, ModListing failedMod) {
        super(msg, cause);
        this.failedMod = failedMod;
    }

    @Override
    public String getMessage() {
        return "Failed mod: " + failedMod;
    }
}