package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The split grid as a placement, headless, over a stand-in desk that moves,
 * shows and unmounts as the real one does where the placement touches it: a
 * mount is a move into a host, shown as asked; an unmount is said by the
 * placement - the host did not start it - before the tab-pane is taken out;
 * and a grid state comes back with the tabs the roster holds, the rest let go
 * as the grid's events.
 */
class GridPlacementTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        loadModule(DIR + "ui/panes/PaneEventsModule.js");
        loadModule(DIR + "workspace/shell/GridPlacementModule.js");
        js.eval("js", """
                var log = [];
                function host(slotId) {
                    var tabs = [], active = null;
                    return { slotId: slotId, tabs: function () { return tabs.slice(); }, activeTab: function () { return active; },
                             has: function (t) { return tabs.indexOf(t) >= 0; }, tabIndexOf: function (t) { return tabs.indexOf(t); },
                             switchTab: function (t) { active = t; },
                             _in: function (tp, i) { tabs.splice(i == null ? tabs.length : i, 0, tp.id); tp._host = this; if (active === null) active = tp.id; },
                             _out: function (tp) { var i = tabs.indexOf(tp.id); tabs.splice(i, 1); tp._host = null;
                                                   if (active === tp.id) active = tabs[i] || tabs[i - 1] || null; } };
                }
                var main = host("main"), side = host("side"), floats = [], register = new Map();
                var desk = {
                    register: { ids: function () { return Array.from(register.keys()); }, get: function (id) { return register.get(id) || null; } },
                    docks: function () { return [main, side].concat(floats.map(function (f) { return f.host; })); },
                    floats: function () { return floats.slice(); },
                    float: function (o) { var f = { id: o.id, host: host(o.id), close: function () { floats.splice(floats.indexOf(f), 1); log.push("float-closed:" + f.id); } };
                                          floats.push(f); return f; },
                    move: function (tp, to, i) { if (tp._host) tp._host._out(tp); to._in(tp, i); log.push("move:" + tp.id + ">" + to.slotId); },
                    show: function (tp) { log.push("front:" + tp.id); },
                    unmount: function (tp) { tp._host._out(tp); log.push("unmount:" + tp.id); }
                };
                var docks = { regions: function () { return [{ id: "main", dock: main }, { id: "side", dock: side }]; },
                              region: function (id) { return id === "main" ? { dock: main } : id === "side" ? { dock: side } : null; } };
                var placement = new GridPlacement({ desk: desk, docks: docks, tabs: { tab: function (id) { return register.get(id) || null; } },
                    onEvent: function (ev) { log.push(ev.kind + ":" + ev.slotId + ":" + (ev.tab ? ev.tab.id + "@" + ev.fromIndex : ev.tabId)); } });
                /** a widget's tab-pane, lent by the register and in no host: what the core has made */
                function lent(id) {
                    var tp = { id: id, _host: null, host: function () { return tp._host; }, widget: { activate: function () { log.push("activate:" + id); } } };
                    register.set(id, tp);
                    return { id: id };
                }
                """);
    }

    private String log() { return js.eval("js", "var s = log.join(' '); log.length = 0; s").asString(); }

    @Test
    void aMountIsAMoveIntoTheHost_shownAsAsked() {
        js.eval("js", "placement.mount(lent('a-1'), null)");
        assertEquals("move:a-1>main front:a-1", log(), "the first region, in front, unless said");
        js.eval("js", "placement.mount(lent('b-1'), { slotId: 'side', how: 'focus' }); placement.mount(lent('c-1'), { slotId: 'main', index: 0, how: 'quiet' })");
        assertEquals("move:b-1>side front:b-1 activate:b-1 move:c-1>main", log(), "shown and handed the keys; quiet, only placed");
        assertEquals("c-1,a-1", js.eval("js", "main.tabs().join(',')").asString(), "where it was asked");
        assertThrows(PolyglotException.class, () -> js.eval("js", "placement.mount(lent('d-1'), { slotId: 'nowhere' })"));
        assertThrows(PolyglotException.class, () -> js.eval("js", "placement.mount(lent('e-1'), { how: 'sideways' })"));
        assertThrows(PolyglotException.class, () -> js.eval("js", "placement.mount({ id: 'never-lent-1' }, null)"));
    }

    @Test
    void anUnmountIsSaidByThePlacement_thenTheTabPaneTakenOut() {
        js.eval("js", "placement.mount(lent('a-1'), null); placement.mount(lent('b-1'), null); log.length = 0; placement.unmount({ id: 'b-1' })");
        assertEquals("TabRemoved:main:b-1@1 unmount:b-1", log(), "said where it was, before it leaves");
        js.eval("js", "placement.unmount({ id: 'b-1' })");
        assertEquals("", log(), "in no host: nothing to say or do");
        assertEquals("b-1", js.eval("js", "placement.unplaced().join(',')").asString(), "its tab-pane still open, in no host");
    }

    /**
     * Coming back: the tabs the roster holds, where the grid has them, and the one each host shows; a float
     * where it lay. A tab the roster does not hold is a stray; let go, it is said closed, each host it was in
     * says what it shows now, and a float it leaves empty closes.
     */
    @Test
    void aGridStateComesBack_andWhatTheRosterDoesNotHoldIsLetGo() {
        String strays = js.eval("js", """
                lent("a-1"); lent("b-1");
                var state = GridStateCodec.transformFrom({
                    layout: { type: "Split", axis: "HORIZONTAL", tracks: [
                        { node: { type: "Cell", region: "main" }, share: { units: 500000, scale: 6 } },
                        { node: { type: "Cell", region: "side" }, share: { units: 500000, scale: 6 } } ] },
                    regions: [ { id: "main", tabs: ["a-1", "x-1", "b-1"], shown: "x-1" }, { id: "side", tabs: [], shown: null } ],
                    floats: [ { id: "float-1", x: 0, y: 0, w: 320, h: 220, tabs: ["x-2"], shown: "x-2" } ],
                    tabs: [ { id: "a-1", kind: "a", title: "A" }, { id: "x-1", kind: "x", title: "X" }, { id: "b-1", kind: "b", title: "B" },
                            { id: "x-2", kind: "x", title: "X 2" } ] });
                var back = placement.restore(state);
                back.strays.map(function (s) { return s.id + "@" + s.slotId + "#" + s.index; }).join(",")
                """).asString();
        assertEquals("x-1@main#1,x-2@float-1#0", strays);
        assertEquals("move:a-1>main move:b-1>main", log(), "the held ones placed; nothing said by the placement");
        assertEquals("a-1,b-1 a-1 1", js.eval("js", "main.tabs().join(',') + ' ' + main.activeTab() + ' ' + floats.length").asString(),
                "the stray it showed is not there: it shows its first; the float made, though nothing of it came back");
        js.eval("js", "placement.letGo(back.strays)");
        assertEquals("TabRemoved:main:x-1@1 TabRemoved:float-1:x-2@0 TabActivated:main:a-1 float-closed:float-1", log(),
                "each stray closed; the host says what it shows; the empty float closed");
        assertTrue(js.eval("js", "placement.unplaced().length === 0").asBoolean());
    }
}
