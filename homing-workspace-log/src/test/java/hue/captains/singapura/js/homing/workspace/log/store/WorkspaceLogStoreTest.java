package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The browser's store, headless over the memory backend: typed events in, the
 * same typed events out, anything else refused; its export is a file the Java
 * validator passes; its identity is the one Java would give.
 */
class WorkspaceLogStoreTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        for (String m : new String[]{"WorkspaceLogStore", "MemoryLog", "WorkspaceLogExport", "WorkspaceLogIdentity"}) {
            loadModule(DIR + "log/store/" + m + "Module.js");
        }
        js.eval("js", """
                var store = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("demo", null), backend: new MemoryLog(),
                                                    now: (() => { let t = 1790000000000; return () => t++; })() });
                var got = {};
                """);
    }

    @Test
    void typedEventsGoInAndComeOutTyped() {
        js.eval("js", """
                const t = new TabId("tab-1"), r = new RegionId("main"), h = new InRegion(r);
                store.append(new TabOpened(t, new WidgetKind("note"), new WidgetTitle("Note"), h, 0))
                    .then(() => store.append(new TabShown(h, t)))
                    .then((logged) => { got.last = logged; return store.events(); })
                    .then((events) => { got.events = events; });
                """);
        assertEquals(2, js.eval("js", "got.events.length").asInt());
        assertTrue(js.eval("js", "got.last instanceof LoggedEvent && got.last.seq.value === 2 && got.last.at === 1790000000001").asBoolean());
        assertTrue(js.eval("js", "got.events[0].event instanceof TabOpened && got.events[0].event.id.value === 'tab-1' && got.events[1].event instanceof TabShown").asBoolean());
    }

    @Test
    void aPlainObjectIsRefusedAndNothingIsKept() {
        js.eval("js", """
                store.append({ type: "TabClosed", id: "tab-1" }).catch((e) => { got.error = e.message; })
                    .then(() => store.events()).then((events) => { got.count = events.length; });
                """);
        assertTrue(js.eval("js", "got.error").asString().contains("append takes a WorkspaceEvent"));
        assertEquals(0, js.eval("js", "got.count").asInt());
    }

    @Test
    void itsExportPassesTheJavaValidator() {
        js.eval("js", """
                const t = new TabId("tab-1"), r = new RegionId("main"), h = new InRegion(r);
                store.append(new TabOpened(t, new WidgetKind("counter"), new WidgetTitle("Counter \\u00e9 \\"2\\""), h, 0))
                    .then(() => store.append(new RegionParted(r, new RegionId("cell-2"), Side.RIGHT)))
                    .then(() => store.append(new TracksChanged(new SplitPath(""), [new Scaled(400000, 6), new Scaled(600000, 6)])))
                    .then(() => store.append(new TabClosed(t)))
                    .then(() => WorkspaceLogExport.of(store)).then((text) => { got.text = text; });
                """);
        String text = js.eval("js", "got.text").asString();
        assertEquals(5, text.split("\n").length);
        assertEquals(ValidateWorkspaceLog.VALID, ValidateWorkspaceLog.validate("store", text, System.out, System.err));
        assertEquals("demo-" + js.eval("js", "store.header.workspaceId.id").asString() + ".workspace.log",
                js.eval("js", "WorkspaceLogExport.fileName(store.header)").asString());
    }

    /**
     * A log that no longer reads is set aside, not lost: its lines as they were
     * kept, the one that does not read among them; the log left empty and logging
     * on; the set-aside log exported as a log file the validator refuses at the
     * line that does not read; and discarded only when asked.
     */
    @Test
    void aLogThatDoesNotReadIsSetAsideWhole_andItsExportNamesTheLine() {
        js.eval("js", """
                var backend = new MemoryLog();
                var kept = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("demo", null), backend: backend, now: () => 1790000000500 });
                const t = new TabId("tab-1"), h = new InRegion(new RegionId("main"));
                kept.append(new TabOpened(t, new WidgetKind("note"), new WidgetTitle("Note"), h, 0))
                    .then(() => backend.add({ kind: "demo", workspaceId: kept.header.workspaceId.id, at: 7, event: { type: "TabGone", id: "tab-1" } }))
                    .then(() => kept.events()).then(() => { got.read = true; }, (e) => { got.why = e.message; })
                    .then(() => kept.setAside(got.why))
                    .then((aside) => { got.aside = aside; return kept.events(); })
                    .then((left) => { got.left = left.length; return kept.setAside("again"); })
                    .then((none) => { got.second = none; return kept.append(new TabClosed(t)); })
                    .then(() => kept.events()).then((on) => { got.on = on.map((e) => e.seq.value); return kept.asides(); })
                    .then((all) => { got.asides = all; });
                """);
        assertEquals(false, js.eval("js", "got.read === true").asBoolean(), "the log does not read");
        assertTrue(js.eval("js", "got.why").asString().contains("seq 2"), js.eval("js", "got.why").asString());
        assertTrue(js.eval("js", "got.aside instanceof SetAsideLog && got.aside.at === 1790000000500 && got.aside.why === got.why").asBoolean());
        assertEquals("[\"{\\\"seq\\\":1,\\\"at\\\":1790000000500,\\\"event\\\":{\\\"type\\\":\\\"TabOpened\\\",\\\"id\\\":\\\"tab-1\\\",\\\"kind\\\":\\\"note\\\",\\\"title\\\":\\\"Note\\\","
                + "\\\"host\\\":{\\\"type\\\":\\\"InRegion\\\",\\\"id\\\":\\\"main\\\"},\\\"index\\\":0}}\",\"{\\\"seq\\\":2,\\\"at\\\":7,\\\"event\\\":{\\\"type\\\":\\\"TabGone\\\",\\\"id\\\":\\\"tab-1\\\"}}\"]",
                js.eval("js", "JSON.stringify(got.aside.lines)").asString(), "the lines as they were kept, the one that does not read among them");
        assertEquals(0, js.eval("js", "got.left").asInt(), "the log is left empty");
        assertTrue(js.eval("js", "got.second === null").asBoolean(), "an empty log has nothing to set aside");
        assertEquals("[3]", js.eval("js", "JSON.stringify(got.on)").asString(), "the log logs on, its numbers climbing past what was set aside");
        assertEquals(1, js.eval("js", "got.asides.length").asInt());

        String text = js.eval("js", "WorkspaceLogExport.asideText(got.aside)").asString();
        var err = new java.io.ByteArrayOutputStream();
        assertEquals(ValidateWorkspaceLog.INVALID, ValidateWorkspaceLog.validate("aside", text, System.out, new java.io.PrintStream(err)));
        assertTrue(err.toString().contains("line 3"), err.toString());
        assertEquals("demo-" + js.eval("js", "kept.header.workspaceId.id").asString() + ".aside-1790000000500.workspace.log",
                js.eval("js", "WorkspaceLogExport.asideFileName(got.aside)").asString());

        js.eval("js", "kept.discardAsides().then((n) => { got.discarded = n; return kept.asides(); }).then((all) => { got.after = all.length; });");
        assertEquals(1, js.eval("js", "got.discarded").asInt());
        assertEquals(0, js.eval("js", "got.after").asInt());
    }

    @Test
    void theIdentityIsTheAddressesOrTheKindsOwn() {
        assertEquals(WorkspaceInstanceId.placeholderFor(WorkspaceKind.of("demo")).toString(),
                js.eval("js", "WorkspaceLogIdentity.placeholder('demo')").asString());
        assertEquals(WorkspaceInstanceId.placeholderFor(WorkspaceKind.of("Focus-Lab_2")).toString(),
                js.eval("js", "WorkspaceLogIdentity.placeholder('Focus-Lab_2')").asString());
        assertEquals("0f1b6c2e-5000-9000-7f1b-6c2e00000001",
                js.eval("js", "WorkspaceLogIdentity.header('demo', '0f1b6c2e-5000-9000-7f1b-6c2e00000001').workspaceId.id").asString());
        assertEquals(js.eval("js", "WorkspaceLogIdentity.placeholder('demo')").asString(),
                js.eval("js", "WorkspaceLogIdentity.header('demo', null).workspaceId.id").asString());
        assertTrue(js.eval("js", "(() => { try { WorkspaceLogIdentity.header('demo', 'nope'); return false; } catch (e) { return true; } })()").asBoolean(),
                "an id that is not a lowercase uuid is refused, never read as the kind's own");
    }

    @Test
    void aFreshWorkspaceIsAUuidOfItsOwn() {
        js.eval("js", "var a = WorkspaceLogIdentity.fresh(), b = WorkspaceLogIdentity.fresh();");
        assertTrue(js.eval("js", "a instanceof WorkspaceInstanceId && a.id !== b.id").asBoolean());
        String id = js.eval("js", "a.id").asString();
        assertEquals(id, WorkspaceInstanceId.parse(id).toString(), "a uuid Java reads, as Java writes it");
        assertEquals('4', id.charAt(14), "version 4");
    }
}
