package skyproc;

import lev.LImport;
import skyproc.exceptions.BadParameter;
import skyproc.exceptions.BadRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

class SubMarkerSet<T extends SubRecord> extends SubRecord {

    static String loadedMarker;
    final Map<String, T> set = new HashMap<>(2);
    ArrayList<String> markers;
    final T prototype;
    boolean forceMarkers = false;

    SubMarkerSet(T prototype) {
        super();
        this.prototype = prototype;
    }

    SubMarkerSet(T prototype, String... markers) {
        super();
        this.markers = new ArrayList<>(Arrays.asList(markers));
        this.prototype = prototype;
    }

    @Override
    void export(ModExporter out) throws IOException {
        for (String t : markers) {
            if (set.containsKey(t)) {
                if (set.get(t).isValid()) {
                    SubData marker = new SubData(t);
                    marker.export(out);
                    set.get(t).export(out);
                    continue;
                }
            }
            if (forceMarkers) {
                SubData marker = new SubData(t);
                marker.export(out);
            }
        }
    }

    @Override
    void parseData(LImport in, Mod srcMod) throws BadRecord, DataFormatException, BadParameter {
        String next = Record.getNextType(in);
        if (markers.contains(next)) {
            logMod(srcMod, "", "Loaded Marker " + next);
            loadedMarker = next;
        } else {
            if (!set.containsKey(loadedMarker)) {
                set.put(loadedMarker, (T) prototype.getNew(next));
            }
            set.get(loadedMarker).parseData(in, srcMod);
        }
    }

    @Override
    int getHeaderLength() {
        return 0;
    }

    @Override
    SubRecord getNew(String t) {
        SubMarkerSet out = new SubMarkerSet(prototype);
        out.markers = markers;
        out.forceMarkers = forceMarkers;
        return out;
    }

    @Override
    int getContentLength(boolean isStringTabled) {
        int len = 0;
        for (String t : markers) {
            if (set.containsKey(t)) {
                if (set.get(t).isValid()) {
                    len += 6 + set.get(t).getTotalLength(isStringTabled);
                    continue;
                }
            }
            if (forceMarkers) {
                len += 6;
            }
        }
        return len;
    }

    public void clear() {
        set.clear();
    }

    public T get(String marker) {
        if (!set.containsKey(marker)) {
            T newItem = (T) prototype.getNew();
            set.put(marker, newItem);
        }
        return set.get(marker);
    }

    @Override
    boolean isValid() {
        return true;
    }

    @Override
    ArrayList<FormID> allFormIDs() {
        ArrayList<FormID> out = new ArrayList<>();
        for (T s : set.values()) {
            out.addAll(s.allFormIDs());
        }
        return out;
    }

    @Override
    ArrayList<String> getTypes() {
        ArrayList<String> out = new ArrayList<>(prototype.getTypes());
        out.addAll(markers);
        return out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SubMarkerSet<T> other = (SubMarkerSet<T>) obj;
        for (String marker : markers) {
            if (!set.get(marker).equals(other.set.get(marker))) {
                return false;
            }
        }
        return true;
    }

    /*
     * SkyBash methods.
     */

    /**
     * Merges straight SubMarkerSets with logging capabilities. This is not
     * intended for mainstream use.
     *
     * @param no The new SubMarkerSet to be merged.
     * @param bo The base SubMarkerSet, to prevent base data from being
     *           re-merged.
     * @return The modified SubMarkerSet.
     */
    @Override
    public SubRecord merge(SubRecord no, SubRecord bo) {
        SubMarkerSet<T> s = this;
        if (!(no == null && bo == null && (no instanceof SubMarkerSet) && (bo instanceof SubMarkerSet))) {
            final SubMarkerSet<T> ns = (SubMarkerSet<T>) no;
            final SubMarkerSet<T> bs = (SubMarkerSet<T>) bo;
            if (!s.equals(ns) && !ns.equals(bs)) {
                s = ns;
                if (Merger.fullLogging) {
                    Merger.logMerge(getType(), s.toString());
                }
            }
        }
        return s;
    }
}
