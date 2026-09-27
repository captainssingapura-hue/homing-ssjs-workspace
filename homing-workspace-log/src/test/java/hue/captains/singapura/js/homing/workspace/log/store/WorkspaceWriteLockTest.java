package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One writer per log. The browser's lock manager is stood in for by one that
 * keeps the Web Locks rules the lock leans on: a request ifAvailable is refused
 * while another holds the name; one that steals takes it, and the holder's
 * request rejects; a lock is held while its callback's promise is pending.
 */
class WorkspaceWriteLockTest extends JsModuleTestBase {

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        loadModule("/homing/js/hue/captains/singapura/js/homing/workspace/log/store/WorkspaceWriteLockModule.js");
        js.eval("js", """
                var locks = { held: new Map(),
                    request(name, options, callback) {
                        const self = this;
                        if (options.ifAvailable && self.held.has(name)) return Promise.resolve(callback(null));
                        if (options.steal && self.held.has(name)) { const h = self.held.get(name); self.held.delete(name); h.abort(new Error("AbortError")); }
                        return new Promise(function (resolve, reject) {
                            const mine = { abort: reject };
                            self.held.set(name, mine);
                            Promise.resolve(callback({ name })).then(function (v) { if (self.held.get(name) === mine) self.held.delete(name); resolve(v); });
                        });
                    } };
                var ws = new WorkspaceInstanceId("7f1b6c2e-5000-9000-7f1b-6c2e00000001");
                var log = new LogKey(new WorkspaceKind("notes"), ws), other = new LogKey(new WorkspaceKind("demo"), ws);
                var said = { a: [], b: [] }, got = {};
                var a = new WorkspaceWriteLock({ log: log, locks: locks, onChange: (w) => said.a.push(w.held.name) });
                var b = new WorkspaceWriteLock({ log: log, locks: locks, onChange: (w) => said.b.push(w.held.name) });
                """);
    }

    @Test
    void theFirstPageWrites_theSecondReadsOnly_anotherLogIsApart() {
        js.eval("js", """
                a.acquire().then((w) => { got.a = w; return b.acquire(); }).then((w) => { got.b = w; })
                    .then(() => new WorkspaceWriteLock({ log: other, locks: locks }).acquire()).then((w) => { got.other = w.held.name; });
                """);
        assertEquals("HERE", js.eval("js", "got.a.held.name").asString());
        assertEquals("ELSEWHERE", js.eval("js", "got.b.held.name").asString());
        assertEquals("HERE", js.eval("js", "got.other").asString(), "another log's lock is its own");
        assertTrue(js.eval("js", "got.a instanceof WriteLock && got.a.log === log").asBoolean(), "said as the WriteLock declared in Java");
        assertTrue(js.eval("js", "WorkspaceWriteLock.writes(got.a) && !WorkspaceWriteLock.writes(got.b)").asBoolean());
        assertEquals("homing.workspace.log/notes/7f1b6c2e-5000-9000-7f1b-6c2e00000001", js.eval("js", "WorkspaceWriteLock.nameOf(log)").asString());
    }

    @Test
    void aTakeOverTakesIt_andThePageThatHeldItIsToldTaken() {
        js.eval("js", "a.acquire().then(() => b.acquire()).then(() => b.takeOver()).then((w) => { got.b = w; })");
        assertEquals("HERE", js.eval("js", "got.b.held.name").asString());
        assertEquals("[\"HERE\",\"TAKEN\"]", js.eval("js", "JSON.stringify(said.a)").asString());
        assertEquals("[\"ELSEWHERE\",\"HERE\"]", js.eval("js", "JSON.stringify(said.b)").asString());
        assertTrue(js.eval("js", "!WorkspaceWriteLock.writes(a.state) && WorkspaceWriteLock.writes(b.state)").asBoolean());
        js.eval("js", "a.takeOver().then((w) => { got.back = w.held.name; })");
        assertEquals("HERE", js.eval("js", "got.back").asString(), "and taken back");
        assertEquals("TAKEN", js.eval("js", "b.state.held.name").asString());
    }

    @Test
    void aLockLetGoIsFreeForTheNext() {
        js.eval("js", "a.acquire().then(() => { a.release(); return Promise.resolve(); }).then(() => b.acquire()).then((w) => { got.b = w.held.name; })");
        assertEquals("HERE", js.eval("js", "got.b").asString());
    }

    @Test
    void withoutLocksAPageWritesUnguarded() {
        js.eval("js", "new WorkspaceWriteLock({ log: log, locks: null }).acquire().then((w) => { got.u = w; })");
        assertEquals("UNGUARDED", js.eval("js", "got.u.held.name").asString());
        assertTrue(js.eval("js", "WorkspaceWriteLock.writes(got.u)").asBoolean());
    }
}
