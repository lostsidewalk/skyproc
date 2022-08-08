package skyproc.exceptions;

import skyproc.ModListing;

import java.util.List;

/**
 * @author Justin Swanson
 */
public class MissingMaster extends Exception {

    private final String errorMessage;

    public MissingMaster(String pluginName, List<ModListing> notInstalled, List<ModListing> inactive, List<ModListing> loadedAfter) {
        StringBuilder errorMessage = new StringBuilder("The plugin '" + pluginName + "' has missing masters:\n");
        for (ModListing f : notInstalled) {
            errorMessage.append("    -'").append(f.print()).append("' is not installed\n");
        }
        for (ModListing f : inactive) {
            errorMessage.append("    -'").append(f.print()).append("' is installed, but not activated\n");
        }
        for (ModListing f : loadedAfter) {
            errorMessage.append("    -'").append(f.print()).append("' is loaded after the mod depending upon it\n");
        }
        this.errorMessage = errorMessage.toString();
    }

    @Override
    public String getMessage() {
        return this.errorMessage;
    }
}
