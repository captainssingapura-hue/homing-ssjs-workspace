package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent;
import hue.captains.singapura.js.homing.workspace.log.FloatEvent;
import hue.captains.singapura.js.homing.workspace.log.LogIds.FloatId;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.store.ValidateWorkspaceLog;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The recorder, headless: the reports the desk, the docks and the grid make,
 * in the order they make them, become the typed events of the log — floats
 * nowhere, a merge its moves and its removal — and the file the browser would
 * export is one the Java validator passes and reads back into the same events.
 */
class WorkspaceRecorderTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        for (String m : new String[]{"WorkspaceLogStore", "MemoryLog", "WorkspaceLogExport", "WorkspaceLogIdentity"}) {
            loadModule(DIR + "log/store/" + m + "Module.js");
        }
        loadModule(DIR + "shell/WorkspaceRecorderModule.js");
        js.eval("js", """
                var store = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("demo", ""), backend: new MemoryLog(),
                                                    now: (() => { let t = 1790000000000; return () => t++; })() });
                var regions = { main: true, "cell-2": true }, kinds = { "tab-1": "opener", "tab-2": "note" }, counted = [];
                var floats = { "float-1": true };
                var rec = new WorkspaceRecorder({ store: store, isRegion: (s) => !!regions[s], isFloat: (s) => !!floats[s], kindOf: (id) => kinds[id] || null,
                                                  onCount: (n) => counted.push(n) });
                function tab(id, title) { return { id: id, title: () => title }; }
                var got = {};
                """);
    }

    private String exported() {
        js.eval("js", "WorkspaceLogExport.of(store).then((t) => { got.text = t; });");
        return js.eval("js", "got.text").asString();
    }

    @Test
    void whatTheComponentsReportIsWhatTheLogHolds() {
        js.eval("js", """
                rec.hear({ kind: "TabAdded", slotId: "main", tab: tab("tab-1", "Open"), index: 0 });
                rec.hear({ kind: "TabActivated", slotId: "main", tabId: "tab-1" });
                kinds["tab-1"] = "note";
                rec.became(tab("tab-1", "Note"), "note");
                rec.hear({ kind: "AddRequested", slotId: "main" });
                rec.hear({ kind: "Subdivided", cellId: "main", newCellId: "cell-2", side: "right" });
                rec.hear({ kind: "TracksChanged", path: "", ratios: [1 / 3, 2 / 3] });
                rec.hear({ kind: "TabRenamed", slotId: "main", tabId: "tab-1", title: "Groceries" });
                rec.hear({ kind: "Opened", id: "float-1", title: "", x: 40, y: 60, w: 320, h: 220 });
                rec.hear({ kind: "TabMoved", srcSlotId: "main", tab: tab("tab-1", "Groceries"), srcIndex: 0, destSlotId: "float-1", destIndex: 0 });
                rec.hear({ kind: "TabActivated", slotId: "float-1", tabId: "tab-1" });
                rec.hear({ kind: "Moved", id: "float-1", x: 400, y: 90 });
                rec.hear({ kind: "Resized", id: "float-1", w: 480, h: 300 });
                rec.hear({ kind: "Raised", id: "float-1" });
                rec.hear({ kind: "TabMoved", srcSlotId: "float-1", tab: tab("tab-1", "Groceries"), srcIndex: 0, destSlotId: "cell-2", destIndex: 0 });
                rec.hear({ kind: "Closed", id: "float-1" });
                rec.hear({ kind: "TabMoved", srcSlotId: "cell-2", tab: tab("tab-1", "Groceries"), srcIndex: 0, destSlotId: "main", destIndex: 0 });
                rec.hear({ kind: "Removed", cellId: "cell-2" });
                rec.hear({ kind: "TabRemoved", slotId: "main", tab: tab("tab-1", "Groceries"), fromIndex: 0 });
                """);
        String text = exported();
        assertEquals(ValidateWorkspaceLog.VALID, ValidateWorkspaceLog.validate("recorded", text, System.out, System.err));
        List<WorkspaceEvent> events = WorkspaceLogFile.read(text).events().stream().map(LoggedEvent::event).toList();
        assertEquals(List.of("TabOpened", "TabShown", "TabBecame", "RegionParted", "TracksChanged", "TabRenamed",
                             "FloatOpened", "TabMoved", "TabShown", "FloatMoved", "FloatResized", "FloatRaised", "TabMoved", "FloatClosed",
                             "TabMoved", "RegionRemoved", "TabClosed"),
                events.stream().map(e -> e.getClass().getSimpleName()).toList(), "a float is recorded like a region; a request is nowhere");
        assertEquals(new TabEvent.TabMoved(TabId.of("tab-1"), Host.floating("float-1"), 0), events.get(7), "the tab afloat is in its float");
        assertEquals(new TabEvent.TabShown(Host.floating("float-1"), TabId.of("tab-1")), events.get(8));
        assertEquals(new FloatEvent.FloatOpened(FloatId.of("float-1"), 40, 60, 320, 220), events.get(6));
        assertEquals("[333333, 666667]", ((RegionEvent.TracksChanged) events.get(4)).shares().stream().map(s -> s.units()).toList().toString());
        assertEquals(17, js.eval("js", "counted[counted.length - 1]").asInt());
    }

    @Test
    void aHostThatIsNeitherARegionNorAFloatIsNotRecorded() {
        js.eval("js", "rec.hear({ kind: 'TabActivated', slotId: 'elsewhere', tabId: 'tab-1' });");
        assertTrue(exported().split("\n").length == 1, "only the header");
    }

    @Test
    void aReportTheTypesRefuseIsNotRecorded_andStopStopsIt() {
        js.eval("js", """
                rec.hear({ kind: "TabAdded", slotId: "main", tab: tab("tab-9", "Unknown"), index: 0 });
                rec.hear({ kind: "TabActivated", slotId: "main", tabId: "tab 1" });
                rec.stop();
                rec.hear({ kind: "TabRemoved", slotId: "main", tab: tab("tab-2", "Note"), fromIndex: 0 });
                """);
        assertTrue(exported().split("\n").length == 1, "only the header: nothing was recorded");
    }

    @Test
    void sharesAreExactMillionthsAddingUpToOne() {
        assertEquals("142858,142857,142857,142857,142857,142857,142857",
                js.eval("js", "WorkspaceRecorder.shares([1/7, 1/7, 1/7, 1/7, 1/7, 1/7, 1/7]).map((s) => s.units).join(',')").asString());
        assertEquals("1,999999", js.eval("js", "WorkspaceRecorder.shares([0.0000001, 0.9999999]).map((s) => s.units).join(',')").asString());
    }
}
