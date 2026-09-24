package hue.captains.singapura.js.homing.workspace.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema 1 → 2: a split stops being two named halves and becomes its tracks.
 *
 * <pre>{@code
 * 1  { "kind": "Split", "orientation": …, "ratio": r, "first": A, "second": B }
 * 2  { "kind": "Split", "orientation": …, "children": [ {node: A, ratio: r},
 *                                                       {node: B, ratio: 1-r} ] }
 * }</pre>
 *
 * <p>Nothing is guessed and nothing can fail: every binary split is a two-track
 * split, and {@code r} is in (0, 1) by schema 1's own constructor, so the second
 * share is positive. The direction that loses is the other one — three tracks
 * cannot be written as two without nesting them, and a nested pair is a
 * different arrangement, not a different spelling of the same one. That is the
 * whole reason for the schema change; see {@link LayoutNode}.</p>
 *
 * <p>Only the layout is touched. Widgets reference panes by {@link PaneId} and
 * a {@code Leaf} is unchanged, so every {@code WidgetLocation.InPane} still
 * resolves.</p>
 *
 * @since schema 2
 */
public final class SplitsBecameNary implements WorkspaceStateMigration {

    public static final SplitsBecameNary INSTANCE = new SplitsBecameNary();

    private SplitsBecameNary() {}

    @Override public int fromVersion() { return 1; }
    @Override public int toVersion()   { return 2; }

    @Override
    public Map<String, Object> migrate(Map<String, Object> oldStateJson) {
        var out = new LinkedHashMap<String, Object>(oldStateJson);
        out.put("schemaVersion", toVersion());
        Object layout = out.get("layout");
        if (layout instanceof Map<?, ?> m) out.put("layout", node(m));
        return out;
    }

    /** One node, rebuilt; a leaf is itself. */
    private static Map<String, Object> node(Map<?, ?> n) {
        var out = new LinkedHashMap<String, Object>();
        for (Map.Entry<?, ?> e : n.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        if (!"Split".equals(out.get("kind"))) return out;
        if (out.containsKey("children")) return out;          // already schema 2; nothing to do

        double ratio = out.get("ratio") instanceof Number r ? r.doubleValue() : 0.5;
        var children = new ArrayList<Map<String, Object>>(2);
        children.add(track(out.get("first"), ratio));
        children.add(track(out.get("second"), 1.0 - ratio));
        out.remove("ratio");
        out.remove("first");
        out.remove("second");
        out.put("children", List.copyOf(children));
        return out;
    }

    private static Map<String, Object> track(Object child, double ratio) {
        var t = new LinkedHashMap<String, Object>();
        t.put("node", child instanceof Map<?, ?> m ? node(m) : child);
        t.put("ratio", ratio);
        return t;
    }
}
