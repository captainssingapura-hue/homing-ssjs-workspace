package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * How a widget's id is made: its PREFIX - the kind, and a concise form of its
 * params when it has any - and a SEQUENCE, climbing for each prefix, never
 * reused. The JavaScript is this, rule for rule (WidgetIdsModule.js), and the
 * two agree (WidgetIdsParityTest).
 *
 * <p>The concise form: the params' values in the order of their keys, each
 * with every run of what is not a letter or a digit made one hyphen and none
 * at its ends, joined by underscores - {@code columns=title,rating} is
 * {@code title-rating}. A value with nothing left in it is kept as its hash; a
 * form longer than {@link #CONCISE_MAX} is the hash of the params whole: eight
 * hex digits of FNV-1a over the params written {@code key=value&…} in the
 * order of their keys, over their UTF-16 code units - the same in both
 * languages.</p>
 */
public final class WidgetIds {

    private WidgetIds() {}

    /** The longest concise form kept as it is; a longer one is the params' hash. */
    public static final int CONCISE_MAX = 24;

    private static final Pattern KIND = Pattern.compile("[a-z][a-z0-9-]*");
    private static final Pattern NOT_ALNUM = Pattern.compile("[^A-Za-z0-9]+");

    /** A widget's prefix: its kind, and {@code _} and the concise form of its params when it has any. */
    public static String prefix(String kind, Map<String, String> params) {
        if (kind == null || !KIND.matcher(kind).matches()) throw new IllegalArgumentException("a kind is lowercase letters, digits and hyphens, a letter first: '" + kind + "'");
        String concise = concise(params);
        return concise.isEmpty() ? kind : kind + "_" + concise;
    }

    /** The concise form of params: nothing for none. */
    public static String concise(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        var sorted = new TreeMap<>(params);
        var out = new StringBuilder();
        for (var e : sorted.entrySet()) {
            String v = NOT_ALNUM.matcher(e.getValue() == null ? "" : e.getValue()).replaceAll("-").replaceAll("^-+|-+$", "");
            if (v.isEmpty()) v = hash(e.getValue() == null ? "" : e.getValue());
            if (out.length() > 0) out.append('_');
            out.append(v);
        }
        return out.length() > CONCISE_MAX ? hash(written(sorted)) : out.toString();
    }

    /** The params as one string, {@code key=value&…} in the order of their keys: what the hash is taken over. */
    static String written(Map<String, String> params) {
        var out = new StringBuilder();
        for (var e : new TreeMap<>(params).entrySet()) {
            if (out.length() > 0) out.append('&');
            out.append(e.getKey()).append('=').append(e.getValue() == null ? "" : e.getValue());
        }
        return out.toString();
    }

    /** Eight hex digits of FNV-1a (32 bits) over a string's UTF-16 code units. */
    static String hash(String s) {
        int h = 0x811c9dc5;
        for (int i = 0; i < s.length(); i++) {
            h ^= s.charAt(i);
            h *= 0x01000193;
        }
        return String.format("%08x", h);
    }

    /** The id of the n-th widget of a prefix: the prefix, a hyphen, the sequence - a WidgetId, which is a prefix and a sequence. */
    public static WidgetId of(String prefix, int n) {
        if (n < 1) throw new IllegalArgumentException("a sequence starts at 1: " + n);
        return WidgetId.of(prefix, n);
    }
}
