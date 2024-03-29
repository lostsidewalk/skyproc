package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * A SubRecord containing a FormID at the start of its structure.
 *
 * @author Justin Swanson
 */
class SubForm extends SubRecordTyped<FormID> {

    FormID ID = new FormID();

    SubForm(String type_) {
        super(type_);
    }

    SubForm(String type, FormID form) {
        this(type);
        setForm(form);
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

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        ID.parseData(in, srcMod);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return ID.toString() + " - " + super.toString();
        } else {
            return super.toString();
        }
    }

    @Override
    boolean isValid() {
        return ID.isValid();
    }

    @Override
    public String print() {
        return ID.getFormStr();
    }

    @Override
    boolean confirmLink() {
        if (SPGlobal.globalDatabase != null) {
            return confirmLink(SPGlobal.globalDatabase);
        } else {
            return true;
        }
    }

    boolean confirmLink(SPDatabase db) {
        return true;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return ID.getContentLength(isStringTabled);
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        ID.export(out);
    }

    @Override
    SubRecord getNew(String type_) {
        return new SubForm(type_);
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
        final SubForm other = (SubForm) obj;
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

    @Override
    ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>(1);
        out.add(ID);
        return out;
    }

    @Override
    FormID translate() {
        return ID;
    }

    @Override
    SubRecord<FormID> translate(FormID in) {
        return new SubForm(getType(), in);
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubForms with logging capabilities.
     *
     * @param no The new SubForm to be merged.
     * @param bo The base SubForm, to prevent base data from being
     *           re-merged.
     * @return The modified SubForm.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubForm f = this;
        if (!(no == null && bo == null && (no instanceof SubForm) && (bo instanceof SubForm))) {
            final SubForm nsf = (SubForm) no;
            final SubForm bsf = (SubForm) bo;
            if (!f.equals(nsf) && !nsf.equals(bsf)) {
                f = nsf;
                if (Merger.fullLogging) {
                    Merger.logMerge(getTypes().get(0), f.toString());
                }
            }
        }
        return f;
    }
}
