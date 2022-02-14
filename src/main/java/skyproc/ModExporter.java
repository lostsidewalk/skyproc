package skyproc;

import lev.LOutFile;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author Justin Swanson
 */
class ModExporter extends LOutFile {

    private Mod exportMod;
    private Mod srcMod;
    private MajorRecord srcMajor;

    ModExporter(File path, Mod mod) throws FileNotFoundException {
        super(path);
        exportMod = mod;
    }

    public Mod getExportMod() {
        return exportMod;
    }

    public Mod getSourceMod() {
        return srcMod;
    }

    public void setSourceMod(Mod srcMod) {
        this.srcMod = srcMod;
    }

    public MajorRecord getSourceMajor() {
        return srcMajor;
    }

    public void setSourceMajor(MajorRecord src) {
        srcMajor = src;
    }
}
