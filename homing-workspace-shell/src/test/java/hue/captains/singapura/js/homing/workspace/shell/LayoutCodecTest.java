package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Diligent-Secretaries-style unit test for {@code LayoutCodec}. Pure
 * functions, no DOM — exercised under GraalVM with minimal stand-in
 * stubs for {@code LayoutNode}, {@code PaneId}, {@code Orientation}.
 *
 * <p>Style note: fixtures are <b>full JS literals</b> built via
 * {@code js.eval(...)}, not Java-side {@code Map} structures marshalled
 * across the boundary. Java's role is lifecycle, the polyglot boundary,
 * and assertions; the JS test content stays in JS.</p>
 */
class LayoutCodecTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/LayoutCodecModule.js";

    /** Stand-in classes — match the contract LayoutCodec exercises. */
    private static final String STUBS = """
            class PaneId {
                constructor(value) { this.value = value; }
            }
            class LayoutNodeLeaf {
                constructor(paneId) { this.paneId = paneId; }
            }
            class LayoutNodeChild {
                constructor(node, ratio) { this.node = node; this.ratio = ratio; }
            }
            class LayoutNodeSplit {
                constructor(orientation, children) {
                    this.orientation = orientation;
                    let sum = 0;
                    for (const c of children) sum += c.ratio;
                    this.children = children.map(c => new LayoutNodeChild(c.node, c.ratio / sum));
                }
                static of(orientation, ratio, first, second) {
                    return new LayoutNodeSplit(orientation, [
                        new LayoutNodeChild(first, ratio),
                        new LayoutNodeChild(second, 1.0 - ratio)]);
                }
            }
            const LayoutNode = { Leaf: LayoutNodeLeaf, Child: LayoutNodeChild, Split: LayoutNodeSplit };
            const Orientation = { HORIZONTAL: 'H', VERTICAL: 'V' };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    private Value codec() { return global("LayoutCodec").getMember("INSTANCE"); }

    /** Evaluate a JS expression and return the {@link Value}.
     *  Tiny helper so tests read as "give me this JS literal". */
    private Value jsLiteral(String expression) {
        return js.eval("js", expression);
    }

    /** Deep-stringify a Value via JSON for whole-tree equality assertions. */
    private String jsonStringify(Value v) {
        Value json = js.getBindings("js").getMember("JSON");
        return json.getMember("stringify").execute(v).asString();
    }

    @Test
    void leafRoundTrip() {
        Value mtLeaf = jsLiteral("({ kind: 'leaf', slotId: 'slot-a' })");
        Value typed  = codec().invokeMember("mtToTyped", mtLeaf);
        assertEquals("slot-a", typed.getMember("paneId").getMember("value").asString());

        Value backMt = codec().invokeMember("typedToMt", typed);
        assertEquals("leaf",   backMt.getMember("kind").asString());
        assertEquals("slot-a", backMt.getMember("slotId").asString());
    }

    @Test
    void everyTrackKeepsItsShareBothWays() {
        Value mtSplit = jsLiteral("""
                ({
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.2 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.3 },
                        { pane: { kind: 'leaf', slotId: 'c' }, ratio: 0.5 }
                    ]
                })""");

        // Three tracks arrive as three. Schema 1 folded them into a split inside
        // a split, which is a different arrangement rather than a spelling of
        // this one - so nothing is folded in either direction now.
        Value typed = codec().invokeMember("mtToTyped", mtSplit);
        assertEquals("H", typed.getMember("orientation").asString());
        assertEquals(3, typed.getMember("children").getArraySize());
        assertEquals(0.2, typed.getMember("children").getArrayElement(0).getMember("ratio").asDouble(), 1e-9);
        assertEquals("a", typed.getMember("children").getArrayElement(0)
                            .getMember("node").getMember("paneId").getMember("value").asString());
        assertEquals("c", typed.getMember("children").getArrayElement(2)
                            .getMember("node").getMember("paneId").getMember("value").asString());

        Value back = codec().invokeMember("typedToMt", typed);
        assertEquals("split",      back.getMember("kind").asString());
        assertEquals("horizontal", back.getMember("orientation").asString());
        assertEquals(3, back.getMember("children").getArraySize());
        assertEquals(0.2, back.getMember("children").getArrayElement(0).getMember("ratio").asDouble(), 1e-9);
        assertEquals(0.3, back.getMember("children").getArrayElement(1).getMember("ratio").asDouble(), 1e-9);
        assertEquals(0.5, back.getMember("children").getArrayElement(2).getMember("ratio").asDouble(), 1e-9);
    }

    @Test
    void aTrackWithNoShareOfItsOwnTakesAnEvenOne() {
        // Schema 1 clamped a 0 or a 1 into (0, 1) because the typed record held
        // ONE ratio and both halves had to fit in it. A track carries its own
        // share now, so a share of zero is not an extreme to clamp - it is a
        // track that named none, and an even one is what it gets.
        Value noShares = jsLiteral("""
                ({
                    kind: 'split', orientation: 'vertical',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.0 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.0 }
                    ]
                })""");
        Value typed = codec().invokeMember("mtToTyped", noShares);
        assertEquals(0.5, typed.getMember("children").getArrayElement(0).getMember("ratio").asDouble(), 1e-9);
        assertEquals(0.5, typed.getMember("children").getArrayElement(1).getMember("ratio").asDouble(), 1e-9);

        // And the shares are normalised, so a caller may write 2 and 1 and mean thirds.
        Value unnormalised = jsLiteral("""
                ({
                    kind: 'split', orientation: 'vertical',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 2 },
                        { pane: { kind: 'leaf', slotId: 'b' }, ratio: 1 }
                    ]
                })""");
        Value thirds = codec().invokeMember("mtToTyped", unnormalised);
        assertEquals(2.0 / 3.0, thirds.getMember("children").getArrayElement(0).getMember("ratio").asDouble(), 1e-9);
    }

    @Test
    void nestedRoundTripPreservesStructure() {
        Value original = jsLiteral("""
                ({
                    kind: 'split', orientation: 'horizontal',
                    children: [
                        { pane: { kind: 'leaf', slotId: 'a' }, ratio: 0.5 },
                        { pane: {
                            kind: 'split', orientation: 'vertical',
                            children: [
                                { pane: { kind: 'leaf', slotId: 'b' }, ratio: 0.4 },
                                { pane: { kind: 'leaf', slotId: 'c' }, ratio: 0.6 }
                            ]
                        }, ratio: 0.5 }
                    ]
                })""");
        Value typed = codec().invokeMember("mtToTyped", original);
        Value back  = codec().invokeMember("typedToMt", typed);
        assertEquals(jsonStringify(original), jsonStringify(back),
                "round-trip mt → typed → mt must preserve the layout JSON");
    }

    @Test
    void nullPassesThrough() {
        assertTrue(codec().invokeMember("mtToTyped", (Object) null).isNull());
        assertTrue(codec().invokeMember("typedToMt", (Object) null).isNull());
    }
}
