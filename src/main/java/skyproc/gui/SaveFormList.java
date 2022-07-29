package skyproc.gui;

import lev.gui.Setting;
import skyproc.FormID;

/**
 * @author Justin Swanson
 */
class SaveFormList extends Setting<FormID[]> {

    static final String delimiter = "<#>";

    public SaveFormList(String title_, FormID[] data_, Boolean[] extraFlags) {
        super(title_, data_, extraFlags);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (FormID f : data) {
            out.append(f.getFormStr()).append(delimiter);
        }
        return out.toString();
    }

    @Override
    public void parse(String in) {
        String[] split = in.split(delimiter);
        data = new FormID[split.length];
        for (int i = 0; i < split.length; i++) {
            data[i] = new FormID(split[i]);
        }
    }

    @Override
    public Setting<FormID[]> copyOf() {
        SaveFormList out = new SaveFormList(title, data, extraFlags);
        out.data = new FormID[data.length];
        for (int i = 0; i < data.length; i++) {
            out.data[i] = new FormID(data[i]);
        }
        out.tie = tie;
        return out;
    }

}