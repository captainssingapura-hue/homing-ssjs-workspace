package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceKind;
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
        loadModule(DIR + "codecs/WorkspaceLogCodecsModule.js");
        for (String m : new String[]{"WorkspaceLogStore", "MemoryLog", "WorkspaceLogExport", "WorkspaceLogIdentity"}) {
            loadModule(DIR + "log/store/" + m + "Module.js");
        }
        js.eval("js", """
                var store = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header("demo", ""), backend: new MemoryLog(),
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

    @Test
    void theIdentityIsTheAddressesOrTheKindsOwn() {
        assertEquals(WorkspaceInstanceId.placeholderFor(WorkspaceKind.of("demo")).toString(),
                js.eval("js", "WorkspaceLogIdentity.placeholder('demo')").asString());
        assertEquals(WorkspaceInstanceId.placeholderFor(WorkspaceKind.of("Focus-Lab_2")).toString(),
                js.eval("js", "WorkspaceLogIdentity.placeholder('Focus-Lab_2')").asString());
        assertEquals("0f1b6c2e-5000-9000-7f1b-6c2e00000001",
                js.eval("js", "WorkspaceLogIdentity.header('demo', '?x=1&workspace=0F1B6C2E-5000-9000-7F1B-6C2E00000001').workspaceId.id").asString());
        assertEquals(js.eval("js", "WorkspaceLogIdentity.placeholder('demo')").asString(),
                js.eval("js", "WorkspaceLogIdentity.header('demo', '?workspace=nope').workspaceId.id").asString());
    }
}
