/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skyproc;

import java.util.ArrayList;

/**
 *
 * @author Justin Swanson
 */
public class Model extends SubShell {

    static final SubPrototype modelPrototype = new SubPrototype() {
	@Override
	protected void addRecords() {
	    add(SubString.getNew("MODL", true));
	    add(new SubData("MODT"));
	    add(new SubData("MODB"));
	    add(new AltTextures("MODS"));
	    add(new SubData("MODD"));
	}
    };

    Model () {
	super(modelPrototype);
    }
    
    Model(Model rhs){
        this();
        subRecords.setSubString("MODL", rhs.getFileName());
	subRecords.setSubData("MODT", rhs.subRecords.getSubData("MODT").translate());
	subRecords.setSubData("MODB", rhs.subRecords.getSubData("MODB").translate());
        ((AltTextures) subRecords.get("MODS")).altTextures.addAll(((AltTextures) rhs.subRecords.get("MODS")).altTextures);
	subRecords.setSubData("MODD", rhs.subRecords.getSubData("MODD").translate());
    }

    @Override
    SubRecord getNew(String type) {
	return new Model();
    }

    /**
     *
     * @param path
     */
    public void setFileName(String path) {
	subRecords.setSubString("MODL", path);
    }

    /**
     *
     * @return
     */
    public String getFileName() {
	return subRecords.getSubString("MODL").print();
    }

    /**
     * @return List of the AltTextures applied.
     */
    public ArrayList<AltTextures.AltTexture> getAltTextures() {
	return ((AltTextures) subRecords.get("MODS")).altTextures;
    }

    SubString getMODL() {return subRecords.getSubString("MODL");}
    SubData getMODT() {return subRecords.getSubData("MODT");}
    /**
     *
     * @param rhs Other MISC record.
     * @return true if:<br> Both sets are empty.<br> or <br> Each set contains
     * matching Alt Textures with the same name and TXST formID reference, in
     * the same corresponding indices.
     */
    public boolean equalAltTextures(Model rhs) {
	return AltTextures.equal(getAltTextures(), rhs.getAltTextures());
    }

    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        super.merge(no, bo);
        Model l = this;
        if (!(no == null && bo == null && (no instanceof Model) && (bo instanceof Model))) {
            final Model nl = (Model) no;
            final Model bl = (Model) bo;
            l.getMODL().merge(nl.getMODL(), bl.getMODL());
            l.getMODT().merge(nl.getMODT(), bl.getMODT());
        }
        return l;
    }
}
