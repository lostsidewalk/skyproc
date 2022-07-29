package skyproc;

import lev.LImport;
import lev.Ln;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

/**
 * @author Justin Swanson
 */
class Owner extends SubRecordTyped {

    final FormID owner = new FormID();
    final FormID global = new FormID();
    int reqRank = 0;
    float itemCondition = 0;

    Owner() {
        super("COED");
    }

    @Override
    SubRecord getNew(String type) {
        return new Owner();
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>(2);
        out.add(owner);
        out.add(global);
        return out;
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        owner.export(out);
        if (isNPCOwner()) {
            global.export(out);
        } else {
            out.write(reqRank);
        }
        out.write(itemCondition);
    }

    boolean isNPCOwner() {
        return SPDatabase.queryMajor(owner, GRUP_TYPE.NPC_);
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        super.parseData(in, srcMod);
        owner.parseData(in, srcMod);
        global.parseData(in, srcMod);
        reqRank = Ln.arrayToInt(global.form);
        itemCondition = in.extractFloat();
    }

    @Override
    boolean isValid() {
        // valid if it has an owner, or it has a global, or it has modified health
        return (!owner.isNull() || (!isNPCOwner() && !global.isNull()) || itemCondition != 0.0);
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return 12;
    }

    float getItemCondition() {
        return itemCondition;
    }

    void setItemCondition(float itemCondition) {
        this.itemCondition = itemCondition;
    }

}
