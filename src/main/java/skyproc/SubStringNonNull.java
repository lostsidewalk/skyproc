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
        if (SPGlobal.logMods) {
            logMod(srcMod, getType().toString(), "Setting " + this + " to " + print());
        }
    }

    @Override
    int getContentLength(ModExporter out) {
        return string.length();
    }

    @Override
    void export(ModExporter out) throws IOException {
        out.write(getType().toString());
        out.write(getContentLength(out), 2);
        out.write(string);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubStringNonNull(type_);
    }
}
