package hue.captains.singapura.js.homing.workspace.log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * One of a widget's params: a key and its value, both strings. A widget is
 * made of its kind and its params, and comes back from them: the log writes
 * them as a list of these, in the order of their keys, each key once - the
 * order the id's concise form is taken in, so a widget's params are one list
 * however they were given.
 *
 * @param key   what the param is; not empty
 * @param value what it is set to; any string
 */
public record WidgetParam(String key, String value) {

    public WidgetParam {
        Objects.requireNonNull(key, "WidgetParam.key");
        Objects.requireNonNull(value, "WidgetParam.value");
        if (key.isEmpty()) throw new IllegalArgumentException("WidgetParam.key — not empty");
    }

    /** Params as the log writes them: in the order of their keys. */
    public static List<WidgetParam> of(Map<String, String> params) {
        var out = new ArrayList<WidgetParam>();
        for (var e : new TreeMap<>(params).entrySet()) out.add(new WidgetParam(e.getKey(), e.getValue()));
        return List.copyOf(out);
    }

    /** Params as a widget is given them: key to value, in the order of their keys. */
    public static Map<String, String> asMap(List<WidgetParam> params) {
        var out = new LinkedHashMap<String, String>();
        for (var p : params) out.put(p.key(), p.value());
        return out;
    }

    /** Params as the log holds them - each key after the one before it, so in order and once - or refused, saying where. */
    public static List<WidgetParam> checked(List<WidgetParam> params, String where) {
        var list = List.copyOf(Objects.requireNonNull(params, where));
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).key().compareTo(list.get(i).key()) >= 0) {
                throw new IllegalArgumentException(where + " — the keys in order, each once: '" + list.get(i - 1).key() + "' then '" + list.get(i).key() + "'");
            }
        }
        return list;
    }
}
