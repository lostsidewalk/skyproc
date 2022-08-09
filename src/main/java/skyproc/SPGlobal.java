package skyproc;

import lev.Ln;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import skyproc.gui.SUMGUI;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Global variables/settings of SkyProc.
 *
 * @author Justin Swanson
 */
@SuppressWarnings("unused")
@Slf4j
@Component
public class SPGlobal {

    private static final String FS_DELIMITER = FileSystems.getDefault().getSeparator();

    private static final String SP_GLOBAL_PATH_TO_DATA = "SP_GLOBAL_PATH_TO_DATA";

    private static final String SP_GLOBAL_PATH_TO_INI = "SP_GLOBAL_PATH_TO_INI";

    /**
     * Path to the Data/ folder to look for plugins to import/export.
     */
    public static String pathToData;
    public static String pathToDataFixed;

    @PostConstruct
    public void postConstruct() {
        SPGlobal.pathToData = System.getenv(SP_GLOBAL_PATH_TO_DATA);
        SPGlobal.pathToDataFixed = pathToData;
        log.info("SPGlobal post-init: pathToData={}", pathToData);
    }

    public static final String pathToPatchers = Ln.getMyDocuments().getPath() + File.separator + "SkyProc Patchers" + File.separator;
    /**
     * A default path to "internal files". This is currently only used for
     * saving custom path information for Skyrim.ini and plugins.txt. This can
     * also be used to store your own internal files.
     */
    public static final String pathToInternalFiles = "Files/";
    /**
     * Skyproc will import and embed the language given by SPGlobal.language
     * every time a patch is created. To offer multi-language support, simply
     * give the users of your program ability to adjust this setting.
     */
    public static Language language = Language.English;
    /**
     * Turns off messages about which record is currently being streamed.
     */
    public static final boolean debugStream = true;
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
    public static final boolean debugStringPairing = true;
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
    public static final boolean forceValidateMode = false;
    static final String header = "SPGlobal";
    static final String gameName = "Skyrim";

    /*
     * Customizable Strings
     */
    static Mod globalPatchOut;
    static final SPDatabase globalDatabase = new SPDatabase();
    public static boolean testing = false;
    public static boolean streamMode = true;
    static final boolean deleteAfterExport = true;
    static boolean mergeMode = false;
    static boolean noModsAfter = true;
    static boolean checkMissingMasters = true;
    static MajorRecord lastStreamed;
    static File skyProcDocuments;
    static final ArrayList<ModListing> modsToSkip = new ArrayList<>();
    static final ArrayList<String> modsToSkipStr = new ArrayList<>();
    static final ArrayList<ModListing> modsWhiteList = new ArrayList<>();
    static final ArrayList<String> modsWhiteListStr = new ArrayList<>();
    static String appDataFolder;
    private static boolean allModsAsMasters = false;

    /**
     * Fetch the language of the game from the Skyrim.ini.
     * Any exceptions encountered while reading the config-file are logged in
     * the debug logs and not passed to the caller.
     * @return the fetched language or the previously defined one (defaults to
     * English)
     */
    public static Language getLanguageFromSkyrimIni() {
        try (FileReader fstream = new FileReader(SPGlobal.getSkyrimINI());
             BufferedReader reader = new BufferedReader(fstream)) {
            String line = reader.readLine();
            while (line != null) {
                if (line.contains("sLanguage")) {
                    String iniLanguage = line.substring(line.indexOf("=") + 1);
                    for (SPGlobal.Language lang : SPGlobal.Language.values()) {
                        if (lang.name().equalsIgnoreCase(iniLanguage)) {
                            return lang;
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
        return language;
    }

    /**
     * @return a File path to the SkyProc Documents folder.
     */
    public static File getSkyProcDocuments() throws IOException {
        if (skyProcDocuments == null) {
            File myDocs = getMyDocumentsSkyrimFolder();
            skyProcDocuments = new File(myDocs.getPath() + File.separator + "SkyProc" + File.separator);
            //noinspection ResultOfMethodCallIgnored
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
     * @param s Mod to skip
     */
    public static void addModToSkip(String s) {
        s = s.toUpperCase();
        if (s.contains(".ESP") || s.contains(".ESM")) {
            addModToSkip(new ModListing(s));
        } else {
            modsToSkipStr.add(s);
        }
    }

    public static boolean isModToSkip(ModListing m) {
        return modsToSkip.contains(m) || isModToSkip(m.print());
    }

    public static boolean isModToSkip(String name) {
        return Ln.hasAnyKeywords(name, modsToSkipStr);
    }

    public static boolean isWhiteListed(ModListing m) {
        return (modsWhiteList.isEmpty() && modsWhiteListStr.isEmpty())
                || modsWhiteList.contains(m) || isWhiteListed(m.print());
    }

    public static boolean isWhiteListed(String name) {
        return (modsWhiteList.isEmpty() && modsWhiteListStr.isEmpty())
                || Ln.hasAnyKeywords(name, modsWhiteListStr);
    }

    /**
     * True if not blacklisted and if white listed (if there are any white
     * listings)
     */
    public static boolean shouldImport(ModListing m) {
        return !SPGlobal.isModToSkip(m) && SPGlobal.isWhiteListed(m);
    }

    /**
     * True if not blacklisted and if white listed (if there are any white
     * listings)
     */
    public static boolean shouldImport(String name) {
        return !SPGlobal.isModToSkip(name) && SPGlobal.isWhiteListed(name);
    }

    /**
     * Adds a mod to the white list. Once a mod is on the white list, only white
     * list mods will be imported by SkyProc.
     */
    public static void addModToWhiteList(ModListing m) {
        modsWhiteList.add(m);
    }

    /**
     * Adds a mod to the white list. Once a mod is on the white list, only white
     * list mods will be imported by SkyProc.
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
     */
    public static File getMyDocumentsSkyrimFolder() throws IOException {
        return getSkyrimINI().getParentFile();
    }

    /**
     * Returns the Skyrim.ini file in the My Documents folder.
     */
    public static File getSkyrimINI() throws IOException {
        File myDocuments = Ln.getMyDocuments();
        String pathToIni = System.getenv(SP_GLOBAL_PATH_TO_INI);
        File ini = new File(pathToIni);

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
     */
    static public String getSkyrimAppData() throws IOException {
        if (appDataFolder == null) {
            appDataFolder = System.getenv("LOCALAPPDATA");
            SPGlobal.logMain(header, SPGlobal.gameName + " LOCALAPPDATA=" + appDataFolder);
            if (appDataFolder == null) {
//                SPGlobal.logError(header, "Can't locate local app data folder directly.");
                appDataFolder = System.getenv("APPDATA");
                SPGlobal.logMain(header, SPGlobal.gameName + " APPDATA=" + appDataFolder);
                if (appDataFolder == null) {
//                    SPGlobal.logError(header, "Can't locate local app data folder.");
                    appDataFolder = Ln.manualFindFile(
                            "your Plugins.txt file.\n" +
                            "This is usually found in your Local Application Data folder.\n" +
                            "You may need to turn on hidden folders to see it.", new File(SPGlobal.pathToInternalFiles + "PluginsListLocation.txt")).getPath();
                    SPGlobal.logMain(header, "Plugin.txt returned: ", appDataFolder, "     Shaving off the " + File.separator + "Plugins.txt.");
                    appDataFolder = appDataFolder.substring(0, appDataFolder.lastIndexOf(FS_DELIMITER));
                } else {
                    SPGlobal.logMain(header, "APPDATA returned: ", appDataFolder, "     Shaving off the \\Application Data.");
                    appDataFolder = appDataFolder.substring(0, appDataFolder.lastIndexOf("\\"));
                    SPGlobal.logMain(header, "path now reads: ", appDataFolder, "     appending \\Local Settings\\Application Data");
                    appDataFolder = appDataFolder + "\\Local Settings\\Application Data";
                    SPGlobal.logMain(header, "path now reads: ", appDataFolder);
                }
            }
            SPGlobal.logMain(header, SPGlobal.gameName + " App data thought to be found at: ", appDataFolder);
        }
        return appDataFolder;
    }

    /**
     * Returns the plugins.txt file that contains load order information.
     */
    static public String getPluginsTxt() throws IOException {
        String pluginsFile = getSkyrimAppData() + FS_DELIMITER + "plugins.txt";
        SPGlobal.logMain(header, SPGlobal.gameName + " pluginsFile=" + pluginsFile);
        File pluginListPath = new File(pluginsFile);
        SPGlobal.logMain(header, SPGlobal.gameName + " Attempting to locate Plugins.txt in path=" + pluginListPath.getPath());
        if (!pluginListPath.exists()) {
            SPGlobal.logMain(header, SPGlobal.gameName + " Plugin file location wrong. Locating manually.");
            pluginsFile = Ln.manualFindFile(
                    "your Plugins.txt file.\n" +
                    "This is usually found in your Local Application Data folder.\n" +
                    "You may need to turn on hidden folders to see it.", new File(SPGlobal.pathToInternalFiles + "PluginsListLocation.txt")).getPath();
        }

        return pluginsFile;
    }

    /**
     * @return The loadorder.txt file that contains all load order information.
     */
    static public String getLoadOrderTxt() throws IOException {
        String loadorderFile = getSkyrimAppData() + File.separator + "loadorder.txt";
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
     */
    static public void setSUMerrorMessage(String message) {
        SUMGUI.setErrorMessage(message);
    }

    private static final Logger slf4j = LoggerFactory.getLogger(SPGlobal.class);

    private static Optional<Logger> optLog() {
        return Optional.ofNullable(log);
    }

    private static void withGlobalLogger(Consumer<Logger> logConsumer) {
        optLog().ifPresent(logConsumer);
    }

    static void logSync(String header, String... print) {
        withGlobalLogger(l -> l.info("[{}] {}", header, print));
    }

    /**
     * Logs a message to the Debug Overview file. <br> Use this for major
     * program "milestones".
     */
    public static void logMain(String header, String... print) {
        withGlobalLogger(l -> l.info("[{}] {}", header, print));
    }

    /**
     * Logs a specific record as blocked in the "Blocked Records.txt" log.
     */
    public static void logBlocked(String header, String reason, MajorRecord m) {
        withGlobalLogger(l -> l.info("[{}] Blocked {} due to: {}", header, m, reason));
    }

    /**
     * @return True if the logger is currently on.
     */
    public static boolean logging() {
        return log != null;
    }

    /**
     * A special function that simply prints to both the debug overview and the
     * asynchronous log for easy location in either place.
     */
    public static void logError(String header, String... print) {
        withGlobalLogger(l -> l.error("[{}] {}", header, print));
    }

    /**
     * Used for printing exception stack data to the debug overview log.
     *
     * @param e Exception to print.
     */
    public static void logException(Throwable e) {
        withGlobalLogger(l -> l.error("EXCEPTION due to: {}", e.getMessage()));
    }

    /**
     * Logs to a specially created log that was previously created using
     * newSpecialLog().
     */
    public static void logSpecial(Enum<?> e, String header, String... print) {
        withGlobalLogger(l -> l.info("[{} {}] {}", e, header, print));
    }

    /**
     * Prints a message to the asynchronous log.
     */
    public static void log(String header, String... print) {
        withGlobalLogger(l -> l.info("[{}] {}", header, print));
    }

    public static void logMod(Mod srcMod, String header, String... data) {
        log.info("[{}] [{}] {}", srcMod.getName(), header, data);
    }

    static void reset() {
        SPDatabase.clear();
        FormID.allIDs.clear();
        Consistency.clear();
    }

    public static boolean getAllModsAsMasters() {
        return allModsAsMasters;
    }

    public static void setAllModsAsMasters(boolean b) {
        allModsAsMasters = b;
    }

    public enum Language {
        English,
        Spanish,
        Italian,
        French,
        German,
        Russian,
        Czech,
        Polish,
        Japanes
    }
}
