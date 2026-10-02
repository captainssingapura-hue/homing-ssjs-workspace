package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A widget an arrangement opens: its name in the arrangement, its kind, and its
 * params as its kind reads them from an address - text, by key, in the order of
 * their keys, as the log keeps them.
 *
 * @param ref    the arrangement's name for it, which its placement places it by
 * @param kind   what it is
 * @param params what it is made with: no key empty
 */
public record ArrangedWidget(WidgetRef ref, WidgetKind kind, SortedMap<String, String> params) {

    public ArrangedWidget {
        Objects.requireNonNull(ref, "ArrangedWidget.ref");
        Objects.requireNonNull(kind, "ArrangedWidget.kind");
        Objects.requireNonNull(params, "ArrangedWidget.params");
        var copy = new TreeMap<String, String>();
        params.forEach((k, v) -> {
            if (k == null || k.isEmpty()) throw new IllegalArgumentException("the widget " + ref + " - a param's key, not empty");
            copy.put(k, Objects.requireNonNull(v, "the widget " + ref + " - the param " + k));
        });
        params = Collections.unmodifiableSortedMap(copy);
    }

    /** A widget of that kind, made with no params. */
    public static ArrangedWidget of(String ref, String kind) {
        return new ArrangedWidget(WidgetRef.of(ref), WidgetKind.of(kind), new TreeMap<>());
    }

    /** A widget of that kind, made with these params. */
    public static ArrangedWidget of(String ref, String kind, Map<String, String> params) {
        return new ArrangedWidget(WidgetRef.of(ref), WidgetKind.of(kind), new TreeMap<>(params));
    }
}
