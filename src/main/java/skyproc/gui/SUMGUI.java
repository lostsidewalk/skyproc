package skyproc.gui;

import lev.Ln;
import lev.gui.*;
import lev.gui.resources.LFonts;
import lev.gui.resources.LImages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import skyproc.*;
import skyproc.SPGlobal.Language;
import skyproc.exceptions.MissingMaster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The standard GUI setup used in SUM. This can be used to create GUIs that
 * manage your settings, handle savefiles, and hook directly into SUM.
 *
 * @author Justin Swanson
 */
@Slf4j
public class SUMGUI extends JFrame {

    static final Color light = new Color(238, 233, 204);
    static final Color lightGray = new Color(190, 190, 190);
    static final Color darkGray = new Color(110, 110, 110);
    static final Color lightred = Color.red;

    /**
     * Bounds of the SUM GUI.
     */
    public final static Rectangle fullDimensions = new Rectangle(0, 0, 950, 632);
    /**
     * Bounds of the left column
     */
    public final static Rectangle leftDimensions = new Rectangle(0, 0, 299, fullDimensions.height - 28); // For status update
    /**
     * Bounds of the middle column
     */
    public final static Rectangle middleDimensions = new Rectangle(leftDimensions.x + leftDimensions.width + 7, 0, 330, fullDimensions.height);
    /**
     * Bounds of the right column
     */
    public final static Rectangle rightDimensions = new Rectangle(middleDimensions.x + middleDimensions.width + 7, 0, 305, fullDimensions.height);
    /**
     * Bounds of the two right columns
     */
    public final static Rectangle middleRightDimensions = new Rectangle(middleDimensions.x, 0, rightDimensions.x + rightDimensions.width, fullDimensions.height);
    /**
     * Help panel on the right column of the GUI.
     */
    static public final LHelpPanel helpPanel = new LHelpPanel(rightDimensions, new Font("Serif", Font.BOLD, 25), light, lightGray, LImages.arrow(true, false), 10);
    /**
     * Bounds of the two left columns
     */
    public final static Rectangle middleLeftDimensions = new Rectangle(0, 0, middleDimensions.x + middleDimensions.width, middleDimensions.height);
    static final String header = "SUM";
    /**
     * Import/Export background thread is stored here for access.
     */
    static public Thread parser;
    /**
     * Progress bar frame that pops up at the end when creating the patch.
     */
    static public final SPProgressBarFrame progress = new SPProgressBarFrame(
            new Font("SansSerif", Font.PLAIN, 12), Color.lightGray,
            new Font("SansSerif", Font.PLAIN, 10), Color.lightGray);
    static JFrame singleton = null;
    static SUM hook;
    static ProcessingThread parserRunnable;
    static boolean imported = false;
    static boolean exitRequested = false;
    static LImagePane backgroundPanel;
    static LLabel patchNeededLabel;
    static boolean needsPatching = false;
    static boolean justPatching = false;
    static boolean justSettings = false;
    static LCheckBox forcePatch;
    static LImagePane skyProcLogo;
    static JTextArea statusUpdate;
    static LLabel versionNum;
    static LButton cancelPatch;
    static LButton startPatch;
    static final SUMGUISave save = new SUMGUISave();
    static Font SUMmainFont = LFonts.MyriadProBold(30);
    static final Font SUMSmallFont = new Font("SansSerif", Font.PLAIN, 10);
    static String errorMessage = "Please contact the author.";

    /**
     * This boolean flag distinguishes between a window-close event caused by
     * the patch button and the X button.
     */
    private static boolean patchrequested = false;

    SUMGUI() {
        super(hook.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        Dimension GUISIZE = new Dimension(962, 667);
        setSize(GUISIZE);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - GUISIZE.width / 2, dim.height / 2 - GUISIZE.height / 2);
        setLayout(null);
        addComponents();
        SUMGUI.helpPanel.setHeaderFont(SUMmainFont.deriveFont(Font.PLAIN, 25));
        SUMGUI.helpPanel.setXOffsets(23, 35);
        addWindowListener(new WindowListener() {

            @Override
            public void windowClosed(WindowEvent arg0) {
            }

            @Override
            public void windowActivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                closingGUIwindow();
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }
        });
        helpPanel.setHeaderColor(hook.getHeaderColor());
        helpPanel.setDefaultY(75);
    }

    /**
     * Opens and hooks onto a program that implements the SUM interface.
     *
     * @param hook     Program to open and hook to
     * @param mainArgs Pass the main String args from your main function here
     */
    public static void open(final SPGlobal spGlobal, final SUM hook, String[] mainArgs) {
        handleArgs(spGlobal, mainArgs);

        loadBlockedMods("Files/BlockList.txt");

        clean();

        SUMGUI.hook = hook;
        SwingUtilities.invokeLater(() -> {
            if (singleton == null) {
                try {
                    if (hook.hasSave()) {
                        hook.getSave().init();
                    }

                    // SUM save
                    save.init();

                    logStatus();
                    SPGlobal.setGlobalPatch(hook.getExportPatch());

                    try {
                        hook.onStart();
                    } catch (Exception ex) {
                        SPGlobal.logException(ex);
                    }

                    if (hook.hasCustomMenu()) {
                        singleton = hook.openCustomMenu();
                    } else {
                        singleton = new SUMGUI();
                    }
                    if (hook.hasStandardMenu()) {
                        SPMainMenuPanel menu = hook.getStandardMenu();
                        if (!menu.hasVersion()) {
                            menu.setVersion(hook.getVersion());
                        }
                        if (hook.getLogo() != null) {
                            menu.addLogo(hook.getLogo());
                        }
                        singleton.add(menu);
                        if (justSettings) {
                            switchToSettingsMode();
                        }
                    }

                    progress.moveToCorrectLocation();
                    if (!justPatching) {
                        progress.setGUIref(singleton);
                    }

                    if (justPatching && needsImporting()) {
                        exitRequested = true;
                        progress.open();
                        runThread();
                    } else if (!justPatching && hook.importAtStart()) {
                        runThread();
                    } else if (testNeedsPatching(false)) {
                        setPatchNeeded(true);
                    }
                } catch (Exception e) {
                    SPGlobal.logException(e);
                    JOptionPane.showMessageDialog(null, "<html>There was an error running the program.<br>"
                            + "Refer to the debug logs.</html>");
                }
            }
        });
    }

    static void clean() {
        ArrayList<File> files = new ArrayList<>();
        files.add(new File(SPGlobal.pathToInternalFiles + "/Last Masterlist.txt"));
        files.add(new File(SPGlobal.pathToInternalFiles + "/Last Modlist.txt"));

        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }

    static void logStatus() {
        SPGlobal.logMain("Status", hook.getName() + " version: " + hook.getVersion());
        SPGlobal.logMain("Status", "Used Memory: " + Ln.toMB((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) + "MB");
        SPGlobal.logMain("Status", "Max Memory: " + Ln.toMB(Runtime.getRuntime().maxMemory()) + "MB");
    }

    static void loadBlockedMods(String path) {
        File f = new File(path);
        if (f.exists()) {
            try {
                boolean foundMods = false;
                ArrayList<String> blockMods = Ln.loadFileToStrings(f, true);
                for (String s : blockMods) {
                    s = Ln.cleanLine(s, "//");
                    if (foundMods && !"".equals(s)) {
                        SPGlobal.addModToSkip(s);
                    } else if (s.contains("MOD BLOCKS")) {
                        foundMods = true;
                    }
                }
            } catch (IOException ex) {
                SPGlobal.logException(ex);
            }
        } /* else {
            // Create a placeholder for people to see
            try {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
                    out.write("==MOD BLOCKS==\n");
                    out.write("//Add a mod names or keywords to look for to block\n//One per line");
                }
            } catch (IOException ex) {
                SPGlobal.logException(ex);
            }
        } */
    }

    static void handleArgs(SPGlobal spGlobal, String[] args) {
        final ArrayList<String> arguments = Ln.toUpper(new ArrayList<>(Arrays.asList(args)));

        if (SPGlobal.logging()) {
            SPGlobal.logMain("Run Location", "Program running from: " + (new File(".").getAbsolutePath()));
            for (String arg : arguments) {
                SPGlobal.logMain("SUM", "Arg: " + arg);
            }
        }

        // Just Patching
        justPatching = arguments.contains("-GENPATCH");

        // Exclude mods after the patch?
        SPGlobal.setNoModsAfter(!arguments.contains("-MODSAFTER"));

        // Progress Bar Location
        int index = arguments.indexOf("-PROGRESSLOCATION");
        if (index != -1) {
            Dimension progressLoc = new Dimension(Integer.parseInt(arguments.get(index + 1)),
                    Integer.parseInt(arguments.get(index + 2)));
            SUMGUI.progress.setCorrectLocation(progressLoc.width, progressLoc.height);
        }

        justSettings = arguments.contains("-JUSTSETTINGS");

        index = arguments.indexOf("-LANGUAGE");
        if (index != -1) {
            String lang = arguments.get(index + 1);
            for (Language l : Language.values()) {
                if (lang.equalsIgnoreCase(l.toString())) {
                    SPGlobal.language = l;
                    break;
                }
            }
        }

        if (arguments.contains("-SUMBLOCK")) {
            try {
                loadBlockedMods(spGlobal.getSkyProcDocuments() + "\\SUM Mod Blocklist.txt");
            } catch (IOException ex) {
                SPGlobal.logException(ex);
            }
        }

        if (arguments.contains("-FORCE")) {
            setPatchNeeded(true);
        }

        SPGlobal.setStreamMode(!arguments.contains("-NOSTREAM"));

        if (arguments.contains("-ALLMODSASMASTERS")) {
            SPGlobal.setAllModsAsMasters(true);
        }

        if (SPGlobal.logging()) {
            if (justPatching) {
                SPGlobal.logMain("SUM", "Program is just patching. (-GENPATCH)");
            }
        }
    }

    static void switchToSettingsMode() {
        patchNeededLabel.setText("Change Settings Mode - No patch will be generated.");
        patchNeededLabel.setVisible(true);
        forcePatch.setVisible(false);
        cancelPatch.setVisible(false);
        startPatch.setVisible(false);

        try {
            final LImagePane backToSUM = new LImagePane(new ClassPathResource("BackToSUMdark.png").getURL());
            backToSUM.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    exitProgram(false, true);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    try {
                        backToSUM.setImage(new ClassPathResource("BackToSUM.png").getURL());
                    } catch (IOException ex) {
                        SPGlobal.logException(ex);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    try {
                        backToSUM.setImage(new ClassPathResource("BackToSUMdark.png").getURL());
                    } catch (IOException ex) {
                        SPGlobal.logException(ex);
                    }
                }
            });
            backToSUM.setLocation(0, 510);
            singleton.add(backToSUM);
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
    }

    /**
     * Lets you set the message to display when an error occurs that causes the
     * program to stop prematurely.
     *
     * @param message
     */
    static public void setErrorMessage(String message) {
        errorMessage = message;
    }

    static void imported() {
        SPProgressBarPlug.setStatus("Done importing.");
        setPatchNeeded(testNeedsPatching(true));
    }

    /**
     * Sets the program to require a patch, and switches the GUI display.
     *
     * @param on
     */
    public static void setPatchNeeded(boolean on) {
        needsPatching = on;
        if (SPGlobal.logging()) {
            SPGlobal.logMain(header, "Patch needed: " + on);
        }
        if (patchNeededLabel != null) {
            patchNeededLabel.setVisible(on);
            forcePatch.setVisible(!on);
        }
    }

    static void setBackgroundPicture(URL backgroundPicture) {
        try {
            backgroundPanel.setImage(backgroundPicture);
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
    }

    static boolean needsImporting() {

        if (forcePatch.isSelected() || testNeedsPatching(false)) {
            if (SPGlobal.logging()) {
                SPGlobal.logMain("Needs Importing", "Needs importing because force patch was on or patch needed updating.");
            }
            setPatchNeeded(true);
            return true;
        }
        try {
            // See if imported mods and last mod list are the same
            ArrayList<String> oldList = save.getStrings(SUMGUISettings.LastModlist);
            for (int i = 0; i < oldList.size(); i++) {
                String oldString = oldList.get(i);
                if (oldString.contains(SPDatabase.dateDelim)) {
                    oldList.set(i, oldString.substring(0, oldString.indexOf(SPDatabase.dateDelim)));
                }
            }
            ArrayList<ModListing> curList = new ArrayList<>(SPImporter.getActiveModList());
            curList.remove(hook.getListing());
            ArrayList<ModListing> curListTmp = new ArrayList<>(curList);

            if (curList.size() != oldList.size()) {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain("Needs Importing", "Needs importing because last Modlist isn't the same size as current.");
                }
                return true;
            }

            for (int i = 0; i < curList.size(); i++) {
                ModListing m = curListTmp.get(i);
                if (!oldList.get(i).equals(m.print().toUpperCase())) {
                    if (SPGlobal.logging()) {
                        SPGlobal.logMain("Needs Importing", "Needs importing because " + oldList.get(i) + " doesn't match " + m.print() + " at index " + i);
                    }
                    return true;
                }
            }

            // Check dates
            ArrayList<String> changedMods = getChangedMods(true);
            if (changedMods.size() > 0) {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain("Needs Importing", "Needs importing because " + changedMods.get(0) + " had its date changed.");
                }
                return true;
            }

            //Don't need a patch, check for custom hook coding
            return SUMGUI.hook.needsPatching();

        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }

        return true;
    }

    static boolean testNeedsPatching(boolean imported) {
        if (needsPatching) {
            return true;
        }

        // Check if patch exists
        if (!SPGlobal.getGlobalPatch().exists()) {
            if (SPGlobal.logging()) {
                SPGlobal.logMain(header, "Patch needed because no patch existed.");
            }
            return true;
        }

        // Check if crashed earlier while needing to patch
        if (save.getBool(SUMGUISettings.CrashState)) {
            if (SPGlobal.logging()) {
                SPGlobal.logMain(header, "Patch needed because it closed prematurely earlier while needing to patch.");
            }
            return true;
        }

        //Check versions
        if (save.getInt(SUMGUISettings.PrevVersion) != NiftyFunc.versionToNum(hook.getVersion())) {
            if (SPGlobal.logging()) {
                SPGlobal.logMain(header, "Needs update because of versioning: " + save.getInt(SUMGUISettings.PrevVersion) + " to " + hook.getVersion());
            }
            return true;
        }

        // Check if savefile has important settings changed
        if (hook.hasSave()) {
            if (hook.getSave().checkFlagOr(0)) {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain(header, "Patch needed because an important setting changed.");
                }
                return true;
            }
        }

        // If imported, check master lists
        if (imported) {
            // Compile old and new Master lists
            ArrayList<String> oldMasterList = save.getStrings(SUMGUISettings.LastMasterlist);

            ArrayList<String> curImportedMods = new ArrayList<>();
            for (ModListing m : SPDatabase.getImportedModListings()) {
                curImportedMods.add(m.print().toUpperCase());
            }
            curImportedMods.remove(SPGlobal.getGlobalPatch().getName().toUpperCase());

            //Remove matching master mods, must be in order
            ArrayList<String> curImportedModsTmp = new ArrayList<>(curImportedMods);
            for (int i = 0; i < curImportedModsTmp.size(); i++) {
                String curName = curImportedModsTmp.get(i);
                if (oldMasterList.contains(curName)) {
                    for (int j = 0; j < oldMasterList.size(); j++) {
                        if (oldMasterList.get(j).equalsIgnoreCase(curName)) {
                            oldMasterList.remove(j);
                            break;
                        } else if (curImportedModsTmp.contains(oldMasterList.get(j))) {
                            //Matching mods out of order, need to patch
                            if (SPGlobal.logging()) {
                                SPGlobal.logMain(header, "Patch needed because masters from before were in a different order.");
                            }
                            return true;
                        }
                    }
                }
            }

            //If old masters are missing, need patch
            if (!oldMasterList.isEmpty()) {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain(header, "Patch needed because old masters are missing:");
                    for (String s : oldMasterList) {
                        SPGlobal.logMain(header, "   " + s);
                    }
                }
                return true;
            }


            //Check mods that were imported last time for date modified changes, and remove them if they weren't changed.
            curImportedMods.removeAll(getChangedMods(false));

            //Check new mods for any records patcher is interested in.  If interesting records found, need patch.
            for (String curString : curImportedMods) {
                Mod curMaster = SPDatabase.getMod(new ModListing(curString));
                ArrayList<GRUP_TYPE> contained = curMaster.getContainedTypes();
                for (GRUP_TYPE g : hook.importRequests()) {
                    if (contained.contains(g)) {
                        if (SPGlobal.logging()) {
                            SPGlobal.logMain(header, "Patch needed because " + curMaster + " had records patch might be interested in.");
                        }
                        return true;
                    }
                }
            }

        }

        //Don't need a patch, check for custom hook coding
        if (SPGlobal.logging()) {
            SPGlobal.logMain(header, "Checking SUM hook to see if it wants to create a patch.");
        }
        return SUMGUI.hook.needsPatching();
    }

    static ArrayList<String> getChangedMods(boolean changed) {
        //Compile old modlist and dates
        ArrayList<String> oldModListRaw = save.getStrings(SUMGUISettings.LastModlist);
        ArrayList<String> oldModList = new ArrayList<>(oldModListRaw.size());
        ArrayList<Long> oldModListDate = new ArrayList<>(oldModListRaw.size());
        ArrayList<String> out = new ArrayList<>();
        for (String modAndDate : oldModListRaw) {
            String[] split = modAndDate.split(SPDatabase.dateDelim);
            oldModList.add(split[0]);
            if (split.length > 1) {
                oldModListDate.add(Long.valueOf(split[1]));
            }
        }

        // If no dates
        if (oldModList.size() != oldModListDate.size()) {
            if (changed) {
                return oldModList;
            } else {
                return new ArrayList<>(0);
            }
        }

        ArrayList<String> oldModListTmp = new ArrayList<>(oldModList);
        for (int i = 0; i < oldModListTmp.size(); i++) {
            String modName = oldModListTmp.get(i);
            File modFile = new File(SPGlobal.pathToDataFixed + modName);
            boolean changedF = modFile.lastModified() != oldModListDate.get(i);
            if ((changed && changedF)
                    || (!changed && !changedF)) {
                out.add(modName);
            }
        }
        return out;
    }

    static void closingGUIwindow() {
        SPGlobal.logMain(header, "Window Closing.");
        if (justSettings || !patchrequested) {
            exitProgram(false, true);
        }
        exitRequested = true;
        if (!imported && !needsImporting()) {
            SPProgressBarPlug.done();
            if (SPGlobal.logging()) {
                SPGlobal.logMain(header, "Closing program early because it does not need importing.");
            }
            exitProgram(false, true);
        } else {
            progress.setExitOnClose();
            progress.open();
        }
        runThread();
    }

    /**
     * Immediately saves settings to file, closes debug logs, and exits the
     * program.<br> NO patch is generated.
     *
     * @param generatedPatch True if a patch was generated before exiting.
     * @param forceClose     Will force close if true, otherwise will dispose and
     *                       naturally let the program close (which may not happen, given certain
     *                       circumstances)
     */
    static public void exitProgram(boolean generatedPatch, boolean forceClose) {
        SPGlobal.log(header, "Exit requested.");
        if (generatedPatch) {
            save.setStrings(SUMGUISettings.LastModlist, Ln.toUpper(SPDatabase.getModListDates()));
            save.setStrings(SUMGUISettings.LastMasterlist, Ln.toUpper(SPGlobal.getGlobalPatch().getMastersStrings()));
            save.setBool(SUMGUISettings.CrashState, false);
            save.setInt(SUMGUISettings.PrevVersion, NiftyFunc.versionToNum(hook.getVersion()));
        } else if (needsPatching) {
            save.setBool(SUMGUISettings.CrashState, true);
        }

        if (singleton != null) {
            singleton.dispose();
        }
        progress.dispose();

        try {
            hook.onExit(generatedPatch);
        } catch (Exception e) {
            SPGlobal.logException(e);
        }
        if (hook.hasSave()) {
            hook.getSave().saveToFile();
        }
        save.saveToFile();
    }

    static void runThread() {
        runThread(null);
    }

    static void runThread(Runnable r) {
        if (parser == null || !parser.isAlive()) {
            parserRunnable = new ProcessingThread();
            parser = new Thread(parserRunnable);
            if (r != null) {
                parserRunnable.afterImporting.add(r);
            }
            parser.start();
        } else {
            if (r != null) {
                parserRunnable.afterImporting.add(r);
            }
        }
    }

    static void lootWarning() {
        if (save.getBool(SUMGUISettings.LOOTWarning)) {
            String message = "<html>This patcher is going to run LOOT first to standardize ordering.<br><br>"
                    + "To turn LOOT execution off, download <a href=\"http://skyrim.nexusmods.com/mods/29865\">SUM</a> and adjust its settings.<br>"
                    + "However, running LOOT is recommended.  "
                    + "Do you want to continue patching?</html>";
            int response = JOptionPane.showConfirmDialog(null, Lg.getQuickHTMLPane(message), "Running LOOT", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                response = JOptionPane.showConfirmDialog(null, "Do you want to see this warning next time?", "Running LOOT", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.NO_OPTION) {
                    save.setBool(SUMGUISettings.LOOTWarning, false);
                }
            } else {
                SUMGUI.exitProgram(false, true);
            }
        }
    }

    /**
     * Starts importing desired mods. Runs code after it's finished.
     *
     * @param codeToRunAfter Runnable object with code to execute after.
     */
    public static void startImport(Runnable codeToRunAfter) {
        if (!imported) {
            runThread(codeToRunAfter);
        } else {
            Thread t = new Thread(codeToRunAfter);
            t.start();
        }
    }

    /**
     * Starts importing desired mods.
     */
    public static void startImport() {
        runThread(null);
    }

    /**
     * Closes the GUI and starts patching if needed. (as if user hit the exit
     * button)
     */
    public void closeWindow() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

    final void addComponents() {
        try {

            backgroundPanel = new LImagePane(new ClassPathResource("background.jpg").getURL());
            super.add(backgroundPanel);

            startPatch = new LButton("Patch");
            startPatch.setLocation(backgroundPanel.getWidth() - startPatch.getWidth() - 5, 5);
            startPatch.addActionListener(e -> {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain(header, "Starting patch because user pressed patch.");
                }
                patchrequested = true;
                closeWindow();
            });
            startPatch.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    helpPanel.setDefaultPos();
                    helpPanel.setContent("This will create a patch if necessary and then exit the program.");
                    helpPanel.setTitle("Start Patch and Exit");
                    helpPanel.hideArrow();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            backgroundPanel.add(startPatch);

            cancelPatch = new LButton("Cancel");
            cancelPatch.setLocation(startPatch.getX() - cancelPatch.getWidth() - 5, 5);
            cancelPatch.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    helpPanel.setContent("This will save your current program settings and exit the program "
                            + "immediately without generating a patch.\n\n"
                            + ""
                            + "After doing so, it is recommended that "
                            + "you start this patcher again and patch correctly before "
                            + "playing the game again.\n\n"
                            + ""
                            + "NOTE: Clicking exit on the progress bar while patching has the same effect.");
                    helpPanel.setTitle("Cancel and Exit");
                    helpPanel.hideArrow();
                    helpPanel.setDefaultPos();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            cancelPatch.addActionListener(e -> {
                if (SPGlobal.logging()) {
                    SPGlobal.logMain(header, "Closing program early because user cancelled.");
                }
                exitProgram(false, true);
            });
            backgroundPanel.add(cancelPatch);

            forcePatch = new LCheckBox("Force Patch on Exit", SUMSmallFont, Color.GRAY);
            forcePatch.setLocation(rightDimensions.x + 10, cancelPatch.getY() + cancelPatch.getHeight() / 2 - forcePatch.getHeight() / 2);
            forcePatch.setOffset(-4);
            forcePatch.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    helpPanel.setContent("This will force the patcher to create a patch, even if "
                            + "it doesn't think it needs to.  Use this if you want to forcibly "
                            + "remake the patch for any reason.");
                    helpPanel.setTitle("Force Patch On Exit");
                    helpPanel.hideArrow();
                    helpPanel.setDefaultPos();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            backgroundPanel.add(forcePatch);

            patchNeededLabel = new LLabel("A patch will be generated upon exit.", SUMSmallFont, Color.GRAY);
            patchNeededLabel.setLocation(forcePatch.getLocation());
            patchNeededLabel.setVisible(false);
            backgroundPanel.add(patchNeededLabel);

            progress.addWindowListener(new WindowListener() {

                @Override
                public void windowClosed(WindowEvent arg0) {
                }

                @Override
                public void windowActivated(WindowEvent arg0) {
                }

                @Override
                public void windowClosing(WindowEvent arg0) {
                    if (progress.getDefaultCloseOperation() == JFrame.DISPOSE_ON_CLOSE) {
                        if (SPGlobal.logging()) {
                            SPGlobal.logMain(header, "Closing program early because progress bar was forced to close by user.");
                        }
                        exitProgram(false, true);
                    }
                }

                @Override
                public void windowDeactivated(WindowEvent arg0) {
                }

                @Override
                public void windowDeiconified(WindowEvent arg0) {
                }

                @Override
                public void windowIconified(WindowEvent arg0) {
                }

                @Override
                public void windowOpened(WindowEvent arg0) {
                }
            });
            if (hook.hasLogo()) {
                SUMGUI.progress.addLogo(hook.getLogo());
            }

            statusUpdate = new JTextArea();
            statusUpdate.setSize(250, 18);
            statusUpdate.setLocation(5, getFrameHeight() - statusUpdate.getHeight() - 10);
            statusUpdate.setForeground(Color.LIGHT_GRAY);
            statusUpdate.setOpaque(false);
            statusUpdate.setText("Started application");
            statusUpdate.setEditable(false);
            statusUpdate.setVisible(true);
            backgroundPanel.add(statusUpdate);

            skyProcLogo = new LImagePane(new ClassPathResource("SkyProc Logo Small.png").getURL());
            skyProcLogo.setLocation(5, statusUpdate.getY() - skyProcLogo.getHeight() - 5);
            backgroundPanel.add(skyProcLogo);

            helpPanel.setBounds(rightDimensions);
            backgroundPanel.add(helpPanel);

            SPProgressBarPlug.addProgressBar(new SUMProgress());

            if (!justPatching) {
                setVisible(true);
            }

        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }

    }

    int getFrameHeight() {
        return this.getHeight() - 28;
    }

    /**
     * @param comp
     * @return
     */
    @Override
    public Component add(Component comp) {
        return backgroundPanel.add(comp, 0);
    }

    enum SUMGUISettings {

        LastMasterlist,
        LastModlist,
        PrevVersion,
        CrashState,
        LOOTWarning
    }

    static class ProcessingThread implements Runnable {

        public final Set<Runnable> afterImporting = new HashSet<>();

        @Override
        public void run() {
            SPGlobal.logMain("START IMPORT THREAD", "Starting of process thread.");
            try {
                if (!imported) {
                    SPImporter.importActiveMods(hook.importRequests());
                    imported();
                    imported = true;
                    for (Runnable r : afterImporting) {
                        r.run();
                    }
                }
                if (exitRequested) {
                    if (needsPatching || forcePatch == null || forcePatch.isSelected()) {

                        // Check if required mods are loaded
                        for (ModListing m : hook.requiredMods()) {
                            if (!SPDatabase.hasMod(m)) {
                                StringBuilder modNames = new StringBuilder();
                                for (ModListing m2 : hook.requiredMods()) {
                                    modNames.append("\n").append(m2.toString());
                                }
                                JOptionPane.showMessageDialog(null, "This patcher requires the following mods, please add them to your load order:" + modNames);
                                SPGlobal.logMain("Required Mods", "Didn't have required mods.  Stopping patcher.");
                                SPProgressBarPlug.done();
                                exitProgram(true, true);
                            }
                        }

                        hook.runChangesToPatch();

                        try {
                            // Export your custom patch.
                            SPGlobal.getGlobalPatch().export();
                        } catch (Exception ex) {
                            // If something goes wrong, show an error message.
                            SPGlobal.logException(ex);
                            JOptionPane.showMessageDialog(null, "There was an error exporting the custom patch.\n(" + ex.getMessage() + ")\n\n" + errorMessage);
                            exitProgram(false, true);
                        }

                        if (SPGlobal.logging()) {
                            SPGlobal.logMain(header, "Closing program after successfully running patch.");
                        }
                    }
                    if (SPGlobal.logging()) {
                        SPGlobal.logMain(header, "Closing program normally from thread.");
                    }
                    SPProgressBarPlug.done();
                    exitProgram(true, false);
                }
            } catch (MissingMaster m) {
                log.error(m.getMessage(), m);
                SPGlobal.logException(m);
                JOptionPane.showMessageDialog(null, m + "\n\n Please activate and try again.");
                exitProgram(false, true);

            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                SPGlobal.logException(e);
                JOptionPane.showMessageDialog(null, "There was an exception thrown during program execution: '" + e + "'\n\n" + errorMessage);

                // if exception occurs
                if (SPGlobal.logging()) {
                    SPGlobal.logMain(header, "Closing program after UNSUCCESSFULLY running import/patch.");
                }
                exitProgram(false, true);
            }
        }

        public void main(String[] args) {
            (new Thread(new ProcessingThread())).start();
        }
    }

    /**
     * Interface that hooks SkyProc's progress bar output to the SUM GUI's
     * progress bar display.
     */
    public static class SUMProgress implements LProgressBarInterface {

        @Override
        public void setMax(int in, String status) {
            progress.setMax(in, status);
            if (!progress.paused()) {
                statusUpdate.setText(status);
            }
        }

        @Override
        public void setStatus(String status) {
            progress.setStatus(status);
            if (!progress.paused()) {
                statusUpdate.setText(status);
            }
        }

        /**
         * @param min
         * @param max
         * @param status
         */
        @Override
        public void setStatusNumbered(int min, int max, String status) {
            progress.setStatusNumbered(min, max, status);
            if (!progress.paused()) {
                statusUpdate.setText(status);
            }
        }

        /**
         * @param status
         */
        @Override
        public void setStatusNumbered(String status) {
            progress.setStatusNumbered(status);
            if (!progress.paused()) {
                statusUpdate.setText(status);
            }
        }

        @Override
        public void incrementBar() {
            progress.incrementBar();
        }

        @Override
        public void reset() {
            progress.incrementBar();
        }

        /**
         * @return
         */
        @Override
        public int getBar() {
            return progress.getBar();
        }

        @Override
        public void setBar(int in) {
            progress.setBar(in);
        }

        /**
         * @return
         */
        @Override
        public int getMax() {
            return progress.getMax();
        }

        @Override
        public void setMax(int in) {
            progress.setMax(in);
        }

        /**
         * @param on
         */
        @Override
        public void pause(boolean on) {
            progress.pause(on);
        }

        /**
         * @return
         */
        @Override
        public boolean paused() {
            return progress.paused();
        }


        @Override
        public void done() {
            progress.done();
        }
    }

    static class SUMGUISave extends LSaveFile {

        SUMGUISave() {
            super(SPGlobal.pathToInternalFiles + "SUMsave");
        }

        @Override
        protected void initSettings() {
            Add(SUMGUISettings.PrevVersion, 0, true);
            Add(SUMGUISettings.LastMasterlist, new ArrayList<>(), false);
            Add(SUMGUISettings.LastModlist, new ArrayList<>(), false);
            Add(SUMGUISettings.CrashState, false, false);
            Add(SUMGUISettings.LOOTWarning, true, false);
        }

        @Override
        protected void initHelp() {
        }
    }
}
