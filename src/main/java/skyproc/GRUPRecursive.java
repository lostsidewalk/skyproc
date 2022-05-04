package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class GRUPRecursive<T extends MajorRecord> extends GRUP<T> {

    GRUPRecursive(T prototype) {
        super(prototype);
    }

    @Override
    public MajorRecord extractMajor(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        MajorRecord m = super.extractMajor(in, srcMod);
        if (m != null && !in.isDone() && "GRUP".equals(getNextType(in))) {
            if (SPGlobal.logging()) {
                // SPGlobal.log("GRUPRecursive", "Extracting an appended GRUP.");
            }
            GRUP g = m.getGRUPAppend();
            g.parseData(g.extractRecordData(in), srcMod);
        }
        return m;
    }
}