package skyproc;

import lev.LMergeMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class has functions that deal with parsing hardcoded Embedded Script info, and generating
 * the corresponding Java code which is used by SkyProc.
 * <p>
 * Validation Files/EmbeddedScriptSource.txt
 * - basic function list, parsed by parseScriptData
 * <p>
 * Validation Files/EmbeddedScriptSourceGib.txt
 * - function list taken from mod.gib.me/skyrim/functions.html
 * - parsed by parseScriptDataGibMe
 *
 * @author Justin Swanson
 */
public class EmbeddedScripts {

    @SuppressWarnings("unused")
    static void parseScriptDataGibMe() throws IOException {

        String dir = "Validation Files/";
        BufferedReader in = new BufferedReader(new FileReader(dir + "EmbeddedScriptSourceGib.txt"));
        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptGibOut.txt"));
        BufferedWriter log = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptGibOutLog.txt"));
        ArrayList<String> enumStrings = new ArrayList<>();
        int curIndex = -1;
        while (in.ready()) {
            String line = in.readLine();
            log.write("Read Line: " + line + "\n");
            Scanner tokenizer = new Scanner(line);
            try {
                int index = Integer.parseInt(tokenizer.next());
                log.write("  Index: " + index + "\n");
                if (index < 4096) {
                    log.write("  Skipped.\n");
                    continue;
                } else {
                    index -= 4096;
                }
                String name = tokenizer.next();
                if (name.contains("ref.")) {
                    name = tokenizer.next();
                }
                log.write("  Ref: " + name + "\n");
                ArrayList<ParamType> parameterMask = new ArrayList<>();
                tokenizer.useDelimiter(",");
                while (tokenizer.hasNext()) {
                    String parameter = tokenizer.next();
                    if (parameter.toUpperCase().contains("NAME")) {
                        parameterMask.add(ParamType.String);
                        log.write("  Parameter " + parameter + " " + ParamType.String + "\n");
                    } else if (parameter.toUpperCase().contains("AXIS")) {
                        parameterMask.add(ParamType.Axis);
                        log.write("  Parameter " + parameter + " " + ParamType.Axis + "\n");
                    } else if (parameter.toUpperCase().contains("UNK")
                            || parameter.toUpperCase().contains("QUEST")
                            || parameter.toUpperCase().contains("ACTOR")
                            || parameter.toUpperCase().contains("CONTAINER")) {
                        parameterMask.add(ParamType.FormID);
                        log.write("  Parameter " + parameter + " " + ParamType.FormID + "\n");
                    } else {
                        parameterMask.add(ParamType.Int);
                        log.write("  Parameter " + parameter + " " + ParamType.Int + "\n");
                    }
                }

                if (parameterMask.size() > 3) {
                    log.write("  Skipped.\n");
                    continue;
                }

                // Generate string
                StringBuilder enumString = new StringBuilder(name + " (");
                boolean first = true;
                for (ParamType b : parameterMask) {
                    if (first) {
                        first = false;
                    } else {
                        enumString.append(", ");
                    }
                    enumString.append("ParamType.").append(b);
                }
                enumString.append("), //").append(index);
                while (++curIndex < index) {
                    enumStrings.add("UNKNOWN" + curIndex + " (),");
                }
                enumStrings.add(enumString.toString());

            } catch (NumberFormatException ex) {
                log.write("  Skipped\n");
            }
        }

        for (String s : enumStrings) {
            out.write(s + "\n");
        }

        in.close();
        out.close();
        log.close();
    }

    static ArrayList<ScriptDef> parseScriptData() throws IOException {
        String dir = "Validation Files/";
        ArrayList<ScriptDef> out = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(dir + "EmbeddedScriptSource.txt"));
        BufferedWriter log = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptOutLog.txt"));
        BufferedWriter error = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptOutError.txt"));
        while (in.ready()) {
            String line = in.readLine();
            log.write("Read Line: " + line + "\n");
            Scanner tokenizer = new Scanner(line);
            tokenizer.useDelimiter(",");
            try {
                int index = Integer.parseInt(tokenizer.next().trim());
                log.write("  Index: " + index + "\n");
                String name = tokenizer.next().trim();
                log.write("  Ref: " + name + "\n");
                ArrayList<ParamType> parameterMask = new ArrayList<>();
                while (tokenizer.hasNext()) {
                    String parameter = tokenizer.next().trim().toUpperCase();
                    if (parameter.equals("")) {
                        continue;
                    }
                    if (parameter.contains("?")) {
                        error.write(name + " Unknown Param: " + parameter + "\n");
                        log.write("Unknown: " + parameter + "\n");
                    } else if (parameter.contains("STRING")) {
                        parameterMask.add(ParamType.String);
                        log.write("  Parameter " + parameter + " " + ParamType.String + "\n");
                    } else if (parameter.contains("AXIS")) {
                        parameterMask.add(ParamType.Axis);
                        log.write("  Parameter " + parameter + " " + ParamType.Axis + "\n");
                    } else if (parameter.contains("FORMID")) {
                        parameterMask.add(ParamType.FormID);
                        log.write("  Parameter " + parameter + " " + ParamType.FormID + "\n");
                    } else if (parameter.contains("SEX")) {
                        parameterMask.add(ParamType.Gender);
                        log.write("  Parameter " + parameter + " " + ParamType.Gender + "\n");
                    } else if (parameter.contains("CRIME")) {
                        parameterMask.add(ParamType.CrimeType);
                        log.write("  Parameter " + parameter + " " + ParamType.CrimeType + "\n");
                    } else if (parameter.contains("CASTING SOURCE")) {
                        parameterMask.add(ParamType.CastingSource);
                        log.write("  Parameter " + parameter + " " + ParamType.CastingSource + "\n");
                    } else if (parameter.contains("WARD STATE")) {
                        parameterMask.add(ParamType.WardState);
                        log.write("  Parameter " + parameter + " " + ParamType.WardState + "\n");
                    } else if (parameter.contains("FLOAT")) {
                        parameterMask.add(ParamType.Float);
                        log.write("  Parameter " + parameter + " " + ParamType.Float + "\n");
                    } else if (parameter.contains("INT")) {
                        parameterMask.add(ParamType.Int);
                        log.write("  Parameter " + parameter + " " + ParamType.Int + "\n");
                    } else {
                        error.write(name + " Unknown Param: " + parameter + "\n");
                        log.write("Unknown: " + parameter + "\n");
                    }
                }

                if (parameterMask.size() > 3) {
                    log.write("  Skipped.\n");
                    error.write("Skipped: " + line + ".  Had more than 3 params.\n");
                    continue;
                }

                ScriptDef def = new ScriptDef();
                def.name = name;
                def.index = index;
                def.params = parameterMask;
                out.add(def);

            } catch (NumberFormatException ex) {
                log.write("  Skipped\n");
            }
        }


        in.close();
        log.close();
        error.close();

        return out;
    }

    /**
     * @throws IOException
     */
    public static void generateEnum() throws IOException {
        ArrayList<ScriptDef> scripts = parseScriptData();
        String dir = "Validation Files/";
        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptOut.txt"));

        for (ScriptDef d : scripts) {
            out.write(d.print());
        }

        out.close();
    }

    /**
     * @throws IOException
     */
    public static void generateEnums() throws IOException {
        ArrayList<ScriptDef> scripts = parseScriptData();
        String dir = "Validation Files/";
        BufferedWriter out = new BufferedWriter(new FileWriter(dir + "EmbeddedScriptOut.txt"));
        ArrayList<String> enumStrings = new ArrayList<>();

        LMergeMap<String, ScriptDef> enumBins = new LMergeMap<>(false);

        for (ScriptDef d : scripts) {
            StringBuilder category = new StringBuilder();
            boolean first = true;
            for (ParamType t : d.params) {
                if (first) {
                    first = false;
                } else {
                    category.append("_");
                }
                category.append(t.toString());
            }
            if (category.toString().equals("")) {
                category = new StringBuilder("NoParams");
            }
            category.insert(0, "P_");
            enumBins.put(category.toString(), d);
        }

        for (String enumName : enumBins.keySet()) {
            ArrayList<ScriptDef> scriptDefs = enumBins.get(enumName);
            out.write("public enum " + enumName + " {\n");
            for (int i = 0; i < scriptDefs.size(); i++) {
                out.write("\t" + scriptDefs.get(i).name + "(" + scriptDefs.get(i).index + ")");
                if (i < scriptDefs.size() - 1) {
                    out.write(",");
                } else {
                    out.write(";");
                }
                out.write("\n");
            }

            out.write("\n\tint index;\n\n");
            out.write("\t" + enumName + "(int index) {\n"
                    + "\t\tthis.index = index;\n"
                    + "\t}\n\n");

            ScriptDef d = scriptDefs.get(0);
            out.write("\tpublic ParamType getType(Param p) {\n");
            out.write("\t\tswitch (p){\n");
            for (Param p : Param.values()) {
                ParamType t;
                if (p.ordinal() < d.params.size()) {
                    t = d.params.get(p.ordinal());
                } else {
                    t = ParamType.NULL;
                }
                out.write("\t\t\tcase " + p + ":\n");
                out.write("\t\t\t\treturn ParamType." + t + ";\n");
            }
            out.write("\t\t\tdefault:\n");
            out.write("\t\t\t\treturn ParamType.NULL;\n");
            out.write("\t\t}\n");
            out.write("\t}\n");
            out.write("}\n\n");
        }

        for (String enumName : enumBins.keySet()) {
            out.write("\t\tfor (" + enumName + " e : " + enumName + ".values()){\n");
            out.write("\t\t\tscriptMap.put(e.index, e);\n");
            out.write("\t\t}\n\n");
        }

        out.close();
    }


    public enum ParamType {


        FormID,

        Int,

        String,

        Axis,

        Gender,

        CrimeType,

        CastingSource,

        WardState,

        Float,

        NULL
    }


    public enum Param {


        One,

        Two,

        Three
    }

    //
    // Note: see EmbeddedScriptsSources.txt
    //
    static class ScriptDef {

        int index;
        String name;
        ArrayList<ParamType> params = new ArrayList<>();

        ScriptDef() {
        }

        String print() {
            StringBuilder out = new StringBuilder(name + " (" + index);
            for (ParamType b : params) {
                out.append(", ParamType.").append(b);
            }
            out.append("),\n");
            return out.toString();
        }
    }

}
