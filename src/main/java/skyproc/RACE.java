package skyproc;

import lev.LFlags;
import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.ActorValue;
import skyproc.genenums.FirstPersonFlags;
import skyproc.genenums.Gender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.DataFormatException;

/**
 * Race Records
 *
 * @author Justin Swanson
 */
public class RACE extends MajorRecordDescription {

    // Static prototypes and definitions
    static final SubPrototype morphProto = new SubPrototype() {
        @Override
        protected void addRecords() {
            add(new SubData("MPAI"));
            add(new SubData("MPAV"));
        }
    };
    static final SubPrototype headPartProto = new SubPrototype() {
        @Override
        protected void addRecords() {
            add(new SubData("INDX"));
            add(new SubForm("HEAD"));
        }
    };
    static final SubPrototype attackDataProto = new SubPrototype() {
        @Override
        protected void addRecords() {
            add(new AttackDataInternal());
            add(SubString.getNew("ATKE", true));
        }
    };
    static final SubPrototype RACEproto = new SubPrototype(MajorRecordDescription.descProto) {
        @Override
        protected void addRecords() {
            add(new SubListCounted<>("SPCT", 4, new SubForm("SPLO")));
            add(new SubForm("WNAM"));
            add(new BodyTemplate());
            add(new KeywordSet());
            add(new DATA());
            SubMarkerSet mfnam = new SubMarkerSet<>(new SubShell(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(SubString.getNew("ANAM", true));
                    add(new SubData("MODT"));
                }
            }), "MNAM", "FNAM");
            mfnam.forceMarkers = true;
            add(mfnam);
            add(new SubList<>(SubString.getNew("MTNM", false)));
            add(new SubFormArray("VTCK", 2));
            add(new SubFormArray("DNAM", 2));
            add(new SubFormArray("HCLF", 2));
            add(new SubData("TINL"));
            add(new SubData("PNAM"));
            add(new SubData("UNAM"));
            add(new SubForm("ATKR"));
            add(new SubList<>(new AttackData()));
            add(new SubShellBulkType(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(new SubData("NAM1"));
                    forceExport("NAM1");

                    SubMarkerSet BodyData = new SubMarkerSet(new SubShell(new SubPrototype() {
                        @Override
                        protected void addRecords() {
                            add(new SubData("INDX"));
                            add(new Model());
                        }
                    }), "MNAM", "FNAM");
                    BodyData.forceMarkers = true;
                    add(BodyData);
                }
            }, false));
            add(new SubFormArray("HNAM", 0));
            add(new SubFormArray("ENAM", 0));
            add(new SubForm("GNAM"));
            add(new SubData("NAM2"));
            add(new SubShellBulkType(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(new SubData("NAM3"));
                    forceExport("NAM3");
                    add(new SubMarkerSet(new SubShell(new SubPrototype() {
                        @Override
                        protected void addRecords() {
                            add(new Model());
                        }
                    }), "MNAM", "FNAM"));
                }
            }, false));
            add(new SubForm("NAM4"));
            add(new SubForm("NAM5"));
            add(new SubForm("NAM7"));
            add(new SubForm("ONAM"));
            add(new SubForm("LNAM"));
            add(new SubList<>(SubString.getNew("NAME", true)));
            add(new SubList<>(new SubShell(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(new SubForm("MTYP"));
                    add(new SubData("SPED"));
                }
            })));
            add(new SubData("VNAM"));
            add(new SubList<>(new SubForm("QNAM")));
            add(new SubForm("UNES"));
            add(new SubList<>(SubString.getNew("PHTN", true)));
            add(new SubList<>(new SubData("PHWT")));
            add(new SubForm("WKMV"));
            add(new SubForm("RNMV"));
            add(new SubForm("SWMV"));
            add(new SubForm("FLMV"));
            add(new SubForm("SNMV"));
            add(new SubForm("SPMV"));
            add(new SubList<>(new SubShellBulkType(new SubPrototype() {
                @Override
                protected void addRecords() {
                    add(new SubData("NAM0"));
                    forceExport("NAM0");
                    add(new SubData("MNAM"));
                    add(new SubData("FNAM"));
                    add(new SubList<>(new SubShell(headPartProto)));
                    add(new SubList<>(new SubShell(morphProto)));
                    add(new SubList<>(new SubForm("RPRM")));
                    add(new SubList<>(new SubForm("RPRF")));
                    add(new SubList<>(new SubForm("AHCM")));
                    add(new SubList<>(new SubForm("AHCF")));
                    add(new SubList<>(new SubForm("FTSM")));
                    add(new SubList<>(new SubForm("FTSF")));
                    add(new SubForm("DFTM"));
                    add(new SubForm("DFTF"));
                    add(new SubList<>(new SubShell(new SubPrototype() {
                        @Override
                        protected void addRecords() {
                            add(new SubData("TINI"));
                            add(SubString.getNew("TINT", true));
                            add(new SubData("TINP"));
                            add(new SubForm("TIND"));
                            add(new SubList<>(new SubShell(new SubPrototype() {
                                @Override
                                protected void addRecords() {
                                    add(new SubForm("TINC"));
                                    add(new SubData("TINV"));
                                    add(new SubData("TIRS"));
                                }
                            })));
                        }
                    })));
                }
            }, false)));
            add(new SubForm("NAM8"));
            add(new SubForm("RNAM"));
        }
    };


    RACE() {
        super();
        subRecords.setPrototype(RACEproto);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("RACE");
    }

    @Override
    Record getNew() {
        return new RACE();
    }

    // Enums

    // Get / set
    DATA getDATA() {
        return (DATA) subRecords.get("DATA");
    }

    /**
     * @param flag
     * @param on
     */
    public void set(RACEFlags flag, boolean on) {
        getDATA().flags.set(flag.ordinal(), on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(RACEFlags flag) {
        return getDATA().flags.get(flag.ordinal());
    }

    // Common Functions

    /**
     * @return FormID of the ARMO record that is worn.
     */
    public FormID getWornArmor() {
        return subRecords.getSubForm("WNAM").getForm();
    }

    /**
     * @param id FormID to set the worn ARMO record to.
     */
    public void setWornArmor(FormID id) {
        subRecords.setSubForm("WNAM", id);
    }


    public ArrayList<FormID> getSpells() {
        return subRecords.getSubList("SPLO").toPublic();
    }

    /**
     * @param spell
     */
    public void addSpell(FormID spell) {
        subRecords.getSubList("SPLO").add(spell);
    }

    /**
     * @param spell
     */
    public void removeSpell(FormID spell) {
        subRecords.getSubList("SPLO").remove(spell);
    }


    public void clearSpells() {
        subRecords.getSubList("SPLO").clear();
    }

    /**
     * @param gender
     * @param model
     */
    public void setModel(Gender gender, String model) {
        getMFData(gender).subRecords.setSubString("ANAM", model);
    }

    SubShell getMFData(Gender gender) {
        SubMarkerSet<SubShell> MFNAM = subRecords.getSubMarker("ANAM");
        switch (gender) {
            case MALE:
                return MFNAM.get("MNAM");
            default:
                return MFNAM.get("FNAM");
        }
    }

    /**
     * @param gender
     * @return
     */
    public String getModel(Gender gender) {
        return getMFData(gender).subRecords.getSubString("ANAM").print();
    }

    /**
     * @param gender
     * @param voice
     */
    public void setVoiceType(Gender gender, FormID voice) {
        SubFormArray VTCK = (SubFormArray) subRecords.get("VTCK");
        switch (gender) {
            case MALE:
                VTCK.IDs.set(0, voice);
                break;
            default:
                VTCK.IDs.set(1, voice);
                break;
        }
    }

    /**
     * @param gender
     * @return
     */
    public FormID getVoiceType(Gender gender) {
        SubFormArray VTCK = (SubFormArray) subRecords.get("VTCK");
        switch (gender) {
            case MALE:
                return VTCK.IDs.get(0);
            default:
                return VTCK.IDs.get(1);
        }
    }

    /**
     * @param gender
     * @param color
     */
    public void setHairColor(Gender gender, FormID color) {
        SubFormArray HCLF = (SubFormArray) subRecords.get("HCLF");
        switch (gender) {
            case MALE:
                HCLF.IDs.set(0, color);
                break;
            default:
                HCLF.IDs.set(1, color);
                break;
        }
    }

    /**
     * @param gender
     * @return
     */
    public FormID getHairColor(Gender gender) {
        SubFormArray HCLF = (SubFormArray) subRecords.get("HCLF");
        switch (gender) {
            case MALE:
                return HCLF.IDs.get(0);
            default:
                return HCLF.IDs.get(1);
        }
    }

    /**
     * @param gender
     * @param part
     */
    public void setDecapHeadPart(Gender gender, FormID part) {
        SubFormArray DNAM = (SubFormArray) subRecords.get("DNAM");
        switch (gender) {
            case MALE:
                DNAM.IDs.set(0, part);
                break;
            default:
                DNAM.IDs.set(1, part);
                break;
        }
    }

    /**
     * @param gender
     * @return
     */
    public FormID getDecapHeadPart(Gender gender) {
        SubFormArray DNAM = (SubFormArray) subRecords.get("DNAM");
        switch (gender) {
            case MALE:
                return DNAM.IDs.get(0);
            default:
                return DNAM.IDs.get(1);
        }
    }

    /**
     * @param gender
     * @param value
     */
    public void setHeight(Gender gender, float value) {
        DATA DATA = getDATA();
        switch (gender) {
            case MALE:
                DATA.maleHeight = value;
            case FEMALE:
                DATA.femaleHeight = value;
        }
    }

    /**
     * @param gender
     * @return
     */
    public float getHeight(Gender gender) {
        DATA DATA = getDATA();
        switch (gender) {
            case MALE:
                return DATA.maleHeight;
            default:
                return DATA.femaleHeight;
        }
    }


    public float getAccelerationRate() {
        return getDATA().accelerationRate;
    }

    /**
     * @param accelerationRate
     */
    public void setAccelerationRate(float accelerationRate) {
        getDATA().accelerationRate = accelerationRate;
    }


    public float getAimAngleTolerance() {
        return getDATA().aimAngleTolerance;
    }

    /**
     * @param aimAngleTolerance
     */
    public void setAimAngleTolerance(float aimAngleTolerance) {
        getDATA().aimAngleTolerance = aimAngleTolerance;
    }


    public float getAngularAcceleration() {
        return getDATA().angularAcceleration;
    }

    /**
     * @param angularAcceleration
     */
    public void setAngularAcceleration(float angularAcceleration) {
        getDATA().angularAcceleration = angularAcceleration;
    }


    public float getAngularTolerance() {
        return getDATA().angularTolerance;
    }

    /**
     * @param angularTolerance
     */
    public void setAngularTolerance(float angularTolerance) {
        getDATA().angularTolerance = angularTolerance;
    }


    public float getBaseCarryWeight() {
        return getDATA().baseCarryWeight;
    }

    /**
     * @param baseCarryWeight
     */
    public void setBaseCarryWeight(float baseCarryWeight) {
        getDATA().baseCarryWeight = baseCarryWeight;
    }


    public float getBaseMass() {
        return getDATA().baseMass;
    }

    /**
     * @param baseMass
     */
    public void setBaseMass(float baseMass) {
        getDATA().baseMass = baseMass;
    }


    public float getDecelerationRate() {
        return getDATA().decelerationRate;
    }

    /**
     * @param decelerationRate
     */
    public void setDecelerationRate(float decelerationRate) {
        getDATA().decelerationRate = decelerationRate;
    }


    public float getFemaleWeight() {
        return getDATA().femaleWeight;
    }

    /**
     * @param femaleWeight
     */
    public void setFemaleWeight(float femaleWeight) {
        getDATA().femaleWeight = femaleWeight;
    }


    public float getFlightRadius() {
        return getDATA().flightRadius;
    }

    /**
     * @param flightRadius
     */
    public void setFlightRadius(float flightRadius) {
        getDATA().flightRadius = flightRadius;
    }


    public float getHealthRegen() {
        return getDATA().healthRegen;
    }

    /**
     * @param healthRegen
     */
    public void setHealthRegen(float healthRegen) {
        getDATA().healthRegen = healthRegen;
    }


    public float getInjuredHealthPct() {
        return getDATA().injuredHealthPct;
    }

    /**
     * @param injuredHealthPct
     */
    public void setInjuredHealthPct(float injuredHealthPct) {
        getDATA().injuredHealthPct = injuredHealthPct;
    }


    public float getMagickaRegen() {
        return getDATA().magickaRegen;
    }

    /**
     * @param magickaRegen
     */
    public void setMagickaRegen(float magickaRegen) {
        getDATA().magickaRegen = magickaRegen;
    }


    public float getMaleHeight() {
        return getDATA().maleHeight;
    }

    /**
     * @param maleHeight
     */
    public void setMaleHeight(float maleHeight) {
        getDATA().maleHeight = maleHeight;
    }


    public float getMaleWeight() {
        return getDATA().maleWeight;
    }

    /**
     * @param maleWeight
     */
    public void setMaleWeight(float maleWeight) {
        getDATA().maleWeight = maleWeight;
    }


    public Size getSize() {
        return getDATA().size;
    }

    /**
     * @param size
     */
    public void setSize(Size size) {
        getDATA().size = size;
    }


    public float getStaminaRegen() {
        return getDATA().staminaRegen;
    }

    /**
     * @param staminaRegen
     */
    public void setStaminaRegen(float staminaRegen) {
        getDATA().staminaRegen = staminaRegen;
    }


    public float getStartingHealth() {
        return getDATA().startingHealth;
    }

    /**
     * @param startingHealth
     */
    public void setStartingHealth(float startingHealth) {
        getDATA().startingHealth = startingHealth;
    }


    public float getStartingMagicka() {
        return getDATA().startingMagicka;
    }

    /**
     * @param startingMagicka
     */
    public void setStartingMagicka(float startingMagicka) {
        getDATA().startingMagicka = startingMagicka;
    }


    public float getStartingStamina() {
        return getDATA().startingStamina;
    }

    /**
     * @param startingStamina
     */
    public void setStartingStamina(float startingStamina) {
        getDATA().startingStamina = startingStamina;
    }


    public float getUnarmedDamage() {
        return getDATA().unarmedDamage;
    }

    /**
     * @param unarmedDamage
     */
    public void setUnarmedDamage(float unarmedDamage) {
        getDATA().unarmedDamage = unarmedDamage;
    }


    public float getUnarmedReach() {
        return getDATA().unarmedReach;
    }

    /**
     * @param unarmedReach
     */
    public void setUnarmedReach(float unarmedReach) {
        getDATA().unarmedReach = unarmedReach;
    }


    public void clearAttackData() {
        subRecords.getSubList("ATKD").clear();
    }


    public ArrayList<AttackData> getAttackData() {
        return subRecords.getSubList("ATKD").toPublic();
    }

    /**
     * @param data
     */
    public void addAttackData(AttackData data) {
        subRecords.getSubList("ATKD").add(data);
    }

    /**
     * @param data
     */
    public void removeAttackData(AttackData data) {
        subRecords.getSubList("ATKD").remove(data);
    }

    /**
     * @param rhs
     */
    public void copyAttackData(RACE rhs) {
        ArrayList<AttackData> attackList = this.getAttackData();
        attackList.clear();
        ArrayList<AttackData> rhsAttackList = rhs.getAttackData();
        for (AttackData rhsData : rhsAttackList) {
            AttackData newData = new AttackData(rhsData.getEventName());
            newData.copyData(rhsData);
            attackList.add(newData);
        }
    }


    public FormID getMaterialType() {
        return subRecords.getSubForm("NAM4").getForm();
    }

    /**
     * @param id
     */
    public void setMaterialType(FormID id) {
        subRecords.setSubForm("NAM4", id);
    }


    public FormID getImpactDataSet() {
        return subRecords.getSubForm("NAM5").getForm();
    }

    /**
     * @param id
     */
    public void setImpactDataSet(FormID id) {
        subRecords.setSubForm("NAM5", id);
    }


    public FormID getDecapitationFX() {
        return subRecords.getSubForm("NAM7").getForm();
    }

    /**
     * @param id
     */
    public void setDecapitationFX(FormID id) {
        subRecords.setSubForm("NAM7", id);
    }


    public FormID getOpenLootSound() {
        return subRecords.getSubForm("ONAM").getForm();
    }

    /**
     * @param id
     */
    public void setOpenLootSound(FormID id) {
        subRecords.setSubForm("ONAM", id);
    }


    public FormID getCloseLootSound() {
        return subRecords.getSubForm("LNAM").getForm();
    }

    /**
     * @param id
     */
    public void setCloseLootSound(FormID id) {
        subRecords.setSubForm("LNAM", id);
    }


    public FormID getUnarmedEquipSlot() {
        return subRecords.getSubForm("UNES").getForm();
    }

    /**
     * @param id
     */
    public void setUnarmedEquipSlot(FormID id) {
        subRecords.setSubForm("UNES", id);
    }


    public void clearTinting() {
        subRecords.getSubList("NAM0").clear();
    }

    SubMarkerSet<SubShell> getBodyData() {
        return subRecords.getSubShell("NAM1").subRecords.getSubMarker("INDX");
    }

    SubShell getBodyData(Gender gender) {
        switch (gender) {
            case MALE:
                return getBodyData().set.get("MNAM");
            default:
                return getBodyData().set.get("FNAM");
        }
    }

    /**
     * @param gender
     * @return
     * @deprecated If needed, request update from Leviathan
     */
    public String getLightingModels(Gender gender) {
        return getBodyData(gender).subRecords.getModel().getFileName();
    }

    /**
     * @param gender
     * @param s
     * @deprecated If needed, request update from Leviathan
     */
    public void setLightingModels(Gender gender, String s) {
        getBodyData(gender).subRecords.getModel().setFileName(s);
    }

    SubMarkerSet<SubShell> getBehaviorGraph() {
        return subRecords.getSubShell("NAM3").subRecords.getSubMarker("MODL");
    }

    SubShell getBehaviorGraph(Gender gender) {
        switch (gender) {
            case MALE:
                return getBehaviorGraph().set.get("MNAM");
            default:
                return getBehaviorGraph().set.get("FNAM");
        }
    }

    /**
     * @param gender
     * @return
     * @deprecated use getPhysicsModel()
     */
    public String getPhysicsModels(Gender gender) {
        return getPhysicsModel(gender).getFileName();
    }

    /**
     * @param gender
     * @param s
     * @deprecated use getPhysicsModel()
     */
    public void setPhysicsModels(Gender gender, String s) {
        getPhysicsModel(gender).setFileName(s);
    }

    /**
     * @param gender
     * @return
     */
    public Model getPhysicsModel(Gender gender) {
        return getBehaviorGraph(gender).subRecords.getModel();
    }


    public ArrayList<FormID> getEquipSlots() {
        return subRecords.getSubList("QNAM").toPublic();
    }

    /**
     * @param in
     */
    public void addEquipSlot(FormID in) {
        subRecords.getSubList("QNAM").add(in);
    }

    /**
     * @param in
     */
    public void removeEquipSlot(FormID in) {
        subRecords.getSubList("QNAM").remove(in);
    }


    public void clearEquipSlots() {
        subRecords.getSubList("QNAM").clear();
    }


    public KeywordSet getKeywordSet() {
        return subRecords.getKeywords();
    }


    public BodyTemplate getBodyTemplate() {
        return subRecords.getBodyTemplate();
    }


    public FormID getArmorRace() {
        return subRecords.getSubForm("RNAM").getForm();
    }

    /**
     * @param race
     */
    public void setArmorRace(FormID race) {
        subRecords.setSubForm("RNAM", race);
    }


    public FormID getMorphRace() {
        return subRecords.getSubForm("NAM8").getForm();
    }

    /**
     * @param race
     */
    public void setMorphRace(FormID race) {
        subRecords.setSubForm("NAM8", race);
    }

    /**
     * @param boostIndex
     * @param skill
     * @param value
     */
    public void setSkillBoost(int boostIndex, ActorValue skill, int value) {
        if (boostIndex < 7 && boostIndex >= 0) {
            getDATA().skillBoosts.set(boostIndex, skill);
            getDATA().skillBoostValues.set(boostIndex, value);
        }
    }

    /**
     * @param boostIndex
     * @return
     */
    public ActorValue getSkillBoostSkill(int boostIndex) {
        if (boostIndex < 7 && boostIndex >= 0) {
            return getDATA().skillBoosts.get(boostIndex);
        } else {
            return null;
        }
    }

    /**
     * @param boostIndex
     * @return
     */
    public int getSkillBoostValue(int boostIndex) {
        if (boostIndex < 7 && boostIndex >= 0) {
            return getDATA().skillBoostValues.get(boostIndex);
        } else {
            return -1;
        }
    }


    public FirstPersonFlags getHeadBipedObject() {
        return getDATA().headBipedObject;
    }

    /**
     * @param object
     */
    public void setHeadBipedObject(FirstPersonFlags object) {
        getDATA().headBipedObject = object;
    }


    public FirstPersonFlags getHairBipedObject() {
        return getDATA().hairBipedObject;
    }

    /**
     * @param object
     */
    public void setHairBipedObject(FirstPersonFlags object) {
        getDATA().hairBipedObject = object;
    }


    public FirstPersonFlags getShieldBipedObject() {
        return getDATA().shieldBipedObject;
    }

    /**
     * @param object
     */
    public void setShieldBipedObject(FirstPersonFlags object) {
        getDATA().shieldBipedObject = object;
    }


    public FirstPersonFlags getBodyBipedObject() {
        return getDATA().bodyBipedObject;
    }

    /**
     * @param object
     */
    public void setBodyBipedObject(FirstPersonFlags object) {
        getDATA().bodyBipedObject = object;
    }

    /**
     * @param flag
     * @param on
     */
    public void set(RaceFlags2 flag, boolean on) {
        getDATA().flags2.set(flag.val, on);
    }

    /**
     * @param flag
     * @return
     */
    public boolean get(RaceFlags2 flag) {
        return getDATA().flags2.get(flag.val);
    }


    public ArrayList<String> getMovementTypeNames() {
        return subRecords.getSubList("MTNM").toPublic();
    }

    /**
     * @param name
     */
    public void addMovementTypeName(String name) {
        subRecords.getSubList("MTNM").add(NiftyFunc.trimToFour(name));
    }


    public void clearMovementTypeNames() {
        subRecords.getSubList("MTNM").clear();
    }

    /**
     * @param name
     */
    public void removeMovementTypeName(String name) {
        subRecords.getSubList("MTNM").remove(NiftyFunc.trimToFour(name));
    }


    public float getFaceGenMainClamp() {
        return subRecords.getSubFloat("PNAM").get();
    }

    /**
     * @param in
     */
    public void setFaceGenMainClamp(float in) {
        subRecords.setSubFloat("PNAM", in);
    }


    public float getFaceGenFaceClamp() {
        return subRecords.getSubFloat("UNAM").get();
    }

    /**
     * @param in
     */
    public void setFaceGenFaceClamp(float in) {
        subRecords.setSubFloat("UNAM", in);
    }


    public FormID getAttackRace() {
        return subRecords.getSubForm("ATKR").getForm();
    }

    /**
     * @param id
     */
    public void setAttackRace(FormID id) {
        subRecords.setSubForm("ATKR", id);
    }

    SubList<SubShell, SubShell> getHeadData() {
        return subRecords.getSubList("NAM0");
    }

    SubShell getHeadData(Gender g) {
        String genderType;
        switch (g) {
            case MALE:
                genderType = "MNAM";
                break;
            default:
                genderType = "FNAM";
        }
        SubList<SubShell, SubShell> data = getHeadData();
        for (SubShell s : data) {
            if (s.subRecords.containsStrict(genderType)) {
                return s;
            }
        }
        SubShell newData = (SubShell) data.prototype.getNew();
        data.add(newData);
        newData.subRecords.getSubData(genderType).initialize(0);
        return newData;
    }

    /**
     * @param g
     * @param id
     */
    public void setDefaultFaceTexture(Gender g, FormID id) {
        switch (g) {
            case FEMALE:
                getHeadData(g).subRecords.setSubForm("DFTF", id);
                break;
            default:
                getHeadData(g).subRecords.setSubForm("DFTM", id);
        }
    }

    /**
     * @param g
     * @return
     */
    public FormID getDefaultFaceTexture(Gender g) {
        switch (g) {
            case FEMALE:
                return getHeadData(g).subRecords.getSubForm("DFTF").getForm();
            default:
                return getHeadData(g).subRecords.getSubForm("DFTM").getForm();
        }
    }

    /**
     * @param g
     * @return
     */
    public ArrayList<FormID> getFaceDetailsTextureSet(Gender g) {
        switch (g) {
            case FEMALE:
                return getHeadData(g).subRecords.getSubList("FTSF").toPublic();
            default:
                return getHeadData(g).subRecords.getSubList("FTSM").toPublic();
        }
    }

    /**
     * @param g
     * @param id
     */
    public void addFaceDetailsTexture(Gender g, FormID id) {
        switch (g) {
            case FEMALE:
                getHeadData(g).subRecords.getSubList("FTSF").add(new SubForm("FTSF", id));
                break;
            default:
                getHeadData(g).subRecords.getSubList("FTSM").add(new SubForm("FTSM", id));
        }
    }

    /**
     * @param g
     * @param id
     */
    public void removeFaceDetailsTexture(Gender g, FormID id) {
        switch (g) {
            case FEMALE:
                getHeadData(g).subRecords.getSubList("FTSF").remove(new SubForm("FTSF", id));
                break;
            default:
                getHeadData(g).subRecords.getSubList("FTSM").remove(new SubForm("FTSM", id));
        }
    }

    /**
     * @param g
     */
    public void clearFaceDetailsTexture(Gender g) {
        switch (g) {
            case FEMALE:
                getHeadData(g).subRecords.getSubList("FTSF").clear();
                break;
            default:
                getHeadData(g).subRecords.getSubList("FTSM").clear();
        }
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        RACE r = this;
        if (!(no == null && bo == null && (no instanceof RACE) && (bo instanceof RACE))) {
            final RACE nr = (RACE) no;
            final RACE br = (RACE) bo;
            SubRecords sList = r.subRecords;
            SubRecords nsList = nr.subRecords;
            SubRecords bsList = br.subRecords;
            for (SubRecord s : sList) {
                s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
            }
        }
        return r;
    }


    public enum RACEFlags {


        Playable,

        FaceGenHead,

        Child,

        TiltFrontBack,

        TiltLeftRight,

        NoShadow,

        Swims,

        Flies,

        Walks,

        Immobile,

        NotPushable,

        NoCombatInWater,

        NoRotatingToHeadTrack,

        DontShowBloodSpray,

        DontShowBloodDecal,

        UsesHeadTrackAnims,

        SpellsAlignWithMagicNode,

        UseWorldRaycastsForFootIK,

        AllowRagdollCollision,

        RegenHPInCombat,

        CantOpenDoors,

        AllowPCDialogue,

        NoKnockdowns,

        AllowPickpocket,

        AlwaysUseProxyController,

        DontShowWeaponBlood,

        OverlayHeadPartList,

        OverrideHeadPartList,

        CanPickupItems,

        AllowMultipleMembraneShaders,

        CanDualWeild,

        AvoidsRoads
    }


    public enum RaceFlags2 {


        UseAdvancedAvoidance(0),

        NonHostile(1),

        AllowMountedCombat(4);
        final int val;

        RaceFlags2(int val) {
            this.val = val;
        }
    }


    public enum Size {


        SMALL,

        MEDIUM,

        LARGE,

        EXTRALARGE
    }

    static final class DATA extends SubRecord {

        final ArrayList<ActorValue> skillBoosts = new ArrayList<>(7);
        final ArrayList<Integer> skillBoostValues = new ArrayList<>(7);
        float maleHeight = 0;
        float femaleHeight = 0;
        float maleWeight = 0;
        float femaleWeight = 0;
        LFlags flags = new LFlags(4);
        float startingHealth = 0;
        float startingMagicka = 0;
        float startingStamina = 0;
        float baseCarryWeight = 0;
        float baseMass = 0;
        float accelerationRate = 0;
        float decelerationRate = 0;
        Size size = Size.MEDIUM;
        FirstPersonFlags headBipedObject = FirstPersonFlags.NONE;
        FirstPersonFlags hairBipedObject = FirstPersonFlags.NONE;
        float injuredHealthPct = 0;
        FirstPersonFlags shieldBipedObject = FirstPersonFlags.NONE;
        float healthRegen = 0;
        float magickaRegen = 0;
        float staminaRegen = 0;
        float unarmedDamage = 0;
        float unarmedReach = 0;
        FirstPersonFlags bodyBipedObject = FirstPersonFlags.NONE;
        float aimAngleTolerance = 0;
        float flightRadius = 0;
        float angularAcceleration = 0;
        float angularTolerance = 0;
        LFlags flags2 = new LFlags(4);
        byte[] mountData;

        DATA() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            for (int i = 0; i < 7; i++) {
                out.write(ActorValue.value(skillBoosts.get(i)), 1);
                out.write(skillBoostValues.get(i), 1);
            }
            out.write(0, 2);
            out.write(maleHeight);
            out.write(femaleHeight);
            out.write(maleWeight);
            out.write(femaleWeight);
            out.write(flags.export(), 4);
            out.write(startingHealth);
            out.write(startingMagicka);
            out.write(startingStamina);
            out.write(baseCarryWeight);
            out.write(baseMass);
            out.write(accelerationRate);
            out.write(decelerationRate);
            out.write(size.ordinal(), 4);
            out.write(headBipedObject.getValue());
            out.write(hairBipedObject.getValue());
            out.write(injuredHealthPct);
            out.write(shieldBipedObject.getValue());
            out.write(healthRegen);
            out.write(magickaRegen);
            out.write(staminaRegen);
            out.write(unarmedDamage);
            out.write(unarmedReach);
            out.write(bodyBipedObject.getValue());
            out.write(aimAngleTolerance);
            out.write(flightRadius);
            out.write(angularAcceleration);
            out.write(angularTolerance);
            out.write(flags2.export());
            if (mountData != null) {
                out.write(mountData);
            }
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            for (int i = 0; i < 7; i++) {
                skillBoosts.add(ActorValue.value(in.extractInt(1)));
                skillBoostValues.add(in.extractInt(1));
            }
            in.skip(2);
            maleHeight = in.extractFloat();
            femaleHeight = in.extractFloat();
            maleWeight = in.extractFloat();
            femaleWeight = in.extractFloat();
            flags.set(in.extract(4));
            startingHealth = in.extractFloat();
            startingMagicka = in.extractFloat();
            startingStamina = in.extractFloat();
            baseCarryWeight = in.extractFloat();
            baseMass = in.extractFloat();
            accelerationRate = in.extractFloat();
            decelerationRate = in.extractFloat();
            size = Size.values()[in.extractInt(4)];
            headBipedObject = FirstPersonFlags.getValue(in.extractInt(4));
            hairBipedObject = FirstPersonFlags.getValue(in.extractInt(4));
            injuredHealthPct = in.extractFloat();
            shieldBipedObject = FirstPersonFlags.getValue(in.extractInt(4));
            healthRegen = in.extractFloat();
            magickaRegen = in.extractFloat();
            staminaRegen = in.extractFloat();
            unarmedDamage = in.extractFloat();
            unarmedReach = in.extractFloat();
            bodyBipedObject = FirstPersonFlags.getValue(in.extractInt(4));
            aimAngleTolerance = in.extractFloat();
            flightRadius = in.extractFloat();
            angularAcceleration = in.extractFloat();
            angularTolerance = in.extractFloat();
            flags2.set(in.extract(4));
            if (!in.isDone()) {
                mountData = in.extract(36);
            }
        }

        @Override
        SubRecord getNew(String type) {
            return new DATA();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            if (mountData == null) {
                return 128;
            } else {
                return 164;
            }
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DATA");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            DATA d = this;
            if (!(no == null && bo == null && (no instanceof DATA) && (bo instanceof DATA))) {
                final DATA nd = (DATA) no;
                final DATA bd = (DATA) bo;
                Merger.merge(d.accelerationRate, nd.accelerationRate, bd.accelerationRate, getType(), "acceleration rate");
                //Merger.merge(d.fluff1, nd.fluff1, bd.fluff1, getType(), "unknown");
                Merger.merge(d.skillBoosts, nd.skillBoosts, bd.skillBoosts, getType());
                Merger.merge(d.skillBoostValues, nd.skillBoostValues, bd.skillBoostValues, getType());
                Merger.merge(d.femaleHeight, nd.femaleHeight, bd.femaleHeight, getType(), "female height");
                Merger.merge(d.maleHeight, nd.maleHeight, bd.maleHeight, getType(), "male height");
                Merger.merge(d.maleWeight, nd.maleWeight, bd.maleWeight, getType(), "male weight");
                Merger.merge(d.femaleWeight, nd.femaleWeight, bd.femaleWeight, getType(), "female weight");
                //Merger.merge(d.fluff3, nd.fluff3, bd.fluff3, getType(), "unknown");
                Merger.merge(d.headBipedObject, nd.headBipedObject, bd.headBipedObject, getType(), "headBipedObject");
                Merger.merge(d.hairBipedObject, nd.hairBipedObject, bd.hairBipedObject, getType(), "hairBipedObject");
                Merger.merge(d.startingHealth, nd.startingHealth, bd.startingHealth, getType(), "starting health");
                Merger.merge(d.startingMagicka, nd.startingMagicka, bd.startingMagicka, getType(), "starting magicka");
                Merger.merge(d.startingStamina, nd.startingStamina, bd.startingStamina, getType(), "starting stamina");
                Merger.merge(d.baseCarryWeight, nd.baseCarryWeight, bd.baseCarryWeight, getType(), "base carry weight");
                Merger.merge(d.baseMass, nd.baseMass, bd.baseMass, getType(), "base mass");
                Merger.merge(d.decelerationRate, nd.decelerationRate, bd.decelerationRate, getType(), "deceleration rate");
                Merger.merge(d.size, nd.size, bd.size, getType(), "size");
                d.flags = Merger.merge(d.flags, nd.flags, bd.flags, getType());
                Merger.merge(d.injuredHealthPct, nd.injuredHealthPct, bd.injuredHealthPct, getType(), "injured health percentage");
                //Merger.merge(d.fluff4, nd.fluff4, bd.fluff4, getType(), "unknown");
                Merger.merge(d.shieldBipedObject, nd.shieldBipedObject, bd.shieldBipedObject, getType(), "shieldBipedObject");
                Merger.merge(d.healthRegen, nd.healthRegen, bd.healthRegen, getType(), "health regen");
                Merger.merge(d.magickaRegen, nd.magickaRegen, bd.magickaRegen, getType(), "magicka regen");
                Merger.merge(d.staminaRegen, nd.staminaRegen, bd.staminaRegen, getType(), "stamina regen");
                Merger.merge(d.unarmedDamage, nd.unarmedDamage, bd.unarmedDamage, getType(), "unarmed damage");
                Merger.merge(d.unarmedReach, nd.unarmedReach, bd.unarmedReach, getType(), "unarmed reach");
                //Merger.merge(d.fluff5, nd.fluff5, bd.fluff5, getType(), "unknown");
                Merger.merge(d.bodyBipedObject, nd.bodyBipedObject, bd.bodyBipedObject, getType(), "bodyBipedObject");
                Merger.merge(d.aimAngleTolerance, nd.aimAngleTolerance, bd.aimAngleTolerance, getType(), "aim angle tolerance");
                Merger.merge(d.flightRadius, nd.flightRadius, bd.flightRadius, getType(), "flight radius");
                Merger.merge(d.angularAcceleration, nd.angularAcceleration, bd.angularAcceleration, getType(), "angular acceleration");
                Merger.merge(d.angularTolerance, nd.angularTolerance, bd.angularTolerance, getType(), "angular tolerance");
                //Merger.merge(d.fluff6, nd.fluff6, bd.fluff6, getType(), "unknown");
                d.flags2 = Merger.merge(d.flags2, nd.flags2, bd.flags2, getType());
            }
            return d;
        }
    }


    static final public class AttackDataInternal extends SubRecord {

        float damageMult = 0;
        float attackChance = 0;
        FormID attackSpell = new FormID();
        LFlags flags = new LFlags(4);
        float attackAngle = 0;
        float strikeAngle = 0;
        float stagger = 0;
        FormID attackType = new FormID();
        float knockDown = 0;
        float recoveryTime = 0;
        float fatigueMult = 0;

        void copy(AttackDataInternal rhs) {
            damageMult = rhs.damageMult;
            attackChance = rhs.attackChance;
            attackSpell = new FormID(rhs.attackSpell);
            flags = new LFlags(rhs.flags);
            attackAngle = rhs.attackAngle;
            strikeAngle = rhs.strikeAngle;
            stagger = rhs.stagger;
            attackType = new FormID(rhs.attackType);
            knockDown = rhs.knockDown;
            recoveryTime = rhs.recoveryTime;
            fatigueMult = rhs.fatigueMult;
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(2);
            out.add(attackSpell);
            out.add(attackType);
            return out;
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(damageMult);
            out.write(attackChance);
            attackSpell.export(out);
            out.write(flags.export());
            out.write(attackAngle);
            out.write(strikeAngle);
            out.write(stagger);
            attackType.export(out);
            out.write(knockDown);
            out.write(recoveryTime);
            out.write(fatigueMult);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, BadParameter, DataFormatException {
            super.parseData(in, srcMod);
            damageMult = in.extractFloat();
            attackChance = in.extractFloat();
            attackSpell.parseData(in, srcMod);
            flags.set(in.extract(4));
            attackAngle = in.extractFloat();
            strikeAngle = in.extractFloat();
            stagger = in.extractFloat();
            attackType.parseData(in, srcMod);
            knockDown = in.extractFloat();
            recoveryTime = in.extractFloat();
            fatigueMult = in.extractFloat();
        }

        @Override
        SubRecord getNew(String type) {
            return new AttackDataInternal();
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("ATKD");
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 44;
        }
    }


    static final public class AttackData extends SubShell {

        AttackData() {
            super(attackDataProto);
        }

        /**
         * @param eventName
         */
        public AttackData(String eventName) {
            this();
            subRecords.setSubString("ATKE", eventName);
        }

        /**
         * @return
         */
        public String getEventName() {
            return subRecords.getSubString("ATKE").print();
        }

        AttackDataInternal getATKD() {
            return (AttackDataInternal) subRecords.get("ATKD");
        }

        /**
         * @param rhs
         */
        public void copyData(AttackData rhs) {
            getATKD().copy(rhs.getATKD());
        }

        @Override
        SubRecord getNew(String type) {
            return new AttackData();
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
            final AttackData other = (AttackData) obj;
            return Objects.equals(this.getEventName(), other.getEventName());
        }

        /**
         * @return
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + Objects.hashCode(this.getEventName());
            return hash;
        }

        /**
         * @return
         */
        public float getDamageMult() {
            return getATKD().damageMult;
        }

        /**
         * @param in
         */
        public void setDamageMult(float in) {
            getATKD().damageMult = in;
        }

        /**
         * @return
         */
        public float getAttackChance() {
            return getATKD().attackChance;
        }

        /**
         * @param in
         */
        public void setAttackChance(float in) {
            getATKD().attackChance = in;
        }

        /**
         * @return
         */
        public FormID getAttackSpell() {
            return getATKD().attackSpell;
        }

        /**
         * @param spell
         */
        public void setAttackSpell(FormID spell) {
            getATKD().attackSpell = spell;
        }

        /**
         * @return
         */
        public float getAttackAngle() {
            return getATKD().attackAngle;
        }

        /**
         * @param in
         */
        public void setAttackAngle(float in) {
            getATKD().attackAngle = in;
        }

        /**
         * @return
         */
        public float getStrikeAngle() {
            return getATKD().strikeAngle;
        }

        /**
         * @param in
         */
        public void setStrikeAngle(float in) {
            getATKD().strikeAngle = in;
        }

        /**
         * @return
         */
        public float getStagger() {
            return getATKD().stagger;
        }

        /**
         * @param in
         */
        public void setStagger(float in) {
            getATKD().stagger = in;
        }

        /**
         * @return
         */
        public FormID getAttackType() {
            return getATKD().attackType;
        }

        /**
         * @param id
         */
        public void setAttackType(FormID id) {
            getATKD().attackType = id;
        }

        /**
         * @return
         */
        public float getKnockDown() {
            return getATKD().knockDown;
        }

        /**
         * @param in
         */
        public void setKnockDown(float in) {
            getATKD().knockDown = in;
        }

        /**
         * @return
         */
        public float getRecoveryTime() {
            return getATKD().recoveryTime;
        }

        /**
         * @param in
         */
        public void setRecoveryTime(float in) {
            getATKD().recoveryTime = in;
        }

        /**
         * @return
         */
        public float getFatigueMult() {
            return getATKD().fatigueMult;
        }

        /**
         * @param in
         */
        public void setFatigueMult(float in) {
            getATKD().fatigueMult = in;
        }
    }
}
