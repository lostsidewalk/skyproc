package skyproc;

import lev.LImport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Justin Swanson
 */
class SubShellBulkType extends SubShell {

    boolean includeFirst;

    SubShellBulkType(SubPrototype proto, boolean includeFirst) {
        super(proto);
        this.includeFirst = includeFirst;
    }

    /**
     * @param in
     * @return
     */
    @Override
    public int getRecordLength(LImport in) {
        String first = getType();
        int size = super.getRecordLength(in);
        in.skip(size);
        String nextType;
        Set<String> targets = new HashSet<>(subRecords.getTypes());
        while (!in.isDone()) {
            nextType = getNextType(in);
            if (!targets.contains(nextType) || (!includeFirst && nextType.equals(first))) {
                break;
            }
            int newSize = super.getRecordLength(in);
            size += newSize;
            in.skip(newSize);
        }
        in.pos(in.pos() - size);
        return size;
    }

    @Override
    ArrayList<String> getTypes() {
        ArrayList<String> out = new ArrayList<>(1);
        out.add(subRecords.getTypes().get(0));
        return out;
    }

    @Override
    SubRecord getNew(String type) {
        return new SubShellBulkType(subRecords.prototype, includeFirst);
    }

}
