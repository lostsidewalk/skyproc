package skyproc.gui;

import lev.Ln;
import lev.gui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import skyproc.*;
import skyproc.SPGlobal.Language;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * SUM - SkyProc Unified Manager<br> This is the main program that hooks
 * together various SkyProc patchers and streamlines their patching processing.
 *
 * @author Justin Swanson
 */
@SpringBootApplication
@ComponentScan(basePackages = {"skyproc"})
public class SUMprogram implements SUM {

    final String version = "1.3.3";
    final ArrayList<String> exclude = new ArrayList<>(2);
    final ArrayList<PatcherLink> links = new ArrayList<>();
    final ArrayList<File> blockedLinks = new ArrayList<>();
    // GUI
    SPMainMenuPanel mmenu;
    HookMenu hookMenu;
    OptionsMenu optionsMenu;
    LScrollPane hookMenuScroll;
    final LSaveFile SUMsave = new SUMsave();
    final Color teal = new Color(75, 164, 134);
    final Color green = new Color(54, 154, 31);
    final Color grey = new Color(230, 230, 230);
    final Font settingFont = SUMGUI.SUMmainFont.deriveFont((float) 15);
    final int scrollOffset = 100;
    int patcherNumber = 0;
    BufferedImage collapsedSetting;
    BufferedImage openSetting;
    LCheckBox forceAllPatches;

    /**
     * Main function that starts the program and GUI.
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(SUMprogram.class);
        builder.headless(false);
        @SuppressWarnings("unused") ConfigurableApplicationContext context = builder.run(args);
    }

    @Autowired
    ApplicationArguments applicationArguments;

    @Autowired
    SPGlobal spGlobal;

    @PostConstruct
    void postConstruct() {
        try {
            if (handleArgs(spGlobal, applicationArguments.getSourceArgs())) {
                SUMprogram sum = new SUMprogram();
                sum.runProgram(spGlobal);
            }
        } catch (Exception e) {
            // If a major error happens, print it everywhere and display a message box.
            System.err.println(e.getMessage());
            SPGlobal.logException(e);
            JOptionPane.showMessageDialog(null, "There was an exception thrown during program execution: '" + e + "'  Check the debug logs.");
        }
    }

    static boolean handleArgs(SPGlobal spGlobal, String[] args) {
        ArrayList<String> argsList = new ArrayList<>();
        for (String s : args) {
            argsList.add(s.toUpperCase());
        }
        if (argsList.contains("-EMBEDDEDSCRIPTGEN")) {
            parseEmbeddedScripts();
            return false;
        }
        if (argsList.contains("-GENPATCH")) {
            SUMGUI.open(spGlobal, new SUMMergerProgram(), args);
            return false;
        }
        return true;
    }

    private static void parseEmbeddedScripts() {
        try {
            EmbeddedScripts.generateEnums();
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
    }

    /**
     * @return Path to the text document containing the most recent list of
     * executed SkyProc patchers.
     * @throws IOException when something horrible happens
     */
    public static String getSUMPatchList() throws IOException {
        return SPGlobal.getSkyProcDocuments() + File.separator + "SUM patch list.txt";
    }

    void runProgram(SPGlobal spGlobal) {
        compileExcludes();

        openDebug();
        SUMsave.init();
        getHooks();
        openGUI(spGlobal);
        initLinkGUIs();
    }

    void compileExcludes() {
        exclude.add("SKYPROC UNIFIED MANAGER.JAR");
        exclude.add("SKYPROC.JAR");

        try {
            BufferedReader in = new BufferedReader(new FileReader("Files/Block Jars.txt"));
            String read;
            while (in.ready()) {
                read = in.readLine();
                if (read.contains("//")) {
                    read = read.substring(0, read.indexOf("//"));
                }
                read = read.trim().toUpperCase();
                if (!read.equals("")) {
                    exclude.add(read.toUpperCase());
                }
            }
        } catch (IOException ex) {
            SPGlobal.logError("Block Jars", "Failed to locate or read 'Block Jars.txt'");
        }
    }

    void openDebug() {
        SPGlobal.debugModMerge = false;
        SPGlobal.debugBSAimport = false;
        SPGlobal.debugNIFimport = false;
    }

    void openGUI(SPGlobal spGlobal) {
        mmenu = new SPMainMenuPanel();
        mmenu.addLogo(this.getLogo());
        mmenu.setVersion(getVersion(), new Point(13, 15));

        hookMenu = new HookMenu(mmenu);
        mmenu.addMenu(hookMenu, green, false, SUMsave, null);
        mmenu.setWelcomePanel(hookMenu);

        optionsMenu = new OptionsMenu(mmenu);
        mmenu.addMenu(optionsMenu, green, false, SUMsave, null);

        try {
            collapsedSetting = ImageIO.read(new ClassPathResource("Open Settings Collapsed.png").getURL());
            openSetting = ImageIO.read(new ClassPathResource("Open Settings.png").getURL());
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }

        SUMGUI.open(spGlobal, this, new String[0]);
        SwingUtilities.invokeLater(() -> {
            SUMGUI.patchNeededLabel.setText("");
            SUMGUI.patchNeededLabel.setLocation(-1000, -1000);
            SUMGUI.forcePatch.setLocation(-1000, -1000);
            forceAllPatches = new LCheckBox("Force All Patches", SUMGUI.SUMSmallFont, Color.GRAY);
            forceAllPatches.setLocation(SUMGUI.rightDimensions.x + 10, SUMGUI.cancelPatch.getY() + SUMGUI.cancelPatch.getHeight() / 2 - forceAllPatches.getHeight() / 2);
            forceAllPatches.setOffset(-4);
            forceAllPatches.addMouseListener(new MouseListener() {
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
                    SUMGUI.helpPanel.setContent("This will force each patcher to create a patch, even if "
                            + "it doesn't think it needs to.  Use this if you want to forcibly "
                            + "remake all patches for any reason.");
                    SUMGUI.helpPanel.setTitle("Force All Patches");
                    SUMGUI.helpPanel.hideArrow();
                    SUMGUI.helpPanel.setDefaultPos();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            SUMGUI.singleton.add(forceAllPatches);
        });
    }

    void initLinkGUIs() {
        ArrayList<PatcherLink> linkTmp = new ArrayList<>(links);
        PatcherLink last = null;
        int height = 0;
        for (PatcherLink link : linkTmp) {
            try {
                link.setup();
                if (last != null) {
                    link.setLocation(link.getX(), last.getY() + last.getHeight() + 15);
                }
                last = link;
                hookMenu.hookMenu.add(link);
                height = link.getY() + link.getHeight();
                hookMenu.revalidate();
            } catch (Exception ex) {
                SPGlobal.logException(ex);
                links.remove(link);
            }
        }
        if (blockedLinks.size() > 0) {
            LTextArea blockedLinksArea = new LTextArea(grey);
            blockedLinksArea.setSize(SUMGUI.middleDimensions.width - 50, 200);
            blockedLinksArea.setLocation(25, height + 20);
            height = blockedLinksArea.getY() + blockedLinksArea.getHeight();
            StringBuilder text = new StringBuilder("Blocked Patchers:\n");
            for (File jar : blockedLinks) {
                text.append("            ").append(jar.getName()).append("\n");
            }
            blockedLinksArea.setText(text.toString());
            hookMenu.hookMenu.add(blockedLinksArea);
        }
        hookMenu.hookMenu.setPreferredSize(new Dimension(SUMGUI.middleDimensions.width, height + 25));
    }

    void getHooks() {
        ArrayList<File> jars = findJars(new File(SPGlobal.pathToPatchers));
        // Locate classes that implement SUM
        for (File jar : jars) {
            try {
                SPGlobal.logSpecial(SUMlogs.JarHook, "Jar Load", "Loading jar " + jar);
                ArrayList<Class<?>> classes = Ln.loadClasses(jar, true);
                // FIXME: use class.getInterfaces to check for SUM
                for (Class<?> c : classes) {
                    //Skip skyproc or lev classes
                    if (c.toString().contains("lev.") || (c.toString().contains("skyproc."))) {
                        continue;
                    }
                    try {
                        Object tester = c.newInstance();
                        if (tester instanceof SUM) {
                            SPGlobal.logSpecial(SUMlogs.JarHook, "Jar Load", "   Added jar " + jar);
                            PatcherLink newLink = new PatcherLink((SUM) c.newInstance(), jar);
                            if (!links.contains(newLink)) {
                                links.add(newLink);
                            }
                            break;
                        }
                    } catch (Throwable ex) { // intentionally catch anything from trying to create classes
                        SPGlobal.logSpecial(SUMlogs.JarHook, "Loading class", "   Skipped " + c + ": " + ex.getMessage());
                    }
                }
            } catch (Throwable ex) { // intentionally catch anything from trying to load jars
                SPGlobal.logSpecial(SUMlogs.JarHook, "Loading jar", "   Skipped jar " + jar + ": " + ex.getMessage());
            }
        }

        // Test links to make sure they're up to date SUM
        ArrayList<PatcherLink> tmpLinks = new ArrayList<>(links);
        for (PatcherLink link : tmpLinks) {
            try {
                // Newest added function.  If it fails, it's an old version of SUM.
                link.hook.description();
            } catch (java.lang.AbstractMethodError ex) {
                links.remove(link);
                blockedLinks.add(link.path);
                SPGlobal.logSpecial(SUMlogs.JarHook, "Checking Links", "Removing link " + link.getName() + " because it was an older version of SUM.");
            } catch (UnsupportedOperationException ex) {
                links.remove(link);
                blockedLinks.add(link.path);
                SPGlobal.logSpecial(SUMlogs.JarHook, "Checking Links", "Removing link " + link.getName() + " because it didn't fully implement SUM (Threw a UnsupportedOperationException).");
            }
        }

        sortLinks(links);
    }

    ArrayList<File> findJars(File dir) {
        SPGlobal.logSpecial(SUMlogs.JarHook, "Jar Load", "Path to patchers: " + dir.getAbsolutePath());
        ArrayList<File> files = Ln.generateFileList(dir, false);
        ArrayList<File> out = new ArrayList<>();
        for (File f : files) {
            if (f.getName().toUpperCase().endsWith(".JAR")
                    && !exclude.contains(f.getName().toUpperCase())) {
                out.add(f);
            }
        }
        return out;
    }

    /**
     * @return the modlisting used for the exported patch.
     */
    @Override
    public ModListing getListing() {
        return new ModListing("SUM", false);
    }

    /**
     * @return True if any hook needs patching.
     */
    @Override
    public boolean needsPatching() {
        return true;
    }

    @Override
    public ArrayList<ModListing> requiredMods() {
        return new ArrayList<>(0);
    }

    @Override
    public String description() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "SkyProc Unified Manager";
    }

    @Override
    public GRUP_TYPE[] dangerousRecordReport() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GRUP_TYPE[] importRequests() {
        return new GRUP_TYPE[0];
    }

    @Override
    public boolean importAtStart() {
        return false;
    }

    @Override
    public boolean hasStandardMenu() {
        return true;
    }

    // SUM methods

    @Override
    public SPMainMenuPanel getStandardMenu() {
        return mmenu;
    }

    @Override
    public boolean hasCustomMenu() {
        return false;
    }

    @Override
    public JFrame openCustomMenu() {
        return null;
    }

    @Override
    public boolean hasLogo() {
        return true;
    }

    @Override
    public URL getLogo() {
        URL url = null;
        try {
            url = new ClassPathResource("SUM program.png").getURL();
        } catch (IOException e) {
            // TODO: log something here
        }

        return url;
    }

    @Override
    public boolean hasSave() {
        return true;
    }

    @Override
    public LSaveFile getSave() {
        return SUMsave;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Mod getExportPatch() {
        Mod patch = new Mod(getListing());
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.setAuthor("Leviathan1753 and friends");
        return patch;
    }

    @Override
    public Color getHeaderColor() {
        return teal;
    }

    /**
     * Runs all hooks onExit() function
     *
     * @param patchWasGenerated
     */
    @Override
    public void onExit(boolean patchWasGenerated) {
        // Save disabled links
        ArrayList<String> disabledLinks = new ArrayList<>();
        for (PatcherLink link : links) {
            if (!link.isActive()) {
                disabledLinks.add(link.getName().toUpperCase());
            }
        }
        SUMsave.setStrings(SUMSettings.DISABLED, disabledLinks);

        // Delete unused blocklist file
        File blocklist = new File("Files\\Blocklist.txt");
        if (blocklist.isFile()) {
            blocklist.delete();
        }
    }

    /**
     * Runs all hooks onStart() function
     */
    @Override
    public void onStart() {
    }

    /**
     * @throws Exception
     */
    @Override
    public void runChangesToPatch() throws Exception {

        if (SUMsave.getBool(SUMSettings.MERGE_PATCH) && !SUMsave.getBool(SUMSettings.RUN_LOOT)) {
            throw new Exception("\n\nMerging requires running LOOT");
        }
        // Setup
        ArrayList<PatcherLink> activeLinks = getActiveLinks();
        setupProgress(activeLinks);
        checkMemAllocation();

        runLOOT();
        sortLinks(activeLinks);

        runEachPatcher(activeLinks);

        mergePatches(activeLinks);

        SUMGUI.progress.done();
        SUMGUI.exitProgram(true, true);
    }

    void checkMemAllocation() {
        File f = new File("Files/MemTester.jar");
        if (f.exists()) {
            ArrayList<String> args = new ArrayList<>();
            args.add("java");
            args.add("-jar");
            args.add("-Xms200m");
            args.add("-Xmx" + SUMsave.getInt(SUMSettings.MAX_MEM) + "m");
            args.add(f.getPath());
            boolean pass = NiftyFunc.startProcess(null, args.toArray(new String[0]));
            if (!pass) {
                JOptionPane.showMessageDialog(null, "Could not allocate " + SUMsave.getInt(SUMSettings.MAX_MEM)
                        + " megabytes from the OS.  Lower the amount and try again.");
                SPGlobal.logMain("Run Changes", "Mem allocation test failed.");
                SUMGUI.exitProgram(false, true);
            }
        }
    }

    void setupProgress(ArrayList<PatcherLink> activeLinks) {
        SUMGUI.progress.setBar(0);
        int progressMax = activeLinks.size();
        if (SUMsave.getBool(SUMSettings.RUN_LOOT)) {
            progressMax++;
        }
        if (SUMsave.getBool(SUMSettings.MERGE_PATCH)) {
            progressMax++;
        }
        SUMGUI.progress.setMax(progressMax);
    }

    ArrayList<PatcherLink> getActiveLinks() {
        ArrayList<PatcherLink> activeLinks = new ArrayList<>();
        for (PatcherLink l : links) {
            if (l.isActive()) {
                activeLinks.add(l);
            }
        }
        return activeLinks;
    }

    void runLOOT() {
        if (SUMsave.getBool(SUMSettings.RUN_LOOT)) {
            SUMGUI.lootWarning();
            // confirmOnError = true => confirmation dialog when LOOT cannot be found, etc.; otherwise quietly carry on
            NiftyFunc.runLOOT(System.getenv("LOOT_PATH"), false);
            SUMGUI.progress.incrementBar();
        }
    }

    void sortLinks(ArrayList<PatcherLink> links) {
        try {
            ArrayList<PatcherLink> sorted = new ArrayList<>(links.size());
            ArrayList<PatcherLink> unsorted = new ArrayList<>(links);
            // Use loadorder.txt if it exists first, then plugins.txt
            String listPath;
            try {
                listPath = SPGlobal.getLoadOrderTxt();
            } catch (IOException ex) {
                listPath = SPGlobal.getPluginsTxt();
            }
            ArrayList<String> pluginList = Ln.loadFileToStrings(listPath, false);
            for (String plugin : pluginList) {
                for (PatcherLink link : unsorted) {
                    if (link.getPatchName().equalsIgnoreCase(plugin.trim()) && !sorted.contains(link)) {
                        sorted.add(link);
                        unsorted.remove(link);
                        break;
                    }
                }
            }
            links.clear();
            links.addAll(sorted);
            links.addAll(unsorted);
        } catch (IOException ex) {
            SPGlobal.logException(ex);
        }
    }

    void runEachPatcher(ArrayList<PatcherLink> activeLinks) {
        SUMGUI.progress.setStatus("Running Patchers");
        for (PatcherLink link : activeLinks) {
            SUMGUI.progress.setStatusNumbered("Running " + link.getName());
            SPGlobal.logMain("Run Changes", "Running jar: " + link.path);
            if (!link.isActive()) {
                SPGlobal.logMain("Run Changes", "Skipped jar because it was not selected: " + link.path);
            } else if (runJarPatcher(link)) {
                SPGlobal.logMain("Run Changes", "Successfully ran jar: " + link.path);
            } else {
                int response = JOptionPane.showConfirmDialog(null, "Failed to properly run " + link.getName() + ".  Continue patching?", "Error", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.NO_OPTION) {
                    SUMGUI.exitProgram(false, true);
                }
                SPGlobal.logMain("Run Changes", "UNsuccessfully ran jar: " + link.path);
            }
        }
    }

    boolean runJarPatcher(PatcherLink link) {
        ArrayList<String> args = new ArrayList<>();
        args.add("java");
        args.add("-jar");
        args.add("-Xms200m");
        args.add("-Xmx" + SUMsave.getInt(SUMSettings.MAX_MEM) + "m");
        args.add(link.path.getPath());
        args.add("-GENPATCH");
        args.add("-NONEW");
        args.add("-NOMODSAFTER");
        args.add("-LANGUAGE");
        args.add("" + Language.values()[SUMsave.getInt(SUMSettings.LANGUAGE)]);
        if (forceAllPatches.isSelected() || SUMsave.getBool(SUMSettings.MERGE_PATCH)) {
            args.add("-FORCE");
        }
        args.add("-PROGRESSLOCATION");
        args.add("" + (SUMGUI.progress.getX() + SUMGUI.progress.getWidth() + 10));
        args.add("" + SUMGUI.progress.getY());
        args.add("-SUMBLOCK");
        if (SUMsave.getBool(SUMSettings.ALL_AS_MASTERS)) {
            args.add("-ALLMODSASMASTERS");
        }
        boolean ret = NiftyFunc.startProcess(new File(link.path.getParentFile().getPath()), args.toArray(new String[0]));
        SUMGUI.progress.incrementBar();
        return ret;
    }

    void mergePatches(ArrayList<PatcherLink> links) throws IOException {
        if (SUMsave.getBool(SUMSettings.MERGE_PATCH)) {
            SUMGUI.progress.setStatusNumbered("Merging Patches");
            // Save out list of patch names
            String path = getSUMPatchList();
            try (BufferedWriter out = new BufferedWriter(new FileWriter(path))) {
                for (PatcherLink link : links) {
                    out.write(link.getPatchName() + "\n");
                }
            }

            File SUM = new File("Files" + File.separator + "SUM.jar").getAbsoluteFile();
            PatcherLink mergerLink = new PatcherLink(null, SUM);
            // SUM triggers the merger program off the -GENPATCH arg
            runJarPatcher(mergerLink);
            SUMGUI.progress.incrementBar();
        }
    }

    enum SUMSettings {
        MAX_MEM,
        MERGE_PATCH,
        DISABLED,
        RUN_LOOT,
        ALL_AS_MASTERS,
        LANGUAGE
    }

    enum SUMlogs {
        JarHook
    }

    static class SUMsave extends SkyProcSave {

        @Override
        protected void initSettings() {
            Add(SUMSettings.MERGE_PATCH, false, true);
            Add(SUMSettings.DISABLED, new ArrayList<>(0), true);
            Add(SUMSettings.RUN_LOOT, false, false);
            Add(SUMSettings.ALL_AS_MASTERS, false, false);
            Add(SUMSettings.MAX_MEM, 750, false);
            Add(SUMSettings.LANGUAGE, 0, true);
        }

        @Override
        protected void initHelp() {
            helpInfo.put(SUMSettings.MERGE_PATCH, "This will merge all of your SkyProc patches into one patch.  This helps if you're hitting the max number of mods.\n" +
                    "Mergeing REQUIRES LOOT to be run by SUM since it adds and removes plugins from your load order.\n" +
                    "WARNING:  This is an experimental setting.  In addition, existing savegames may break when switching this setting on/off, as all the references the savegame uses to the patches will be broken.  It is recommended you start new savegames when changing this setting.");
            helpInfo.put(SUMSettings.RUN_LOOT, "SUM will run LOOT before running the patchers to confirm that they are all in the correct load order.  It is highly recommended you leave this setting on.\n" +
                    "NOTE:  Be aware that LOOT reserves the right to change load ordering as it sees fit.  If it adjusts its load order and shuffles SkyProc patchers around to be in a different order, your savegame may or may not function with the new ordering.  This is most likely to occur if the SkyProc patcher is brand new, and hasn't been processed yet by LOOT.\n");
            helpInfo.put(SUMSettings.ALL_AS_MASTERS, "This will attempt to use all active plugins as the masters for generated patches.\n" +
                    "This can significantly reduce patching time on large load orders.\n" +
                    "This is still an experimental setting.");
            helpInfo.put(SUMSettings.MAX_MEM,
                    "This will determine the max amount of megabytes of memory patchers will be allowed to use.\n" +
                    "If a patcher runs out of memory the program will essentially halt as it tries to scrap by with too little memory. If you experience this, then try allocating more memory.\n" +
                    "Windows has the final say in how much memory it will allow the programs.  If your request is denied you'll see an error.  Just lower your memory request and try again.");
            helpInfo.put(SUMSettings.LANGUAGE,
                    "You can set your preferred language here.  This will make the patchers import strings files of that language first.");
        }
    }

    // Internal Classes
    class HookMenu extends SPSettingPanel {

        JPanel hookMenu;

        HookMenu(SPMainMenuPanel parent_) {
            super(parent_, "Patcher List", grey);
            initialize();
        }

        @Override
        protected final void initialize() {
            super.initialize();

            hookMenu = new JPanel();
            hookMenu.setOpaque(false);
            hookMenu.setLayout(null);

            hookMenuScroll = new LScrollPane(hookMenu);
            hookMenuScroll.setSize(SUMGUI.middleDimensions.width,
                    SUMGUI.middleDimensions.height - scrollOffset);
            hookMenuScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            hookMenuScroll.setLocation(0, scrollOffset);
            hookMenuScroll.getVerticalScrollBar().setUnitIncrement(20);
            Add(hookMenuScroll);
        }
    }

    class OptionsMenu extends SPSettingPanel {

        LCheckBox runLoot;
        LCheckBox AllModsAsMasters;
        LCheckBox mergePatches;
        LNumericSetting maxMem;
        LLabel langLabel;
        LComboBox language;

        OptionsMenu(SPMainMenuPanel parent_) {
            super(parent_, "SUM Options", grey);
        }

        @Override
        protected final void initialize() {
            super.initialize();

            mergePatches = new LCheckBox("Merge Patches", settingFont, SUMGUI.light);
            mergePatches.setOffset(-3);
            mergePatches.addShadow();
            mergePatches.tie(SUMSettings.MERGE_PATCH, SUMsave, SUMGUI.helpPanel, true);
            setPlacement(mergePatches);
            AddSetting(mergePatches);

            runLoot = new LCheckBox("Run LOOT", settingFont, SUMGUI.light);
            runLoot.setOffset(-3);
            runLoot.addShadow();
            runLoot.tie(SUMSettings.RUN_LOOT, SUMsave, SUMGUI.helpPanel, true);
            setPlacement(runLoot);
            AddSetting(runLoot);

            AllModsAsMasters = new LCheckBox("All Mods As Masters For Patches", settingFont, SUMGUI.light);
            AllModsAsMasters.setOffset(-3);
            AllModsAsMasters.addShadow();
            AllModsAsMasters.tie(SUMSettings.ALL_AS_MASTERS, SUMsave, SUMGUI.helpPanel, true);
            setPlacement(AllModsAsMasters);
            AddSetting(AllModsAsMasters);

            maxMem = new LNumericSetting("Max Allocated Memory",
                    settingFont, SUMGUI.light, 250, 2000, 250);
            maxMem.tie(SUMSettings.MAX_MEM, SUMsave, SUMGUI.helpPanel, true);
            setPlacement(maxMem);
            AddSetting(maxMem);

            langLabel = new LLabel("Language", settingFont, SUMGUI.light);
            language = new LComboBox("Language");
            language.setSize(150, 25);
            for (Enum e : SPGlobal.Language.values()) {
                language.addItem(e);
            }
            language.tie(SUMSettings.LANGUAGE, SUMsave, SUMGUI.helpPanel, true);
            setPlacement(language, last.x + langLabel.getWidth() + 15, last.y);
            AddSetting(language);
            langLabel.setLocation(language.getX() - langLabel.getWidth() - 15, language.getY() + language.getHeight() / 2 - langLabel.getHeight() / 2);
            langLabel.addShadow();
            settingsPanel.add(langLabel);

            alignRight();

        }
    }

    class PatcherLink extends LComponent {

        LImagePane logo;
        LLabel title;
        JCheckBox cbox;
        final SUM hook;
        final File path;
        JPanel menu;
        LImagePane setting;

        PatcherLink(final SUM hook, final File path) {
            super();
            this.hook = hook;
            this.path = path;
        }

        final void setup() {
            setupGUI();
        }

        @SuppressWarnings("null")
        final void setupGUI() {

            Component using = null;

            cbox = new JCheckBox();
            cbox.setSize(cbox.getPreferredSize());
            cbox.setOpaque(false);
            cbox.setVisible(true);
            cbox.setSelected(!SUMsave.getStrings(SUMSettings.DISABLED).contains(getName().toUpperCase()));

            int desiredMargin = 15;
            int desiredWidth = SUMGUI.middleDimensions.width - desiredMargin * 2;
            int width = cbox.getWidth() + 10;

            try {
                URL logoImage = null;
                try {
                    logoImage = hook.getLogo();
                } catch (Exception ignored) {
                }
                logoImage = logoImage == null ? getDefaultLogoImage() : logoImage;
                logo = new LImagePane(logoImage);
                if (logo.getWidth() + width > desiredWidth) {
                    logo.setMaxSize(desiredWidth - width, 0);
                }
                using = logo;
                add(logo);
            } catch (IOException ex) {
                SPGlobal.logException(ex);
                logo = null;
            }
            if (logo == null) {
                title = new LLabel(hook.getName(), SUMGUI.SUMmainFont.deriveFont((float) 25), hook.getHeaderColor());
                title.addShadow();
                using = title;
                add(title);
            }

            width += using.getWidth();
            using.addMouseListener(new LinkClick());
            cbox.setLocation(desiredMargin, using.getHeight() / 2 - cbox.getHeight() / 2);
            add(cbox);
            using.setLocation(cbox.getX() + cbox.getWidth() + 10, 0);

            // Add settings
            setting = new LImagePane(collapsedSetting);
            setting.setLocation(SUMGUI.middleDimensions.width - 10 - setting.getWidth(), using.getHeight() / 2 - setting.getHeight() / 2);
            setting.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ArrayList<String> args = new ArrayList<>();
                    args.add("java");
                    args.add("-jar");
                    args.add("-Xms500m");
                    args.add("-Xmx1000m");
                    args.add(path.getPath());
                    args.add("-NONEW");
                    args.add("-JUSTSETTINGS");
                    NiftyFunc.startProcess(new File(path.getParentFile().getPath() + "\\"), args.toArray(new String[0]));
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setting.setImage(openSetting);
                    setting.setLocation(SUMGUI.middleDimensions.width - 10 - setting.getWidth(), setting.getY());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setting.setImage(collapsedSetting);
                    setting.setLocation(SUMGUI.middleDimensions.width - 10 - setting.getWidth(), setting.getY());
                }
            });
            add(setting, 0);
            if (setting.getY() < 0) {
                setting.setLocation(setting.getX(), 0);
                using.setLocation(using.getX(), setting.getHeight() / 2 - using.getHeight() / 2);
                using = setting;
            }

            cbox.setLocation(desiredMargin, using.getHeight() / 2 - cbox.getHeight() / 2);
            setSize(SUMGUI.middleDimensions.width, using.getHeight());

            // Tie to help
            MouseListener updateHelp = new MouseListener() {
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
                    SUMGUI.helpPanel.setContent(hook.description());
                    SUMGUI.helpPanel.setTitle(hook.getName());
                    SUMGUI.helpPanel.hideArrow();
                    SUMGUI.helpPanel.setDefaultPos();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            };
            addMouseListener(updateHelp);
            using.addMouseListener(updateHelp);
        }

        boolean isActive() {
            return cbox.isSelected();
        }

        @Override
        public String getName() {
            return hook.getName();
        }

        public String getPatchName() {
            return hook.getListing().toString();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.hook);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PatcherLink other = (PatcherLink) obj;
            return hook.getName().equalsIgnoreCase(other.hook.getName());
        }

        class LinkClick implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                cbox.setSelected(!cbox.isSelected());
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }
        }
    }

    private URL getDefaultLogoImage() {
        URL url = null;
        try {
            url = new ClassPathResource("no_logo.png").getURL();
        } catch (IOException e) {
            // TODO: log this
        }

        return url;
    }
}
