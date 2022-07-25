package skyproc;

import java.io.IOException;

/**
 * @author Justin Swanson
 */
class StringNull extends StringNonNull {

    public StringNull(String in) {
        super(in);
    }

    public StringNull() {
    }

    @Override
    void export(ModExporter out) throws IOException {
        super.export(out);
        out.write(0, 1);
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        return super.getContentLength(isStringTabled) + 1;
    }

    @Override
    Record getNew() {
        return new StringNull();
    }


}
