package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubFlag extends SubRecordTyped {
    LFlags flags;

    SubFlag(String type_, int size) {
        super(type_);
        flags = new LFlags(size);
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        out.write(flags.export(), flags.length());
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        flags.set(in.getAllBytes());
        logMod(srcMod, toString(), "Setting " + this + " to : " + print());
    }

    void set(int bit, boolean on) {
        flags.set(bit, on);
    }

    boolean is(int bit) {
        return flags.get(bit);
    }

    @Override
    public String print() {
        return flags.toString();
    }

    @Override
    SubRecord getNew(String type) {
        return new SubFlag(type, flags.length());
    }

    @Override
    public boolean isValid() {
        return flags != null;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return flags.length();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubFlag other = (SubFlag) obj;
        return Objects.equals(this.flags, other.flags);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.flags);
        return hash;
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubFlags with logging capabilities.
     *
     * @param no The new SubFlag to be merged.
     * @param bo The base SubFlag, to prevent base data from being
     *           re-merged.
     * @return The modified SubFlag.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubFlag f = this;
        if (!(no == null && bo == null && (no instanceof SubFlag) && (bo instanceof SubFlag))) {
            final SubFlag nf = (SubFlag) no;
            final SubFlag bf = (SubFlag) bo;
            f.flags = Merger.merge(f.flags, nf.flags, bf.flags, getType());
        }
        return f;
    }
}
