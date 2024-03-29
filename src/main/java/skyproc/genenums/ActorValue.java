package skyproc.genenums;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A list of values commonly used by Skyrim for various purposes. UNKNOWN
 * entries are simply placeholders.
 *
 * @author Justin Swanson
 */
public enum ActorValue {


    Aggression,

    Confidence,

    Energy,

    Morality,

    Mood,

    Assistance,

    OneHanded,

    TwoHanded,

    Marksman,

    Block,

    Smithing,

    HeavyArmor,

    LightArmor,

    Pickpocket,

    Lockpicking,

    Sneak,

    Alchemy,

    Speechcraft,

    Alteration,

    Conjuration,

    Destruction,

    Illusion,

    Restoration,

    Enchanting,

    Health,

    Magicka,

    Stamina,

    HealRate,

    MagickaRate,

    StaminaRate,

    SpeedMult,

    InventoryWeight,

    CarryWeight,

    DragonRend,

    UNKNOWN34,

    UNKNOWN35,

    UNKNOWN36,

    UNKNOWN37,

    UNKNOWN38,

    DamageResist,

    PoisonResist,

    FireResist,

    ElectricResist,

    FrostResist,

    MagicResist,

    DiseaseResist,

    UNKNOWN46,

    UNKNOWN47,

    UNKNOWN48,

    UNKNOWN49,

    UNKNOWN50,

    UNKNOWN51,

    UNKNOWN52,

    Paralysis,

    Invisibility,

    NightEye,

    DetectLifeRange,

    WaterBreathing,

    WaterWalking,

    IgnoreCrippledLimbs,

    Fame,

    Infamy,

    JumpingBonus,

    WardPower,

    RightItemCharge,

    LeftItemCharge,

    EquippedItemCharge,

    ArmorPerks,

    ShieldPerks,

    Variable01,

    Variable02,

    Variable03,

    Variable04,

    Variable05,

    Variable06,

    Variable07,

    Variable08,

    Variable09,

    Variable10,

    FavorActive,

    FavorsPerDay,

    FavorsPerDayTimer,

    EquippedStaffCharge,

    AbsorbChance,

    Blindness,

    WeaponSpeedMult,

    ShoutRecoveryMult,

    BowStaggerBonus,

    Telekinesis,

    FavorPointsBonus,

    LastBribedIntimidated,

    LastFlattered,

    MovementNoiseMult,

    BypassVendorStolenCheck,

    BypassVendorKeywordCheck,

    WaitingForPlayer,

    OneHandedMod,

    TwoHandedMod,

    MarksmanMod,

    BlockMod,

    SmithingMod,

    HeavyArmorMod,

    LightArmorMod,

    PickPocketMod,

    LockpickingMod,

    SneakMod,

    AlchemyMod,

    SpeechcraftMod,

    AlterationMod,

    ConjurationMod,

    DestructionMod,

    IllusionMod,

    RestorationMod,

    EnchantingMod,

    OneHandedSkillAdvance,

    TwoHandedSkillAdvance,

    MarksmanSkillAdvance,

    BlockSkillAdvance,

    SmithingSkillAdvance,

    HeavyArmorSkillAdvance,

    LightArmorSkillAdvance,

    PickPocketSkillAdvance,

    LockpickingSkillAdvance,

    SneakSkillAdvance,

    AlchemySkillAdvance,

    SpeechcraftSkillAdvance,

    AlterationSkillAdvance,

    ConjurationSkillAdvance,

    DestructionSkillAdvance,

    IllusionSkillAdvance,

    RestorationSkillAdvance,

    EnchantingSkillAdvance,

    LeftWeaponSpeedMult,

    DragonSouls,

    CombatHealthRegenMult,

    OneHandedPowerMod,

    TwoHandedPowerMod,

    MarksmanPowerMod,

    BlockPowerMod,

    SmithingPowerMod,

    HeavyArmorPowerMod,

    LightArmorPowerMod,

    PickPocketPowerMod,

    LockpickingPowerMod,

    SneakPowerMod,

    AlchemyPowerMod,

    SpeechcraftPowerMod,

    AlterationPowerMod,

    ConjurationPowerMod,

    DestructionPowerMod,

    IllusionPowerMod,

    RestorationPowerMod,

    EnchantingPowerMod,

    UNKNOWN153,

    AttackDamageMult,

    HealRateMult,

    MagickaRateMult,

    StaminaRateMult,

    UNKNOWN158,

    UNKNOWN159,

    UNKNOWN160,

    UNKNOWN161,

    UNKNOWN162,

    ReflectDamage,

    UNKNOWN164,

    UNKNOWN165,

    UNKNOWN166,

    UNKNOWN167,

    UNKNOWN168,

    UNKNOWN169,

    UNKNOWN170,

    UNKNOWN171,

    UNKNOWN172,

    UNKNOWN173,

    UNKNOWN174,

    UNKNOWN175,

    UNKNOWN176,

    UNKNOWN177,

    UNKNOWN178,

    UNKNOWN179,

    UNKNOWN180,

    UNKNOWN181,

    UNKNOWN182,

    UNKNOWN183,

    UNKNOWN184,

    UNKNOWN185,

    UNKNOWN186,

    UNKNOWN187,

    UNKNOWN188,

    UNKNOWN189,

    UNKNOWN190,

    UNKNOWN191,

    UNKNOWN192,

    UNKNOWN193,

    UNKNOWN194,

    UNKNOWN195,

    UNKNOWN196,

    UNKNOWN197,

    UNKNOWN198,

    UNKNOWN199,

    UNKNOWN200,

    UNKNOWN201,

    UNKNOWN202,

    UNKNOWN203,

    UNKNOWN204,

    UNKNOWN205,

    UNKNOWN206,

    UNKNOWN207,

    UNKNOWN208,

    UNKNOWN209,

    UNKNOWN210,

    UNKNOWN211,

    UNKNOWN212,

    UNKNOWN213,

    UNKNOWN214,

    UNKNOWN215,

    UNKNOWN216,

    UNKNOWN217,

    UNKNOWN218,

    UNKNOWN219,

    UNKNOWN220,

    UNKNOWN221,

    UNKNOWN222,

    UNKNOWN223,

    UNKNOWN224,

    UNKNOWN225,

    UNKNOWN226,

    UNKNOWN227,

    UNKNOWN228,

    UNKNOWN229,

    UNKNOWN230,

    UNKNOWN231,

    UNKNOWN232,

    UNKNOWN233,

    UNKNOWN234,

    UNKNOWN235,

    UNKNOWN236,

    UNKNOWN237,

    UNKNOWN238,

    UNKNOWN239,

    UNKNOWN240,

    UNKNOWN241,

    UNKNOWN242,

    UNKNOWN243,

    UNKNOWN244,

    UNKNOWN245,

    UNKNOWN246,

    UNKNOWN247,

    UNKNOWN248,

    UNKNOWN249,

    UNKNOWN250,

    UNKNOWN251,

    UNKNOWN252,

    UNKNOWN253,

    UNKNOWN254,

    NONE,

    UNKNOWN;

    /**
     * @param in
     * @return
     */
    static public int value(ActorValue in) {
        if (in == null || in == UNKNOWN || in == NONE) {
            return -1;
        } else {
            return in.ordinal();
        }
    }

    /**
     * @param in
     * @return
     */
    static public ActorValue value(int in) {
        if (in < ActorValue.values().length - 2 && in >= 0) {
            return ActorValue.values()[in];
        } else {
            return NONE;
        }
    }

    // Highly specific function meant to parse the function list from
    // http://www.uesp.net/wiki/Tes5Mod:Actor_Value_Indices#Actor_Value_Codes as an easier way to generate
    // code, rather than typing it all by hand.
    // Shouldn't need to be used by you or any users.
    static void parseData() throws IOException {

        String dir = "Validation Files/";
        BufferedReader in = new BufferedReader(new FileReader(dir + "ActorValueSource.txt"));
        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "ActorValueOut.txt"));
        BufferedWriter log = new BufferedWriter(new FileWriter(dir + "ActorValueOutLog.txt"));

        String[] values = new String[256];

        ArrayList<String> outStrings = new ArrayList<>();
        while (in.ready()) {
            String line = in.readLine();
            log.write("Read Line: " + line);
            Scanner tokenizer = new Scanner(line);
            try {
                int index = Integer.parseInt(tokenizer.next());
                log.write("  Index: " + index);
                String name = tokenizer.next();
                log.write("  Ref: " + name);

                // Generate string
                values[index] = name;

            } catch (NumberFormatException ex) {
                log.write("  Skipped");
            }
        }

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                out.write(values[i] + ",\n");
            } else {
                out.write("UNKNOWN" + i + ",\n");
            }
        }

        in.close();
        out.close();
        log.close();
    }
}
