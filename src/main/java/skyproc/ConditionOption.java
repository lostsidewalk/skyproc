package skyproc;

import lev.LImport;
import skyproc.Condition.RunOnType;
import skyproc.genenums.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


class ConditionOption implements Serializable {

    int index;

    Enum script;

    RunOnType runType = RunOnType.Subject;

    FormID reference = new FormID();

    byte[] p3placeholder = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    public static ConditionOption getOption(int index) {
        Enum script = Condition.getScript(index);
        if (script == null) {
            SPGlobal.logError("Conditions", "Did not have a script for index: " + index);
        }
        ConditionOption out = null;
        Class c = script.getClass();
        if (c.equals(Condition.P_NoParams.CanFlyHere.getClass())) {
            out = new ConditionOption();
        } else if (c.equals(Condition.P_Axis.GetAngle.getClass())) {
            out = new Cond_Axis();
        } else if (c.equals(Condition.P_FormID_CastingSource.IsCurrentSpell.getClass())) {
            out = new Cond_FormID_CastingSource();
        } else if (c.equals(Condition.P_FormID_Int.GetStageDone.getClass())) {
            out = new Cond_FormID_Int();
        } else if (c.equals(Condition.P_FormID_FormID.GetFactionCombatReaction.getClass())) {
            out = new Cond_FormID_FormID();
        } else if (c.equals(Condition.P_CastingSource_FormID.SpellHasKeyword.getClass())) {
            out = new Cond_CastingSource_FormID();
        } else if (c.equals(Condition.P_FormID.CanPayCrimeGold.getClass())) {
            out = new Cond_FormID();
        } else if (c.equals(Condition.P_Gender.GetIsSex.getClass())) {
            out = new Cond_Gender();
        } else if (c.equals(Condition.P_CastingSource.GetCurrentCastingType.getClass())) {
            out = new Cond_CastingSource();
        } else if (c.equals(Condition.P_Int_FormID_Int.GetEventData.getClass())) {
            out = new Cond_Int_FormID_Int();
        } else if (c.equals(Condition.P_Int_FormID.GetKeywordDataForAlias.getClass())) {
            out = new Cond_Int_FormID();
        } else if (c.equals(Condition.P_WardState.IsWardState.getClass())) {
            out = new Cond_WardState();
        } else if (c.equals(Condition.P_Int.EPModSkillUsage_IsAdvanceAction.getClass())) {
            out = new Cond_Int();
        } else if (c.equals(Condition.P_FormID_String.GetQuestVariable.getClass())) {
            out = new Cond_FormID_String();
        } else if (c.equals(Condition.P_FormID_Axis.GetRelativeAngle.getClass())) {
            out = new Cond_FormID_Axis();
        } else if (c.equals(Condition.P_FormID_CrimeType.GetCrime.getClass())) {
            out = new Cond_FormID_CrimeType();
        } else if (c.equals(Condition.P_FormID_Float.GetWithinDistance.getClass())) {
            out = new Cond_FormID_Float();
        } else if (c.equals(Condition.P_Int_Int.GetPlayerControlsDisabled.getClass())) {
            out = new Cond_Int_Int();
        } else if (c.equals(Condition.P_String.GetGraphVariableFloat.getClass())) {
            out = new Cond_String();
        }
        out.index = index;
        out.script = script;

        return out;
    }

    public void export(ModExporter out) throws IOException {
        exportParam1(out);
        out.write(runType.ordinal());
        reference.export(out);
        exportParam3(out);
    }

    public void parseData(LImport in, Mod srcMod) {
        parseParam1(in, srcMod);
        runType = RunOnType.values()[in.extractInt(4)];
        reference.parseData(in, srcMod);
        parseParam3(in, srcMod);
        SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Run Type: " + runType + ", Reference: " + reference);
    }

    public ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>(3);
        out.add(reference);
        return out;
    }

    public void exportParam1(ModExporter out) throws IOException {
        out.write(0);
        out.write(0);
    }

    public void exportParam3(ModExporter out) throws IOException {
        out.write(p3placeholder);
    }

    public void parseParam1(LImport in, Mod srcMod) {
        in.skip(8);
    }

    public void parseParam3(LImport in, Mod srcMod) {
        p3placeholder = in.extract(4);
    }

    public Object getParam1() {
        return null;
    }

    public Object getParam2() {
        return null;
    }

    public Object getParam3() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConditionOption other = (ConditionOption) obj;
        if (this.index != other.index) {
            return false;
        }
        if (this.script != other.script) {
            return false;
        }
        if (this.runType != other.runType) {
            return false;
        }
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        return Arrays.equals(this.p3placeholder, other.p3placeholder);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.index;
        hash = 97 * hash + (this.script != null ? this.script.hashCode() : 0);
        hash = 97 * hash + (this.runType != null ? this.runType.hashCode() : 0);
        hash = 97 * hash + Objects.hashCode(this.reference);
        hash = 97 * hash + Arrays.hashCode(this.p3placeholder);
        return hash;
    }

    public static class Cond_FormID extends ConditionOption {

        final FormID p1;

        Cond_FormID() {
            p1 = new FormID();
        }

        Cond_FormID(FormID id) {
            p1 = id;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            in.skip(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID other = (Cond_FormID) obj;
            return Objects.equals(this.p1, other.p1);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 97 * hash + Objects.hashCode(this.p1);
            return hash;
        }
    }

    public static class Cond_Axis extends ConditionOption {

        Axis axis;

        Cond_Axis() {
        }

        Cond_Axis(Axis a) {
            axis = a;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(axis.toString(), 4);
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            axis = Axis.get(in.extractString(1));
            in.skip(7);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Axis: " + axis);
        }

        @Override
        public Object getParam1() {
            return axis;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Axis other = (Cond_Axis) obj;
            return this.axis == other.axis;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 71 * hash + (this.axis != null ? this.axis.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_FormID_CastingSource extends ConditionOption {

        final FormID p1;
        CastingSource source;

        Cond_FormID_CastingSource() {
            p1 = new FormID();
        }

        Cond_FormID_CastingSource(FormID id, CastingSource source) {
            p1 = id;
            this.source = source;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(source.ordinal());
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            source = CastingSource.values()[in.extractInt(4)];
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1 + ", Casting Source: " + source);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return source;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_CastingSource other = (Cond_FormID_CastingSource) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return this.source == other.source;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 89 * hash + Objects.hashCode(this.p1);
            hash = 89 * hash + (this.source != null ? this.source.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_CastingSource_FormID extends ConditionOption {

        CastingSource source;
        final FormID p2;

        Cond_CastingSource_FormID() {
            p2 = new FormID();
        }

        Cond_CastingSource_FormID(CastingSource source, FormID id) {
            p2 = id;
            this.source = source;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p2);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(source.ordinal());
            p2.export(out);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            source = CastingSource.values()[in.extractInt(4)];
            p2.parseData(in, srcMod);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Casting Source: " + source + ", FormID: " + p2);
        }

        @Override
        public Object getParam1() {
            return source;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_CastingSource_FormID other = (Cond_CastingSource_FormID) obj;
            if (this.source != other.source) {
                return false;
            }
            return Objects.equals(this.p2, other.p2);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 67 * hash + (this.source != null ? this.source.hashCode() : 0);
            hash = 67 * hash + Objects.hashCode(this.p2);
            return hash;
        }
    }

    public static class Cond_FormID_Int extends ConditionOption {

        final FormID p1;
        int p2;

        Cond_FormID_Int() {
            p1 = new FormID();
        }

        Cond_FormID_Int(FormID id, int i) {
            p1 = id;
            p2 = i;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(p2);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            p2 = in.extractInt(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1 + ", Int: " + p2);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_Int other = (Cond_FormID_Int) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return this.p2 == other.p2;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 59 * hash + Objects.hashCode(this.p1);
            hash = 59 * hash + this.p2;
            return hash;
        }
    }

    public static class Cond_FormID_FormID extends ConditionOption {

        final FormID p1;
        final FormID p2;

        Cond_FormID_FormID() {
            p1 = new FormID();
            p2 = new FormID();
        }

        Cond_FormID_FormID(FormID id1, FormID id2) {
            p1 = id1;
            p2 = id2;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            out.add(p2);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            p2.export(out);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            p2.parseData(in, srcMod);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID 1: " + p1 + ", FormID 2: " + p2);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_FormID other = (Cond_FormID_FormID) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return Objects.equals(this.p2, other.p2);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 37 * hash + Objects.hashCode(this.p1);
            hash = 37 * hash + Objects.hashCode(this.p2);
            return hash;
        }
    }

    public static class Cond_Gender extends ConditionOption {

        Gender g;

        Cond_Gender() {
        }

        Cond_Gender(Gender g) {
            this.g = g;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(g.ordinal());
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            g = Gender.values()[in.extractInt(4)];
            in.skip(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Gender: " + g);
        }

        @Override
        public Object getParam1() {
            return g;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Gender other = (Cond_Gender) obj;
            return this.g == other.g;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 53 * hash + (this.g != null ? this.g.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_CastingSource extends ConditionOption {

        CastingSource source;

        Cond_CastingSource() {
        }

        Cond_CastingSource(CastingSource source) {
            this.source = source;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(source.ordinal());
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            source = CastingSource.values()[in.extractInt(4)];
            in.skip(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Casting Source: " + source);
        }

        @Override
        public Object getParam1() {
            return source;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_CastingSource other = (Cond_CastingSource) obj;
            return this.source == other.source;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 23 * hash + (this.source != null ? this.source.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_Int_FormID_Int extends ConditionOption {

        int p1;
        final FormID p2;
        int p3;

        Cond_Int_FormID_Int() {
            p2 = new FormID();
        }

        Cond_Int_FormID_Int(int i1, FormID id, int i2) {
            p1 = i1;
            p2 = id;
            p3 = i2;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p2);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(p1);
            p2.export(out);
        }

        @Override
        public void exportParam3(ModExporter out) throws IOException {
            out.write(p3);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1 = in.extractInt(4);
            p2.parseData(in, srcMod);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Int 1: " + p1 + ", FormID: " + p1 + ", Int 2: " + p3);
        }

        @Override
        public void parseParam3(LImport in, Mod srcMod) {
            p3 = in.extractInt(4);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return p3;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Int_FormID_Int other = (Cond_Int_FormID_Int) obj;
            if (this.p1 != other.p1) {
                return false;
            }
            if (!Objects.equals(this.p2, other.p2)) {
                return false;
            }
            return this.p3 == other.p3;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 41 * hash + this.p1;
            hash = 41 * hash + Objects.hashCode(this.p2);
            hash = 41 * hash + this.p3;
            return hash;
        }
    }

    public static class Cond_Int_FormID extends ConditionOption {

        int p1;
        final FormID p2;

        Cond_Int_FormID() {
            p2 = new FormID();
        }

        Cond_Int_FormID(int i1, FormID id) {
            p1 = i1;
            p2 = id;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p2);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(p1);
            p2.export(out);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1 = in.extractInt(4);
            p2.parseData(in, srcMod);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Int: " + p1 + ", FormID: " + p2);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Int_FormID other = (Cond_Int_FormID) obj;
            if (this.p1 != other.p1) {
                return false;
            }
            return Objects.equals(this.p2, other.p2);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 11 * hash + this.p1;
            hash = 11 * hash + Objects.hashCode(this.p2);
            return hash;
        }
    }

    public static class Cond_WardState extends ConditionOption {

        WardState state;

        Cond_WardState() {
        }

        Cond_WardState(WardState state) {
            this.state = state;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(state.ordinal());
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            state = WardState.values()[in.extractInt(4)];
            in.skip(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Ward State: " + state);
        }

        @Override
        public Object getParam1() {
            return state;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_WardState other = (Cond_WardState) obj;
            return this.state == other.state;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 37 * hash + (this.state != null ? this.state.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_Int extends ConditionOption {

        int p1;

        Cond_Int() {
        }

        Cond_Int(int i1) {
            p1 = i1;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(p1);
            out.write(0);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1 = in.extractInt(4);
            in.skip(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Int: " + p1);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Int other = (Cond_Int) obj;
            return this.p1 == other.p1;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 29 * hash + this.p1;
            return hash;
        }
    }

    public static class Cond_FormID_String extends ConditionOption {

        final FormID p1;
        byte[] p2;

        Cond_FormID_String() {
            p1 = new FormID();
        }

        Cond_FormID_String(FormID id, String s) {
            p1 = id;
            p2 = new byte[]{(byte) 0x2e, (byte) 0xe2, (byte) 0x9d, (byte) 0xf0};
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(p2);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            p2 = in.extract(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return p2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_String other = (Cond_FormID_String) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return Arrays.equals(this.p2, other.p2);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 97 * hash + Objects.hashCode(this.p1);
            hash = 97 * hash + Arrays.hashCode(this.p2);
            return hash;
        }
    }

    public static class Cond_FormID_Axis extends ConditionOption {

        final FormID p1;
        Axis a;

        Cond_FormID_Axis() {
            p1 = new FormID();
        }

        Cond_FormID_Axis(FormID id, Axis a) {
            p1 = id;
            this.a = a;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(a.toString(), 4);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            a = Axis.get(in.extractString(1));
            in.skip(3);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1 + ", Axis: " + a);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return a;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_Axis other = (Cond_FormID_Axis) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return this.a == other.a;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 37 * hash + Objects.hashCode(this.p1);
            hash = 37 * hash + (this.a != null ? this.a.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_FormID_CrimeType extends ConditionOption {

        final FormID p1;
        CrimeType c;

        Cond_FormID_CrimeType() {
            p1 = new FormID();
        }

        Cond_FormID_CrimeType(FormID id, CrimeType c) {
            p1 = id;
            this.c = c;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(c.ordinal());
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            c = CrimeType.values()[in.extractInt(4)];
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1 + ", Crime Type: " + c);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return c;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_CrimeType other = (Cond_FormID_CrimeType) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return this.c == other.c;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 23 * hash + Objects.hashCode(this.p1);
            hash = 23 * hash + (this.c != null ? this.c.hashCode() : 0);
            return hash;
        }
    }

    public static class Cond_FormID_Float extends ConditionOption {

        final FormID p1;
        float f;

        Cond_FormID_Float() {
            p1 = new FormID();
        }

        Cond_FormID_Float(FormID id, float f) {
            p1 = id;
            this.f = f;
        }

        @Override
        public ArrayList<FormID> allFormIDs() {
            ArrayList<FormID> out = super.allFormIDs();
            out.add(p1);
            return out;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            p1.export(out);
            out.write(f);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1.parseData(in, srcMod);
            f = in.extractFloat();
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  FormID: " + p1 + ", Float: " + f);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return f;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_FormID_Float other = (Cond_FormID_Float) obj;
            if (!Objects.equals(this.p1, other.p1)) {
                return false;
            }
            return Float.floatToIntBits(this.f) == Float.floatToIntBits(other.f);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 59 * hash + Objects.hashCode(this.p1);
            hash = 59 * hash + Float.floatToIntBits(this.f);
            return hash;
        }
    }

    public static class Cond_Int_Int extends ConditionOption {

        int i1;
        int i2;

        Cond_Int_Int() {
        }

        Cond_Int_Int(int i1, int i2) {
            this.i1 = i1;
            this.i2 = i2;
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(i1);
            out.write(i2);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            i1 = in.extractInt(4);
            i2 = in.extractInt(4);
            SPGlobal.logMod(srcMod, this.getClass().getSimpleName(), "  Int 1: " + i1 + ", Int 2: " + i2);
        }

        @Override
        public Object getParam1() {
            return i1;
        }

        @Override
        public Object getParam2() {
            return i2;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_Int_Int other = (Cond_Int_Int) obj;
            if (this.i1 != other.i1) {
                return false;
            }
            return this.i2 == other.i2;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 79 * hash + this.i1;
            hash = 79 * hash + this.i2;
            return hash;
        }
    }

    public static class Cond_String extends ConditionOption {

        byte[] p1;

        Cond_String() {
        }

        Cond_String(String s) {
            p1 = new byte[]{(byte) 0x2e, (byte) 0xe2, (byte) 0x9d, (byte) 0xf0};
        }

        @Override
        public void exportParam1(ModExporter out) throws IOException {
            out.write(p1);
        }

        @Override
        public void parseParam1(LImport in, Mod srcMod) {
            p1 = in.extract(4);
        }

        @Override
        public Object getParam1() {
            return p1;
        }

        @Override
        public Object getParam2() {
            return null;
        }

        @Override
        public Object getParam3() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!equals(obj)) {
                return false;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cond_String other = (Cond_String) obj;
            return Arrays.equals(this.p1, other.p1);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 41 * hash + Arrays.hashCode(this.p1);
            return hash;
        }
    }

}
