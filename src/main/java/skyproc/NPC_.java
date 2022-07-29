package skyproc;

import lev.LFlags;
import lev.LImport;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;
import skyproc.genenums.Skill;
import skyproc.genenums.SoundVolume;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * An actor in the world.
 *
 * @author Justin Swanson
 */
public class NPC_ extends MajorRecordNamed implements Serializable {

    // Static prototypes and definitions
    static final SubPrototype NPC_proto = new SubPrototype(MajorRecordNamed.namedProto) {
        @Override
        protected void addRecords() {
            after(new ScriptPackage(), "EDID");
            add(new SubData("OBND", new byte[12]));
            add(new ACBS());
            add(new SubList<>(new SubFormInt("SNAM")));
            add(new SubForm("INAM"));
            add(new SubForm("VTCK"));
            add(new SubForm("TPLT"));
            add(new SubForm("RNAM"));
            add(new SubListCounted<>("SPCT", 4, new SubForm("SPLO")));
            add(new DestructionData());
            add(new SubForm("WNAM"));
            add(new SubForm("ANAM"));
            add(new SubForm("ATKR"));
            add(new SubList<>(new AttackPackage()));
            add(new SubForm("SPOR"));
            add(new SubForm("OCOR"));
            add(new SubForm("GWOR"));
            add(new SubForm("ECOR"));
            add(new SubListCounted<>("PRKZ", 4, new SubFormInt("PRKR")));
            add(new SubListCounted<>("COCT", 4, new ItemListing()));
            add(new AIDT());
            add(new SubList<>(new SubForm("PKID")));
            add(new KeywordSet());
            add(new SubForm("CNAM"));
            reposition("FULL");
            add(new SubStringPointer("SHRT", SubStringPointer.Files.STRINGS));
            add(new SubData("DATA"));
            forceExport("DATA");
            add(new DNAM());
            add(new SubList<>(new SubForm("PNAM")));
            add(new SubForm("HCLF"));
            add(new SubForm("ZNAM"));
            add(new SubForm("GNAM"));
            add(new SubData("NAM5"));
            add(new SubFloat("NAM6"));
            add(new SubFloat("NAM7"));
            add(new SubInt("NAM8"));
            add(new SubList<>(new SoundPackage()));
            add(new SubForm("CSCR"));
            add(new SubForm("DOFT"));
            add(new SubForm("SOFT"));
            add(new SubForm("DPLT"));
            add(new SubForm("CRIF"));
            add(new SubForm("FTST"));
            add(new SubRGB("QNAM"));
            add(new NAM9());
            add(new NAMA());
            add(new SubList<>(new TintLayer()));
        }
    };

    // Common Functions
    NPC_() {
        super();
        subRecords.setPrototype(NPC_proto);
    }

    /**
     * Takes in another NPC, and assumes all the information associated with the
     * input flags. It also unchecks the specific template flags on the
     * NPC.<br><br>
     * <p>
     * If the parameter NPC is templated to another NPC, this function will
     * recursively call in order to get the "correct" template information. If
     * during this recursive call the function encounters a Leveled List on the
     * template chain, then the function will skip assuming that flag type, and
     * instead mark the flag on the NPC (if it wasn't already).<br><br>
     * <p>
     * If no template flags remain checked after this function has run, then the
     * NPC's template reference is set to NULL.<br><br> This function makes a
     * deep copy of all templated info.
     *
     * @param otherNPC NPC FormID to assume info from.
     * @param flags    Types of information to assume. If none are given, then the
     *                 NPCs active flags will be assumed.
     */
    public void templateTo(NPC_ otherNPC, TemplateFlag... flags) {
        if (flags.length == 0) {
            ArrayList<TemplateFlag> flagsList = new ArrayList<>();
            for (TemplateFlag f : TemplateFlag.values()) {
                if (get(f)) {
                    flagsList.add(f);
                }
            }
            flags = flagsList.toArray(flags);
        }
        NPC_ dup = (NPC_) Ln.deepCopy(otherNPC);
        for (TemplateFlag f : flags) {
            if (templateToInternal(dup, f)) {
                set(f, false);
            }
        }

        // If NPC no longer has any template flags on, remove template.
        boolean templated = false;
        for (TemplateFlag f : TemplateFlag.values()) {
            if (get(f)) {
                templated = true;
                break;
            }
        }
        if (!templated) {
            setTemplate(FormID.NULL);
        }
    }

    /**
     * Takes in another NPC, and assumes all the information associated with the
     * input flags. It also unchecks the specific template flags on the
     * NPC.<br><br>
     * <p>
     * If the parameter NPC is templated to another NPC, this function will
     * recursively call in order to get the "correct" template information. If
     * during this recursive call the function encounters a Leveled List on the
     * template chain, then the function will skip assuming that flag type, and
     * instead mark the flag on the NPC (if it wasn't already).<br><br>
     * <p>
     * If no template flags remain checked after this function has run, then the
     * NPC's template reference is set to NULL.<br><br> This function makes a
     * deep copy of all templated info.
     *
     * @param npc
     * @param flags Types of information to assume. If none are given, then the
     *              NPCs active flags will be assumed.
     */
    public void templateTo(FormID npc, TemplateFlag... flags) {
        templateTo((NPC_) SPDatabase.getMajor(npc, GRUP_TYPE.NPC_), flags);
    }

    boolean templateToInternal(NPC_ otherNPC, TemplateFlag flag) {
        if (otherNPC == null) {
            return false;
        }
        if (otherNPC.get(flag)) {
            NPC_ otherNPCsTemplate = (NPC_) SPDatabase.getMajor(otherNPC.getTemplate(), GRUP_TYPE.NPC_);
            if (otherNPCsTemplate != null) {
                return templateToInternal(otherNPCsTemplate, flag);
            } else {
                return false;
            }
        } else {
            switch (flag) {
                case USE_TRAITS:
                    set(NPCFlag.Female, otherNPC.get(NPCFlag.Female));
                    setRace(otherNPC.getRace());
                    setSkin(otherNPC.getSkin());
                    setHeight(otherNPC.getHeight());
                    setWeight(otherNPC.getWeight());
                    setFarAwayModelSkin(otherNPC.getFarAwayModelSkin());
                    setVoiceType(otherNPC.getVoiceType());
                    getACBS().dispositionBase = otherNPC.getACBS().dispositionBase;
                    setDeathItem(otherNPC.getDeathItem());
                    set(NPCFlag.OppositeGenderAnims, otherNPC.get(NPCFlag.OppositeGenderAnims));
                    //Sound Tab
                    this.setSoundVolume(otherNPC.getSoundVolume());
                    this.setAudioTemplate(otherNPC.getAudioTemplate());
                    subRecords.add(otherNPC.subRecords.getSubList("CSDT"));
                    break;
                case USE_STATS:
                    getACBS().level = otherNPC.getACBS().level;
                    set(NPCFlag.PCLevelMult, otherNPC.get(NPCFlag.PCLevelMult));
                    set(NPCStat.MIN_CALC_LEVEL, otherNPC.get(NPCStat.MIN_CALC_LEVEL));
                    set(NPCStat.MAX_CALC_LEVEL, otherNPC.get(NPCStat.MAX_CALC_LEVEL));
                    getACBS().healthOffset = otherNPC.getACBS().healthOffset;
                    getACBS().magickaOffset = otherNPC.getACBS().magickaOffset;
                    getACBS().fatigueOffset = otherNPC.getACBS().fatigueOffset;
                    for (Skill s : Skill.NPC_Skills()) {
                        this.set(s, otherNPC.get(s));
                        this.setMod(s, otherNPC.getMod(s));
                    }
                    getACBS().speed = otherNPC.getACBS().speed;
                    getACBS().bleedout = otherNPC.getACBS().bleedout;
                    setNPCClass(otherNPC.getNPCClass());
                    break;
                case USE_FACTIONS:
                    this.clearFactions();
                    for (SubFormInt s : otherNPC.getFactions()) {
                        addFaction(s.getForm(), s.getNum());
                    }
                    this.setCrimeFaction(otherNPC.getCrimeFaction());
                    break;
                case USE_SPELL_LIST:
                    this.clearSpells();
                    for (FormID f : otherNPC.getSpells()) {
                        addSpell(f);
                    }
                    this.clearPerks();
                    for (SubFormInt s : otherNPC.getPerks()) {
                        addPerk(s.getForm(), s.getNum());
                    }
                    break;
                case USE_AI_DATA:
                    this.setAggression(otherNPC.getAggression());
                    this.setMood(otherNPC.getMood());
                    this.setConfidence(otherNPC.getConfidence());
                    this.setAssistance(otherNPC.getAssistance());
                    this.setMorality(otherNPC.getMorality());
                    this.setEnergy(otherNPC.getEnergy());
                    this.set(NPCFlag.AggroRadiusBehavior, otherNPC.get(NPCFlag.AggroRadiusBehavior));
                    this.setAggroWarn(otherNPC.getAggroWarn());
                    this.setAggroWarnAttack(otherNPC.getAggroWarnAttack());
                    this.setAggroAttack(otherNPC.getAggroAttack());
                    this.setCombatStyle(otherNPC.getCombatStyle());
                    this.setGiftFilter(otherNPC.getGiftFilter());
                    break;
                case USE_INVENTORY:
                    this.setDefaultOutfit(otherNPC.getDefaultOutfit());
                    this.setSleepingOutfit(otherNPC.getSleepingOutfit());
                    this.setGearedUpWeapons(otherNPC.getGearedUpWeapons());
                    this.clearItems();
                    for (ItemListing f : otherNPC.getItems()) {
                        this.addItem(new ItemListing(f.getForm(), f.getCount()));
                    }
                    break;
                case USE_AI_PACKAGES:
                    this.clearAIPackages();
                    for (FormID id : otherNPC.getAIPackages()) {
                        this.addAIPackage(id);
                    }
                    break;
                case USE_DEF_PACK_LIST:
                    this.setDefaultPackageList(otherNPC.getDefaultPackageList());
                    this.setSpectatorOverride(otherNPC.getSpectatorOverride());
                    this.setObserveDeadOverride(otherNPC.getObserveDeadOverride());
                    this.setGuardWornOverride(otherNPC.getGuardWornOverride());
                    this.setCombatOverride(otherNPC.getCombatOverride());
                    break;
                case USE_ATTACK_DATA:
                    this.setAttackDataRace(otherNPC.getAttackDataRace());
                    this.clearAttackPackages();
                    for (AttackPackage a : otherNPC.getAttackPackages()) {
                        this.addAttackPackage(a);
                    }

                    break;
                case USE_BASE_DATA:
                    this.setName(otherNPC.getName());
                    this.setShortName(otherNPC.getShortName());
                    this.set(NPCFlag.Essential, otherNPC.get(NPCFlag.Essential));
                    this.set(NPCFlag.Protected, otherNPC.get(NPCFlag.Protected));
                    this.set(NPCFlag.Respawn, otherNPC.get(NPCFlag.Respawn));
                    this.set(NPCFlag.Summonable, otherNPC.get(NPCFlag.Summonable));
                    this.set(NPCFlag.SimpleActor, otherNPC.get(NPCFlag.SimpleActor));
                    this.set(NPCFlag.DoesntAffectStealthMeter, otherNPC.get(NPCFlag.DoesntAffectStealthMeter));
                    break;
                case USE_KEYWORDS:
                    subRecords.add(otherNPC.getKeywordSet());
                    break;

            }
            return true;
        }
    }

    /**
     * Checks the NPC's template chain to see if a Leveled List is on it, and
     * returns it if found.<br><br> Flags can be specified if you only want to
     * return a Leveled List if AT LEAST one of those flags is checked.
     *
     * @param templateFlagsToCheck Flags to consider. If none are given, then
     *                             all considered.
     * @return Leveled List on the template chain, if the NPC has one of the
     * flags, and a Leveled List exists on its template chain.
     */
    public LVLN isTemplatedToLList(NPC_.TemplateFlag... templateFlagsToCheck) {
        return NiftyFunc.isTemplatedToLList(getForm(), templateFlagsToCheck, 0);
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("NPC_");
    }

    @Override
    Record getNew() {
        return new NPC_();
    }

    // Get/Set methods
    ACBS getACBS() {
        return (ACBS) subRecords.get("ACBS");
    }


    public ScriptPackage getScriptPackage() {
        return subRecords.getScripts();
    }

    /**
     * @param flag Template flag to set.
     * @param on   What to set the template flag to.
     */
    public void set(TemplateFlag flag, boolean on) {
        getACBS().templateFlags.set(flag.value, on);
    }

    // Enums

    /**
     * @param flag Template flag to get.
     * @return Template flag's status.
     */
    public boolean get(TemplateFlag flag) {
        return getACBS().templateFlags.get(flag.value);
    }

    /**
     * Returns the group of factions assigned to the NPC. Changing this group by
     * adding or removing factions will affect that NPC.
     *
     * @return The group of factions assigned to the NPC.
     * @see //SubRecordList
     */
    public ArrayList<SubFormInt> getFactions() {
        return subRecords.getSubList("SNAM").toPublic();
    }

    /**
     * @param factionRef FormID of the faction to add the NPC into.
     * @param rank       Rank within the faction to set the NPC at.
     * @return True if faction was added.
     */
    public boolean addFaction(FormID factionRef, int rank) {
        return subRecords.getSubList("SNAM").add(new SubFormInt("SNAM", factionRef, rank));
    }

    /**
     * @param factionRef FormID matching the FactionRef record to remove.
     * @return True if faction was removed.
     */
    public boolean removeFaction(FormID factionRef) {
        return subRecords.getSubList("SNAM").remove(new SubFormInt("SNAM", factionRef, 0));
    }


    public void clearFactions() {
        subRecords.getSubList("SNAM").clear();
    }


    public ArrayList<SubFormInt> getPerks() {
        return subRecords.getSubList("PRKR").toPublic();
    }

    /**
     * @param perkRef
     * @param rank
     */
    public void addPerk(FormID perkRef, int rank) {
        subRecords.getSubList("PRKR").add(new SubFormInt("PRKR", perkRef, rank));
    }

    /**
     * @param perkRef
     * @return
     */
    public boolean removePerk(FormID perkRef) {
        return subRecords.getSubList("PRKR").remove(new SubFormInt("PRKR", perkRef, 0));
    }


    public void clearPerks() {
        subRecords.getSubList("PRKR").clear();
    }

    AIDT getAIDT() {
        return (AIDT) subRecords.get("AIDT");
    }

    // Special functions

    /**
     * @param flag NPCFlag to get.
     * @return NPCFlag's status.
     */
    public boolean get(NPCFlag flag) {
        switch (flag) {
            case AggroRadiusBehavior:
                return getAIDT().aggroRadiusBehavior;
            default:
                return getACBS().ACBSflags.get(flag.value);
        }
    }

    /**
     * @param flag NPCFlag to set.
     * @param on   What to set the NPCFlag to.
     */
    public void set(NPCFlag flag, boolean on) {
        switch (flag) {
            case AggroRadiusBehavior:
                getAIDT().aggroRadiusBehavior = on;
                break;
            default:
                getACBS().ACBSflags.set(flag.value, on);
        }
    }

    DNAM getDNAM() {
        return (DNAM) subRecords.get("DNAM");
    }

    /**
     * Returns the base value of the skill represented by the given enum.
     *
     * @param skill The enum of the skill to return the base value of.
     * @return The base value of the skill represented by the given enum.
     * @see //Skills
     */
    public int get(Skill skill) {
        return getDNAM().getSkillBase(skill);
    }

    /**
     * Sets the base value of the skill represented by the given enum.
     *
     * @param skill The enum of the skill to set to the value.
     * @param value Sets the base value of the skill to this value.
     * @see //Skills
     */
    public void set(Skill skill, int value) {
        if (value < 0) {
            value = 0;
        }
        getDNAM().setSkillBase(skill, value);
    }

    /**
     * Returns the mod value of the skill represented by the given enum.
     *
     * @param skill The enum of the skill to return the mod value of.
     * @return The mod value of the skill represented by the given enum.
     * @see //Skills
     */
    public int getMod(Skill skill) {
        return getDNAM().getSkillMod(skill);
    }

    /**
     * Sets the mod value of the skill represented by the given enum.
     *
     * @param skill The enum of the skill to set to the value.
     * @param value Sets the mod value of the skill to this value.
     * @see //Skills
     */
    public void setMod(Skill skill, int value) {
        if (value < 0) {
            value = 0;
        }
        getDNAM().setSkillMod(skill, value);
    }


    public Aggression getAggression() {
        return getAIDT().aggression;
    }

    /**
     * @param level
     */
    public void setAggression(Aggression level) {
        getAIDT().aggression = level;
    }


    public Confidence getConfidence() {
        return getAIDT().confidence;
    }

    /**
     * @param level
     */
    public void setConfidence(Confidence level) {
        getAIDT().confidence = level;
    }


    public Morality getMorality() {
        return getAIDT().morality;
    }

    /**
     * @param level
     */
    public void setMorality(Morality level) {
        getAIDT().morality = level;
    }


    public Assistance getAssistance() {
        return getAIDT().assistance;
    }

    /**
     * @param level
     */
    public void setAssistance(Assistance level) {
        getAIDT().assistance = level;
    }

    /**
     * Returns the value of the stat data represented by the given enum.
     *
     * @param stat The enum of the stat data to return.
     * @return The value of the stat data represented by the given enum.
     * @see //Stat_Values
     */
    public int get(NPCStat stat) {
        switch (stat) {
            case SPELL_POINTS_BASE:
                return getACBS().magickaOffset;
            case FATIGUE_BASE:
                return getACBS().fatigueOffset;
            case LEVEL:
                return getACBS().level;
            case MIN_CALC_LEVEL:
                return getACBS().minCalcLevel;
            case MAX_CALC_LEVEL:
                return getACBS().maxCalcLevel;
            case SPEED_MULT:
                return getACBS().speed;
            case DISPOSITION_BASE:
                return getACBS().dispositionBase;
            default:
                return -1;
        }
    }

    /**
     * Sets the value of the stat data represented by the given enum.
     *
     * @param stat  The enum of the stat data to set to the value.
     * @param value Sets the value of the stat data to this value.
     * @see //Stat_Values
     */
    public void set(NPCStat stat, int value) {
        if (value < 0) {
            value = 0;
        }
        switch (stat) {
            case SPELL_POINTS_BASE:
                getACBS().magickaOffset = value;
                break;
            case FATIGUE_BASE:
                getACBS().fatigueOffset = value;
                break;
            case LEVEL:
                getACBS().level = value;
                break;
            case MIN_CALC_LEVEL:
                getACBS().minCalcLevel = value;
                break;
            case MAX_CALC_LEVEL:
                getACBS().maxCalcLevel = value;
                break;
            case SPEED_MULT:
                getACBS().speed = value;
                break;
            case DISPOSITION_BASE:
                getACBS().dispositionBase = value;
                break;
        }
    }

    /**
     * The item to be added to the NPC's inventory upon death.
     *
     * @return
     */
    public FormID getDeathItem() {
        return subRecords.getSubForm("INAM").getForm();
    }

    /**
     * The item to be added to the NPC's inventory upon death.
     *
     * @param deathItemRef
     */
    public void setDeathItem(FormID deathItemRef) {
        subRecords.setSubForm("INAM", deathItemRef);
    }

    /**
     * The voice type of the NPC.
     *
     * @return
     */
    public FormID getVoiceType() {
        return subRecords.getSubForm("VTCK").getForm();
    }

    /**
     * The voice type of the NPC.
     *
     * @param voiceTypeRef
     */
    public void setVoiceType(FormID voiceTypeRef) {
        subRecords.setSubForm("VTCK", voiceTypeRef);
    }


    public FormID getTemplate() {
        return subRecords.getSubForm("TPLT").getForm();
    }

    /**
     * @param templateRef
     */
    public void setTemplate(FormID templateRef) {
        subRecords.setSubForm("TPLT", templateRef);
    }

    /**
     * @return True if NPC has a template actor
     */
    public boolean isTemplated() {
        return !getTemplate().equals(FormID.NULL);
    }


    public FormID getRace() {
        return subRecords.getSubForm("RNAM").getForm();
    }

    /**
     * @param raceRef
     */
    public void setRace(FormID raceRef) {
        subRecords.setSubForm("RNAM", raceRef);
    }


    public ArrayList<FormID> getSpells() {
        return subRecords.getSubList("SPLO").toPublic();
    }

    /**
     * @param spellReference FormID of the spell to give to the NPC.
     * @return True if spell was added.
     */
    public boolean addSpell(FormID spellReference) {
        return subRecords.getSubList("SPLO").add(spellReference);
    }

    /**
     * Removes a spell from the NPC. If a spell with this FormID does not exist,
     * this spell does nothing.
     *
     * @param spellReference FormID of the spell to remove from the NPC
     * @return True if spell was removed.
     */
    public boolean removeSpell(FormID spellReference) {
        return subRecords.getSubList("SPLO").remove(spellReference);
    }


    public void clearSpells() {
        subRecords.getSubList("SPLO").clear();
    }

    /**
     * @param itemReference
     * @param count
     * @return
     */
    public boolean addItem(FormID itemReference, int count) {
        return subRecords.getSubList("CNTO").add(new ItemListing(itemReference, count));
    }

    /**
     * @param item
     * @return
     */
    public boolean addItem(ItemListing item) {
        return subRecords.getSubList("CNTO").add(item);
    }

    /**
     * @param itemReference
     * @return
     */
    public boolean removeItem(FormID itemReference) {
        return subRecords.getSubList("CNTO").remove(new ItemListing(itemReference));
    }


    public void clearItems() {
        subRecords.getSubList("CNTO").clear();
    }


    public ArrayList<ItemListing> getItems() {
        return subRecords.getSubList("CNTO").toPublic();
    }

    public ArrayList<FormID> getItemForms() {
        ArrayList<ItemListing> items = getItems();
        ArrayList<FormID> out = new ArrayList<>(items.size());
        for (ItemListing item : items) {
            out.add(item.getForm());
        }
        return out;
    }

    /**
     * Adds an AI package with the FormID to the NPC.
     *
     * @param aiPackageReference
     * @return True if AI package was added.
     */
    public boolean addAIPackage(FormID aiPackageReference) {
        return subRecords.getSubList("PKID").add(aiPackageReference);
    }

    /**
     * @param aiPackageReference
     * @return True if AI package was removed.
     */
    public boolean removeAIPackage(FormID aiPackageReference) {
        return subRecords.getSubList("PKID").remove(aiPackageReference);
    }

    /**
     * @return SubRecordList of AI packages.
     */
    public ArrayList<FormID> getAIPackages() {
        return subRecords.getSubList("PKID").toPublic();
    }


    public void clearAIPackages() {
        subRecords.getSubList("PKID").clear();
    }


    public FormID getNPCClass() {
        return subRecords.getSubForm("CNAM").getForm();
    }

    /**
     * @param classReference
     */
    public void setNPCClass(FormID classReference) {
        subRecords.setSubForm("CNAM", classReference);
    }


    public FormID getHairColor() {
        return subRecords.getSubForm("HCLF").getForm();
    }

    /**
     * @param hairColorRef
     */
    public void setHairColor(FormID hairColorRef) {
        subRecords.setSubForm("HCLF", hairColorRef);
    }


    public FormID getSkin() {
        return subRecords.getSubForm("WNAM").getForm();
    }

    /**
     * @param wornArmorRef
     */
    public void setSkin(FormID wornArmorRef) {
        subRecords.setSubForm("WNAM", wornArmorRef);
    }


    public FormID getAttackDataRace() {
        return subRecords.getSubForm("ATKR").getForm();
    }

    /**
     * @param attackRaceRef
     */
    public void setAttackDataRace(FormID attackRaceRef) {
        subRecords.setSubForm("ATKR", attackRaceRef);
    }


    public FormID getDefaultOutfit() {
        return subRecords.getSubForm("DOFT").getForm();
    }

    /**
     * @param defaultOutfitRef
     */
    public void setDefaultOutfit(FormID defaultOutfitRef) {
        subRecords.setSubForm("DOFT", defaultOutfitRef);
    }


    public FormID getSleepingOutfit() {
        return subRecords.getSubForm("SOFT").getForm();
    }

    /**
     * @param sleepingOutfitRef
     */
    public void setSleepingOutfit(FormID sleepingOutfitRef) {
        subRecords.setSubForm("SOFT", sleepingOutfitRef);
    }


    public FormID getCombatStyle() {
        return subRecords.getSubForm("ZNAM").getForm();
    }

    /**
     * @param combatRef
     */
    public void setCombatStyle(FormID combatRef) {
        subRecords.setSubForm("ZNAM", combatRef);
    }


    public FormID getCrimeFaction() {
        return subRecords.getSubForm("CRIF").getForm();
    }

    /**
     * @param crimeFactionRef
     */
    public void setCrimeFaction(FormID crimeFactionRef) {
        subRecords.setSubForm("CRIF", crimeFactionRef);
    }


    public FormID getFeatureSet() {
        return subRecords.getSubForm("FTST").getForm();
    }

    /**
     * @param headPartsRef
     */
    public void setFeatureSet(FormID headPartsRef) {
        subRecords.setSubForm("FTST", headPartsRef);
    }


    public FormID getAudioTemplate() {
        return subRecords.getSubForm("CSCR").getForm();
    }

    /**
     * @param audioTemplateRef
     */
    public void setAudioTemplate(FormID audioTemplateRef) {
        subRecords.setSubForm("CSCR", audioTemplateRef);
    }


    public FormID getDefaultPackageList() {
        return subRecords.getSubForm("DPLT").getForm();
    }

    /**
     * @param list
     */
    public void setDefaultPackageList(FormID list) {
        subRecords.setSubForm("DPLT", list);
    }


    public float getHeight() {
        return subRecords.getSubFloat("NAM6").get();
    }

    /**
     * @param height
     */
    public void setHeight(float height) {
        subRecords.setSubFloat("NAM6", height);
    }


    public float getWeight() {
        return subRecords.getSubFloat("NAM7").get();
    }

    /**
     * @param weight
     */
    public void setWeight(float weight) {
        subRecords.setSubFloat("NAM7", weight);
    }


    public FormID getFarAwayModelSkin() {
        return subRecords.getSubForm("ANAM").getForm();
    }

    /**
     * @param id
     */
    public void setFarAwayModelSkin(FormID id) {
        subRecords.setSubForm("ANAM", id);
    }


    public float getFarAwayModelDistance() {
        return getDNAM().farAwayDistance;
    }

    /**
     * @param dist
     */
    public void setFarAwayModelDistance(float dist) {
        getDNAM().farAwayDistance = dist;
    }


    public int getHealthOffset() {
        return getACBS().healthOffset;
    }

    /**
     * @param value
     */
    public void setHealthOffset(int value) {
        getACBS().healthOffset = value;
    }


    public int getMagickaOffset() {
        return getACBS().magickaOffset;
    }

    /**
     * @param value
     */
    public void setMagickaOffset(int value) {
        getACBS().magickaOffset = value;
    }


    public int getFatigueOffset() {
        return getACBS().fatigueOffset;
    }

    /**
     * @param value
     */
    public void setFatigueOffset(int value) {
        getACBS().fatigueOffset = value;
    }


    public Mood getMood() {
        return getAIDT().mood;
    }

    /**
     * @param value
     */
    public void setMood(Mood value) {
        getAIDT().mood = value;
    }


    public int getEnergy() {
        return getAIDT().energy;
    }

    /**
     * @param energy
     */
    public void setEnergy(int energy) {
        getAIDT().energy = energy;
    }


    public int getAggroWarn() {
        return getAIDT().aggroWarn;
    }

    /**
     * @param aggro
     */
    public void setAggroWarn(int aggro) {
        getAIDT().aggroWarn = aggro;
    }


    public int getAggroWarnAttack() {
        return getAIDT().aggroWarnAttack;
    }

    /**
     * @param aggro
     */
    public void setAggroWarnAttack(int aggro) {
        getAIDT().aggroWarnAttack = aggro;
    }


    public int getAggroAttack() {
        return getAIDT().aggroAttack;
    }

    /**
     * @param aggro
     */
    public void setAggroAttack(int aggro) {
        getAIDT().aggroAttack = aggro;
    }


    public FormID getGiftFilter() {
        return subRecords.getSubForm("ACBS").getForm();
    }

    /**
     * @param id
     */
    public void setGiftFilter(FormID id) {
        subRecords.setSubForm("GNAM", id);
    }


    public int getGearedUpWeapons() {
        return getDNAM().gearedUpWeapons;
    }

    /**
     * @param value
     */
    public void setGearedUpWeapons(int value) {
        getDNAM().gearedUpWeapons = value;
    }


    public FormID getSpectatorOverride() {
        return subRecords.getSubForm("SPOR").getForm();
    }

    /**
     * @param list
     */
    public void setSpectatorOverride(FormID list) {
        subRecords.setSubForm("SPOR", list);
    }


    public FormID getObserveDeadOverride() {
        return subRecords.getSubForm("OCOR").getForm();
    }

    /**
     * @param list
     */
    public void setObserveDeadOverride(FormID list) {
        subRecords.setSubForm("OCOR", list);
    }


    public FormID getGuardWornOverride() {
        return subRecords.getSubForm("GWOR").getForm();
    }

    /**
     * @param list
     */
    public void setGuardWornOverride(FormID list) {
        subRecords.setSubForm("GWOR", list);
    }


    public FormID getCombatOverride() {
        return subRecords.getSubForm("ECOR").getForm();
    }

    /**
     * @param list
     */
    public void setCombatOverride(FormID list) {
        subRecords.setSubForm("ECOR", list);
    }


    public String getShortName() {
        return subRecords.getSubStringPointer("SHRT").print();
    }

    /**
     * @param alias
     */
    public void setShortName(String alias) {
        subRecords.setSubStringPointer("SHRT", alias);
    }


    public SoundVolume getSoundVolume() {
        return SoundVolume.values()[subRecords.getSubInt("NAM8").get()];
    }

    /**
     * @param vol
     */
    public void setSoundVolume(SoundVolume vol) {
        subRecords.setSubInt("NAM8", vol.ordinal());
    }

    NAM9 getNAM9() {
        return (NAM9) subRecords.get("NAM9");
    }

    /**
     * @param part
     * @param value
     */
    public void setFaceValue(FacePart part, float value) {
        switch (part) {
            case NoseLongShort:
                getNAM9().noseLong = value;
                break;
            case NoseUpDown:
                getNAM9().noseUp = value;
                break;
            case JawUpDown:
                getNAM9().jawUp = value;
                break;
            case JawNarrowWide:
                getNAM9().jawWide = value;
                break;
            case JawForwardBack:
                getNAM9().jawForward = value;
                break;
            case CheeksUpDown:
                getNAM9().cheekUp = value;
                break;
            case CheeksForwardBack:
                getNAM9().cheekForward = value;
                break;
            case EyesUpDown:
                getNAM9().eyeUp = value;
                break;
            case EyesInOut:
                getNAM9().eyeIn = value;
                break;
            case BrowsUpDown:
                getNAM9().browUp = value;
                break;
            case BrowsForwardBack:
                getNAM9().browForward = value;
                break;
            case LipsUpDown:
                getNAM9().lipUp = value;
                break;
            case LipsInOut:
                getNAM9().lipIn = value;
                break;
            case ChinThinWide:
                getNAM9().chinWide = value;
                break;
            case ChinUpDown:
                getNAM9().chinUp = value;
                break;
            case ChinOverbite:
                getNAM9().chinOverbite = value;
                break;
            case EyesForwardBack:
                getNAM9().eyesForward = value;
                break;
        }
        getNAM9().valid = true;
    }

    /**
     * @param part
     * @return
     */
    public float getFaceValue(FacePart part) {
        switch (part) {
            case NoseLongShort:
                return getNAM9().noseLong;
            case NoseUpDown:
                return getNAM9().noseUp;
            case JawUpDown:
                return getNAM9().jawUp;
            case JawNarrowWide:
                return getNAM9().jawWide;
            case JawForwardBack:
                return getNAM9().jawForward;
            case CheeksUpDown:
                return getNAM9().cheekUp;
            case CheeksForwardBack:
                return getNAM9().cheekForward;
            case EyesUpDown:
                return getNAM9().eyeUp;
            case EyesInOut:
                return getNAM9().eyeIn;
            case BrowsUpDown:
                return getNAM9().browUp;
            case BrowsForwardBack:
                return getNAM9().browForward;
            case LipsUpDown:
                return getNAM9().lipUp;
            case LipsInOut:
                return getNAM9().lipIn;
            case ChinThinWide:
                return getNAM9().chinWide;
            case ChinUpDown:
                return getNAM9().chinUp;
            case ChinOverbite:
                return getNAM9().chinOverbite;
            default:
                return getNAM9().eyesForward;
        }
    }


    public ArrayList<TintLayer> getTinting() {
        return subRecords.getSubList("TINI").collection;
    }

    /**
     * @param tinting
     * @return
     */
    public boolean addTinting(TintLayer tinting) {
        return subRecords.getSubList("TINI").add(tinting);
    }

    /**
     * @param tinting
     * @return
     */
    public boolean removeTinting(TintLayer tinting) {
        return subRecords.getSubList("TINI").remove(tinting);
    }


    public void clearTinting() {
        subRecords.getSubList("TINI").clear();
    }


    public ArrayList<SoundPackage> getSounds() {
        return subRecords.getSubList("CSDT").collection;
    }

    /**
     * @param sounds
     * @return
     */
    public boolean addSoundPackage(SoundPackage sounds) {
        return subRecords.getSubList("CSDT").add(sounds);
    }

    /**
     * @param sounds
     * @return
     */
    public boolean removeSoundPackage(SoundPackage sounds) {
        return subRecords.getSubList("CSDT").remove(sounds);
    }


    public void clearSoundPackages() {
        subRecords.getSubList("CSDT").clear();
    }

    /**
     * @param color
     * @return
     */
    public float getFaceTint(RGB color) {
        return subRecords.getSubRGB("QNAM").get(color);
    }

    /**
     * @param color
     * @param value
     */
    public void setFaceTint(RGB color, float value) {
        subRecords.setSubRGB("QNAM", color, value);
    }

    NAMA getNAMA() {
        return (NAMA) subRecords.get("NAMA");
    }


    public int getNosePreset() {
        return getNAMA().nose;
    }

    /**
     * @param val
     */
    public void setNosePreset(int val) {
        getNAMA().nose = val;
        getNAMA().valid = true;
    }


    public int getEyePreset() {
        return getNAMA().eyes;
    }

    /**
     * @param val
     */
    public void setEyePreset(int val) {
        NAMA n = getNAMA();
        n.eyes = val;
        n.valid = true;
    }


    public int getMouthPreset() {
        return getNAMA().mouth;
    }

    /**
     * @param val
     */
    public void setMouthPreset(int val) {
        NAMA n = getNAMA();
        n.mouth = val;
        n.valid = true;
    }


    public ArrayList<FormID> getHeadParts() {
        return subRecords.getSubList("PNAM").toPublic();
    }

    /**
     * @param pnam
     */
    public void addHeadPart(FormID pnam) {
        subRecords.getSubList("PNAM").add(pnam);
    }

    /**
     * @param pnam
     */
    public void removeHeadPart(FormID pnam) {
        subRecords.getSubList("PNAM").remove(pnam);
    }


    public void clearHeadParts() {
        subRecords.getSubList("PNAM").clear();
    }


    public ArrayList<AttackPackage> getAttackPackages() {
        return subRecords.getSubList("ATKD").toPublic();
    }

    /**
     * @param attack
     * @return
     */
    public boolean addAttackPackage(AttackPackage attack) {
        return subRecords.getSubList("ATKD").add(attack);
    }

    /**
     * @param attack
     * @return
     */
    public boolean removeAttackPackage(AttackPackage attack) {
        return subRecords.getSubList("ATKD").remove(attack);
    }


    public void clearAttackPackages() {
        subRecords.getSubList("ATKD").clear();
    }


    public KeywordSet getKeywordSet() {
        return subRecords.getKeywords();
    }

    SubList getCOCT() {
        return subRecords.getSubList("COCT");
    }

    SubList getSNAM() {
        return subRecords.getSubList("SNAM");
    }

    SubList getPRKZ() {
        return subRecords.getSubList("PRKZ");
    }

    @Override
    public MajorRecord merge(MajorRecord no, MajorRecord bo) {
        super.merge(no, bo);
        NPC_ n = this;
        if (!(no == null && bo == null && (no instanceof NPC_) && (bo instanceof NPC_))) {
            NPC_ nn = (NPC_) no;
            NPC_ bn = (NPC_) bo;
            SubRecords sList = n.subRecords;
            SubRecords nsList = nn.subRecords;
            SubRecords bsList = bn.subRecords;
            for (SubRecord s : sList) {
                if (s.equals(n.getCOCT())) {
                    n.getCOCT().mergeList(nn.getCOCT(), bn.getCOCT());
                } else if (s.equals(n.getSNAM())) {
                    n.getSNAM().mergeList(nn.getSNAM(), bn.getSNAM());
                } else if (s.equals(n.getPRKZ())) {
                    n.getPRKZ().mergeListSFISpecial(nn.getPRKZ(), bn.getPRKZ());
                } else {
                    s.merge(nsList.get(s.getType()), bsList.get(s.getType()));
                }
            }
        }
        return n;
    }

    /**
     * Enum representing the various stats of the NPC
     */
    public enum NPCStat {

        /**
         * Determines the initial disposition of the NPC to the player.
         */
        DISPOSITION_BASE,
        /**
         * Determines the fatigue of the NPC.<br> Not confirmed whether this is
         * actually used.
         */
        FATIGUE_BASE,
        /**
         * Level of the NPC.
         */
        LEVEL,
        /**
         * Min level when calculated for PC offset.
         */
        MIN_CALC_LEVEL,
        /**
         * Max level when calculated for PC offset.
         */
        MAX_CALC_LEVEL,
        /**
         * Determines the speed of the NPC.
         */
        SPEED_MULT,
        /**
         * Determines the initial mana of the NPC.<br> Not confirmed whether
         * this is actually used.
         */
        SPELL_POINTS_BASE
    }


    public enum SoundLocation {

        /**
         *
         */
        Idle,
        /**
         *
         */
        Aware,
        /**
         *
         */
        Attack,
        /**
         *
         */
        Hit,
        /**
         *
         */
        Death,
        /**
         *
         */
        Weapon,
        /**
         *
         */
        MovementLoop,
        /**
         *
         */
        ConsciousLoop
    }

    /**
     * The template flags telling the NPC which parts to use from its target
     * template.
     */
    public enum TemplateFlag {

        /**
         * Flag to use the traits page of its template.
         */
        USE_TRAITS(0),
        /**
         *
         */
        USE_STATS(1),
        /**
         *
         */
        USE_FACTIONS(2),
        /**
         *
         */
        USE_SPELL_LIST(3),
        /**
         *
         */
        USE_AI_DATA(4),
        /**
         *
         */
        USE_AI_PACKAGES(5),
        /**
         *
         */
        USE_BASE_DATA(7),
        /**
         *
         */
        USE_INVENTORY(8),
        /**
         *
         */
        USE_SCRIPTS(9),
        /**
         *
         */
        USE_DEF_PACK_LIST(10),
        /**
         *
         */
        USE_ATTACK_DATA(11),
        /**
         *
         */
        USE_KEYWORDS(12);
        final int value;

        TemplateFlag(int in) {
            value = in;
        }
    }

    /**
     * Collection of various flags applied to the NPC (ACBS flags)
     */
    public enum NPCFlag {

        /**
         *
         */
        Female(0),
        /**
         *
         */
        Essential(1),
        /**
         *
         */
        IsCharGenFacePreset(2),
        /**
         *
         */
        Respawn(3),
        /**
         *
         */
        AutoCalcStats(4),
        /**
         *
         */
        Unique(5),
        /**
         *
         */
        DoesntAffectStealthMeter(6),
        /**
         *
         */
        PCLevelMult(7),
        /**
         *
         */
        UseTemplate(8),
        /**
         *
         */
        Unknown_9(9),
        /**
         *
         */
        Unknown_10(10),
        /**
         *
         */
        Protected(11),
        /**
         *
         */
        Unknown_12(12),
        /**
         *
         */
        Unknown_13(13),
        /**
         *
         */
        Summonable(14),
        /**
         *
         */
        Unknown_15(15),
        /**
         *
         */
        DoesntBleed(16),
        /**
         *
         */
        Unknown_17(17),
        /**
         *
         */
        BleedoutOverride(18),
        /**
         *
         */
        OppositeGenderAnims(19),
        /**
         *
         */
        SimpleActor(20),
        /**
         *
         */
        LoopedScript(21),
        /**
         *
         */
        Unknown_22(22),
        /**
         *
         */
        Unknown_23(23),
        /**
         *
         */
        Unknown_24(24),
        /**
         *
         */
        Unknown_25(25),
        /**
         *
         */
        Unknown_26(26),
        /**
         *
         */
        Unknown_27(27),
        /**
         *
         */
        LoopedAudio(28),
        /**
         *
         */
        IsGhost(29),
        /**
         *
         */
        Unknown_30(30),
        /**
         *
         */
        Invulnerable(31),
        /**
         *
         */
        AggroRadiusBehavior(-1);
        final int value;

        NPCFlag(int value) {
            this.value = value;
        }
    }


    public enum Aggression {

        /**
         *
         */
        Unaggressive,
        /**
         *
         */
        Aggressive,
        /**
         *
         */
        VeryAggressive,
        /**
         *
         */
        Frenzied
    }


    public enum Assistance {

        /**
         *
         */
        HelpsNobody,
        /**
         *
         */
        HelpsAllies,
        /**
         *
         */
        HelpsFriends
    }


    public enum Morality {

        /**
         *
         */
        AnyCrime,
        /**
         *
         */
        ViolenceAgainstEnemies,
        /**
         *
         */
        PropertyCrimeOnly,
        /**
         *
         */
        NoCrime
    }


    public enum Confidence {

        /**
         *
         */
        Cowardly,
        /**
         *
         */
        Cautious,
        /**
         *
         */
        Average,
        /**
         *
         */
        Brave,
        /**
         *
         */
        Foolhardy
    }


    public enum Mood {

        /**
         *
         */
        Neutral,
        /**
         *
         */
        Angry,
        /**
         *
         */
        Fear,
        /**
         *
         */
        Happy,
        /**
         *
         */
        Sad,
        /**
         *
         */
        Surprised,
        /**
         *
         */
        Puzzled,
        /**
         *
         */
        Disgusted,
    }


    public enum FacePart {

        /**
         *
         */
        NoseLongShort,
        /**
         *
         */
        NoseUpDown,
        /**
         *
         */
        JawUpDown,
        /**
         *
         */
        JawNarrowWide,
        /**
         *
         */
        JawForwardBack,
        /**
         *
         */
        CheeksUpDown,
        /**
         *
         */
        CheeksForwardBack,
        /**
         *
         */
        EyesUpDown,
        /**
         *
         */
        EyesInOut,
        /**
         *
         */
        BrowsUpDown,
        /**
         *
         */
        BrowsInOut,
        /**
         *
         */
        BrowsForwardBack,
        /**
         *
         */
        LipsUpDown,
        /**
         *
         */
        LipsInOut,
        /**
         *
         */
        ChinThinWide,
        /**
         *
         */
        ChinUpDown,
        /**
         *
         */
        ChinOverbite,
        /**
         *
         */
        EyesForwardBack
    }

    /**
     * Sound package containing sounds to play for different actions
     */
    public static class SoundPackage extends SubShell implements Serializable {

        static final SubPrototype soundPackageProto = new SubPrototype() {
            @Override
            protected void addRecords() {
                add(new SubInt("CSDT"));
                add(new SubList<>(new SoundPair()));
            }
        };

        SoundPackage(SoundLocation location) {
            this();
            setLocation(location);
        }

        SoundPackage() {
            super(soundPackageProto);
        }

        @Override
        SubRecord getNew(String type_) {
            return new SoundPackage();
        }

        /**
         * @return
         */
        public SoundLocation getLocation() {
            SubInt csdt = subRecords.getSubInt("CSDT");
            if (csdt.get() < SoundLocation.values().length) {
                return SoundLocation.values()[csdt.get()];
            } else {
                return SoundLocation.Idle;
            }
        }

        /**
         * @param loc
         */
        public final void setLocation(SoundLocation loc) {
            subRecords.setSubInt("CSDT", loc.ordinal());
        }

        /**
         * @return
         */
        public ArrayList<SoundPair> getSoundPairs() {
            return new ArrayList<>(subRecords.getSubList("CSDI").toPublic());
        }

        /**
         * @param pair
         * @deprecated modifying the ArrayList will now directly affect the
         * record.
         */
        public void addSoundPair(SoundPair pair) {
            subRecords.getSubList("CSDI").add(pair);
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            SoundPackage o = this;
            if (!(no == null && bo == null && (no instanceof SoundPackage) && (bo instanceof SoundPackage))) {
                final SoundPackage na = (SoundPackage) no;
                final SoundPackage ba = (SoundPackage) bo;
                if (!o.equals(na) && !na.equals(ba)) {
                    o = na;
                    if (Merger.fullLogging) {
                        Merger.logMerge(getType(), o.toString());
                    }
                }
            }
            return o;
        }
    }

    /**
     * Pair containing sound to play and chance to play it
     */
    public static class SoundPair extends SubShell {

        static final SubPrototype soundPairProto = new SubPrototype() {
            @Override
            protected void addRecords() {
                add(new SubForm("CSDI"));
                forceExport("CSDI");
                add(new SubInt("CSDC", 1));
            }
        };

        SoundPair() {
            super(soundPairProto);
        }

        /**
         * @param sound
         * @param chance
         */
        public SoundPair(FormID sound, int chance) {
            this();
            setChance(chance);
            setSound(sound);
        }

        /**
         * @return
         */
        public int getChance() {
            return subRecords.getSubInt("CSDC").get();
        }

        /**
         * @param chance
         */
        public final void setChance(int chance) {
            if (chance < 0) {
                chance = 0;
            } else if (chance > 100) {
                chance = 100;
            }

            subRecords.setSubInt("CSDC", chance);
        }

        /**
         * @return
         */
        public FormID getSound() {
            return subRecords.getSubForm("CSDI").getForm();
        }

        /**
         * @param sound
         */
        public final void setSound(FormID sound) {
            subRecords.setSubForm("CSDI", sound);
        }

        @Override
        SubRecord getNew(String type) {
            return new SoundPair();
        }
    }


    public static class TintLayer extends SubShell implements Serializable {

        static final SubPrototype tintPrototype = new SubPrototype() {
            @Override
            protected void addRecords() {
                add(new SubInt("TINI", 2));
                add(new SubRGBshort("TINC"));
                add(new SubInt("TINV"));
                add(new SubIntSigned("TIAS", 2));
            }
        };

        TintLayer() {
            super(tintPrototype);
        }

        /**
         * @param tini Tint Index
         */

        public TintLayer(int tini) {
            super(tintPrototype);
            setIndex(tini);
        }

        @Override
        SubRecord getNew(String type_) {
            return new TintLayer();
        }

        /**
         * @return
         */
        public int getIndex() {
            return subRecords.getSubInt("TINI").get();
        }

        /**
         * @param in
         */
        public void setIndex(int in) {
            subRecords.setSubInt("TINI", in);
        }

        /**
         * @param color
         * @param value
         */
        public void setColor(RGBA color, short value) {
            subRecords.setSubRGBshort("TINC", color, value);
        }

        /**
         * @param color
         * @return
         */
        public short getColor(RGBA color) {
            return subRecords.getSubRGBshort("TINC").get(color);
        }

        /**
         * @return
         */
        public float getInterpolation() {
            return ((float) subRecords.getSubInt("TINV").get()) / 100.0f;
        }

        /**
         * @param value
         */
        public void setInterpolation(float value) {
            int val = (int) (value * 100);
            subRecords.setSubInt("TINV", val);
        }

        /**
         * @return
         */
        public int getPreset() {
            return subRecords.getSubInt("TIAS").get();
        }

        /**
         * @param value
         */
        public void setPreset(int value) {
            subRecords.setSubInt("TIAS", value);
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            TintLayer o = this;
            if (!(no == null && bo == null && (no instanceof TintLayer) && (bo instanceof TintLayer))) {
                final TintLayer nd = (TintLayer) no;
                final TintLayer bd = (TintLayer) bo;
                if (!o.equals(nd) && !no.equals(bd)) {
                    o = nd;
                    if (Merger.fullLogging) {
                        Merger.logMerge(getType(), o.toString());
                    }
                }
            }
            return o;
        }
    }

    static class DNAM extends SubRecord implements Serializable {

        byte[] skills = new byte[36];
        int health = 1;
        int magicka = 1;
        int stamina = 1;
        byte[] fluff1 = new byte[2];
        float farAwayDistance = 0;
        int gearedUpWeapons = 1;
        byte[] fluff2 = new byte[3];

        DNAM() {
            super();
        }

        @Override
        SubRecord getNew(String type) {
            return new DNAM();
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 52;
        }

        int getSkillBase(Skill in) {
            return skills[Skill.NPC_Value(in)];
        }

        int getSkillMod(Skill in) {
            return skills[Skill.NPC_Value(in) + 18];
        }

        void setSkillBase(Skill in, int to) {
            skills[Skill.NPC_Value(in)] = (byte) to;
        }

        void setSkillMod(Skill in, int to) {
            skills[Skill.NPC_Value(in) + 18] = (byte) to;
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            skills = in.extract(36);
            health = in.extractInt(2);
            magicka = in.extractInt(2);
            stamina = in.extractInt(2);
            fluff1 = in.extract(2);
            farAwayDistance = in.extractFloat();
            gearedUpWeapons = in.extractInt(1);
            fluff2 = in.extract(3);
            logMod(srcMod, "", "DNAM record: ");
            String temp;
            for (Skill s : Skill.NPC_Skills()) {
                temp = " BASE:" + getSkillBase(s) + ", MOD:" + getSkillMod(s);
                logMod(srcMod, "", "  " + s.toString() + Ln.spaceLeft(false, 15 - s.toString().length() + temp.length(), ' ', temp));
            }
            logMod(srcMod, "", "  " + "Health: " + health + ", Magicka: " + magicka + ", Stamina: " + stamina);
            logMod(srcMod, "", "  " + "Far Away Distance: " + farAwayDistance + ", Geared Up weapons: " + gearedUpWeapons);
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(skills, 36);
            out.write(health, 2);
            out.write(magicka, 2);
            out.write(stamina, 2);
            out.write(fluff1, 2);
            out.write(farAwayDistance);
            out.write(gearedUpWeapons, 1);
            out.write(fluff2, 3);
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("DNAM");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            DNAM o = this;
            if (!(no == null && bo == null && (no instanceof DNAM) && (bo instanceof DNAM))) {
                final DNAM nd = (DNAM) no;
                final DNAM bd = (DNAM) bo;
                health = Merger.merge(o.health, nd.health, bd.stamina, getType(), "health");
                magicka = Merger.merge(o.magicka, nd.magicka, bd.magicka, getType(), "magicka");
                stamina = Merger.merge(o.stamina, nd.stamina, bd.stamina, getType(), "health");
                fluff1 = Merger.merge(o.fluff1, nd.fluff1, bd.fluff1, getType(), "unknown");
                fluff2 = Merger.merge(o.fluff2, nd.fluff2, bd.fluff2, getType(), "unknown");
                farAwayDistance = Merger.merge(o.farAwayDistance, nd.farAwayDistance, bd.farAwayDistance, getType(), "far away distance");
                gearedUpWeapons = Merger.merge(o.gearedUpWeapons, nd.gearedUpWeapons, bd.gearedUpWeapons, getType(), "geared up weapons");
                for (Skill s : Skill.values()) {
                    if (o.getSkillBase(s) != nd.getSkillBase(s) && nd.getSkillBase(s) != bd.getSkillBase(s)) {
                        o.setSkillBase(s, nd.getSkillBase(s));
                        if (Merger.fullLogging) {
                            Merger.logMerge("Skills", s.toString(), o.toString());
                        }
                    }
                }
                for (Skill s : Skill.values()) {
                    if (o.getSkillMod(s) != nd.getSkillMod(s) && nd.getSkillMod(s) != bd.getSkillMod(s)) {
                        o.setSkillMod(s, nd.getSkillMod(s));
                        if (Merger.fullLogging) {
                            Merger.logMerge("Skill Mods", s.toString(), o.toString());
                        }
                    }
                }
            }
            return o;
        }
    }

    static class ACBS extends SubRecord implements Serializable {

        LFlags ACBSflags = new LFlags(4);
        int magickaOffset = 0;
        int fatigueOffset = 0;
        int level = 0;
        int minCalcLevel = 0;
        int maxCalcLevel = 0;
        int speed = 100;
        int dispositionBase = 0;
        LFlags templateFlags = new LFlags(2);
        int healthOffset = 0;
        int bleedout = 0;

        ACBS() {
            super();
        }

        @Override
        SubRecord getNew(String type) {
            return new ACBS();
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            ACBSflags.set(in.extract(4));
            magickaOffset = in.extractInt(2);
            fatigueOffset = in.extractInt(2);
            level = in.extractInt(2);
            minCalcLevel = in.extractInt(2);
            maxCalcLevel = in.extractInt(2);
            speed = in.extractInt(2);
            dispositionBase = in.extractInt(2);
            templateFlags.set(in.extract(2));
            healthOffset = in.extractInt(2);
            bleedout = in.extractInt(2);
            logMod(srcMod, "", "ACBS record: ");
            logMod(srcMod, "", "  " + "Base Spell Points: " + magickaOffset + ", Base Fatigue: " + fatigueOffset);
            logMod(srcMod, "", "  " + "Level: " + level + ", Min Calculated Level: " + minCalcLevel + ", Max Calculated Level: " + maxCalcLevel);
            logMod(srcMod, "", "  " + "Speed Multiplier: " + speed + ", Disposition Base: " + dispositionBase);
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(ACBSflags.export(), 4);
            out.write(magickaOffset, 2);
            out.write(fatigueOffset, 2);
            out.write(level, 2);
            out.write(minCalcLevel, 2);
            out.write(maxCalcLevel, 2);
            out.write(speed, 2);
            out.write(dispositionBase, 2);
            out.write(templateFlags.export(), 2);
            out.write(healthOffset, 2);
            out.write(bleedout, 2);
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 24;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("ACBS");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            ACBS a = this;
            if (!(no == null && bo == null && (no instanceof ACBS) && (bo instanceof ACBS))) {
                final ACBS na = (ACBS) no;
                final ACBS ba = (ACBS) bo;
                ACBSflags = Merger.merge(ACBSflags, na.ACBSflags, ba.ACBSflags, getType());
                magickaOffset = Merger.merge(this.magickaOffset, na.magickaOffset, ba.magickaOffset, getType(), "magicka offset");
                fatigueOffset = Merger.merge(this.fatigueOffset, na.fatigueOffset, ba.fatigueOffset, getType(), "fatigue offset");
                level = Merger.merge(this.level, na.level, ba.level, getType(), "level");
                minCalcLevel = Merger.merge(this.minCalcLevel, na.minCalcLevel, ba.minCalcLevel, getType(), "min calc level");
                maxCalcLevel = Merger.merge(this.maxCalcLevel, na.maxCalcLevel, ba.maxCalcLevel, getType(), "max calc level");
                speed = Merger.merge(this.speed, na.speed, ba.speed, getType(), "speed");
                dispositionBase = Merger.merge(this.dispositionBase, na.dispositionBase, ba.dispositionBase, getType(), "disposition base");
                healthOffset = Merger.merge(this.healthOffset, na.healthOffset, ba.healthOffset, getType(), "health offset");
                bleedout = Merger.merge(this.bleedout, na.bleedout, ba.bleedout, getType(), "bleedout");
                templateFlags = Merger.merge(templateFlags, na.templateFlags, ba.templateFlags, getType());
            }
            return a;
        }
    }

    static class AIDT extends SubRecord implements Serializable {

        Aggression aggression = Aggression.Unaggressive;
        Confidence confidence = Confidence.Cowardly;
        Morality morality = Morality.AnyCrime;
        Assistance assistance = Assistance.HelpsNobody;
        int energy = 0;
        Mood mood = Mood.Neutral;
        boolean aggroRadiusBehavior = false;
        byte[] fluff = new byte[1];
        int aggroWarn = 0;
        int aggroWarnAttack = 0;
        int aggroAttack = 0;

        AIDT() {
            super();
        }

        @Override
        SubRecord getNew(String type) {
            return new AIDT();
        }

        @Override
        final void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            aggression = Aggression.values()[in.extractInt(1)];
            confidence = Confidence.values()[in.extractInt(1)];
            energy = in.extractInt(1);
            morality = Morality.values()[in.extractInt(1)];
            mood = Mood.values()[in.extractInt(1)];
            assistance = Assistance.values()[in.extractInt(1)];
            aggroRadiusBehavior = in.extractBool(1);
            fluff = in.extract(1);
            aggroWarn = in.extractInt(4);
            aggroWarnAttack = in.extractInt(4);
            aggroAttack = in.extractInt(4);
            logMod(srcMod, "", "AIDT record: ");
            logMod(srcMod, "", "  Aggression: " + aggression + ", Confidence: " + confidence + ", Morality: " + morality);
            logMod(srcMod, "", "  Assistance: " + assistance + ", Mood: " + mood + ", AggroRadiusBehavior: " + aggroRadiusBehavior);
            logMod(srcMod, "", "  Aggro Attack: " + aggroAttack + ", Aggro Warn: " + aggroWarn + ", Aggro Warn/Attack: " + aggroWarnAttack);
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(aggression.ordinal(), 1);
            out.write(confidence.ordinal(), 1);
            out.write(energy, 1);
            out.write(morality.ordinal(), 1);
            out.write(mood.ordinal(), 1);
            out.write(assistance.ordinal(), 1);
            out.write(aggroRadiusBehavior, 1);
            out.write(fluff, 1);
            out.write(aggroWarn);
            out.write(aggroWarnAttack);
            out.write(aggroAttack);
        }

        @Override
        boolean isValid() {
            return true;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 20;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("AIDT");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            AIDT a = this;
            if (!(no == null && bo == null && (no instanceof AIDT) && (bo instanceof AIDT))) {
                final AIDT na = (AIDT) no;
                final AIDT ba = (AIDT) bo;
                aggression = (Aggression) Merger.merge(a.aggression, na.aggression, ba.aggression, getType(), "aggression");
                confidence = (Confidence) Merger.merge(a.confidence, na.confidence, ba.confidence, getType(), "confidence");
                morality = (Morality) Merger.merge(a.morality, na.morality, ba.morality, getType(), "morality");
                assistance = (Assistance) Merger.merge(a.assistance, na.assistance, ba.assistance, getType(), "assistance");
                mood = (Mood) Merger.merge(a.mood, na.mood, ba.mood, getType(), "mood");
                energy = Merger.merge(a.energy, na.energy, ba.energy, getType(), "energy");
                aggroRadiusBehavior = Merger.merge(a.aggroRadiusBehavior, na.aggroRadiusBehavior, ba.aggroRadiusBehavior, getType(), "aggroRadiusBehavior");
                aggroWarn = Merger.merge(a.aggroWarn, na.aggroWarn, ba.aggroWarn, getType(), "aggroWarn");
                aggroWarnAttack = Merger.merge(a.aggroWarnAttack, na.aggroWarnAttack, ba.aggroWarnAttack, getType(), "aggroWarnAttack");
                aggroAttack = Merger.merge(a.aggroAttack, na.aggroAttack, ba.aggroAttack, getType(), "aggroAttack");
                fluff = Merger.merge(a.fluff, na.fluff, ba.fluff, getType(), "unknown");
            }
            return a;
        }
    }

    public static class AttackPackage extends SubShell implements Serializable {

        static final SubPrototype attackPackageProto = new SubPrototype() {
            @Override
            protected void addRecords() {
                add(new ATKD());
                add(SubString.getNew("ATKE", true));
            }
        };

        AttackPackage() {
            super(attackPackageProto);
        }

        @Override
        SubRecord getNew(String type_) {
            return new AttackPackage();
        }

        ATKD getATKD() {
            return (ATKD) subRecords.get("ATKD");
        }

        void setATKD(ATKD rhs) {
            subRecords.add(rhs);
        }

        String getATKE() {
            return subRecords.getSubString("ATKE").print();
        }

        void setATKE(String in) {
            subRecords.setSubString("ATKE", in);
        }
    }

    static class ATKD extends SubRecord implements Serializable {

        float damageMult;
        float attackChance;
        final FormID attackSpell = new FormID();
        final LFlags flags = new LFlags(4);
        float attackAngle;
        float strikeAngle;
        float stagger;
        final FormID attackType = new FormID();
        float knockdown;
        float recovery;
        float staminaMult;
        boolean valid = false;

        ATKD() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            if (isValid()) {
                out.write(damageMult);
                out.write(attackChance);
                attackSpell.export(out);
                out.write(flags.export());
                out.write(attackAngle);
                out.write(strikeAngle);
                out.write(stagger);
                attackType.export(out);
                out.write(knockdown);
                out.write(recovery);
                out.write(staminaMult);
            }
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            damageMult = in.extractFloat();
            attackChance = in.extractFloat();
            attackSpell.parseData(in, srcMod);
            flags.set(in.extract(4));
            attackAngle = in.extractFloat();
            strikeAngle = in.extractFloat();
            stagger = in.extractFloat();
            attackType.parseData(in, srcMod);
            knockdown = in.extractFloat();
            recovery = in.extractFloat();
            staminaMult = in.extractFloat();
            valid = true;
        }

        @Override
        boolean isValid() {
            return valid;
        }

        @Override
        SubRecord getNew(String type) {
            return new ATKD();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 44;
        }

        @Override
        ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = new ArrayList<>(2);
            out.add(attackSpell);
            out.add(attackType);
            return out;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("ATKD");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            ATKD o = this;
            if (!(no == null && bo == null && (no instanceof ATKD) && (bo instanceof ATKD))) {
                final ATKD na = (ATKD) no;
                final ATKD ba = (ATKD) bo;
                if (!o.equals(na) && !na.equals(ba)) {
                    o = na;
                    if (Merger.fullLogging) {
                        SPGlobal.log(getType() + ": ", "Merged " + getType() + " to " + o + " for "
                                + Merger.currentRecord + " from Mod " + Merger.currentMod);
                    }
                }
            }
            return o;
        }
    }

    static class NAM9 extends SubRecord implements Serializable {

        float noseLong = 0;
        float noseUp = 0;
        float jawUp = 0;
        float jawWide = 0;
        float jawForward = 0;
        float cheekUp = 0;
        float cheekForward = 0;
        float eyeUp = 0;
        float eyeIn = 0;
        float browUp = 0;
        float browIn = 0;
        float browForward = 0;
        float lipUp = 0;
        float lipIn = 0;
        float chinWide = 0;
        float chinUp = 0;
        float chinOverbite = 0;
        float eyesForward = 0;
        byte[] unknown = new byte[4];
        boolean valid = false;

        NAM9() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(noseLong);
            out.write(noseUp);
            out.write(jawUp);
            out.write(jawWide);
            out.write(jawForward);
            out.write(cheekUp);
            out.write(cheekForward);
            out.write(eyeUp);
            out.write(eyeIn);
            out.write(browUp);
            out.write(browIn);
            out.write(browForward);
            out.write(lipUp);
            out.write(lipIn);
            out.write(chinWide);
            out.write(chinUp);
            out.write(chinOverbite);
            out.write(eyesForward);
            out.write(unknown, 4);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            noseLong = in.extractFloat();
            noseUp = in.extractFloat();
            jawUp = in.extractFloat();
            jawWide = in.extractFloat();
            jawForward = in.extractFloat();
            cheekUp = in.extractFloat();
            cheekForward = in.extractFloat();
            eyeUp = in.extractFloat();
            eyeIn = in.extractFloat();
            browUp = in.extractFloat();
            browIn = in.extractFloat();
            browForward = in.extractFloat();
            lipUp = in.extractFloat();
            lipIn = in.extractFloat();
            chinWide = in.extractFloat();
            chinUp = in.extractFloat();
            chinOverbite = in.extractFloat();
            eyesForward = in.extractFloat();
            unknown = in.extract(4);
            valid = true;
        }

        @Override
        boolean isValid() {
            return valid;
        }

        @Override
        SubRecord getNew(String type) {
            return new NAM9();
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 76;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("NAM9");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            NAM9 o = this;
            if (!(no == null && bo == null && (no instanceof NAM9) && (bo instanceof NAM9))) {
                final NAM9 nn = (NAM9) no;
                final NAM9 bn = (NAM9) bo;
                if (!o.equals(nn) && !nn.equals(bn)) {
                    o = nn;
                    if (Merger.fullLogging) {
                        SPGlobal.log(getType() + ": ", "Merged " + getType() + " to " + o + " for "
                                + Merger.currentRecord + " from Mod " + Merger.currentMod);
                    }
                }
            }
            return o;
        }
    }

    static class NAMA extends SubRecord implements Serializable {

        int nose;
        int unknown;
        int eyes;
        int mouth;
        boolean valid = false;

        NAMA() {
            super();
        }

        @Override
        void export(ModExporter out) throws IOException {
            super.export(out);
            out.write(nose);
            out.write(unknown);
            out.write(eyes);
            out.write(mouth);
        }

        @Override
        void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
            super.parseData(in, srcMod);
            nose = in.extractInt(4);
            unknown = in.extractInt(4);
            eyes = in.extractInt(4);
            mouth = in.extractInt(4);
            valid = true;
        }

        @Override
        SubRecord getNew(String type) {
            return new NAMA();
        }

        @Override
        boolean isValid() {
            return valid;
        }

        @Override
        int getContentLength(boolean isStringTabled) {
            return 16;
        }

        @Override
        ArrayList<String> getTypes() {
            return Record.getTypeList("NAMA");
        }

        @Override
        public SubRecord merge(SubRecord no, SubRecord bo) {
            NAMA o = this;
            if (!(no == null && bo == null && (no instanceof NAMA) && (bo instanceof NAMA))) {
                final NAMA nn = (NAMA) no;
                final NAMA bn = (NAMA) bo;
                if (!o.equals(nn) && !nn.equals(bn)) {
                    o = nn;
                    if (Merger.fullLogging) {
                        SPGlobal.log(getType() + ": ", "Merged " + getType() + " to " + o + " for "
                                + Merger.currentRecord + " from Mod " + Merger.currentMod);
                    }
                }
            }
            return o;
        }
    }
}
