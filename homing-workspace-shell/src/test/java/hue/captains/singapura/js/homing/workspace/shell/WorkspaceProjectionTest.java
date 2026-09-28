package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The projection's round trip, headless: a layout to the grid's own tree and
 * back — through the grid's own validation — to the same millionths; and a
 * state placed in a workspace and read back to the same state, byte for byte,
 * with a stand-in workspace that behaves as the desk and the docks do where the
 * projection touches them, and places a state as a placement comes back.
 */
class WorkspaceProjectionTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        loadModule(DIR + "ui/splitgrid/SplitGridTreeModule.js");
        loadModule(DIR + "workspace/shell/WorkspaceRecorderModule.js");
        loadModule(DIR + "workspace/shell/WorkspaceProjectionModule.js");
        js.eval("js", """
                var layout = LayoutCodec.transformFrom({ type: "Split", axis: "HORIZONTAL", tracks: [
                    { node: { type: "Cell", region: "main" }, share: { units: 333333, scale: 6 } },
                    { node: { type: "Split", axis: "VERTICAL", tracks: [
                        { node: { type: "Cell", region: "cell-2" }, share: { units: 331342, scale: 6 } },
                        { node: { type: "Cell", region: "cell-3" }, share: { units: 668658, scale: 6 } } ] }, share: { units: 666667, scale: 6 } } ] });
                function fakeHost() {
                    var tabs = [], active = null;
                    return { tabs: function () { return tabs.slice(); }, activeTab: function () { return active; },
                             has: function (t) { return tabs.indexOf(t) >= 0; }, switchTab: function (t) { active = t; },
                             _add: function (t) { tabs.push(t); if (active === null) active = t; } };
                }
                function fakeWorkspace(tree) {
                    var docks = {}, titles = {}, kinds = {}, floats = [];
                    SplitGridTree.cells(tree).forEach(function (id) { docks[id] = { id: id, dock: fakeHost() }; });
                    var ws = {
                        kindOf: function (id) { return kinds[id]; },
                        docks: { grid: { layout: function () { return SplitGridTree.validate(tree); } }, region: function (id) { return docks[id] || null; } },
                        desk: { register: { get: function (id) { return { title: function () { return titles[id]; } }; } },
                                floats: function () { return floats.slice(); },
                                float: function (o) { var f = { id: o.id, host: fakeHost(), frame: { bounds: function () { return { x: o.x, y: o.y, w: o.w, h: o.h }; } } }; floats.push(f); return f; } },
                        /** its tabs where a state has them, as a placement comes back: each host's in order, and the one it shows */
                        place: function (state) {
                            state.tabs.forEach(function (t) { titles[t.id.value] = t.title.value; kinds[t.id.value] = t.kind.value; });
                            function fill(host, h) { h.tabs.forEach(function (id) { host._add(id.value); }); if (h.shown) host.switchTab(h.shown.value); }
                            state.regions.forEach(function (r) { fill(docks[r.id.value].dock, r); });
                            state.floats.forEach(function (f) { fill(ws.desk.float({ id: f.id.value, x: f.x, y: f.y, w: f.w, h: f.h }).host, f); });
                        }
                    };
                    return ws;
                }
                """);
    }

    @Test
    void aLayoutGoesToTheGridsTreeAndComesBackToTheSameMillionths() {
        assertTrue(js.eval("js", """
                const tree = SplitGridTree.validate(WorkspaceProjection.gridLayout(layout));
                JSON.stringify(LayoutCodec.transformTo(WorkspaceProjection.logLayout(tree))) === JSON.stringify(LayoutCodec.transformTo(layout))
                """).asBoolean());
    }

    /**
     * A workspace whose titles are its widgets' compares placements alone - every tab titled by its kind -
     * and one whose log holds tabs it cannot bring back compares what it could: those tabs gone, each host
     * showing its first when the one it showed went, a float kept though they leave it empty.
     */
    @Test
    void untitledComparesPlacementAlone_andWithoutTakesTabsOut() {
        String got = js.eval("js", """
                const s = GridStateCodec.transformFrom({ layout: LayoutCodec.transformTo(layout),
                    regions: [ { id: "main", tabs: ["a-1", "x-1", "b-1"], shown: "x-1" }, { id: "cell-2", tabs: [], shown: null },
                               { id: "cell-3", tabs: ["b-2"], shown: "b-2" } ],
                    floats: [ { id: "float-1", x: 0, y: 0, w: 320, h: 220, tabs: ["x-2"], shown: "x-2" } ],
                    tabs: [ { id: "a-1", kind: "a", title: "A" }, { id: "x-1", kind: "x", title: "X" }, { id: "b-1", kind: "b", title: "B one" },
                            { id: "b-2", kind: "b", title: "B two" }, { id: "x-2", kind: "x", title: "X two" } ] });
                const renamed = GridStateCodec.transformFrom(Object.assign(GridStateCodec.transformTo(s), {
                    tabs: GridStateCodec.transformTo(s).tabs.map(function (t) { return Object.assign({}, t, { title: "Renamed " + t.id }); }) }));
                const w = GridStateCodec.transformTo(WorkspaceProjection.without(s, ["x-1", "x-2"]));
                [WorkspaceProjection.same(s, renamed), WorkspaceProjection.same(WorkspaceProjection.untitled(s), WorkspaceProjection.untitled(renamed)),
                 w.regions[0].tabs.join(","), w.regions[0].shown, w.regions[2].shown, w.floats.length, w.floats[0].tabs.length, String(w.floats[0].shown),
                 w.tabs.map(function (t) { return t.id; }).join(",")].join(" ")
                """).asString();
        assertEquals("false true a-1,b-1 a-1 b-2 1 0 null a-1,b-1,b-2", got);
    }

    @Test
    void aStatePlacedReadsBackAsThatState() {
        String same = js.eval("js", """
                const state = GridStateCodec.transformFrom({ layout: LayoutCodec.transformTo(layout),
                    regions: [ { id: "main", tabs: ["tab-4", "tab-1"], shown: "tab-1" },
                               { id: "cell-2", tabs: [], shown: null },
                               { id: "cell-3", tabs: ["tab-2"], shown: "tab-2" } ],
                    floats: [ { id: "float-3", x: -12, y: 40, w: 320, h: 220, tabs: ["tab-6", "tab-5"], shown: "tab-5" } ],
                    tabs: [ { id: "tab-4", kind: "note", title: "Note 2" }, { id: "tab-1", kind: "field", title: "Groceries" },
                            { id: "tab-2", kind: "counter", title: "Counter" },
                            { id: "tab-6", kind: "note", title: "Mine" }, { id: "tab-5", kind: "opener", title: "Open 5" } ] });
                const ws = fakeWorkspace(WorkspaceProjection.gridLayout(layout));
                ws.place(state);
                const read = WorkspaceProjection.read(ws);
                WorkspaceProjection.same(state, read) + " " + JSON.stringify(GridStateCodec.transformTo(read.regions.length ? read : state)).length
                """).asString();
        assertTrue(same.startsWith("true "), same);
    }

    /** A tab that did not come back reads back as missing: the two are not the same, and without it they are. */
    @Test
    void aTabThatDidNotComeBackIsMissedByTheRead() {
        assertEquals("false true", js.eval("js", """
                const state = GridStateCodec.transformFrom({ layout: { type: "Cell", region: "main" },
                    regions: [ { id: "main", tabs: ["tab-1", "tab-2"], shown: "tab-1" } ], floats: [],
                    tabs: [ { id: "tab-1", kind: "gone", title: "Gone" }, { id: "tab-2", kind: "note", title: "Note" } ] });
                const ws = fakeWorkspace({ kind: "cell", id: "main" });
                ws.place(WorkspaceProjection.without(state, ["tab-1"]));
                const read = WorkspaceProjection.read(ws);
                WorkspaceProjection.same(state, read) + " " + WorkspaceProjection.same(WorkspaceProjection.without(state, ["tab-1"]), read)
                """).asString());
    }
}
