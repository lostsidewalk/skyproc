package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
public class PROJ extends MajorRecordNamed {

    // Static prototypes and definitions
    static final SubPrototype PROJprototype = new SubPrototype(MajorRecordNamed.namedProto) {

        @Override
        protected void addRecords() {
            add(new SubData("OBND", new byte[12]));
            reposition("FULL");
            add(new Model());
            add(new DestructionData());
            add(new DATA());
            add(SubString.getNew("NAM1", true));
            add(new SubData("NAM2"));
            add(new SubData("VNAM")); // SoundVolume
        }
    };

    // Common Functions
    PROJ() {
        super();
        subRecords.setPrototype(PROJprototype);
    }

    // Enums

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("PROJ");
    }

    @Override
    Record getNew() {
        return new PROJ();
    }

    /**
     * @return
     * @deprecated use getModelData()
     */
    public String getModel() {
        return subRecords.getModel().getFileName();
    }

    /**
     * @param path
     * @deprecated use getModelData()
     */
    public void setModel(String path) {
        subRecords.getModel().setFileName(path);
    }


    public String getEffectModel() {
        return subRecords.getSubString("NAM1").print();
    }

    //Get/Set

    /**
     * @param filename
     */
    public void setEffectModel(String filename) {
        subRecords.setSubString("NAM1", filename);
    }

    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }

    /**
     * @param flag
     * @param on
     */
    public void set(ProjectileFlag flag, boolean on) {
        getDATA().flags.set(flag.value, on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(ProjectileFlag flag) {
        return getDATA().flags.get(flag.value);
    }


    public ProjectileType getProjType() {
        return ProjectileType.values()[getDATA().projType.getFirstTrue()];
    }

    /**
     * @param t
     */
    public void setProjType(ProjectileType t) {
        LFlags flags = getDATA().projType;
        flags.clear();
        flags.set(t.ordinal(), true);
    }


    public float getGravity() {
        return getDATA().gravity;
    }

    /**
     * @param gravity
     */
    public void setGravity(float gravity) {
        getDATA().gravity = gravity;
    }


    public float getSpeed() {
        return getDATA().speed;
    }

    /**
     * @param speed
     */
    public void setSpeed(float speed) {
        getDATA().speed = speed;
    }


    public float getRange() {
        return getDATA().range;
    }

    /**
     * @param range
     */
    public void setRange(float range) {
        getDATA().range = range;
    }


    public FormID getLight() {
        return getDATA().light;
    }

    /**
     * @param light
     */
    public void setLight(FormID light) {
        getDATA().light = light;
    }


    public FormID getMuzzleLight() {
        return getDATA().muzzleLight;
    }

    /**
     * @param light
     */
    public void setMuzzleLight(FormID light) {
        getDATA().muzzleLight = light;
    }


    public float getTracerChance() {
        return getDATA().tracerChance;
    }

    /**
     * @param chance
     */
    public void setTracerChance(float chance) {
        getDATA().tracerChance = chance;
    }


    public float getProximity() {
        return getDATA().proximity;
    }

    /**
     * @param proximity
     */
    public void setProximity(float proximity) {
        getDATA().proximity = proximity;
    }


    public float getTimer() {
        return getDATA().timer;
    }

    /**
     * @param timer
     */
    public void setTimer(float timer) {
        getDATA().timer = timer;
    }


    public FormID getExplosionType() {
        return getDATA().explosionType;
    }

    /**
     * @param explType
     */
    public void setExplosionType(FormID explType) {
        getDATA().explosionType = explType;
    }


    public FormID getSound() {
        return getDATA().sound;
    }

    /**
     * @param sound
     */
    public void setSound(FormID sound) {
        getDATA().sound = sound;
    }


    public float getMuzzleFlashDuration() {
        return getDATA().muzzleFlashDuration;
    }

    /**
     * @param duration
     */
    public void setMuzzleFlashDuration(float duration) {
        getDATA().muzzleFlashDuration = duration;
    }


    public float getFadeDuration() {
        return getDATA().fadeDuration;
    }

    /**
     * @param duration
     */
    public void setFadeDuration(float duration) {
        getDATA().fadeDuration = duration;
    }


    public float getImpactForce() {
        return getDATA().impactForce;
    }

    /**
     * @param force
     */
    public void setImpactForce(float force) {
        getDATA().impactForce = force;
    }


    public FormID getExplosionSound() {
        return getDATA().explosionSound;
    }

    /**
     * @param sound
     */
    public void setExplosionSound(FormID sound) {
        getDATA().explosionSound = sound;
    }


    public FormID getDisableSound() {
        return getDATA().disableSound;
    }

    /**
     * @param sound
     */
    public void setDisableSound(FormID sound) {
        getDATA().disableSound = sound;
    }


    public FormID getDefaultWeaponSource() {
        return getDATA().defaultWeaponSource;
    }

    /**
     * @param weaponSource
     */
    public void setDefaultWeaponSource(FormID weaponSource) {
        getDATA().defaultWeaponSource = weaponSource;
    }


    public float getConeSpread() {
        return getDATA().coneSpread;
    }

    /**
     * @param spread
     */
    public void setConeSpread(float spread) {
        getDATA().coneSpread = spread;
    }


    public float getCollisionRadius() {
        return getDATA().collisionRadius;
    }

    /**
     * @param radius
     */
    public void setCollisionRadius(float radius) {
        getDATA().collisionRadius = radius;
    }


    public float getLifetime() {
        return getDATA().lifetime;
    }

    /**
     * @param lifetime
     */
    public void setLifetime(float lifetime) {
        getDATA().lifetime = lifetime;
    }


    public float getRelaunchInterval() {
        return getDATA().relaunchInterval;
    }

    /**
     * @param interval
     */
    public void setRelaunchInterval(float interval) {
        getDATA().relaunchInterval = interval;
    }


    public FormID getDecalData() {
        return getDATA().decalData;
    }

    /**
     * @param decal
     */
    public void setDecalData(FormID decal) {
        getDATA().decalData = decal;
    }


    public Model getModelData() {
        return subRecords.getModel();
    }


    public enum ProjectileFlag {


        Explosion(1),

        AltTrigger(2),

        MuzzleFlash(3),

        CanBeDisabled(5),

        CanBePickedUp(6),

        SuperSonic(7),

        CritPinsLimbs(8),

        PassThroughSmallTransparent(9),

        DisableCombatAimCorrection(10);
        final int value;

        ProjectileFlag(int val) {
            value = val;
        }
    }


    public enum ProjectileType {

        /**
         *
         */
        Missile, //1
        /**
         *
         */
        Lobber, //2
        /**
         *
         */
        Beam, //4
        /**
         *
         */
        Flame, //8
        /**
         *
         */
        Cone, //10
        /**
         *
         */
        Barrier, //20
        /**
         *
         */
        Arrow; //40

        static ProjectileType get(int value) {
            switch (value) {
                case 1:
                    return Missile;
                case 2:
                    return Lobber;
                case 4:
                    return Beam;
                case 8:
                    return Flame;
                case 16:
                    return Cone;
                case 32:
                    return Barrier;
                default:
                    return Arrow;
            }
        }
    }

    static class DATA extends SubRecord {

        final LFlags flags = new LFlags(2);
        final LFlags projType = new LFlags(2);
        float gravity = 0;
        float speed = 0;
        float range = 0;   //1
        FormID light = new FormID();
        FormID muzzleLight = new FormID();
        float tracerChance = 0;
        float proximity = 0;  // 2
        float timer = 0;
        FormID explosionType = new FormID();
        FormID sound = new FormID();
        float muzzleFlashDuration = 0;  //3
        float fadeDuration = 0;
        float impactForce = 0;
        FormID explosionSound = new FormID();
        FormID disableSound = new FormID();  //4
        FormID defaultWeaponSource = new FormID();
        float coneSpread = 0;
        float collisionRadius = 0;
        float lifetime = 0; //5
        float relaunchInterval = 0;
        FormID decalData = new FormID();
        byte[] collisionLayer = new byte[4];

        DATA() {
            super();
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>();
            out.add(light);
            out.add(muzzleLight);
            out.add(explosionType);
            out.add(sound);
            out.add(explosionSound);
            out.add(disableSound);
            out.add(defaultWeaponSource);
            out.add(decalData);
            return out;
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(flags.export(), 2);
            out.write(projType.export(), 2);
            out.write(gravity);
            out.write(speed);
            out.write(range);
            light.export(out);
            muzzleLight.export(out);
            out.write(tracerChance);
            out.write(proximity);
            out.write(timer);
            explosionType.export(out);
            sound.export(out);
            out.write(muzzleFlashDuration);
            out.write(fadeDuration);
            out.write(impactForce);
            explosionSound.export(out);
            disableSound.export(out);
            defaultWeaponSource.export(out);
            out.write(coneSpread);
            out.write(collisionRadius);
            out.write(lifetime);
            out.write(relaunchInterval);
            decalData.export(out);
            out.write(collisionLayer);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, BadParameter, DataFormatException {
            super.parseData(in, srcMod);
            flags.set(in.extract(2));
            projType.set(in.extract(2));
            gravity = in.extractFloat();
            speed = in.extractFloat();
            range = in.extractFloat();   //16
            light.parseData(in, srcMod);
            muzzleLight.parseData(in, srcMod);
            tracerChance = in.extractFloat();
            proximity = in.extractFloat();  //32
            timer = in.extractFloat();
            explosionType.parseData(in, srcMod);
            sound.parseData(in, srcMod);
            muzzleFlashDuration = in.extractFloat();  //48
            fadeDuration = in.extractFloat();
            impactForce = in.extractFloat();
            explosionSound.parseData(in, srcMod);
            disableSound.parseData(in, srcMod);  //64
            defaultWeaponSource.parseData(in, srcMod);
            coneSpread = in.extractFloat();
            collisionRadius = in.extractFloat();
            lifetime = in.extractFloat(); // 80
            relaunchInterval = in.extractFloat();
            if (!in.isDone()) {
                decalData.parseData(in, srcMod);
            }
            if (!in.isDone()) {
                collisionLayer = in.extract(4);  // 92
            }
        }

        @Override
        SubRecord getNew(String type) {
            return new DATA();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 92;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }
    }
}
