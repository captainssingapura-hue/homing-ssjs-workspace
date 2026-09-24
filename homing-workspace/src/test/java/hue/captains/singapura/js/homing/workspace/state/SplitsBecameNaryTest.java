package hue.captains.singapura.js.homing.workspace.state;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Schema 1 → 2. The migration claims it cannot fail; these are the reasons.
 */
class SplitsBecameNaryTest {

    private static Map<String, Object> leaf(String id) {
        return new LinkedHashMap<>(Map.of("kind", "Leaf", "paneId", id));
    }

    private static Map<String, Object> binary(String orientation, double ratio, Object first, Object second) {
        var m = new LinkedHashMap<String, Object>();
        m.put("kind", "Split");
        m.put("orientation", orientation);
        m.put("ratio", ratio);
        m.put("first", first);
        m.put("second", second);
        return m;
    }

    private static Map<String, Object> state(Object layout) {
        var m = new LinkedHashMap<String, Object>();
        m.put("schemaVersion", 1);
        m.put("layout", layout);
        m.put("widgetsById", Map.of());
        return m;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> layoutOf(Map<String, Object> migrated) {
        return (Map<String, Object>) migrated.get("layout");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> tracksOf(Map<String, Object> split) {
        return (List<Map<String, Object>>) split.get("children");
    }

    @Test
    void itSaysWhichVersionsItJoins() {
        assertEquals(1, SplitsBecameNary.INSTANCE.fromVersion());
        assertEquals(2, SplitsBecameNary.INSTANCE.toVersion());
    }

    @Test
    void aBinarySplitBecomesTwoTracks() {
        var out = SplitsBecameNary.INSTANCE.migrate(
                state(binary("HORIZONTAL", 0.3, leaf("a"), leaf("b"))));

        assertEquals(2, out.get("schemaVersion"));
        var split = layoutOf(out);
        assertEquals("HORIZONTAL", split.get("orientation"));
        assertFalse(split.containsKey("ratio"),  "the single ratio is gone");
        assertFalse(split.containsKey("first"),  "the named halves are gone");
        assertFalse(split.containsKey("second"));

        var tracks = tracksOf(split);
        assertEquals(2, tracks.size());
        assertEquals(0.3, (Double) tracks.get(0).get("ratio"), 1e-9);
        assertEquals(0.7, (Double) tracks.get(1).get("ratio"), 1e-9, "the second share is what is left");
        assertEquals(leaf("a"), tracks.get(0).get("node"));
        assertEquals(leaf("b"), tracks.get(1).get("node"));
    }

    @Test
    void itReachesAllTheWayDown() {
        var out = SplitsBecameNary.INSTANCE.migrate(state(
                binary("VERTICAL", 0.5,
                        binary("HORIZONTAL", 0.25, leaf("tl"), leaf("tr")),
                        leaf("bottom"))));

        var tracks = tracksOf(layoutOf(out));
        @SuppressWarnings("unchecked")
        var nested = (Map<String, Object>) tracks.get(0).get("node");
        assertEquals(0.25, (Double) tracksOf(nested).get(0).get("ratio"), 1e-9);
        assertEquals(0.75, (Double) tracksOf(nested).get(1).get("ratio"), 1e-9);
    }

    /**
     * A leaf is a leaf at either version, which is why every
     * {@code WidgetLocation.InPane} still resolves after the migration: widgets
     * reference panes by id, and no id is touched.
     */
    @Test
    void aLeafIsUnchangedAndSoAreThePaneIds() {
        var out = SplitsBecameNary.INSTANCE.migrate(state(leaf("only")));
        assertEquals(leaf("only"), layoutOf(out));
        assertEquals(2, out.get("schemaVersion"));
    }

    /** Running it twice is running it once: schema 2 has children already. */
    @Test
    void itIsIdempotent() {
        var once  = SplitsBecameNary.INSTANCE.migrate(state(binary("HORIZONTAL", 0.4, leaf("a"), leaf("b"))));
        var twice = SplitsBecameNary.INSTANCE.migrate(once);
        assertEquals(once.get("layout"), twice.get("layout"));
    }

    @Test
    void whatItTouchesIsTheLayoutAndNothingElse() {
        var in = state(binary("HORIZONTAL", 0.5, leaf("a"), leaf("b")));
        in.put("chrome", Map.of("theme", "editorial"));
        var out = SplitsBecameNary.INSTANCE.migrate(in);
        assertEquals(Map.of("theme", "editorial"), out.get("chrome"));
        assertEquals(Map.of(), out.get("widgetsById"));
    }

    /**
     * The result is a schema-2 layout, so the record accepts it. Written out as
     * the record rather than the JSON, which is the shape the codec builds.
     */
    @Test
    void theResultIsALayoutTheRecordAccepts() {
        var out = tracksOf(layoutOf(SplitsBecameNary.INSTANCE.migrate(
                state(binary("HORIZONTAL", 0.3, leaf("a"), leaf("b"))))));

        var rebuilt = new LayoutNode.Split(Orientation.HORIZONTAL, List.of(
                new LayoutNode.Child(new LayoutNode.Leaf(PaneId.of("a")), (Double) out.get(0).get("ratio")),
                new LayoutNode.Child(new LayoutNode.Leaf(PaneId.of("b")), (Double) out.get(1).get("ratio"))));

        assertEquals(2, rebuilt.children().size());
        assertEquals(0.3, rebuilt.ratio(0), 1e-9);
        assertTrue(rebuilt.child(0) instanceof LayoutNode.Leaf);
    }
}
