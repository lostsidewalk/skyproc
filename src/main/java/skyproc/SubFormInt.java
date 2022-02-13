package skyproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.DataFormatException;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

/**
 * SubRecord that has a FormID followed by an integer.
 * @author Justin Swanson
 */
public class SubFormInt extends SubRecordTyped {

    FormID ID = new FormID();
    int num;

    SubFormInt(String in) {
	super(in);
    }

    SubFormInt(String type, FormID id, int number) {
	super(type);
	ID = id;
	num = number;
    }

    @Override
    void export(ModExporter out) throws IOException {
	super.export(out);
	ID.export(out);
	out.write(num);
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
	ID.parseData(in, srcMod);
        num = in.extractInt(4);
    }

    @Override
    int getContentLength(ModExporter out) {
	return ID.getContentLength() + 4;
    }
    
    @Override
    ArrayList<FormID> allFormIDs() {
	ArrayList<FormID> out = new ArrayList<>(1);
	out.add(ID);
	return out;
    }

    @Override
    SubRecord getNew(String type_) {
	return new SubFormInt(type_);
    }

    @Override
    public String print() {
	return "ID: " + ID + ", value: " + num;
    }

    void copyForm(FormID in) {
	ID = new FormID(in);
    }

    /**
     *
     * @param id FormID to set the record's to.
     */
    public void setForm(FormID id) {
	ID = id;
    }

    /**
     *
     * @return The FormID string of the Major Record.
     */
    public String getFormStr() {
	return ID.getArrayStr(true);
    }

    /**
     *
     * @return The name of the mod from which this Major Record originates.
     */
    public ModListing getFormMaster() {
	return ID.getMaster();
    }

    FormID copyOfForm() {
	return new FormID(ID);
    }

    /**
     * Returns the FormID object of the Sub Record. Note that any changes made
     * to this FormID will be reflected in the Sub Record also.
     *
     * @return The FormID object of the Sub Record.
     */
    public FormID getForm() {
	return ID;
    }
    
    /**
     *
     * @param number Number to associate with this record.
     */
    public void setNum(int number) {
	num = number;
    }

    /**
     *
     * @return Number associated with this record.
     */
    public int getNum() {
	return num;
    }

    /**
     * Takes the FormID into the equals calculations
     *
     * @param obj Another SubFormRecord
     * @return
     */
    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final SubFormInt other = (SubFormInt) obj;
	if (!Objects.equals(this.ID, other.ID)) {
	    return false;
	}
	return true;
    }

    /**
     * Takes the FormID into the hashcode calculations
     *
     * @return
     */
    @Override
    public int hashCode() {
	int hash = 7;
	hash = 29 * hash + (this.ID != null ? this.ID.hashCode() : 0);
	return hash;
    }

    /*
     * SkyBash methods.
     */
    /**
     * Merges straight SubFormInts with logging capabilities. If the form is
     * identical, it also updates the integer.
     *
     * @param no The new SubFormInt to be merged.
     * @param bo The base SubFormInt, to prevent base data from being re-merged.
     * @return The modified SubFormInt.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubFormInt sfi = this;
        if (!(no == null && bo == null && (no instanceof SubFormInt) && (bo instanceof SubFormInt))) {
            final SubFormInt newsfi = (SubFormInt) no;
            final SubFormInt basesfi = (SubFormInt) bo;
            if (newsfi.ID.equals(sfi.ID) && newsfi.num != basesfi.num) {
                sfi.num = newsfi.num;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), sfi.toString());
                }
            } else if (!newsfi.equals(basesfi)) {
                sfi = newsfi;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), sfi.toString());
                }
            }
        }
        return sfi;
    }
}