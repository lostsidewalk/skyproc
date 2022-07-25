package skyproc;

import lev.Ln;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import skyproc.exceptions.BadRecord;
import skyproc.gui.SPDefaultGUI;
import skyproc.gui.SPProgressBarPlug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Justin Swanson
 */
@Slf4j
public class SkyProcTester {

    ArrayList<FormID> badIDs;
    //    static GRUP_TYPE[] types = {GRUP_TYPE.DIAL};
    GRUP_TYPE[] types = GRUP_TYPE.values();
    boolean streaming = false;
    ArrayList<GRUP_TYPE> skip = new ArrayList<>(List.of(GRUP_TYPE.BOOK));

    @BeforeEach
    public void beforeEach() {
        setSkyProcGlobal();
        badIDs = new ArrayList<>();
        ModListing skyrim = new ModListing("Skyrim.esm");
        badIDs.add(new FormID("018A45", skyrim));  //RiverwoodZone
        badIDs.add(new FormID("00001E", skyrim));  //NoZoneZone
        SPGlobal.testing = true;
        SPDefaultGUI gui = new SPDefaultGUI("Tester Program", "A tester program meant to flex SkyProc.");
    }

    @Test
    void validateAll() throws Exception {
        ModTestPackage[] mods = {
                new ModTestPackage("Skyrim.esm", "Skyrim.esm", "Update.esm"),
//                new ModTestPackage("Dawnguard.esm", "Skyrim.esm", "Update.esm", "Dawnguard.esm"),
//                new ModTestPackage("Dragonborn.esm", "Skyrim.esm", "Update.esm", "Dragonborn.esm")
        };
        SPGlobal.checkMissingMasters = false;
        for (ModTestPackage p : mods) {
            boolean isValid = validate(p);
            log.info("validated mod={}, isValid={}", p, isValid);
            assertTrue(isValid);
        }
        log.info("TESTING COMPLETE");
    }

    @Test
    void copyTest() throws IOException, BadRecord {
        SPProgressBarPlug.pause(true);

        boolean passed = true;
        Mod merger = new Mod(new ModListing("tmpMerge.esp"));
        merger.addAsOverrides(SPGlobal.getDB());
        for (FormID f : badIDs) {
            merger.remove(f);
        }

        Mod patch = new Mod(new ModListing("Test.esp"));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.setAuthor("Leviathan1753");

        for (GRUP g : merger) {
            for (Object o : g) {
                MajorRecord m = (MajorRecord) o;
                m.copyOf(patch);
            }
        }

        patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
        passed = passed && NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + "Test.esp", 10);

        SPProgressBarPlug.pause(false);
        SPProgressBarPlug.incrementBar();

        assertTrue(passed);
    }

    private boolean validate(ModTestPackage p) throws Exception {

        SubStringPointer.shortNull = false;

        FormID.allIDs.clear();

        SPProgressBarPlug.reset();
        SPProgressBarPlug.setMax(types.length);

        boolean exportPass = true;
        boolean idPass = true;
        for (GRUP_TYPE g : types) {
            if (!GRUP_TYPE.unfinished(g) && !GRUP_TYPE.internal(g) && !skip.contains(g)) {
                for (ModListing m : p.importList) {
                    SPImporter.importMod(m, SPGlobal.pathToDataFixed, g);
                }
                if (!test(g, p)) {
                    SPProgressBarPlug.setStatus("FAILED: " + g);
                    exportPass = false;
                    log.info("GRUP failed validation: {}", g);
                } else {
                    log.info("GRUP passed validation: {}", g);
                }
                SPProgressBarPlug.setStatus("Validating DONE");
                for (FormID id : FormID.allIDs) {
                    if (!id.isNull() && id.getMaster() == null && !badIDs.contains(id)) {
                        log.warn("A bad id: " + id);
                        log.warn("Some FormIDs were unstandardized!");
                        return false;
                    }
                }
                SPGlobal.reset();
            }
        }

        SPGlobal.reset();
        return exportPass && idPass;
    }

    private boolean test(GRUP_TYPE type, ModTestPackage p) throws IOException {
        log.info("Testing {} in {}", type, p.main);
        SPProgressBarPlug.setStatus("Validating " + type);
        SPProgressBarPlug.pause(true);

        String testModName = String.format("%s_Test.esp", type);
        Mod patch = new Mod(new ModListing(testModName));
        patch.setFlag(Mod.Mod_Flags.STRING_TABLED, false);
        patch.addAsOverrides(SPDatabase.getMod(p.main), type);
        // Test to see if stream has been prematurely imported
        if (SPGlobal.streamMode && type != GRUP_TYPE.NPC_) {
            GRUP g = patch.GRUPs.get(type);
            if (!g.listRecords.isEmpty()) {
                MajorRecord m = (MajorRecord) g.listRecords.get(0);
                if (m.subRecords.map.size() > 2) {
                    log.warn("Premature streaming occurred: {}", m);
                    return false;
                }
            }
        }
        // Remove known bad ids
        for (FormID f : badIDs) {
            patch.remove(f);
        }
        patch.setAuthor("Leviathan1753");
        for (ModListing depend : p.importList) {
            patch.addMaster(depend);
        }
        try {
            patch.export(new File(SPGlobal.pathToDataFixed + patch.getName()));
        } catch (BadRecord ex) {
            SPGlobal.logException(ex);
            log.error("Records lengths were off for type={}", type);
        }
        boolean passed = NiftyFunc.validateRecordLengths(SPGlobal.pathToDataFixed + testModName, 10);
        File validF = new File("Validation Files/" + type + "_" + p.main.printNoSuffix() + ".esp");
        if (validF.isFile()) {
            passed = Ln.validateCompare(SPGlobal.pathToDataFixed + testModName, validF.getPath(), 256) && passed;
        } else {
            log.error("Didn't have a source file to validate bytes to for type={}", type);
        }

        if (!passed) {
            log.warn("Validation failed for testFile=" + SPGlobal.pathToDataFixed + testModName + ", keyFile=" + validF.getPath());
        }

        SPProgressBarPlug.pause(false);
        SPProgressBarPlug.incrementBar();
        return passed;
    }

    private void setSkyProcGlobal() {
        SPGlobal.streamMode = streaming;
        SPGlobal.setGlobalPatch(new Mod(new ModListing("Test", false)));
    }

    private static class ModTestPackage {
        ModListing main;
        ModListing[] importList;

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
}
