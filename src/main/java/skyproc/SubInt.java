/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyproc;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;
import lev.LOutFile;
import lev.LShrinkArray;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/**
 *
 * @author Justin Swanson
 */
class SubInt extends SubRecordTyped<Integer> {

    private Integer data;
    int length = 4;

    SubInt(String type) {
	super(type);
    }

    SubInt(String type, int length) {
	this(type);
	this.length = length;
    }

    @Override
    SubRecord getNew(String type) {
	return new SubInt(type, length);
    }

    @Override
    int getContentLength(ModExporter out) {
	return length;
    }

    public void set(int in) {
	data = in;
    }

    public int get() {
	if (data == null) {
	    data = 0;
	}
	return data;
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
	super.parseData(in, srcMod);
	data = in.extractInt(length);
	if (SPGlobal.logMods){
	    logMod(srcMod, toString(), "Setting " + toString() + " to : " + print());
	}
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
	    out.write(data, length);
	}
    }

    @Override
    boolean isValid() {
	return data != null;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final SubInt other = (SubInt) obj;
	if (!Objects.equals(this.data, other.data)) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 71 * hash + Objects.hashCode(this.data);
	return hash;
    }

    @Override
    Integer translate() {
	return data;
    }

    @Override
    SubRecord<Integer> translate(Integer in) {
	SubInt out = (SubInt) getNew(getType());
	out.set(in);
	return out;
    }

	/*
	 * SkyBash methods.
	 */
	/**
	 * Merges straight SubInts with logging capabilities.
	 *
	 * @param no The new SubInt to be merged.
	 * @param bo The base SubInt, to prevent base data from being
	 * re-merged.
	 * @return The modified SubInt.
	 */
	@Override
	public SubRecord merge(SubRecord no, SubRecord bo) {
		SubInt integer = this;
		if (!(no == null && bo == null && (no instanceof SubInt) && (bo instanceof SubInt))) {
			final SubInt newint = (SubInt) no;
			final SubInt baseint = (SubInt) bo;
			if (!integer.equals(newint) && !newint.equals(baseint)) {
				integer = newint;
				if (Merger.fullLogging) {
					Merger.logMerge(getType(), integer.toString());
				}
			}
		}
		return integer;
	}
}
