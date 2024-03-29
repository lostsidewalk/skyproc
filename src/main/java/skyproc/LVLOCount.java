package skyproc;

/**
 * @author pc tech
 */
public class LVLOCount {

    final LeveledEntry lvlo;
    int count;

    LVLOCount(LeveledEntry in) {
        lvlo = in;
        count = 1;
    }

    public void add() {
        count++;
    }

    public void remove() {
        if (count >= 1) {
            count--;
        } else {
            count = 0;
        }
    }

    public void modify(int num) {
        count += num;
        if (count < 0) {
            count = 0;
        }
    }
}
