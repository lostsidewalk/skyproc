package skyproc;

import lev.LImport;
import lev.Ln;

import java.io.IOException;


/**
 * @author Justin Swanson
 */
public class SubStringNonNull extends SubString {

    SubStringNonNull(String t) {
        super(t);
    }

    @Override
    void parseData(LImport in, Mod srcMod) {
        in.skip(getIdentifierLength() + getSizeLength());
        string = Ln.arrayToString(in.extractInts(in.available()));
        logMod(srcMod, getType(), "Setting " + this + " to " + print());
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return string.length();
    }

    @Override
    void export(ModExporter out) throws IOException {
        out.write(getType());
        out.write(getContentLength(out.getExportMod().isFlag(Mod.Mod_Flags.STRING_TABLED)), 2);
        out.write(string);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubStringNonNull(type_);
    }
}
