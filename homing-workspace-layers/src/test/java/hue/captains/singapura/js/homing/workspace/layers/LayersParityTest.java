package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCore;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCore.Request;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.layers.PanePlacement.Location;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.codec.WorkspaceEventCodec;
import hue.captains.singapura.js.homing.workspace.log.codec.WorkspaceStateCodec;
import hue.captains.singapura.js.homing.workspace.log.fold.WorkspaceFold;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The layers, live, and their duals: the same requests - widgets opened into
 * the pane, shown, closed - executed by the Java core over the Java pane and by
 * the JavaScript ones write the same lines; the log folded, each comes back to
 * the same workspace, roster first - and goes on under the same ids.
 */
class LayersParityTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/";
    private static final LogHeader HEADER = LogHeader.of(WorkspaceKind.of("bench"), WorkspaceInstanceId.parse("7f1b6c2e-5000-9000-7f1b-6c2e00000002"));

    private static final WorkspaceCore.Kind<String, String> KIND = new WorkspaceCore.Kind<>() {
        @Override public String make(String container, Map<String, String> params) { return container; }
        @Override public void dispose(String widget) {}
    };

    /** A workspace of one pane, headless: the core's panes a register of names, its placement the pane. */
    record Workspace(WorkspaceCore<String, String, Location> core, PanePlacement pane) {
        static Workspace made() {
            var pane = new PanePlacement();
            var core = new WorkspaceCore<String, String, Location>(Map.of("books-grid", KIND, "book-jumbotron", KIND), new WorkspaceCore.Panes<>() {
                @Override public String lend(WorkspaceCore.Entry<?> e) { return "pane-" + e.id(); }
                @Override public void release(WorkspaceCore.Entry<?> e) {}
            }, pane);
            return new Workspace(core, pane);
        }
        void record(List<WorkspaceEvent> log) { RosterLayer.record(core, log::add); PaneLayer.record(pane, log::add); }
        void open(String kind, Map<String, String> params, Location at) { core.execute(new Request.Open<>(kind, params, at)); }
        void close(String id) { core.execute(new Request.Close<>(WidgetId.of(id))); }
        void show(String id) { pane.show(Optional.ofNullable(id).map(WidgetId::of)); }
    }

    private static List<String> lines(List<WorkspaceEvent> events) {
        return events.stream().map(e -> JsonText.write(WorkspaceEventCodec.INSTANCE.transformTo(e))).toList();
    }

    private static final String SCENARIO_JS = """
        function made() {
            var pane = new PanePlacement();
            class W { constructor(container) { this.container = container; } dispose() {} }
            var core = new WorkspaceCore({ kinds: { "books-grid": { Widget: W }, "book-jumbotron": { Widget: W } },
                panes: { lend: function (e) { return "pane-" + e.id; }, release: function () {} }, placement: pane });
            return { core: core, pane: pane };
        }
        function record(ws, log) {
            var sink = { append: function (e) { log.push(JSON.stringify(WorkspaceEventCodec.transformTo(e))); } };
            RosterLayer.record(ws.core, sink);
            PaneLayer.record(ws.pane, sink);
        }
        var logged = [], ws = made();
        record(ws, logged);
        ws.core.execute(WorkspaceRequest.open("books-grid", {}, "shown"));
        ws.core.execute(WorkspaceRequest.open("book-jumbotron", {}, "behind"));
        ws.core.execute(WorkspaceRequest.open("books-grid", { columns: "title,rating" }, "shown"));
        ws.core.execute(WorkspaceRequest.close("books-grid_title-rating-1"));
        ws.core.execute(WorkspaceRequest.close("books-grid-1"));
        ws.pane.show(null);
        ws.pane.show("book-jumbotron-1");
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        js.eval("js", "var console = { error: function () {} };");
        loadModule(DIR + "core/WidgetIdsModule.js");
        loadModule(DIR + "core/WorkspaceRequestModule.js");
        loadModule(DIR + "core/WorkspaceCoreModule.js");
        for (String m : new String[]{"RosterLayer", "PanePlacement", "PaneLayer"}) loadModule(DIR + "layers/" + m + "Module.js");
    }

    @Test
    void theSameRequests_writeTheSameLines_andComeBackAlike() {
        // RECORDED, in Java
        var log = new ArrayList<WorkspaceEvent>();
        var ws = Workspace.made();
        ws.record(log);
        ws.open("books-grid", Map.of(), Location.SHOWN);
        ws.open("book-jumbotron", Map.of(), Location.BEHIND);
        ws.open("books-grid", Map.of("columns", "title,rating"), Location.SHOWN);
        ws.close("books-grid_title-rating-1");
        ws.close("books-grid-1");
        ws.show(null);
        ws.show("book-jumbotron-1");
        List<String> java = lines(log);

        // and in JavaScript: the same lines
        js.eval("js", SCENARIO_JS);
        List<String> inJs = js.eval("js", "logged").as(List.class);
        assertEquals(java, inJs);
        assertTrue(java.get(0).contains("\"WidgetOpened\"") && java.get(1).contains("\"PaneShown\""), "an open: created, then mounted - " + java);
        assertTrue(java.get(2).contains("book-jumbotron-1") && !java.get(3).contains("\"PaneShown\""), "mounted behind: the pane says nothing - " + java);
        assertTrue(java.get(5).contains("\"PaneShown\"") && java.get(5).contains("book-jumbotron-1") && java.get(6).contains("\"WidgetClosed\""),
                "a close: unmounted - the pane showing the next - then closed: " + java);

        // FOLDED, and come back to - roster first, then the pane - in both languages
        var logged = new ArrayList<LoggedEvent>();
        for (int i = 0; i < log.size(); i++) logged.add(new LoggedEvent(EventSeq.of(i + 1), Instant.ofEpochMilli(1_790_000_000_000L + i), log.get(i)));
        FoldedState folded = WorkspaceFold.foldFrom(WorkspaceFold.start(HEADER), logged);
        String state = JsonText.write(WorkspaceStateCodec.INSTANCE.transformTo(folded.state()));

        var back = Workspace.made();
        var restored = RosterLayer.restore(back.core(), folded.state().roster());
        PaneLayer.restore(back.pane(), back.core(), folded.state().pane());
        var after = new ArrayList<WorkspaceEvent>();
        back.record(after);
        back.open("books-grid", Map.of(), Location.SHOWN);
        String javaBack = back.core().entries().stream().map(e -> e.id().value()).toList() + " shown " + back.pane().shown().map(WidgetId::value).orElse("none")
                + " then " + lines(after);

        String jsBack = js.eval("js", """
                (function (text) {
                    var state = WorkspaceStateCodec.transformFrom(JSON.parse(text));
                    var back = made();
                    RosterLayer.restore(back.core, state.roster);
                    PaneLayer.restore(back.pane, back.core, state.pane);
                    var after = [];
                    record(back, after);
                    back.core.execute(WorkspaceRequest.open("books-grid", {}, "shown"));
                    return "[" + back.core.entries().map(function (e) { return e.id; }).join(", ") + "] shown " + (back.pane.shown() || "none")
                         + " then [" + after.join(", ") + "]";
                })
                """).execute(state).asString();
        assertEquals(javaBack, jsBack);
        assertEquals(List.of(WidgetId.of("book-jumbotron-1")), restored.opened());
        assertTrue(javaBack.startsWith("[book-jumbotron-1, books-grid-2] shown books-grid-2"),
                "books-grid-1 was closed, and its id spent all the same: the grid opened after is the second, mounted shown - " + javaBack);
    }

    @Test
    void thePaneShowsTheNextWhenTheOneShownIsUnmounted() {
        var pane = new PanePlacement();
        var said = new ArrayList<String>();
        pane.on(n -> said.add(((PanePlacement.Notice.PaneShown) n).widget().map(WidgetId::value).orElse("none")));
        var entries = new ArrayList<WorkspaceCore.Entry<String>>();
        for (String id : List.of("a-1", "b-1", "c-1")) {
            var e = new WorkspaceCore.Entry<String>(WidgetId.of(id), "a", Map.of(), "w");
            entries.add(e);
            pane.mount(e, Location.BEHIND);
        }
        assertEquals(List.of(), said, "mounted behind: nothing shown");
        pane.show(Optional.of(WidgetId.of("b-1")));
        pane.unmount(entries.get(1));
        pane.unmount(entries.get(2));
        pane.unmount(entries.get(0));
        assertEquals(List.of("b-1", "c-1", "a-1", "none"), said, "the one after it, else the one before, else none");
    }
}
