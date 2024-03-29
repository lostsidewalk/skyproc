package skyproc;

import lev.LImport;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubString extends SubRecordTyped<String> {

    String string;

    SubString(String type_) {
        super(type_);
    }

    SubString(String type, String in) {
        this(type);
        string = in;
    }

    static SubString getNew(String type, boolean nullterminated) {
        if (nullterminated) {
            return new SubString(type);
        } else {
            return new SubStringNonNull(type);
        }
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        string = Ln.arrayToString(in.extractInts(in.available() - 1));
        logMod(srcMod, getType(), "Setting " + this + " to " + print());
    }

    @Override
    boolean isValid() {
        return (string != null);
    }

    public void setString(String input) {
        string = input;
    }

    @Override
    public String print() {
        if (isValid()) {
            return string;
        } else {
            return "";
        }
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return string.length() + 1;
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        out.write(string);
        out.write(0, 1);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubString(type_);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SubString)) {
            return false;
        }
        final SubString other = (SubString) obj;
        return Objects.equals(this.string, other.string);
    }

    public boolean equalsIgnoreCase(SubString in) {
        return equalsIgnoreCase(in.string);
    }

    public boolean equalsIgnoreCase(String in) {
        return string.equalsIgnoreCase(in);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.string != null ? this.string.hashCode() : 0);
        return hash;
    }

    public int hashUpperCaseCode() {
        int hash = 7;
        hash = 29 * hash + (this.string != null ? this.string.toUpperCase().hashCode() : 0);
        return hash;
    }

    @Override
    String translate() {
        return string;
    }

    @Override
    SubRecord<String> translate(String in) {
        SubString out = (SubString) getNew(getType());
        out.string = in;
        return out;
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubStrings with logging capabilities.
     *
     * @param no The new SubString to be merged in.
     * @param bo The base SubString, to prevent base data from being
     *           re-merged.
     * @return The modified SubString.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubString s = this;
        if (!(no == null && bo == null && (no instanceof SubString) && (bo instanceof SubString))) {
            final SubString ns = (SubString) no;
            final SubString bs = (SubString) bo;
            if (!s.equals(ns) && !ns.equals(bs)) {
                s = ns;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), s.toString());
                }
            }
        }
        return s;
    }
}
