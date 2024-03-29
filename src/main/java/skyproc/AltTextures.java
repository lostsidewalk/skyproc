package skyproc;

import lev.LImport;
import lev.LShrinkArray;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class AltTextures extends SubRecordTyped {

    final ArrayList<AltTexture> altTextures = new ArrayList<>();

    AltTextures(String t) {
        super(t);
    }

    /**
     * @param alts
     * @param rhsAlts
     * @return
     */
    public static boolean equal(ArrayList<AltTexture> alts, ArrayList<AltTexture> rhsAlts) {
        if (alts.size() != rhsAlts.size()) {
            return false;
        }
        if (alts.isEmpty() && rhsAlts.isEmpty()) {
            return true;
        }

        Set<AltTexture> altSet = new HashSet<>(alts);
        for (AltTexture t : rhsAlts) {
            if (!altSet.contains(t)) {
                return false;
            }
        }

        return true;
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        if (isValid()) {
            out.write(altTextures.size());
            for (AltTexture t : altTextures) {
                t.export(out);
            }
        }
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        int numTextures = in.extractInt(4);
        for (int i = 0; i < numTextures; i++) {
            int strLength = Ln.arrayToInt(in.getInts(0, 4));
            AltTexture newText = new AltTexture(new LShrinkArray(in.extract(12 + strLength)), srcMod);
            altTextures.add(newText);
            logMod(srcMod, "", "New Texture Alt -- Name: " + newText.name + ", texture: " + newText.texture + ", index: " + newText.index);
        }
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>(altTextures.size());
        for (AltTexture t : altTextures) {
            out.add(t.texture);
        }
        return out;
    }

    @Override
    SubRecord getNew(String type) {
        return new AltTextures(type);
    }

    @Override
    boolean isValid() {
        return !altTextures.isEmpty();
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        int len = 4;  // num Textures
        for (AltTexture t : altTextures) {
            len += t.getTotalLength();
        }
        return len;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AltTextures other = (AltTextures) obj;

        Iterator<AltTexture> lhs = this.altTextures.iterator();
        Iterator<AltTexture> rhs = other.altTextures.iterator();
        while (lhs.hasNext() && rhs.hasNext()) {
            AltTexture lhsNext = lhs.next();
            AltTexture rhsNext = rhs.next();
            if (!lhsNext.equals(rhsNext)) {
                return false;
            }
        }
        return !lhs.hasNext() && !rhs.hasNext();
    }

    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        AltTextures a = this;
        if (!(no == null && bo == null && (no instanceof AltTextures) && (bo instanceof AltTextures))) {
            final AltTextures na = (AltTextures) no;
            final AltTextures ba = (AltTextures) bo;
            Merger.merge(a.altTextures, na.altTextures, ba.altTextures, getType(), "alternate textures list");
        }
        return a;
    }


    public static class AltTexture implements Serializable {

        String name;
        FormID texture = new FormID();
        int index;

        /**
         * Creates a new AltTexture, which can be added to the ARMA to give it
         * an alternate texture.
         *
         * @param name  Name of the NiTriShape to apply this TXST to.
         * @param txst  FormID of the TXST to apply as the alt.
         * @param index Index of the NiTriShape to apply this TXST to.
         */
        public AltTexture(String name, FormID txst, int index) {
            this.name = name;
            this.texture = txst;
            this.index = index;
        }

        AltTexture(LShrinkArray in, Mod srcMod) {
            parseData(in, srcMod);
        }

        final void parseData(LShrinkArray in, Mod srcMod) {
            int strLength = in.extractInt(4);
            name = in.extractString(strLength);
            texture.parseData(in, srcMod);
            index = in.extractInt(4);
        }

        void export(ModExporter out) throws IOException {
            out.write(name.length());
            out.write(name);
            texture.export(out);
            out.write(index);
        }

        int getTotalLength() {
            return name.length() + 12;
        }

        /**
         * @return Name of the AltTexture.
         */
        public String getName() {
            return name;
        }

        /**
         * @param name String to set the AltTexture name to.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return FormID of the TXST the AltTexture is tied to.
         */
        public FormID getTexture() {
            return texture;
        }

        /**
         * @param txst FormID of the TXST to tie the AltTexture to.
         */
        public void setTexture(FormID txst) {
            texture = txst;
        }

        /**
         * @return The NiTriShape index assigned to the AltTexture.
         */
        public int getIndex() {
            return index;
        }

        /**
         * @param index The NiTriShape index to assign.
         */
        public void setIndex(int index) {
            this.index = index;
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
            final AltTexture other = (AltTexture) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return this.index == other.index;
        }

        /**
         * @return
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.texture != null ? this.texture.hashCode() : 0);
            hash = 29 * hash + this.index;
            return hash;
        }
    }
}
