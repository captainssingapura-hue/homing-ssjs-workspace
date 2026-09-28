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
 * state restored into a workspace and read back to the same state, byte for
 * byte, with a stand-in workspace that behaves as the desk, the docks and the
 * tab source do where the projection touches them.
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
                    return {
                        docks: { grid: { layout: function () { return SplitGridTree.validate(tree); } }, region: function (id) { return docks[id] || null; } },
                        desk: { register: { get: function (id) { return { title: function () { return titles[id]; } }; } },
                                floats: function () { return floats.slice(); },
                                float: function (o) { var f = { id: o.id, host: fakeHost(), frame: { bounds: function () { return { x: o.x, y: o.y, w: o.w, h: o.h }; } } }; floats.push(f); return f; } },
                        source: { has: function (k) { return k !== "gone"; }, kindOf: function (id) { return kinds[id]; },
                                  add: function (dock, kind, how, back) { titles[back.id] = back.title; kinds[back.id] = kind; dock._add(back.id); } }
                    };
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

    @Test
    void aStateRestoredReadsBackAsThatState() {
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
                WorkspaceProjection.restore(ws, state);
                const read = WorkspaceProjection.read(ws);
                WorkspaceProjection.same(state, read) + " " + JSON.stringify(GridStateCodec.transformTo(read.regions.length ? read : state)).length
                """).asString();
        assertTrue(same.startsWith("true "), same);
    }

    @Test
    void aTabOfAKindThePageDoesNotKnowIsNotRestored_andTheReadSaysSo() {
        assertEquals(false, js.eval("js", """
                const state = GridStateCodec.transformFrom({ layout: { type: "Cell", region: "main" },
                    regions: [ { id: "main", tabs: ["tab-1"], shown: "tab-1" } ], floats: [],
                    tabs: [ { id: "tab-1", kind: "gone", title: "Gone" } ] });
                const ws = fakeWorkspace({ kind: "cell", id: "main" });
                WorkspaceProjection.restore(ws, state);
                WorkspaceProjection.same(state, WorkspaceProjection.read(ws))
                """).asBoolean());
    }
}
