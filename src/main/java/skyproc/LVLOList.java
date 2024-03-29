package skyproc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pc tech
 */
public class LVLOList {

    final HashMap<LeveledEntry, LVLOCount> list = new HashMap<>();
    final ArrayList<LeveledEntry> listLVLO = new ArrayList<>();

    LVLOList() {
    }

    LVLOList(ArrayList<LeveledEntry> in) {
        for (LeveledEntry leveledEntry : in) {
            if (!listLVLO.contains(leveledEntry)) {
                listLVLO.add(leveledEntry);
                list.put(leveledEntry, new LVLOCount(leveledEntry));
            } else {
                list.get(leveledEntry).add();
            }
        }
    }

    public int getCount(LeveledEntry in) {
        return list.get(in).count;
    }

    public void setCount(LeveledEntry in, int num) {
        list.get(in).count = num;
    }

    public boolean contains(LeveledEntry in) {
        return list.containsKey(in);
    }

    public LVLOCount get(LeveledEntry in) {
        return list.get(in);
    }

    public void put(LeveledEntry in, LVLOCount lc, int count) {
        list.put(in, lc);
        lc.count = count;
    }

    public void add(LeveledEntry in) {
        if (list.containsKey(in)) {
            list.get(in).add();
        } else {
            list.put(in, new LVLOCount(in));
        }
    }

    public void remove(LeveledEntry in) {
        if (list.containsKey(in)) {
            list.get(in).remove();
        }
    }

    public void modify(LeveledEntry in, int num) {
        list.get(in).modify(num);
    }
}
