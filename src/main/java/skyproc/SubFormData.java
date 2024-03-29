package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class SubFormData extends SubRecordTyped {

    FormID ID = new FormID();
    byte[] data;

    SubFormData(String type, FormID id, byte[] data) {
        super(type);
        ID = id;
        this.data = data;
    }

    SubFormData(String in) {
        super(in);
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        ID.parseData(in, srcMod);
        setData(in.extract(4));
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return data.length + ID.getContentLength(isStringTabled);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubFormData(type_);
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        ID.export(out);
        out.write(data, 0);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] in) {
        data = in;
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>(1);
        out.add(ID);
        return out;
    }

    void copyForm(FormID in) {
        ID = new FormID(in);
    }

    /**
     * @return The FormID string of the Major Record.
     */
    public String getFormStr() {
        return ID.getArrayStr(true);
    }

    /**
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
     * @param id FormID to set the record's to.
     */
    public void setForm(FormID id) {
        ID = id;
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
        final SubFormData other = (SubFormData) obj;
        return Objects.equals(this.ID, other.ID);
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
     * Merges straight SubFormDatas with logging capabilities. If the form is
     * identical, it also updates the data.
     *
     * @param no The new SubFormData to be merged.
     * @param bo The base SubFormData, to prevent base data from being re-merged.
     * @return The modified SubFormData.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubFormData sfi = this;
        if (!(no == null && bo == null && (no instanceof SubFormData) && (bo instanceof SubFormData))) {
            final SubFormData newsfi = (SubFormData) no;
            final SubFormData basesfi = (SubFormData) bo;
            if (newsfi.ID.equals(sfi.ID) && Arrays.equals(newsfi.data, basesfi.data)) {
                sfi.data = newsfi.data;
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
