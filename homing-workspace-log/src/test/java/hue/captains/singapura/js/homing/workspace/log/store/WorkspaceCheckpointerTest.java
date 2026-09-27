package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checkpoints taken as the log goes, the fold in the worker's own script: every
 * so many events the page hands the worker the last checkpoint and what came
 * after it, keeps what comes back, and posts it to the server it was given. The
 * worker here is its script in a scope of its own, spoken to as a Worker is -
 * by messages that cross as JSON, as structured cloning would carry them.
 */
class WorkspaceCheckpointerTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/log/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        for (String m : new String[]{"ExactShare", "LayoutAlgebra", "WorkspaceFold", "CheckpointFold"}) loadModule(DIR + "fold/" + m + "Module.js");
        for (String m : new String[]{"WorkspaceLogStore", "MemoryLog", "WorkspaceLogIdentity", "WorkspaceCheckpointer"}) loadModule(DIR + "store/" + m + "Module.js");
        js.eval("js", """
                var worker = { onmessage: null, posted: 0,
                               postMessage(m) { this.posted++; const data = JSON.parse(JSON.stringify(m)); Promise.resolve().then(() => self.onmessage({ data })); } };
                var self = { postMessage(out) { const data = JSON.parse(JSON.stringify(out)); Promise.resolve().then(() => worker.onmessage({ data })); } };
                """);
        loadModule(DIR + "store/CheckpointWorkerModule.js");
        js.eval("js", """
                var backend = new MemoryLog(), sent = [], got = {};
                var store = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("demo", null), backend: backend,
                                                    now: (() => { let t = 1790000000000; return () => t++; })() });
                var cp = new WorkspaceCheckpointer({ store: store, worker: worker, every: 5, upload: "/workspace/checkpoints",
                                                     post: (address, text) => { sent.push({ address, text }); return Promise.resolve(); } });
                var main = new InRegion(new RegionId("main"));
                function opens(from, to) {
                    let p = Promise.resolve(), n = 0;
                    for (let i = from; i <= to; i++) {
                        const t = new TabId("tab-" + i);
                        p = p.then(() => store.append(new TabOpened(t, new WidgetKind("note"), new WidgetTitle("Note " + i), main, i - 1)))
                             .then(() => cp.recorded(++recorded))
                             .then(() => store.append(new TabShown(main, t)))
                             .then(() => cp.recorded(++recorded));
                    }
                    return p;
                }
                var recorded = 0;
                """);
    }

    @Test
    void everySoManyEventsTheWorkerFoldsACheckpointThatIsTheWholeFold() {
        js.eval("js", """
                opens(1, 6).then(() => cp._busy).then(() => store.checkpoint()).then((c) => { got.c = c; return store.events(); })
                    .then((all) => {
                        got.whole = WorkspaceFold.fold(store.header, all); got.all = all.length;
                        got.prefix = JSON.stringify(FoldedStateCodec.transformTo(WorkspaceFold.fold(store.header, all.slice(0, got.c.events))));
                    });
                """);
        assertEquals(12, js.eval("js", "got.all").asInt());
        // taken at 5 and at 10, each covering all that was logged when it read the log - which may be an event more
        int events = js.eval("js", "got.c.events").asInt();
        assertTrue(events >= 10 && events <= 12, "through the tenth or after: " + events);
        assertEquals(events, js.eval("js", "got.c.folded.through.value").asInt());
        assertEquals(js.eval("js", "got.prefix").asString(), js.eval("js", "JSON.stringify(FoldedStateCodec.transformTo(got.c.folded))").asString(),
                "it is the fold of the events it covers");
        assertTrue(js.eval("js", "got.c instanceof Checkpoint && got.c.fold === Checkpoint.FOLD").asBoolean());
        assertEquals(2, js.eval("js", "worker.posted").asInt(), "one fold in the worker per checkpoint");

        js.eval("js", "cp.take().then((c) => { got.last = c; })");
        assertEquals(12, js.eval("js", "got.last.events").asInt());
        assertEquals(js.eval("js", "JSON.stringify(FoldedStateCodec.transformTo(got.whole))").asString(),
                js.eval("js", "JSON.stringify(FoldedStateCodec.transformTo(got.last.folded))").asString(),
                "the checkpoint is what the whole log folds to");
        js.eval("js", "cp.take().then((c) => { got.none = c; })");
        assertTrue(js.eval("js", "got.none === null").asBoolean(), "nothing new, nothing folded");

        assertEquals(3, js.eval("js", "sent.length").asInt(), "each kept checkpoint posted to the server");
        assertEquals("/workspace/checkpoints", js.eval("js", "sent[2].address").asString());
        assertEquals(js.eval("js", "JSON.stringify(CheckpointCodec.transformTo(got.last))").asString(), js.eval("js", "sent[2].text").asString());
    }

    @Test
    void aCheckpointOfOtherRulesIsFoldedAgainFromTheOpening() {
        js.eval("js", """
                opens(1, 3).then(() => cp.take()).then((c) => {
                    const stale = new Checkpoint(c.folded, c.events, Checkpoint.FOLD + 1);
                    return backend.dropCheckpoint("demo", store.header.workspaceId.id)
                        .then(() => backend.putCheckpoint("demo", store.header.workspaceId.id, 1, CheckpointCodec.transformTo(stale)));
                }).then(() => opens(4, 4)).then(() => cp.take()).then((c) => { got.c = c; });
                """);
        assertEquals(8, js.eval("js", "got.c.events").asInt(), "all eight, folded again: the stale one not folded on");
        assertEquals(Integer.valueOf(js.eval("js", "Checkpoint.FOLD").asInt()), js.eval("js", "got.c.fold").asInt());
    }

    @Test
    void anEventTheFoldRefusesMakesNoCheckpoint_andKeepsTheOneThereWas() {
        js.eval("js", """
                opens(1, 2).then(() => cp.take()).then((c) => { got.before = c.events; })
                    .then(() => store.append(new TabClosed(new TabId("tab-9"))))
                    .then(() => cp.take()).then(() => { got.took = true; }, (e) => { got.why = e.message; })
                    .then(() => store.checkpoint()).then((c) => { got.after = c.events; });
                """);
        assertTrue(js.eval("js", "got.why").asString().contains("tab-9"), js.eval("js", "String(got.why)").asString());
        assertEquals(4, js.eval("js", "got.before").asInt());
        assertEquals(4, js.eval("js", "got.after").asInt(), "the last good checkpoint stays");
    }

    @Test
    void settingTheLogAsideDropsItsCheckpoint() {
        js.eval("js", """
                opens(1, 3).then(() => cp.take()).then(() => store.setAside("for the test"))
                    .then(() => store.checkpoint()).then((c) => { got.after = c; });
                """);
        assertTrue(js.eval("js", "got.after === null").asBoolean());
    }
}
