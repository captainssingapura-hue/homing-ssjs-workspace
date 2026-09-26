package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The saved-state layer, headless: the local store over any storage with the
 * localStorage shape, keyed per workspace kind under one prefix and never
 * remote; the debounced persister over an injected scheduler; the capture of
 * a live view into a WorkspaceState; and the facade that wires them, the
 * state codec and the widget kinds' params codecs together.
 */
class WorkspaceStatePersistenceTest extends JsModuleTestBase {

    private static final String REGISTRY = "/homing/js/hue/captains/singapura/js/homing/workspace/persistence/WidgetParamsCodecRegistryModule.js";
    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/persistence/WorkspaceStatePersistenceModule.js";

    // A storage of the localStorage shape; a scheduler the test runs by hand; the typed state's classes and its codec, stubbed
    private static final String SHIM = """
        function memoryStorage() {
            var m = new Map();
            return { get length() { return m.size; }, key: function (i) { return Array.from(m.keys())[i] || null; },
                     getItem: function (k) { return m.has(k) ? m.get(k) : null; }, setItem: function (k, v) { m.set(k, String(v)); },
                     removeItem: function (k) { m.delete(k); }, clear: function () { m.clear(); }, _m: m };
        }
        var tickets = new Map(), nextTicket = 0, delays = [];
        var scheduler = { schedule: function (fn, ms) { delays.push(ms); tickets.set(++nextTicket, fn); return nextTicket; },
                          cancel: function (t) { tickets.delete(t); } };
        function runTimers() { var due = Array.from(tickets.values()); tickets.clear(); due.forEach(function (f) { f(); }); }
        class WidgetInstance { constructor(id, kind) { this.id = id; this.kind = kind; } }
        class WorkspaceState {
            static CURRENT_SCHEMA_VERSION = 7;
            constructor(schema, kind, at, layout, widgetsById, chrome) { Object.assign(this, { schema, kind, at, layout, widgetsById, chrome }); }
        }
        var WorkspaceStateCodec = { transformTo: function (s) { return { kind: s.kind, layout: s.layout }; },
                                    transformFrom: function (w) { return { decoded: true, kind: w.kind, layout: w.layout }; } };
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", SHIM);
        loadModule(REGISTRY);
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    private String refusal(String src) {
        return assertThrows(PolyglotException.class, () -> eval(src)).getMessage();
    }

    /** The local store: per kind, under one prefix, as JSON; forgets on clear; lists its own kinds only; never remote. */
    @Nested
    class TheLocalStore {

        @Test
        void savesLoadsAndClears_perKind_asJsonUnderItsPrefix() {
            eval("var st = memoryStorage(); var s = createLocalStorageStore(st); s.save('Demo', { a: [1, 2] })");
            assertEquals("{\"a\":[1,2]}", eval("st.getItem('homing.workspace.Demo')").asString());
            assertEquals("[1,2]", eval("JSON.stringify(s.load('Demo').a)").asString());
            assertTrue(eval("s.load('Other') === null").asBoolean(), "no prior state: null");
            eval("s.clear('Demo')");
            assertTrue(eval("s.load('Demo') === null").asBoolean());
        }

        @Test
        void listsOnlyItsOwnKinds_andIsNeverRemote() {
            eval("var st = memoryStorage(); st.setItem('someone.else', 'x'); var s = createLocalStorageStore(st); s.save('A', 1); s.save('B', 2)");
            assertEquals("A,B", eval("s.listSavedKinds().join(',')").asString());
            assertFalse(eval("s.isRemote()").asBoolean(), "state belongs to the user: local only");
        }

        @Test
        void refusesAStorageOfTheWrongShape_aNamelessKind_andNoWire() {
            assertTrue(refusal("createLocalStorageStore({})").contains("localStorage interface"));
            assertTrue(refusal("createLocalStorageStore(memoryStorage()).save('', {})").contains("non-empty string"));
            assertTrue(refusal("createLocalStorageStore(memoryStorage()).save('A', undefined)").contains("must not be undefined"));
        }
    }

    /** The persister: bursts coalesce into one save after the window; a forced save runs now and cancels the pending one; errors are kept. */
    @Nested
    class ThePersister {

        @BeforeEach
        void aPersister() {
            eval("var saves = 0, fail = null; var p = createWorkspaceStatePersister({ scheduler: scheduler, debounceMs: 250, saveFn: function () { if (fail) throw fail; saves++; } })");
        }

        @Test
        void aBurstOfTriggers_isOneSave_afterTheWindow() {
            eval("p.triggerSave(); p.triggerSave(); p.triggerSave()");
            assertTrue(eval("p.isPending() && saves === 0 && tickets.size === 1").asBoolean(), "one pending save, none yet");
            assertEquals("250", eval("String(delays[delays.length - 1])").asString(), "after the window it was given");
            eval("runTimers()");
            assertTrue(eval("saves === 1 && !p.isPending()").asBoolean());
        }

        @Test
        void aForcedSave_runsNow_andThePendingOneIsNoMore() {
            eval("p.triggerSave(); p.forceSave(); runTimers()");
            assertEquals(1, eval("saves").asInt(), "saved once, now; the cancelled one never runs");
            assertFalse(eval("p.isPending()").asBoolean());
        }

        @Test
        void aFailedSave_isKept_thrownWhenForced_andClearedByTheNextThatSucceeds() {
            eval("fail = new Error('disk full'); p.triggerSave(); runTimers()");
            assertEquals("disk full", eval("p.lastError.message").asString(), "a debounced failure is kept, not thrown");
            assertTrue(refusal("p.forceSave()").contains("disk full"), "a forced one throws");
            eval("fail = null; p.forceSave()");
            assertTrue(eval("p.lastError === null && saves === 1").asBoolean());
        }

        @Test
        void itWantsASaveFnAndAScheduler_andWaits500UnlessTold() {
            assertTrue(refusal("createWorkspaceStatePersister({ scheduler: scheduler })").contains("saveFn"));
            assertTrue(refusal("createWorkspaceStatePersister({ saveFn: function () {} })").contains("scheduler"));
            eval("createWorkspaceStatePersister({ saveFn: function () {}, scheduler: scheduler }).triggerSave()");
            assertEquals("500", eval("String(delays[delays.length - 1])").asString());
        }
    }

    /** The capture: the view's four axes into one WorkspaceState, its widgets by id, at the current schema. */
    @Nested
    class TheCapture {

        @Test
        void aViewBecomesAWorkspaceState_itsWidgetsById() {
            eval("var st = captureLiveWorkspace({ workspaceKind: function () { return 'Demo'; }, layout: function () { return 'L'; }, chrome: function () { return 'C'; },"
               + " widgets: function () { return [new WidgetInstance('w1', 'Note'), new WidgetInstance('w2', 'Books')]; } })");
            assertTrue(eval("st instanceof WorkspaceState && st.schema === 7 && st.kind === 'Demo' && st.layout === 'L' && st.chrome === 'C' && st.at instanceof Date").asBoolean());
            assertEquals("w1:Note,w2:Books", eval("Array.from(st.widgetsById.entries()).map(function (e) { return e[0] + ':' + e[1].kind; }).join(',')").asString());
        }

        @Test
        void aViewMissingAnAxis_orYieldingSomethingElse_isRefused() {
            assertTrue(refusal("captureLiveWorkspace(null)").contains("view is required"));
            assertTrue(refusal("captureLiveWorkspace({ workspaceKind: function () {}, layout: function () {}, widgets: function () { return []; } })").contains("view.chrome()"));
            assertTrue(refusal("captureLiveWorkspace({ workspaceKind: function () {}, layout: function () {}, chrome: function () {}, widgets: function () { return [{ id: 'x' }]; } })")
                    .contains("WidgetInstance values"));
        }
    }

    /** The facade: the params codecs registered, a persister that captures, encodes and stores, and a restore that decodes. */
    @Nested
    class TheFacade {

        @Test
        void attachRegistersTheParamsCodecs_andAPersisterSavesTheEncodedCapture() {
            eval("var st = memoryStorage(); var codec = { transformTo: function (v) { return v; }, transformFrom: function (w) { return w; } };"
               + "var layer = WorkspaceStatePersistence.attach({ workspaceKind: 'Demo', storage: st, scheduler: scheduler, paramsCodecs: { Note: codec } });");
            assertTrue(eval("WidgetParamsCodecRegistry.get('Note') === codec && layer.workspaceKind === 'Demo'").asBoolean(), "the widget kinds' codecs, registered");
            eval("layer.create(function () { return { kind: 'Demo', layout: 'L' }; }).forceSave()");
            assertEquals("{\"kind\":\"Demo\",\"layout\":\"L\"}", eval("st.getItem('homing.workspace.Demo')").asString(), "captured, encoded, stored under the kind");
            assertTrue(eval("var r = layer.tryRestore(); r.decoded === true && r.layout === 'L'").asBoolean(), "and decoded on restore");
        }

        @Test
        void aCaptureOfNothing_savesNothing_andNothingSaved_restoresNull() {
            eval("var st = memoryStorage(); var layer = WorkspaceStatePersistence.attach({ workspaceKind: 'Demo', storage: st, scheduler: scheduler });"
               + "layer.create(function () { return null; }).forceSave();");
            assertEquals(0, eval("st.length").asInt());
            assertTrue(eval("layer.tryRestore() === null").asBoolean());
        }

        @Test
        void attachWantsAKindAndAStorage_andCreateACaptureFn() {
            assertTrue(refusal("WorkspaceStatePersistence.attach({ storage: memoryStorage() })").contains("workspaceKind required"));
            assertTrue(refusal("WorkspaceStatePersistence.attach({ workspaceKind: 'Demo', storage: {} })").contains("localStorage interface"));
            assertTrue(refusal("WorkspaceStatePersistence.attach({ workspaceKind: 'Demo', storage: memoryStorage(), scheduler: scheduler }).create(null)").contains("captureFn"));
        }
    }
}
