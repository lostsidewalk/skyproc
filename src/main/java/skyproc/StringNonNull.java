package skyproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Justin Swanson
 */
class StringNonNull extends Record {

    String data;

    StringNonNull() {
    }

    StringNonNull(String in) {
        data = in;
    }

    public void set(String in) {
        data = in;
    }

    @Override
    void export(ModExporter out) throws IOException {
        out.write(getContentLength(out.getExportMod().isFlag(Mod.Mod_Flags.STRING_TABLED)), 2);
        out.write(data);
    }

    @Override
    boolean isValid() {
        return data != null;
    }

    public boolean equalsIgnoreCase(StringNull in) {
        return equalsIgnoreCase(in.data);
    }

    public boolean equalsIgnoreCase(String in) {
        return data.equalsIgnoreCase(in);
    }

    @Override
    public String toString() {
        return data;
    }

    @Override
    public String print() {
        return toString();
    }

    @Override
    ArrayList<String> getTypes() {
        return Record.getTypeList("NULL");
    }

    @Override
    Record getNew() {
        return new StringNonNull();
    }

    @Override
    int getHeaderLength() {
        return 2;
    }

    @Override
    int getFluffLength() {
        return 0;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return data.length();
    }

    @Override
    int getSizeLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StringNonNull other = (StringNonNull) obj;
        return Objects.equals(this.data, other.data);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.data != null ? this.data.hashCode() : 0);
        return hash;
    }


}
