package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the holder hears from the grid, the docks and the desk: every event
 * naming the holder's own record of the tab; a float never heard of — a tab
 * afloat is where it left, comes down as a move from there, and closed afloat
 * closed there; a merge's moves marked as part of it; and Shift+↓ answered
 * here, at the tab's chip. Over a panes of fakes: the tables are the
 * assembly's, and this is the part that reads and writes them.
 */
class WorkspacePaneEventsTest extends JsModuleTestBase {

    private static final String PANES = "/homing/js/hue/captains/singapura/js/homing/ui/panes/";
    private static final String SHELL = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(PANES + "PaneEventsModule.js");
        loadModule(SHELL + "WorkspacePaneEventsModule.js");
        eval("""
            var sent = [], forgot = [], detached = [], reg = new Map();
            var panes = {
                _tabObjs: new Map(), _floated: new Map(), _merging: null,
                _panes: new Map([['main', {}], ['cell-1', {}]]),
                _emit: function (ev) { sent.push(ev); },
                _forgetIcon: function (id) { forgot.push(id); },
                _desk: { register: { get: function (id) { return reg.get(id) || null; } },
                         detach: function (t, at) { detached.push({ id: t.id, at: at }); } }
            };
            function tabPane(id, pinned) {
                var t = { id: id, pinned: !!pinned, title: function () { return id; },
                          chip: { getBoundingClientRect: function () { return { left: 100, bottom: 40 }; } } };
                reg.set(id, t);
                return t;
            }
            var a = tabPane('tab-1');
            var rec = { id: 'tab-1', title: 'Notes', widgetInstanceUuid: 'Note:1' };
            panes._tabObjs.set('tab-1', rec);
            function fire(ev) { WorkspacePaneEvents.fire(panes, ev); }
            """);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void everyEventNamesTheHoldersRecord_neverTheTabPane() {
        eval("fire(PaneEvents.TabMoved('main', a, 0, 'cell-1', 1)); fire(PaneEvents.TabActivated('cell-1', 'tab-1'));");
        assertEquals(2, eval("sent.length").asInt());
        assertTrue(eval("sent[0].tab === rec && sent[1].tab === rec").asBoolean());
    }

    @Test
    void aTabAfloatIsWhereItLeft_andComesDownAsAMoveFromThere() {
        eval("fire(PaneEvents.TabMoved('main', a, 2, 'float-1', 0)); fire(PaneEvents.TabActivated('float-1', 'tab-1'));");
        assertEquals(0, eval("sent.length").asInt(), "a float is nobody's record");
        eval("fire(PaneEvents.TabMoved('float-1', a, 0, 'float-2', 1));");
        assertEquals(0, eval("sent.length").asInt(), "nor is a float dropped on a float");
        eval("fire(PaneEvents.TabMoved('float-2', a, 1, 'cell-1', 3));");
        assertEquals(1, eval("sent.length").asInt());
        assertEquals("TabMoved main 2 cell-1 3", eval("var m = sent[0]; [m.kind, m.srcSlotId, m.srcIndex, m.destSlotId, m.destIndex].join(' ')").asString(),
                "from where it left the docks to where it landed");
        assertTrue(eval("sent[0].tab === rec && panes._floated.size === 0").asBoolean());
    }

    @Test
    void aTabClosedAfloat_closedWhereItLeft() {
        eval("fire(PaneEvents.TabMoved('main', a, 2, 'float-1', 0)); fire(PaneEvents.TabRemoved('float-1', a, 0));");
        assertEquals("TabRemoved main 2", eval("var r = sent[0]; [r.kind, r.slotId, r.fromIndex].join(' ')").asString());
        assertTrue(eval("sent.length === 1 && sent[0].tab === rec").asBoolean());
        assertFalse(eval("panes._tabObjs.has('tab-1') || panes._floated.has('tab-1')").asBoolean(), "out of the tables");
        assertEquals("tab-1", eval("forgot.join()").asString(), "its icon with it");
    }

    @Test
    void aMergesMovesAreMarkedAsPartOfIt_andItsRemovedSaysToward() {
        eval("panes._merging = { slotId: 'cell-1', toward: 'main' };"
           + "fire(PaneEvents.TabMoved('cell-1', a, 0, 'main', 1)); fire(PaneEvents.TabActivated('main', 'tab-1'));"
           + "fire(Object.freeze({ kind: 'Removed', cellId: 'cell-1' }));");
        assertEquals("cell-1 cell-1", eval("sent[0].merging + ' ' + sent[1].merging").asString(), "the move, and what the dock then shows");
        assertEquals("main", eval("sent[2].toward").asString());
        assertTrue(eval("sent[2].merging === undefined").asBoolean(), "the Removed IS the merge");
    }

    @Test
    void shiftDown_floatsTheTabAtItsChip_unlessPinned() {
        eval("fire(PaneEvents.DetachRequested('main', 'tab-1'));");
        assertEquals("tab-1 160 54", eval("var d = detached[0]; d.id + ' ' + d.at.x + ' ' + d.at.y").asString());
        eval("tabPane('tab-2', true); fire(PaneEvents.DetachRequested('main', 'tab-2'));");
        assertEquals(1, eval("detached.length").asInt(), "a pinned tab stays");
        assertEquals(0, eval("sent.length").asInt(), "a question answered here, never passed on");
    }
}
