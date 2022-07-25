package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubShell extends SubRecord<SubShell> {

    SubRecordsDerived subRecords;

    SubShell(SubPrototype proto) {
        super();
        subRecords = new SubRecordsDerived(proto);
    }

    @Override
    boolean isValid() {
        return subRecords.isValid();
    }

    @Override
    int getHeaderLength() {
        return 0;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return subRecords.length(isStringTabled);
    }

    @Override
    void export(ModExporter out) throws IOException {
        subRecords.export(out);
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        return subRecords.allFormIDs();
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        subRecords.importSubRecords(in, srcMod);
    }

    SubPrototype getPrototype() {
        return subRecords.prototype;
    }

    @Override
    ArrayList<String> getTypes() {
        return subRecords.getTypes();
    }

    @Override
    SubRecord getNew(String type) {
        return new SubShell(subRecords.prototype);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.subRecords);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubShell other = (SubShell) obj;
        return this.subRecords.equals(other.subRecords);
    }
}
