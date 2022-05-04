package skyproc;

import lev.Ln;
import lev.debug.LDebug;
import lev.debug.LLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skyproc.gui.SUMGUI;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Global variables/settings of SkyProc.
 *
 * @author Justin Swanson
 */
public class SPGlobal {

    /**
     * Path to the Data/ folder to look for plugins to import/export.<br><br> By
     * default, this is set to "../../", meaning the patch has to be in a
     * subfolder of "Data/". (ex "Data/SkyProc Patchers/My SkyProc Patcher/My
     * Patcher.jar")
     */
    // TODO: make this externally configurable
    public static final String pathToData = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Skyrim Special Edition\\Data\\";
    // FileSystems.getDefault().getPath("..\\..\\skyproc\\Data").toAbsolutePath().normalize().toString();
    public static String pathToDataFixed = (pathToData.endsWith("\\") ? pathToData : pathToData + "\\");
    /**
     *
     */
    public static String pathToPatchers = Ln.getMyDocuments().getPath() + "//SkyProc Patchers//";
    /**
     * A default path to "internal files". This is currently only used for
     * saving custom path information for Skyrim.ini and plugins.txt. This can
     * also be used to store your own internal files.
     */
    public static String pathToInternalFiles = "Files/";
    /**
     * Skyproc will import and embed the language given by SPGlobal.language
     * every time a patch is created. To offer multi-language support, simply
     * give the users of your program ability to adjust this setting.
     */
    public static Language language = Language.English;
    /**
     * Turns off messages about which record is currently being streamed.
     */
    public static boolean debugStream = true;
    /**
     * Displays information about BSA importing
     */
    public static boolean debugBSAimport = true;
    /**
     * Displays information about NIF importing
     */
    public static boolean debugNIFimport = false;
    /**
     * Prints messages about records pairing strings with external STRINGS
     * files.<br> Prints to the sync log<br>
     */
    public static boolean debugStringPairing = true;
    /**
     * Print messages concerning the merging of two plugins.<br> Prints to the
     * sync log<br>
     */
    public static boolean debugModMerge = false;
    /*
     * Toggle to force to processing exactly like vanilla files for validation.
     * Switches MajorRecord with flag DELETED to print full contents.
     * Needed because TesVEdit now removes all subrecords of DELETED majorRecords.
     */
    public static boolean forceValidateMode = false;
    static String header = "SPGlobal";
    static String gameName = "Skyrim";

    /*
     * Customizable Strings
     */
    static String pathToDebug = "SkyProcDebug/";
    static Mod globalPatchOut;
    static SPLogger log;
    static boolean logMods = true;
    static SPDatabase globalDatabase = new SPDatabase();
    public static boolean testing = false;
    public static boolean streamMode = true;
    static boolean deleteAfterExport = true;
    static boolean mergeMode = false;
    static boolean noModsAfter = true;
    static boolean checkMissingMasters = true;
    static MajorRecord lastStreamed;
    static File skyProcDocuments;
    static ArrayList<ModListing> modsToSkip = new ArrayList<>();
    static ArrayList<String> modsToSkipStr = new ArrayList<>();
    static ArrayList<ModListing> modsWhiteList = new ArrayList<>();
    static ArrayList<String> modsWhiteListStr = new ArrayList<>();
    static String appDataFolder;
    private static boolean allModsAsMasters = false;

    /**
     * Returns a File path to the SkyProc Documents folder.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static File getSkyProcDocuments() throws FileNotFoundException, IOException {
        if (skyProcDocuments == null) {
            File myDocs = SPGlobal.getMyDocumentsSkyrimFolder();
            skyProcDocuments = new File(myDocs.getPath() + "\\SkyProc\\");
            skyProcDocuments.mkdirs();
        }
        return skyProcDocuments;
    }

    /**
     * @return The database defined as the Global Database
     */
    public static SPDatabase getDB() {
        return globalDatabase;
    }

    /**
     * @return the set Global Patch, or null if one hasn' been set.
     */
    public static Mod getGlobalPatch() {
        return globalPatchOut;
    }

    /**
     * Creating your patch ahead of time, and setting it as the Global Patch
     * will prevent it from being imported by getActiveMods() and getAllMods()
     *
     * @param patch Mod to set as the global patch.
     */
    public static void setGlobalPatch(Mod patch) {
        if (globalPatchOut != null) {
            modsToSkip.remove(globalPatchOut.getInfo());
        }
        globalPatchOut = patch;
        modsToSkip.add(globalPatchOut.getInfo());
    }

    /**
     * @param m Mod to skip when importing.
     */
    public static void addModToSkip(ModListing m) {
        modsToSkip.add(m);
    }

    /**
     * @param s
     */
    public static void addModToSkip(String s) {
        s = s.toUpperCase();
        if (s.contains(".ESP") || s.contains(".ESM")) {
            addModToSkip(new ModListing(s));
        } else {
            modsToSkipStr.add(s);
        }
    }

    /**
     * @param m
     * @return
     */
    public static boolean isModToSkip(ModListing m) {
        return modsToSkip.contains(m) || isModToSkip(m.print());
    }

    /**
     * @param name
     * @return
     */
    public static boolean isModToSkip(String name) {
        return Ln.hasAnyKeywords(name, modsToSkipStr);
    }

    /**
     * @param m
     * @return
     */
    public static boolean isWhiteListed(ModListing m) {
        return (modsWhiteList.isEmpty() && modsWhiteListStr.isEmpty())
                || modsWhiteList.contains(m) || isWhiteListed(m.print());
    }

    /**
     * @param name
     * @return
     */
    public static boolean isWhiteListed(String name) {
        return (modsWhiteList.isEmpty() && modsWhiteListStr.isEmpty())
                || Ln.hasAnyKeywords(name, modsWhiteListStr);
    }

    /**
     * True if not blacklisted and if white listed (if there are any white
     * listings)
     *
     * @param m
     * @return
     */
    public static boolean shouldImport(ModListing m) {
        return !SPGlobal.isModToSkip(m) && SPGlobal.isWhiteListed(m);
    }

    /**
     * True if not blacklisted and if white listed (if there are any white
     * listings)
     *
     * @param name
     * @return
     */
    public static boolean shouldImport(String name) {
        return !SPGlobal.isModToSkip(name) && SPGlobal.isWhiteListed(name);
    }

    /**
     * Adds a mod to the white list. Once a mod is on the white list, only white
     * list mods will be imported by SkyProc.
     *
     * @param m
     */
    public static void addModToWhiteList(ModListing m) {
        modsWhiteList.add(m);
    }

    /**
     * Adds a mod to the white list. Once a mod is on the white list, only white
     * list mods will be imported by SkyProc.
     *
     * @param s
     */
    public static void addModToWhiteList(String s) {
        if (s.contains(".ESP") || s.contains(".ESM")) {
            addModToWhiteList(new ModListing(s));
        } else {
            modsWhiteListStr.add(s);
        }
    }

    /**
     * Querys the Global Database and returns whether the FormID exists.<br>
     * NOTE: it is recommended you use the version that only searches in
     * specific GRUPs for speed reasons.
     *
     * @param query FormID to look for.
     * @return True if FormID exists in the database.
     */
    static public boolean queryMajor(FormID query) {
        return SPDatabase.queryMajor(query, SPGlobal.getDB());
    }

    /**
     * Querys the Global Database and returns whether the FormID exists. It
     * limits its search to the given GRUP types for speed reasons.
     *
     * @param query      FormID to look for.
     * @param grup_types GRUPs to look in.
     * @return True if FormID exists in the database.
     */
    static public boolean queryMajor(FormID query, GRUP_TYPE... grup_types) {
        return SPDatabase.queryMajor(query, SPGlobal.getDB(), grup_types);
    }

    /**
     * Returns the My Documents Skyrim folder where the ini's are located.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    static public File getMyDocumentsSkyrimFolder() throws FileNotFoundException, IOException {
        return getSkyrimINI().getParentFile();
    }

    /**
     * Returns the Skyrim.ini file in the My Documents folder.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    static public File getSkyrimINI() throws FileNotFoundException, IOException {
        File myDocuments = Ln.getMyDocuments();
        File ini = new File(myDocuments.getPath() + "//My Games//Skyrim Special Edition//Skyrim.ini");

        // See if there's a manual override
        File override = new File(SPGlobal.pathToInternalFiles + "Skyrim-INI-Location.txt");
        if (override.exists()) {
            SPGlobal.log(header, "Skyrim.ini override file exists: " + override);
            BufferedReader in = new BufferedReader(new FileReader(override));
            File iniTmp = new File(in.readLine());
            if (iniTmp.exists()) {
                SPGlobal.log(header, "Skyrim.ini location override: " + iniTmp);
                ini = iniTmp;
            } else {
                SPGlobal.log(header, "Skyrim.ini location override thought to be in: " + iniTmp + ", but it did not exist.");
            }
        }

        if (!ini.exists()) {
            SPGlobal.logMain(header, "Skyrim.ini believed to be in: " + ini + ". But it does not exist.  Locating manually.");
            ini = Ln.manualFindFile("your Skyrim.ini file.", new File(SPGlobal.pathToInternalFiles + "SkyrimINIlocation.txt"));
        } else if (SPGlobal.logging()) {
            SPGlobal.logMain(header, "Skyrim.ini believed to be in: " + ini + ". File exists.");
        }
        return ini;
    }

    /*
     * Logging functions
     */

    /**
     * @return Path to the skyrim local application data folder.
     * @throws IOException
     */
    static public String getSkyrimAppData() throws IOException {
        if (appDataFolder == null) {
            appDataFolder = System.getenv("LOCALAPPDATA");

            // If XP
            if (appDataFolder == null) {
                SPGlobal.logError(header, "Can't locate local app data folder directly, probably running XP.");
                appDataFolder = System.getenv("APPDATA");

                // If Messed Up
                if (appDataFolder == null) {
                    SPGlobal.logError(header, "Can't locate local app data folder.");
                    appDataFolder = Ln.manualFindFile("your Plugins.txt file.\nThis is usually found in your Local Application Data folder.\n"
                            + "You may need to turn on hidden folders to see it.", new File(SPGlobal.pathToInternalFiles + "PluginsListLocation.txt")).getPath();
                    SPGlobal.logMain(header, "Plugin.txt returned: ", appDataFolder, "     Shaving off the \\Plugins.txt.");
                    appDataFolder = appDataFolder.substring(0, appDataFolder.lastIndexOf("\\"));
                    // remove \\Skyrim so it can be added again below. Yep
                    appDataFolder = appDataFolder.substring(0, appDataFolder.lastIndexOf("\\"));
                } else {
                    SPGlobal.logMain(header, "APPDATA returned: ", appDataFolder, "     Shaving off the \\Application Data.");
                    appDataFolder = appDataFolder.substring(0, appDataFolder.lastIndexOf("\\"));
                    SPGlobal.logMain(header, "path now reads: ", appDataFolder, "     appending \\Local Settings\\Application Data");
                    appDataFolder = appDataFolder + "\\Local Settings\\Application Data";
                    SPGlobal.logMain(header, "path now reads: ", appDataFolder);
                }
            }
            appDataFolder += "\\Skyrim Special Edition";
            SPGlobal.logMain(header, SPGlobal.gameName + " App data thought to be found at: ", appDataFolder);
        }
        return appDataFolder;
    }

    /**
     * Returns the plugins.txt file that contains load order information.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    static public String getPluginsTxt() throws FileNotFoundException, IOException {
        String pluginsFile = getSkyrimAppData() + "\\plugins.txt";
        File pluginListPath = new File(pluginsFile);
        if (!pluginListPath.exists()) {
            SPGlobal.logMain(header, SPGlobal.gameName + " Plugin file location wrong. Locating manually.");
            pluginsFile = Ln.manualFindFile("your Plugins.txt file.\nThis is usually found in your Local Application Data folder.\n"
                    + "You may need to turn on hidden folders to see it.", new File(SPGlobal.pathToInternalFiles + "PluginsListLocation.txt")).getPath();
        }
        return pluginsFile;
    }

    /**
     * @return The loadorder.txt file that contains all load order information.
     * @throws IOException
     */
    static public String getLoadOrderTxt() throws IOException {
        String loadorderFile = getSkyrimAppData() + "\\loadorder.txt";
        File loadorderPath = new File(loadorderFile);
        if (!loadorderPath.exists()) {
            throw new FileNotFoundException("Load Order Text file does not exist at: " + loadorderFile);
        }
        return loadorderFile;
    }

    /**
     * @param on True if you want data to be streamed on demand; False if you
     *           want it to all be imported at once.
     */
    static public void setStreamMode(boolean on) {
        streamMode = on;
    }

    /**
     * @param on True if you want the patcher to ignore mods that come after it
     *           in the load order.
     */
    static public void setNoModsAfter(boolean on) {
        noModsAfter = on;
    }

    /**
     * Lets you set the message to display when an error occurs that causes the
     * program to stop prematurely.
     *
     * @param message
     */
    static public void setSUMerrorMessage(String message) {
        SUMGUI.setErrorMessage(message);
    }

    /**
     * Initializes the Debug Logs in a "SkyProcDebug/" folder, and allows you to
     * print messages to them.<br> Do this step early in your program.
     */
    public static void createGlobalLog() {
        createGlobalLog("SkyProcDebug/");
    }

    /**
     * Initializes the Debug Logs in a path + "SkyProcDebug/" folder, and allows
     * you to print messages to them.<br> Do this step early in your program.
     *
     * @param path The path to create the "SkyProcDebug/" folder.
     */
    public static void createGlobalLog(String path) {
        pathToDebug = path;
        log = new SPLogger(path);
    }

    private static final Logger slf4j = LoggerFactory.getLogger(SPGlobal.class);

    private static Optional<SPLogger> optLog() {
        return Optional.ofNullable(log);
    }

    private static void withGlobalLogger(Consumer<SPLogger> logConsumer) {
        optLog().ifPresentOrElse(logConsumer, () -> slf4j.warn("No global logger defined"));
    }

    static void logSync(String header, String... print) {
        withGlobalLogger(l -> l.logSync(header, print));
    }

    /**
     * Logs a message to the Debug Overview file. <br> Use this for major
     * program "milestones".
     *
     * @param header
     * @param print
     */
    public static void logMain(String header, String... print) {
        withGlobalLogger(l -> l.logMain(header, print));
    }

    /**
     * Logs a specific record as blocked in the "Blocked Records.txt" log.
     *
     * @param header
     * @param reason Reason for blocking the record.
     * @param m      Record that was blocked.
     */
    public static void logBlocked(String header, String reason, MajorRecord m) {
        withGlobalLogger(l -> l.logSpecial(SPLogger.SpecialTypes.BLOCKED, header, "Blocked " + m + " for reason: " + reason));
    }

    /**
     * @return True if the logger is currently on.
     */
    public static boolean logging() {
        if (log != null) {
            return SPGlobal.log.logging();
        } else {
            return false;
        }
    }

    /**
     * @param on Turns the logger on/off.
     */
    public static void logging(Boolean on) {
        withGlobalLogger(l -> l.logging(on));
    }

    /**
     * @return True if the logger is currently on.
     */
    public static boolean loggingSync() {
        if (log != null) {
            return SPGlobal.log.loggingSync();
        } else {
            return false;
        }
    }

    /**
     * @param on Turns the logger on/off.
     */
    public static void loggingSync(Boolean on) {
        withGlobalLogger(l -> l.loggingSync(on));
    }

    /**
     * Turns the LLogger async log on/off.
     *
     * @param on
     */
    public static void loggingAsync(Boolean on) {
        withGlobalLogger(l -> l.loggingAsync(on));
    }

    /**
     * @return Whether the LLogger's async log is on/off.
     */
    public static boolean loggingAsync() {
        if (log != null) {
            return SPGlobal.log.loggingAsync();
        } else {
            return false;
        }
    }

    /**
     * Flushes the Debug buffers to the files.
     */
    public static void flush() {
        withGlobalLogger(LLogger::flush);
    }

    /**
     * A special function that simply prints to both the debug overview and the
     * asynchronous log for easy location in either place.
     *
     * @param header
     * @param print
     */
    public static void logError(String header, String... print) {
        withGlobalLogger(l -> l.logError(header, print));
    }

    /**
     * Used for printing exception stack data to the debug overview log.
     *
     * @param e Exception to print.
     */
    public static void logException(Throwable e) {
        withGlobalLogger(l -> l.logException(e));
    }

    /**
     * Logs to a specially created log that was previously created using
     * newSpecialLog().
     *
     * @param e      The enum you defined to symbolize the "key" to the special log.
     * @param header
     * @param print
     */
    public static void logSpecial(Enum e, String header, String... print) {
        withGlobalLogger(l -> l.logSpecial(e, header, print));
    }

    /**
     * Creates a new special log with any enum value as the key.
     *
     * @param e       Any enum you define to symbolize the "key" to the special log.
     * @param logName
     */
    public static void newSpecialLog(Enum e, String logName) {
        withGlobalLogger(l -> l.addSpecial(e, logName));
    }

    /**
     * @return if mod specific log files will be written
     */
    public static boolean logMods() {
        return logMods;
    }

    /**
     * @param on set mod specific logging
     */
    public static void logMods(boolean on) {
        logMods = on;
        if (on) {
            logging(true);
        }
    }

    static void logMod(Mod m, String h, String... data) {
        if (logMods && log != null) {
            log.logMod(m, h, data);
        }
    }

    static void newSyncLog(String fileName) {
        withGlobalLogger(l -> l.newSyncLog(fileName));
    }

    /**
     * Prints a message to the asynchronous log.
     *
     * @param header
     * @param print
     */
    public static void log(String header, String... print) {
        withGlobalLogger(l -> l.log(header, print));
    }

    /**
     * Creates a new asynchronous log.
     *
     * @param fileName Name of the log.
     */
    public static void newLog(String fileName) {
        withGlobalLogger(l -> l.newLog(fileName));
    }
    // Debug Globals

    static void sync(boolean flag) {
        withGlobalLogger(l -> l.sync(flag));
    }

    static boolean sync() {
        if (log != null) {
            return log.sync();
        } else {
            return false;
        }
    }

    /**
     * Redirects System.out to the asynchronous log stream.
     */
    public static void redirectSystemOutStream() {
        if (log == null) {
            createGlobalLog();
        }
        OutputStream outToDebug = new OutputStream() {
            @Override
            public void write(final int b) {
                if (b != 116) {
                    log("", String.valueOf((char) b));
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String output = new String(b, off, len);
                if (output.length() > 2) {
                    log("", output);
                }
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(outToDebug, true));
    }

    static void reset() {
        SPDatabase.activePlugins.clear();
        SPDatabase.addedPlugins.clear();
        SPDatabase.modLookup.clear();
        FormID.allIDs.clear();
        Consistency.clear();
    }

    /**
     * Closes all logs.
     */
    public static void closeDebug() {
        LDebug.wrapUp();
    }

    /**
     * Returns the path to the debug folder.
     *
     * @return
     */
    public static String pathToDebug() {
        return pathToDebug;
    }

    public static boolean getAllModsAsMasters() {
        return allModsAsMasters;
    }

    public static void setAllModsAsMasters(boolean b) {
        allModsAsMasters = b;
    }


    public enum Language {

        /**
         *
         */
        English,
        /**
         *
         */
        Spanish,
        /**
         *
         */
        Italian,
        /**
         *
         */
        French,
        /**
         *
         */
        German,
        /**
         *
         */
        Russian,
        /**
         *
         */
        Czech,
        /**
         *
         */
        Polish,
        /**
         *
         */
        Japanes
    }

}
