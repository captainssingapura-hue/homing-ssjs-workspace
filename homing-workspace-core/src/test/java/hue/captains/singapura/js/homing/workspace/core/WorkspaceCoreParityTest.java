package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCore.Request;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The core, and its dual: the same scenario run on the Java core and on the
 * JavaScript one says the same things in the same order - what the register of
 * panes and the placement are asked, what is made and disposed, what the core
 * says - under the same ids. Each request in its type's order: an open,
 * created then mounted; a close, unmounted then closed.
 */
class WorkspaceCoreParityTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/core/";

    /** What a widget of the scenario is: a name, and what was said of it. */
    record Fake(String container) {}

    /** The scenario, as the Java core runs it: every step written into the list. */
    private static List<String> inJava() {
        var said = new ArrayList<String>();
        WorkspaceCore.Kind<String, Fake> kind = new WorkspaceCore.Kind<>() {
            @Override public Fake make(String container, Map<String, String> params) {
                if ("fail".equals(params.get("mode"))) throw new IllegalStateException("cannot be made");
                said.add("make in " + container); return new Fake(container);
            }
            @Override public void dispose(Fake w) { said.add("dispose " + w.container()); }
        };
        var core = new WorkspaceCore<String, Fake, String>(Map.of("books-grid", kind, "book-jumbotron", kind), new WorkspaceCore.Panes<>() {
            @Override public String lend(WorkspaceCore.Entry<?> e) { said.add("lend " + e.id()); return "pane-" + e.id(); }
            @Override public void release(WorkspaceCore.Entry<?> e) { said.add("release " + e.id()); }
        }, new WorkspaceCore.Placement<>() {
            @Override public void mount(WorkspaceCore.Entry<?> e, String at) {
                if ("nowhere".equals(at)) throw new IllegalStateException("no such place");
                said.add("mount " + e.id() + " at " + at);
            }
            @Override public void unmount(WorkspaceCore.Entry<?> e) { said.add("unmount " + e.id()); }
        });
        core.on(n -> said.add(switch (n) {
            case WorkspaceCore.Notice.WidgetOpened o -> "opened " + o.entry().id();
            case WorkspaceCore.Notice.WidgetClosing c -> "closing " + c.entry().id();
            case WorkspaceCore.Notice.WidgetClosed c -> "closed " + c.id() + " " + c.kind();
        }));
        core.execute(new Request.Open<>("books-grid", Map.of(), "left"));
        core.execute(new Request.Open<>("books-grid", Map.of("columns", "title,rating"), "right"));
        core.execute(new Request.Open<>("books-grid", Map.of(), "left"));
        core.execute(new Request.Close<>(WidgetId.of("books-grid-1")));
        core.execute(new Request.Open<>("books-grid", Map.of(), "left"));
        core.create("book-jumbotron", Map.of(), WidgetId.of("book-jumbotron-5"));
        core.execute(new Request.Open<>("book-jumbotron", Map.of(), "right"));
        try { core.execute(new Request.Open<>("book-jumbotron", Map.of("mode", "fail"), "left")); } catch (IllegalStateException e) { said.add("refused"); }
        try { core.execute(new Request.Open<>("book-jumbotron", Map.of(), "nowhere")); } catch (IllegalStateException e) { said.add("not mounted"); }
        core.execute(new Request.Open<>("book-jumbotron", Map.of("mode", "fail-not"), "left"));
        core.spend("books-grid", 7);
        core.spend("books-grid", 2);
        core.execute(new Request.Open<>("books-grid", Map.of(), "left"));
        said.add("roster " + String.join(" ", core.entries().stream().map(e -> e.id().value()).toList()));
        core.dispose();
        said.add("left " + core.entries().size());
        return said;
    }

    private static final String SCENARIO = """
        var said = [];
        class Fake {
            constructor(container, params) { if (params.mode === "fail") throw new Error("cannot be made"); said.push("make in " + container); this.container = container; }
            dispose() { said.push("dispose " + this.container); }
        }
        var core = new WorkspaceCore({ kinds: { "books-grid": { Widget: Fake }, "book-jumbotron": { Widget: Fake } },
            panes: { lend: function (e) { said.push("lend " + e.id); return "pane-" + e.id; }, release: function (e) { said.push("release " + e.id); } },
            placement: { mount: function (e, at) { if (at === "nowhere") throw new Error("no such place"); said.push("mount " + e.id + " at " + at); },
                         unmount: function (e) { said.push("unmount " + e.id); } } });
        core.on(function (n) {
            said.push(n.kind === "WidgetOpened" ? "opened " + n.entry.id : n.kind === "WidgetClosing" ? "closing " + n.entry.id : "closed " + n.id + " " + n.widgetKind);
        });
        core.execute(WorkspaceRequest.open("books-grid", {}, "left"));
        core.execute(WorkspaceRequest.open("books-grid", { columns: "title,rating" }, "right"));
        core.execute(WorkspaceRequest.open("books-grid", {}, "left"));
        core.execute(WorkspaceRequest.close("books-grid-1"));
        core.execute(WorkspaceRequest.open("books-grid", {}, "left"));
        core.create("book-jumbotron", {}, "book-jumbotron-5");
        core.execute(WorkspaceRequest.open("book-jumbotron", {}, "right"));
        try { core.execute(WorkspaceRequest.open("book-jumbotron", { mode: "fail" }, "left")); } catch (e) { said.push("refused"); }
        try { core.execute(WorkspaceRequest.open("book-jumbotron", {}, "nowhere")); } catch (e) { said.push("not mounted"); }
        core.execute(WorkspaceRequest.open("book-jumbotron", { mode: "fail-not" }, "left"));
        core.spend("books-grid", 7);
        core.spend("books-grid", 2);
        core.execute(WorkspaceRequest.open("books-grid", {}, "left"));
        said.push("roster " + core.entries().map(function (e) { return e.id; }).join(" "));
        core.dispose();
        said.push("left " + core.entries().length);
        """;

    @Test
    void theSameScenario_saysTheSameThings_underTheSameIds() {
        loadModule(DIR + "WidgetIdsModule.js");
        loadModule(DIR + "WorkspaceRequestModule.js");
        loadModule(DIR + "WorkspaceCoreModule.js");
        js.eval("js", SCENARIO);
        List<String> inJs = js.eval("js", "said").as(List.class);
        List<String> java = inJava();
        assertEquals(java, inJs);
        String all = String.join(" | ", java);
        assertTrue(all.contains("lend books-grid-1 | make in pane-books-grid-1 | opened books-grid-1 | mount books-grid-1 at left"),
                "an open: created - its pane lent, the widget made in it, said opened - then mounted: " + all);
        assertTrue(all.contains("unmount books-grid-1 | closing books-grid-1 | dispose pane-books-grid-1 | release books-grid-1 | closed books-grid-1 books-grid"),
                "a close: unmounted, then closed - said closing, disposed, its pane released, said closed: " + all);
        assertTrue(java.contains("opened books-grid-3"), "a closed widget's id is never given again: " + java);
        assertTrue(all.contains("opened book-jumbotron-5 | lend book-jumbotron-6"), "created under the id given, and mounted by nothing: " + all);
        assertTrue(java.contains("release book-jumbotron_fail-1"), "a widget that could not be made gives its pane back, its id spent: " + java);
        assertTrue(all.contains("opened book-jumbotron-7 | closing book-jumbotron-7 | dispose pane-book-jumbotron-7 | release book-jumbotron-7 | closed book-jumbotron-7 book-jumbotron | not mounted"),
                "a mount that fails closes what the open created: " + all);
        assertTrue(java.contains("opened books-grid-8"), "spent up to 7, never back: the next is past it: " + java);
        assertTrue(java.contains("roster books-grid_title-rating-1 books-grid-2 books-grid-3 book-jumbotron-5 book-jumbotron-6 book-jumbotron_fail-not-1 books-grid-8"), all);
        assertTrue(all.endsWith("closed books-grid-2 books-grid | closing books-grid_title-rating-1 | dispose pane-books-grid_title-rating-1 | release books-grid_title-rating-1 | closed books-grid_title-rating-1 books-grid | left 0"),
                "taken down: every widget closed, the last opened first, no placement asked: " + all);
        assertTrue(!all.contains("unmount books-grid_title-rating-1"), "the take-down unmounts nothing");
    }

    @Test
    void anIdGivenMustBeOneItsKindAndParamsMake_andNotHeld() {
        var core = new WorkspaceCore<String, Fake, String>(Map.of("note", new WorkspaceCore.Kind<>() {
            @Override public Fake make(String c, Map<String, String> p) { return new Fake(c); }
            @Override public void dispose(Fake w) {}
        }), new WorkspaceCore.Panes<>() {
            @Override public String lend(WorkspaceCore.Entry<?> e) { return "pane"; }
            @Override public void release(WorkspaceCore.Entry<?> e) {}
        }, new WorkspaceCore.Placement<>() {
            @Override public void mount(WorkspaceCore.Entry<?> e, String at) {}
            @Override public void unmount(WorkspaceCore.Entry<?> e) {}
        });
        core.create("note", Map.of(), WidgetId.of("note-2"));
        assertThrows(IllegalArgumentException.class, () -> core.create("note", Map.of(), WidgetId.of("note-2")), "held already");
        assertThrows(IllegalArgumentException.class, () -> core.create("note", Map.of("t", "x"), WidgetId.of("note-3")), "not what its params make");
        assertThrows(IllegalArgumentException.class, () -> core.execute(new Request.Open<>("sheet", Map.of(), "here")), "no such kind");
        assertThrows(IllegalArgumentException.class, () -> core.execute(new Request.Close<>(WidgetId.of("note-9"))), "no such widget");
        assertEquals("note-3", core.execute(new Request.Open<>("note", Map.of(), "here")).orElseThrow().id().value());
    }
}
