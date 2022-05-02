package skyproc;

import lev.gui.LSaveFile;

/**
 * @author Justin Swanson
 */
public abstract class SkyProcSave extends LSaveFile {

    public SkyProcSave() {
        super(SPGlobal.pathToInternalFiles + "/Savefile");
    }
}
