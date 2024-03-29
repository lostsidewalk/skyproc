package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.CastType;
import skyproc.genenums.DeliveryType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class ENCH extends EnchantedItem {

    // Static prototypes and definitions
    static final SubPrototype ENCHproto = new SubPrototype(MagicItem.magicItemProto) {
        @Override
        protected void addRecords() {
            reposition("OBND");
            reposition("FULL");
            remove("DESC");
            add(new ENIT());
            reposition("EFID");
        }
    };

    // Common Functions
    ENCH() {
        super();
        subRecords.setPrototype(ENCHproto);
    }

    // Enums

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("ENCH");
    }

    @Override
    Record getNew() {
        return new ENCH();
    }

    // Get/Set
    ENIT getENIT() {
        return (ENIT) subRecords.get("ENIT");
    }


    public int getBaseCost() {
        return getENIT().baseCost;
    }

    /**
     * @param cost
     */
    public void setBaseCost(int cost) {
        getENIT().baseCost = cost;
    }

    /**
     * @param in
     * @param on
     */
    public void set(ENCHFlag in, boolean on) {
        getENIT().flags.set(in.value, on);
    }

    /**
     * @param in
     * @return
     */
    public boolean get(ENCHFlag in) {
        return getENIT().flags.get(in.value);
    }


    public CastType getCastType() {
        return getENIT().castType;
    }

    /**
     * @param type
     */
    public void setCastType(CastType type) {
        getENIT().castType = type;
    }


    public int getChargeAmount() {
        return getENIT().chargeAmount;
    }

    /**
     * @param amount
     */
    public void setChargeAmount(int amount) {
        getENIT().chargeAmount = amount;
    }


    public DeliveryType getDeliveryType() {
        return getENIT().targetType;
    }

    /**
     * @param type
     */
    public void setDeliveryType(DeliveryType type) {
        getENIT().targetType = type;
    }


    public EnchantType getEnchantType() {
        return getENIT().enchantType;
    }

    /**
     * @param type
     */
    public void setEnchantType(EnchantType type) {
        getENIT().enchantType = type;
    }


    public float getChargeTime() {
        return getENIT().chargeTime;
    }

    /**
     * @param time
     */
    public void setChargeTime(float time) {
        getENIT().chargeTime = time;
    }


    public FormID getBaseEnchantment() {
        return getENIT().baseEnchantment;
    }

    /**
     * @param id
     */
    public void setBaseEnchantment(FormID id) {
        getENIT().baseEnchantment = id;
    }


    public FormID getWornRestrictions() {
        return getENIT().wornRestrictions;
    }

    /**
     * @param id
     */
    public void setWornRestrictions(FormID id) {
        getENIT().wornRestrictions = id;
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        ENCH e = this;
        if (!(no == null && bo == null && (no instanceof ENCH) && (bo instanceof ENCH))) {
            final ENCH ne = (ENCH) no;
            final ENCH be = (ENCH) bo;
            SubRecords sList = e.subRecords;
            SubRecords nsList = ne.subRecords;
            SubRecords bsList = be.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return e;
    }


    public enum EnchantType {


        Enchantment(6),

        StaffEnchantment(12);
        final int value;

        EnchantType(int value) {
            this.value = value;
        }

        static EnchantType value(int in) {
            switch (in) {
                case 12:
                    return StaffEnchantment;
                default:
                    return Enchantment;
            }
        }
    }


    public enum ENCHFlag {


        ManualCalc(0),

        ExtendDurationOnRecast(3);
        final int value;

        ENCHFlag(int in) {
            value = in;
        }
    }

    static final class ENIT extends SubRecord {

        int baseCost = 0;
        LFlags flags = new LFlags(4);
        CastType castType = CastType.ConstantEffect;
        int chargeAmount = 0;
        DeliveryType targetType = DeliveryType.Self;
        EnchantType enchantType = EnchantType.Enchantment;
        float chargeTime = 0;
        FormID baseEnchantment = new FormID();
        FormID wornRestrictions = new FormID();
        boolean old = false;

        ENIT() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(baseCost, 4);
            out.write(flags.export());
            out.write(castType.ordinal(), 4);
            out.write(chargeAmount, 4);
            out.write(targetType.ordinal(), 4);
            out.write(enchantType.value, 4);
            out.write(chargeTime);
            baseEnchantment.export(out);
            if (!old) {
                wornRestrictions.export(out);
            }
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            baseCost = in.extractInt(4);
            flags.set(in.extract(4));
            castType = CastType.values()[in.extractInt(4)];
            chargeAmount = in.extractInt(4);
            targetType = DeliveryType.values()[in.extractInt(4)];
            enchantType = EnchantType.value(in.extractInt(4));
            chargeTime = in.extractFloat();
            baseEnchantment.parseData(in, srcMod);
            if (!in.isDone()) {
                wornRestrictions.parseData(in, srcMod);
            } else {
                old = true;
            }
        }

        @Override
        SubRecord getNew(String type) {
            return new ENIT();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            if (old) {
                return 32;
            } else {
                return 36;
            }
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(2);
            out.add(baseEnchantment);
            out.add(wornRestrictions);
            return out;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("ENIT");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            ENIT e = this;
            if (!(no == null && bo == null && (no instanceof ENIT) && (bo instanceof ENIT))) {
                final ENIT ne = (ENIT) no;
                final ENIT be = (ENIT) bo;
                Merger.merge(e.baseCost, ne.baseCost, be.baseCost, getType(), "base cost");
                e.flags = Merger.merge(e.flags, ne.flags, be.flags, getType());
                Merger.merge(e.castType, ne.castType, be.castType, getType(), "cast type");
                e.baseEnchantment.merge(ne.baseEnchantment, be.baseEnchantment, getType());
                e.wornRestrictions.merge(ne.wornRestrictions, be.wornRestrictions, getType());
                Merger.merge(e.chargeAmount, ne.chargeAmount, be.chargeAmount, getType(), "charge amount");
                Merger.merge(e.targetType, ne.targetType, be.targetType, getType(), "target type");
                Merger.merge(e.enchantType, ne.enchantType, be.enchantType, getType(), "enchant type");
                Merger.merge(e.chargeTime, ne.chargeTime, be.chargeTime, getType(), "charge time");
            }
            return e;
        }
    }
}
