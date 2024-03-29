package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubFloat extends SubRecordTyped {

    float data;

    SubFloat(String type) {
        super(type);
    }

    @Override
    SubRecord getNew(String type) {
        return new SubFloat(type);
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return 4;
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        data = in.extractFloat();
        logMod(srcMod, toString(), "Setting " + this + " to : " + print());
    }

    @Override
    public String print() {
        if (isValid()) {
            return String.valueOf(data);
        } else {
            return super.toString();
        }
    }

    @Override
    void export(ModExporter out) throws IOException {
        if (isValid()) {
            super.export(out);
            out.write(data);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof SubFloat)) {
            return false;
        }
        SubFloat s = (SubFloat) o; // Convert the object to a Person
        return (Float.compare(this.data, s.data) == 0);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Float.floatToIntBits(this.data);
        return hash;
    }

    public void set(float in) {
        data = in;
    }

    public float get() {
        return data;
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubFloats with logging capabilities.
     *
     * @param no The new SubFloat to be merged.
     * @param bo The base SubFloat, to prevent base data from being
     *           re-merged.
     * @return The modified SubFloat.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubFloat f = this;
        if (!(no == null && bo == null && (no instanceof SubFloat) && (bo instanceof SubFloat))) {
            final SubFloat nf = (SubFloat) no;
            final SubFloat bf = (SubFloat) bo;
            if (!f.equals(nf) && !nf.equals(bf)) {
                f = nf;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), nf.toString());
                }
            }
        }
        return f;
    }
}
