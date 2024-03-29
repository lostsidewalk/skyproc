package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * A Magic Effect reference object that is used to represent attached MGEF
 * records on spells.
 *
 * @author Justin Swanson
 */
public class MagicEffectRef extends SubShellBulkType {

    static final SubPrototype magicEffProto = new SubPrototype() {
        @Override
        protected void addRecords() {
            add(new SubForm("EFID"));
            add(new EFIT());
            add(new SubList<>(new Condition()));
        }
    };

    /**
     * @param magicEffectRef A formID to a MGEF record.
     */
    public MagicEffectRef(FormID magicEffectRef) {
        this();
        subRecords.setSubForm("EFID", magicEffectRef);
    }

    MagicEffectRef() {
        super(magicEffProto, false);
    }

    @Override
    SubRecord getNew(String type) {
        return new MagicEffectRef();
    }

    @Override
    boolean isValid() {
        return subRecords.isAnyValid();
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
        final MagicEffectRef other = (MagicEffectRef) obj;
        return this.getMagicRef().equals(other.getMagicRef());
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + getMagicRef().hashCode();
        return hash;
    }


    public FormID getMagicRef() {
        return subRecords.getSubForm("EFID").getForm();
    }

    // Get/Set

    /**
     * @param magicRef
     */
    public void setMagicRef(FormID magicRef) {
        subRecords.setSubForm("EFID", magicRef);
    }

    EFIT getEFIT() {
        return (EFIT) subRecords.get("EFIT");
    }


    public float getMagnitude() {
        return getEFIT().magnitude;
    }

    /**
     * @param magnitude
     */
    public void setMagnitude(float magnitude) {
        getEFIT().magnitude = magnitude;
    }


    public int getAreaOfEffect() {
        return getEFIT().AOE;
    }

    /**
     * @param aoe
     */
    public void setAreaOfEffect(int aoe) {
        getEFIT().AOE = aoe;
    }


    public int getDuration() {
        return getEFIT().duration;
    }

    /**
     * @param duration
     */
    public void setDuration(int duration) {
        getEFIT().duration = duration;
    }


    public ArrayList<Condition> getConditions() {
        return subRecords.getSubList("CTDA").toPublic();
    }

    /**
     * @param c
     */
    public void addCondition(Condition c) {
        subRecords.getSubList("CTDA").add(c);
    }

    /**
     * @param c
     */
    public void removeCondition(Condition c) {
        subRecords.getSubList("CTDA").remove(c);
    }

    SubForm getEFID() {
        return subRecords.getSubForm("EFID");
    }

    SubList getCTDAs() {
        return subRecords.getSubList("CTDA");
    }

    /**
     * Merges straight MagicEffectRefs with logging capabilities.
     *
     * @param no The new MagicEffectRef to be merged.
     * @param bo The base MagicEffectRef, to prevent base data from being
     *           re-merged.
     * @return The modified MagicEffectRef.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        MagicEffectRef s = this;
        if (!(no == null && bo == null && (no instanceof MagicEffectRef) && (bo instanceof MagicEffectRef))) {
            final MagicEffectRef ns = (MagicEffectRef) no;
            final MagicEffectRef bs = (MagicEffectRef) bo;
            if (!s.equals(ns) && !ns.equals(bs)) {
                s.getEFID().merge(ns.getEFID(), bs.getEFID());
                s.getCTDAs().merge(ns.getCTDAs(), bs.getCTDAs());
                s.getEFIT().merge(ns.getEFIT(), bs.getEFIT());
            }
        }
        return s;
    }

    static class EFIT extends SubRecord {

        float magnitude = 0;
        int AOE = 0;
        int duration = 0;

        EFIT() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(magnitude);
            out.write(AOE);
            out.write(duration);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            magnitude = in.extractFloat();
            AOE = in.extractInt(4);
            duration = in.extractInt(4);
        }

        @Override
        SubRecord getNew(String type) {
            return new EFIT();
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 12;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("EFIT");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            EFIT e = this;
            if (!(no == null && bo == null && (no instanceof EFIT) && (bo instanceof EFIT))) {
                final EFIT ns = (EFIT) no;
                final EFIT bs = (EFIT) bo;
                Merger.merge(e.AOE, ns.AOE, bs.AOE, getType(), "AoE");
                Merger.merge(e.magnitude, ns.magnitude, bs.magnitude, getType(), "magnitude");
                Merger.merge(e.duration, ns.duration, bs.duration, getType(), "duration");
            }
            return e;
        }
    }
}