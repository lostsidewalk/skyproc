package skyproc;

import java.util.ArrayList;

/**
 *
 * @author Arkangel
 */
public class COBJ extends MajorRecord {

    // Static prototypes and definitions
    static final SubPrototype COBJproto = new SubPrototype(MajorRecord.majorProto) {

	@Override
	protected void addRecords() {
	    add(new SubListCounted<>("COCT", 4, new SubFormInt("CNTO")));
	    add(new Owner());
	    add(new SubList<>(new Condition()));
	    add(new SubForm("CNAM"));
	    add(new SubForm("BNAM"));
	    add(new SubInt("NAM1", 2));
	}
    };

    // Common Functions
    /**
     * Creates a new COBJ record with CK default settings.
     * @param edid A unique EDID
     */
    public COBJ (String edid) {
	this();
	originateFromPatch(edid);
	subRecords.getSubInt("NAM1").set(1);
    }

    COBJ() {
	super();
	subRecords.setPrototype(COBJproto);
    }

    @Override
    ArrayList<String> getTypes() {
	return Record.getTypeList("COBJ");
    }

    @Override
    Record getNew() {
	return new COBJ();
    }

    // Get/Set
    /**
     * @deprecated COBJ does not have keywords, this function was added in error.
     * @return
     */
    public KeywordSet getKeywordSet() {
        throw new UnsupportedOperationException("COBJ does not have keywords"); 
    }

    /**
     *
     * @return
     */
    public ArrayList<Condition> getConditions() {
	return subRecords.getSubList("CTDA").toPublic();
    }

    /**
     * 
     * @param c
     */
    public void addCondition(Condition c) {
	subRecords.getSubList("CTDA").add(c);
    }

    /**
     * 
     * @param c
     */
    public void removeCondition(Condition c) {
	subRecords.getSubList("CTDA").remove(c);
    }

    /**
     * 
     * @param itemReference
     * @param count
     * @return
     */
    public boolean addIngredient(FormID itemReference, int count) {
	return subRecords.getSubList("CNTO").add(new SubFormInt("CNTO", itemReference, count));
    }

    /**
     * 
     * @param itemReference
     * @return
     */
    public boolean removeIngredient(FormID itemReference) {
	return subRecords.getSubList("CNTO").remove(new SubFormInt("CNTO", itemReference, 1));
    }

    /**
     * 
     */
    public void clearIngredients() {
	subRecords.getSubList("CNTO").clear();
    }

    /**
     *
     * @return
     */
    public ArrayList<SubFormInt> getIngredients() {
	return subRecords.getSubList("CNTO").toPublic();
    }

    /**
     *
     * @return
     */
    public FormID getResultFormID() {
	return subRecords.getSubForm("CNAM").getForm();
    }

    /**
     *
     * @param form
     */
    public void setResultFormID(FormID form) {
	subRecords.setSubForm("CNAM", form);
    }

    /**
     *
     * @return
     */
    public FormID getBenchKeywordFormID() {
	return subRecords.getSubForm("BNAM").getForm();
    }

    /**
     *
     * @param form
     */
    public void setBenchKeywordFormID(FormID form) {
	subRecords.setSubForm("BNAM", form);
    }

    /**
     *
     * @return
     */
    public int getOutputQuantity() {
	return subRecords.getSubInt("NAM1").get();
    }

    /**
     *
     * @param n
     */
    public void setOutputQuantity(int n) {
	subRecords.setSubInt("NAM1", n);
    }

    SubList getCOCT() {return subRecords.getSubList("COCT");}

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        COBJ c = this;
        if (!(no == null && bo == null && (no instanceof COBJ) && (bo instanceof COBJ))) {
            final COBJ nc = (COBJ) no;
            final COBJ bc = (COBJ) bo;
            SubRecords sList = c.subRecords;
            SubRecords nsList = nc.subRecords;
            SubRecords bsList = bc.subRecords;
            for (SubRecord s : sList) {
                if (s.equals(c.getCOCT())) {
                    c.getCOCT().mergeList(nc.getCOCT(), bc.getCOCT());
                } else {
                    s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
                }
            }
        }
        return c;
    }
}
