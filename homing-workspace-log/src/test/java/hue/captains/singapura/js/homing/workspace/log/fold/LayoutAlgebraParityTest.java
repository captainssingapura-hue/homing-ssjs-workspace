package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent.Side;
import hue.captains.singapura.js.homing.workspace.log.Layout.Track;
import hue.captains.singapura.js.homing.workspace.log.codec.LayoutCodec;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The layout algebra held to two things at once, over random walks of parts,
 * removals — toward a neighbour across a splitter, toward one that is not,
 * toward none — and re-sharings: the JavaScript algebra gives the same layout
 * as the Java one, to the byte; and the grid's own SplitGridTree, given the
 * same layout and the same move, gives the same structure, its doubles within
 * a millionth of the exact shares.
 */
class LayoutAlgebraParityTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private Value viaAlgebra, viaGrid;

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        loadModule(DIR + "workspace/log/fold/ExactShareModule.js");
        loadModule(DIR + "workspace/log/fold/LayoutAlgebraModule.js");
        loadModule(DIR + "ui/splitgrid/SplitGridTreeModule.js");
        js.eval("js", """
                function toGrid(l) {
                    return l.type === "Cell" ? { kind: "cell", id: l.region }
                        : { kind: "split", orientation: l.axis === "HORIZONTAL" ? "horizontal" : "vertical",
                            children: l.tracks.map((t) => ({ node: toGrid(t.node), ratio: t.share.units / 1e6 })) };
                }
                function shape(g) { return g.kind === "cell" ? g.id : "(" + g.orientation[0] + ":" + g.children.map((c) => shape(c.node)).join(",") + ")"; }
                function ratios(g, out) { if (g.kind === "split") g.children.forEach((c) => { out.push(c.ratio); ratios(c.node, out); }); return out; }
                function viaAlgebra(op, layout, a, b, c) {
                    const l = LayoutCodec.transformFrom(JSON.parse(layout));
                    const r = op === "subdivide" ? LayoutAlgebra.subdivide(l, new RegionId(a), Side.of(b), new RegionId(c))
                            : op === "remove" ? LayoutAlgebra.remove(l, new RegionId(a), b === null ? null : new RegionId(b))
                            : LayoutAlgebra.tracks(l, a, JSON.parse(b).map((u) => new Scaled(u, 6)));
                    return JSON.stringify(LayoutCodec.transformTo(r));
                }
                function viaGrid(op, layout, a, b, c) {
                    const g0 = toGrid(JSON.parse(layout));
                    const g = op === "subdivide" ? SplitGridTree.subdivide(g0, a, b.toLowerCase(), c) : SplitGridTree.remove(g0, a, b === null ? undefined : b);
                    return shape(g) + "|" + JSON.stringify(ratios(g, []));
                }
                """);
        viaAlgebra = js.getBindings("js").getMember("viaAlgebra");
        viaGrid = js.getBindings("js").getMember("viaGrid");
    }

    private static String text(Layout l) { return JsonText.write(LayoutCodec.INSTANCE.transformTo(l)); }

    private static String shape(Layout l) {
        return switch (l) {
            case Layout.Cell c -> c.region().value();
            case Layout.Split s -> "(" + (s.axis().name().equals("HORIZONTAL") ? "h" : "v") + ":"
                    + String.join(",", s.tracks().stream().map(t -> shape(t.node())).toList()) + ")";
        };
    }

    private static void ratios(Layout l, List<Double> out) {
        if (l instanceof Layout.Split s) for (Track t : s.tracks()) { out.add(t.share().units() / 1e6); ratios(t.node(), out); }
    }

    private record SplitAt(String path, int size) {}

    private static void splits(Layout l, String path, List<SplitAt> out) {
        if (l instanceof Layout.Split s) {
            out.add(new SplitAt(path, s.tracks().size()));
            for (int i = 0; i < s.tracks().size(); i++) splits(s.tracks().get(i).node(), path.isEmpty() ? String.valueOf(i) : path + "/" + i, out);
        }
    }

    private void againstTheGrid(String op, Layout before, Layout after, String a, String b, String c) {
        String[] grid = viaGrid.execute(op, text(before), a, b, c).asString().split("\\|", 2);
        assertEquals(grid[0], shape(after), () -> op + " " + a + " " + b + " " + c + " on " + shape(before));
        String inner = grid[1].substring(1, grid[1].length() - 1);
        double[] theirs = inner.isEmpty() ? new double[0] : java.util.Arrays.stream(inner.split(",")).mapToDouble(Double::parseDouble).toArray();
        var mine = new ArrayList<Double>();
        ratios(after, mine);
        assertEquals(theirs.length, mine.size());
        for (int i = 0; i < theirs.length; i++) {
            double gap = Math.abs(theirs[i] - mine.get(i));
            assertTrue(gap <= 1.000001e-6, "share " + i + " is " + mine.get(i) + ", the grid's " + theirs[i] + " after " + op + " on " + shape(before));
        }
    }

    @Test
    void randomWalksGiveTheGridsStructure_andTheSameLayoutInBothLanguages() {
        int moves = 0;
        for (int seed = 1; seed <= 60; seed++) {
            var rnd = new Random(seed);
            Layout l = new Layout.Cell(RegionId.of("main"));
            int fresh = 0;
            for (int step = 0; step < 24; step++) {
                List<RegionId> cells = Layout.regions(l);
                int pick = rnd.nextInt(10);
                Layout next;
                if (pick < 5 || cells.size() == 1) {
                    RegionId at = cells.get(rnd.nextInt(cells.size()));
                    Side side = Side.values()[rnd.nextInt(4)];
                    RegionId nu = RegionId.of("r" + (++fresh));
                    next = LayoutAlgebra.subdivide(l, at, side, nu);
                    assertEquals(text(next), viaAlgebra.execute("subdivide", text(l), at.value(), side.name(), nu.value()).asString());
                    againstTheGrid("subdivide", l, next, at.value(), side.name(), nu.value());
                } else if (pick < 8) {
                    RegionId gone = cells.get(rnd.nextInt(cells.size()));
                    int how = rnd.nextInt(3);
                    String toward = how == 0 ? null : how == 1 ? gone.value() : cells.get(rnd.nextInt(cells.size())).value();
                    next = LayoutAlgebra.remove(l, gone, Optional.ofNullable(toward).map(RegionId::of));
                    assertEquals(text(next), viaAlgebra.execute("remove", text(l), gone.value(), toward, null).asString());
                    againstTheGrid("remove", l, next, gone.value(), toward, null);
                } else {
                    var at = new ArrayList<SplitAt>();
                    splits(l, "", at);
                    if (at.isEmpty()) continue;
                    SplitAt s = at.get(rnd.nextInt(at.size()));
                    long[] units = new long[s.size()];
                    long left = Layout.WHOLE;
                    for (int i = 0; i < units.length - 1; i++) {
                        units[i] = 1000 + rnd.nextInt((int) (left / (units.length - i)) - 1000);
                        left -= units[i];
                    }
                    units[units.length - 1] = left;
                    var shares = new ArrayList<Scaled>();
                    for (long u : units) shares.add(Scaled.of(u, Track.SCALE));
                    next = LayoutAlgebra.tracks(l, s.path(), shares);
                    assertEquals(text(next), viaAlgebra.execute("tracks", text(l), s.path(), java.util.Arrays.toString(units), null).asString());
                }
                l = next;
                moves++;
            }
        }
        assertTrue(moves > 1000, "moves made: " + moves);
    }
}
