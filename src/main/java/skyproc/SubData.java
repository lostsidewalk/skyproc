package skyproc;

import lev.LImport;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubData extends SubRecordTyped<byte[]> {

    byte[] data;

    SubData(String type_) {
        super(type_);
    }

    SubData(String type_, byte[] in) {
        this(type_);
        if (in != null) {
            data = new byte[in.length];
            System.arraycopy(in, 0, data, 0, in.length);
        }
    }

    SubData(String type_, int in) {
        this(type_, Ln.toByteArray(in));
    }

    void initialize(int size) {
        data = new byte[size];
    }

    public void modValue(int mod) {
        setData(Ln.toByteArray(Ln.arrayToInt(getData()) + mod, getData().length));
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        setData(in.extractAllBytes());
    }

    @Override
    boolean isValid() {
        return data != null;
    }

    void setData(int data, int size) {
        setData(Ln.toByteArray(data, size));
    }

    void setDataAbs(int data, int min, int max) {
        setData(Ln.toByteArray(Math.abs(data), min, max));
    }

    byte[] getData() {
        return data;
    }

    void setData(byte[] data_) {
        data = data_;
    }

    void setData(int data) {
        setData(data, 4);
    }

    int toInt() {
        return Ln.arrayToInt(data);
    }

    @Override
    public String print() {
        if (isValid()) {
            return Ln.printHex(data, true, false);
        } else {
            return super.toString();
        }
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        if (isValid()) {
            return data.length;
        } else {
            return 0;
        }
    }

    @Override
    void export(ModExporter out) throws IOException {
        if (data == null) {
            data = new byte[0];
        }
        super.export(out);
        out.write(data, 0);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubData(type_, data);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof SubRecordTyped)) {
            return false;
        }
        SubData b = (SubData) o;
        return (Arrays.equals(this.data, b.data));
    }

    @Override
    byte[] translate() {
        return data;
    }

    @Override
    SubRecord<byte[]> translate(byte[] in) {
        return new SubData(getType(), in);
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubData with logging capabilities.
     *
     * @param no The new SubData to be merged.
     * @param bo The base SubData, to prevent base data from being re-merged.
     * @return The modified SubData.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubData d = this;
        if (!(no == null && bo == null && (no instanceof SubData) && (bo instanceof SubData))) {
            final SubData nd = (SubData) no;
            final SubData bd = (SubData) bo;
            if (!d.equals(nd) && !nd.equals(bd)) {
                d = nd;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), nd.toString());
                }
            }
        }
        return d;
    }

}
