package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;

public class KeywordSet extends SubRecord {

    private final static ArrayList<String> type = new ArrayList<>(Arrays.asList("KSIZ", "KWDA"));
    final SubData counter = new SubData("KSIZ", 0);
    final SubFormArray keywords = new SubFormArray("KWDA", 0);

    KeywordSet() {
        super();
    }

    KeywordSet(KeywordSet rhs) {
        this();
        counter.setData(rhs.keywords.size(), 4);
        for (FormID key : rhs.getKeywordRefs()) {
            addKeywordRef(key);
        }
    }

    @Override
    SubRecord getNew(String type) {
        return new KeywordSet();
    }

    @Override
    boolean isValid() {
        return counter.getData() != null && counter.getData()[0] != 0x00 && keywords.isValid();
    }

    @Override
    int getHeaderLength() {
        return 0;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return counter.getTotalLength(isStringTabled)
                + keywords.getTotalLength(isStringTabled);
    }

    @Override
    void export(ModExporter out) throws IOException {
        if (isValid()) {
            counter.setData(keywords.size(), 4);
            counter.export(out);
            keywords.export(out);
        }
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        switch (getNextType(in)) {
            case "KSIZ":
                counter.parseData(in, srcMod);
//                keywords = new SubFormArray("KWDA", counter.toInt());
                break;
            case "KWDA":
                keywords.parseData(in, srcMod);
                break;
        }
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        return keywords.IDs;
    }

    /**
     * Returns a COPY of the list of FormIDs associated with this keyword set.
     *
     * @return
     */
    public ArrayList<FormID> getKeywordRefs() {
        return new ArrayList<>(keywords.IDs);
    }

    /**
     * Adds a keyword to the list if it is not already in the list
     *
     * @param keywordRef A KYWD formID
     */
    public void addKeywordRef(FormID keywordRef) {
        if (!keywords.contains(keywordRef)) {
            keywords.add(keywordRef);
            //counter.modValue(1);
        }
    }

    /**
     * Removes a keyword to the list
     *
     * @param keywordRef A KYWD formID
     */
    public void removeKeywordRef(FormID keywordRef) {
        if (keywords.remove(keywordRef)) {
            //counter.modValue(-1);
        }
    }


    public void clearKeywordRefs() {
        keywords.clear();
        counter.setData(0, 4);
    }

    /**
     * @param set
     * @return True if every keyword in this set is contained in the parameter's
     * set.
     */
    public boolean containedIn(KeywordSet set) {
        return keywords.containedIn(set.keywords);
    }

    /**
     * @param obj
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
        final KeywordSet other = (KeywordSet) obj;
        return Objects.equals(this.keywords, other.keywords);
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.keywords != null ? this.keywords.hashCode() : 0);
        return hash;
    }

    @Override
    ArrayList<String> getTypes() {
        return type;
    }

    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        KeywordSet k = this;
        if (!(no == null && bo == null && (no instanceof KeywordSet) && (bo instanceof KeywordSet))) {
            final KeywordSet nk = (KeywordSet) no;
            final KeywordSet bk = (KeywordSet) bo;
            k.keywords.merge(nk.keywords, bk.keywords);
        }
        return k;
    }
}
