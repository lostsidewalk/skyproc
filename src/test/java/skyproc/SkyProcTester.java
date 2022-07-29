package skyproc;

import lev.Ln;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import skyproc.exceptions.BadRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkyProcTester {

    static final ArrayList<FormID> badIDs;

    static {
        badIDs = new ArrayList<>();
        ModListing skyrim = new ModListing("Skyrim.esm");
        badIDs.add(new FormID("018A45", skyrim));  //RiverwoodZone
        badIDs.add(new FormID("00001E", skyrim));  //NoZoneZone
    }

    static final boolean streaming = false;

    final ArrayList<GRUP_TYPE> skip = new ArrayList<>(List.of(GRUP_TYPE.BOOK));

    private static class ModTestPackage {
        final ModListing main;
        final ModListing[] importList;

        public ModTestPackage(String main, String... list) {
            this.main = new ModListing(main);
            importList = new ModListing[list.length];
            for (int i = 0; i < list.length; i++) {
                importList[i] = new ModListing(list[i]);
            }
        }

        @Override
        public String toString() {
            return "main=" + main + ", importList=" + Arrays.toString(importList);
        }
    }

    @BeforeAll
    public static void beforeAll() {
        SPGlobal.streamMode = streaming;
        SPGlobal.setGlobalPatch(new Mod(new ModListing("Test", false)));
        SPGlobal.testing = true;
    }

    static final String DASHED_LINE = "==========".repeat(10);

    @BeforeEach
    void beforeEach() {
        System.out.println(DASHED_LINE);
    }

    @Test
    void validateGRUPExports() {
        ModTestPackage[] mods = {
                new ModTestPackage("Update.esm", "Skyrim.esm", "Update.esm"),
                new ModTestPackage("Skyrim.esm", "Skyrim.esm", "Update.esm"),
                new ModTestPackage("Dawnguard.esm", "Skyrim.esm", "Update.esm", "Dawnguard.esm"),
                new ModTestPackage("Dragonborn.esm", "Skyrim.esm", "Update.esm", "Dragonborn.esm")
        };
        SPGlobal.checkMissingMasters = false;
        for (GRUP_TYPE grupType : GRUP_TYPE.values()) {
            for (ModTestPackage p : mods) {
                try {
                    Boolean testResult = validateGRUPExport(p, grupType);
                    if (testResult != null) {
                        boolean isValid = testResult;
                        debug("validated GRUP={}, testPackage={}, isValid={}", grupType, p, isValid);
                        assertTrue(isValid);
                    } else {
                        info("Skipping GRUP={}, testPackage={} (no validation file)", grupType, p); // skipped
                    }
                } catch (Exception e) {
                    error(e.getMessage());
                    Assertions.fail(e.getMessage());
                }
            }
        }
        info("TESTING COMPLETE");
    }

    private Boolean validateGRUPExport(ModTestPackage p, GRUP_TYPE g) {
        debug("validateTestPackage p={}, GRUP={}", p, g);
        SubStringPointer.shortNull = false;

        FormID.allIDs.clear();

        boolean exportPass = true;
        if (!GRUP_TYPE.unfinished(g) && !GRUP_TYPE.internal(g) && !skip.contains(g)) {
            try {
                for (ModListing m : p.importList) {
                    SPImporter.importMod(m, SPGlobal.pathToDataFixed, g);
                }
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            }
            try {
                String testModName = doExport(g, p.main, p.importList);
                boolean passed = NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + testModName, 10);
                File validF = new File("Validation Files/" + g + "_" + p.main.printNoSuffix() + ".esp");
                if (validF.isFile()) {
                    passed = Ln.validateCompare(SPGlobal.pathToDataFixed + testModName, validF.getPath(), 0) && passed;
                } else {
                    debug("No source file to validate GRUP={}, expected={}", g, validF.getName());
                    return null; // skipped
                }

                SPGlobal.reset();

                if (!passed) {
                    exportPass = false;
                    warn("Validation failed for testFile={}, keyFile={}", SPGlobal.pathToDataFixed + testModName, validF.getPath());
                } else {
                    info("Validation succeeded for testFile={}, keyFile={}", SPGlobal.pathToDataFixed + testModName, validF.getPath());
                }
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            }
            for (FormID id : FormID.allIDs) {
                if (!id.isNull() && id.getMaster() == null && !badIDs.contains(id)) {
                    warn("A bad id: {}", id);
                    warn("Some FormIDs were unstandardized!");
                    return false;
                }
            }
        }

        SPGlobal.reset();

        return exportPass;
    }

    private String doExport(GRUP_TYPE type, ModListing main, ModListing[] importList) throws IOException {
        info("Testing {} in {}", type, main);

        String testModName = String.format("%s_Test.esp", type);
        Mod patch = new Mod(new ModListing(testModName));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.addAsOverrides(SPDatabase.getMod(main), type);
        // Test to see if stream has been prematurely imported
        if (SPGlobal.streamMode && type != GRUP_TYPE.NPC_) {
            GRUP<?> g = patch.GRUPs.get(type);
            if (!g.listRecords.isEmpty()) {
                MajorRecord m = g.listRecords.get(0);
                if (m.subRecords.map.size() > 2) {
                    warn("Premature streaming occurred: {}", m);
                    return null;
                }
            }
        }
        // Remove known bad ids
        for (FormID f : badIDs) {
            patch.remove(f);
        }
        patch.setAuthor("Leviathan1753");
        for (ModListing depend : importList) {
            patch.addMaster(depend);
        }
        try {
            patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
        } catch (BadRecord ex) {
            SPGlobal.logException(ex);
            error("Records lengths were off for type={}", type);
        }

        return testModName;
    }

    @Test
    void validateGRUPCopy() {
        info("Starting copy test...");

        try {
            doCopy();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

        boolean passed = NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + "Test.esp", 10);

        info("Completed copy test ({})", passed ? "success" : "failure");

        assertTrue(passed);
    }

    private void doCopy() throws BadRecord, IOException {
        Mod merger = new Mod(new ModListing("tmpMerge.esp"));
        merger.addAsOverrides(SPGlobal.getDB());
        for (FormID f : badIDs) {
            merger.remove(f);
        }

        Mod patch = new Mod(new ModListing("Test.esp"));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.setAuthor("Leviathan1753");

        for (GRUP<?> g : merger) {
            for (MajorRecord o : g) {
                o.copyOf(patch);
            }
        }

        patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
    }

    //
    //
    //

    private static final boolean DEBUG_ENABLED = false;

    static void debug(String str, Object... args) {
        if (DEBUG_ENABLED) {
            console(str, args);
        }
    }

    static void info(String str, Object... args) {
        console(str, args);
    }

    static void warn(String str, Object... args) {
        console(str, args);
    }

    static void error(String str, Object... args) {
        console(str, args);
    }

    static void console(String str, Object... args) {
        if (args != null) {
            for (Object arg : args) {
                str = str.replaceFirst("\\{}", arg.toString());
            }
        }

        System.out.println(str);
    }
}
