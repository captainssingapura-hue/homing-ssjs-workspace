package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A log read back to what it folds to, every layer at once: from nothing, from
 * its events, from a checkpoint and what came after; a checkpoint of other rules
 * not folded on; a log that will not fold set aside, and the page afresh.
 */
class WorkspaceLoadTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/log/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        for (String m : new String[]{"ExactShare", "LayoutAlgebra", "RosterFold", "PaneFold", "GridFold", "WorkspaceFold", "CheckpointFold"}) loadModule(DIR + "fold/" + m + "Module.js");
        for (String m : new String[]{"WorkspaceLogStore", "MemoryLog", "WorkspaceLogIdentity", "WorkspaceLoad"}) loadModule(DIR + "store/" + m + "Module.js");
        js.eval("js", """
                var said = [];
                var console = { warn: (m) => said.push(m), error: (m) => said.push(m) };
                var store = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("bench", null), backend: new MemoryLog(),
                                                    now: (() => { let t = 1790000000000; return () => t++; })() });
                var got = {};
                function opened(id) { return new WidgetOpened(new WidgetId(id), new WidgetKind(id.replace(/-\\d+$/, "")), []); }
                function loaded(r) { got.r = r; got.state = r.folded ? JSON.stringify(WorkspaceStateCodec.transformTo(r.folded.state)) : null; }
                """);
    }

    private String str(String src) { return js.eval("js", src).asString(); }

    @Test
    void aLogComesBackAsItFolds_everyLayerAtOnce() {
        js.eval("js", "WorkspaceLoad.load(store).then(loaded)");
        assertEquals(0, js.eval("js", "got.r.logged").asInt());
        assertTrue(js.eval("js", "got.r.folded.through.value === 0 && got.r.why === null").asBoolean(), "nothing logged: the opening");

        js.eval("js", """
                store.append(opened("books-grid-1")).then(() => store.append(opened("book-jumbotron-1")))
                     .then(() => store.append(new PaneShown(new WidgetId("book-jumbotron-1"))))
                     .then(() => store.append(new WidgetClosed(new WidgetId("books-grid-1"))))
                     .then(() => WorkspaceLoad.load(store)).then(loaded);
                """);
        assertEquals(4, js.eval("js", "got.r.logged").asInt());
        assertEquals("book-jumbotron-1", str("got.r.folded.state.roster.widgets.map((w) => w.id.value).join(',')"));
        assertEquals("book-jumbotron-1", str("got.r.folded.state.pane.shown.value"));
        assertEquals("book-jumbotron:1,books-grid:1", str("got.r.folded.state.roster.sequences.map((s) => s.prefix + ':' + s.last).join(',')"));
    }

    @Test
    void fromACheckpoint_andWhatCameAfter_andNotFromOneOfOtherRules() {
        js.eval("js", """
                store.append(opened("books-grid-1")).then(() => store.events())
                     .then((all) => store.putCheckpoint(CheckpointFold.next(null, store.header, all)))
                     .then(() => store.append(opened("books-grid-2")))
                     .then(() => WorkspaceLoad.load(store)).then(loaded);
                """);
        assertEquals(2, js.eval("js", "got.r.logged").asInt());
        assertEquals("books-grid-1,books-grid-2", str("got.r.folded.state.roster.widgets.map((w) => w.id.value).join(',')"));

        js.eval("js", """
                store.checkpoint().then((c) => store.dropCheckpoint().then(() => store.putCheckpoint(new Checkpoint(c.folded, c.events, Checkpoint.FOLD + 1))))
                     .then(() => WorkspaceLoad.load(store)).then(loaded).then(() => store.checkpoint()).then((c) => { got.after = c; });
                """);
        assertEquals("books-grid-1,books-grid-2", str("got.r.folded.state.roster.widgets.map((w) => w.id.value).join(',')"), "folded whole");
        assertTrue(js.eval("js", "got.after === null").asBoolean(), "the page that writes the log drops it");
        assertTrue(str("said.join('|')").contains("folded by rules " + 3 + ", not 2"), str("said.join('|')"));
    }

    @Test
    void aLogThatWillNotFold_isSetAside_andThePageStartsAfresh() {
        js.eval("js", """
                store.append(new WidgetClosed(new WidgetId("books-grid-1")))
                     .then(() => WorkspaceLoad.load(store)).then(loaded).then(() => store.asides()).then((a) => { got.asides = a; return store.events(); })
                     .then((e) => { got.left = e.length; });
                """);
        assertTrue(js.eval("js", "got.r.folded === null && got.r.logged === 0").asBoolean(), "afresh");
        assertTrue(str("got.r.why").contains("the widget books-grid-1 is not open"), str("got.r.why"));
        assertEquals(1, js.eval("js", "got.asides.length").asInt(), "kept whole, aside");
        assertEquals(0, js.eval("js", "got.left").asInt(), "and the log left empty");

        js.eval("js", "store.append(new WidgetClosed(new WidgetId('books-grid-1'))).then(() => WorkspaceLoad.load(store, { writes: false })).then(loaded).then(() => store.events()).then((e) => { got.left = e.length; })");
        assertTrue(js.eval("js", "got.r.folded === null").asBoolean());
        assertEquals(1, js.eval("js", "got.left").asInt(), "a page that does not write the log leaves it to the one that does");
    }
}
