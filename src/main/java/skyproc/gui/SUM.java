package skyproc.gui;

import lev.gui.LSaveFile;
import skyproc.GRUP_TYPE;
import skyproc.Mod;
import skyproc.ModListing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * Interface that allows your SkyProc program to be automatically detected and loaded into SkyProc Unified Manager.
 *
 * @author Justin Swanson
 */
public interface SUM {
    /**
     * @return Name of the project/mod.
     */
    String getName();

    /**
     * One of the major "danger zones" of SkyProc is the potential for compounding record duplications.<br><br>
     * <p>
     * Please export an array of any record types that you duplicate and create an unbounded large number of.  This will be
     * used to quickly identify potential issues cross-SkyProc patchers.
     * <p>
     * An example of the danger:<br>
     * A mod might duplicate each weapon in a load order for each enchantment, making (# of weapons X # of enchantments) new weapon records.<br>
     * This can add up to be a huge number of new records for the single patcher alone.  If you combine these new records with another SkyProc patcher
     * that duplicates weapons exponentially as well, then an absurdly large number of records could be created.
     *
     * @return Array of the types that this patcher will be creating a large amount of new records of.
     */
    GRUP_TYPE[] dangerousRecordReport();

    /**
     * @return An array of the types of records that this patcher would like to import and have access to.
     */
    GRUP_TYPE[] importRequests();

    /**
     * @return Whether the standard GUI should start importing the mods as soon as it opens.
     */
    boolean importAtStart();

    /**
     * @return True if your patcher has a SPMainMenuPanel for a SUMGUI
     */
    boolean hasStandardMenu();

    /**
     * Return a SPMainMenuPanel that has been customized with your patcher's contents.<br>
     * Customizing constitutes first creating your own SPSettingPanels extending classes in your project.<br>
     * In this function, you then SPMainMenuPanel.addMenu() on each of your SPSettingPanels you created.
     *
     * @return A SPMainMenuPanel customized with your patcher's contents.
     * @see SPMainMenuPanel
     */
    SPMainMenuPanel getStandardMenu();

    /**
     * True if you have a custom made GUI.<br>
     * NOTE: The SPDefaultGUI counts as a custom GUI.
     *
     * @return True if you have a custom made GUI.
     */
    boolean hasCustomMenu();

    /**
     * Opens and displays the custom menu of your patcher and returns it.<br>
     * If you do not have a custom menu, simply put this line (after making sure hasCustomMenu() returns false):<br>
     * <i>throw new UnsupportedOperationException("Not supported yet.");</i>
     *
     * @return
     */
    JFrame openCustomMenu();

    /**
     * @return True if you have a logo for your patcher.
     */
    boolean hasLogo();

    /**
     * Returns a URL to the patcher's logo image.  <br><br>
     * An example line from Automatic Variants is:<br>
     * <i>return new ClassPathResource("AutoVarGUITitle.png").getURL();</i><br>
     * SettingsOther is simply a class that is in the same package as the png.
     * It can be any class.<br><br>
     * If you do not have a logo, simply put this line (after making sure hasLogo() returns false):<br>
     * <i>throw new UnsupportedOperationException("Not supported yet.");</i>
     *
     * @return URL to your patcher's logo.
     */
    URL getLogo();

    /**
     * True if you have a savefile.<br>
     * You can create your own savefile and specify the settings in it by extending
     * the LSaveFile class.
     *
     * @return
     * @see LSaveFile
     */
    boolean hasSave();

    /**
     * @return The singleton instance of your custom LSaveFile.
     */
    LSaveFile getSave();

    /**
     * @return The string representation of the current version of your patcher.
     */
    String getVersion();

    /**
     * @return The modlisting used for your export patch.
     */
    ModListing getListing();

    /**
     * Create a new mod that will be your export patch and return it.  This includes
     * customizing the header flags as desired.
     *
     * @return
     */
    Mod getExportPatch();

    /**
     * @return The preferred header color for your mod.
     */
    Color getHeaderColor();

    /**
     * Custom code to determine if a patch is needed (in addition to the normal SUM patch needed rules).
     *
     * @return Whether or not your program requires a patch.
     */
    boolean needsPatching();

    /**
     * @return A description of your patcher for display in SUM.
     */
    String description();

    /**
     * @return A list of ModListings of mods required to be present in order to
     * patch.  Program will stop and display error if any are missing.
     */
    ArrayList<ModListing> requiredMods();

    /**
     * Code to run before GUI displays.  This code runs AFTER your save is loaded.
     */
    void onStart();

    /**
     * Code to run before program closes.
     *
     * @param patchWasGenerated True if a patch was generated before calling this function.
     */
    void onExit(boolean patchWasGenerated);

    /**
     * This function should start the processing code of your patcher.<br>
     * Assume:<br>
     * 1. Mods have already been imported. Do not reimport the load order here.<br>
     * 2. ALWAYS reference SPGlobal.getGlobalPatch() when referring to the export patch.  Otherwise
     * SUM will not play nice with it.  In earlier portions of your code (in main() perhaps) you should set the global
     * patch to be your export patch.<br>
     * 3. Do not export the patch in this function.  Just process and add the desired records to the global patch.<br><br>
     * <p>
     * To run your program you then do the following in your main() function:<br>
     * 1. Import desired mods.<br>
     * 2. Call this function.<br>
     * 3. Export the patch.<br><br>
     * Everything that you want done aside from importing/exporting needs to be inside this function or SUM won't do it.
     *
     * @throws Exception
     */
    void runChangesToPatch() throws Exception;
}
