package skyproc;

import lev.debug.LDebug;
import lev.debug.LLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An extended Levnifty LLogger object that also has a BLOCKED logstream as a place
 * to record any records that were blocked/skipped during the patch.<br>
 * Look at levnifty JavaDocs for more information about LLogger.
 *
 * @author Justin Swanson
 */
class SPLogger extends LLogger {

    Map<Mod, LDebug> modImports = new HashMap<>();
    int modNum = 0;

    /**
     * Creates a new LLogger object with the debug path specified.  If the folder
     * does not exist, it will be created.
     *
     * @param in Path to the folder where all the debug logs should be exported.
     */
    public SPLogger(String in) {
        super(in);
        addSpecial(SpecialTypes.BLOCKED, "Blocked Records.txt");
        addSpecial(Consistency.LogTypes.CONSISTENCY, Consistency.debugFolder + "Requests.txt");
        addSpecial(BSA.LogTypes.BSA, "BSA.txt");
    }

    @Override
    public ArrayList<LDebug> allDebugs() {
        ArrayList out = super.allDebugs();
        out.addAll(modImports.values());
        return out;
    }

    public void logMod(Mod srcMod, String header, String... data) {
        LDebug log = modImports.get(srcMod);
        if (log == null) {
            log = new LDebug(debugPath + "Mod Import/" + modNum++ + " - " + srcMod.getName() + ".txt", 40);
            modImports.put(srcMod, log);
        }
        log.w(header, data);
    }

    /**
     * List of special types offered by SPLogger.  To log to one of them, use
     * logSpecial from LLogger.
     */
    public enum SpecialTypes {
        /**
         * A logstream used for logging which records have been skipped/blockec.
         */
        BLOCKED
    }
}
