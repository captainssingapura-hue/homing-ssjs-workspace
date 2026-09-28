package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The core, and its dual: the same scenario run on the Java core and on the
 * JavaScript one says the same things in the same order - what the placement
 * is asked, what is made and disposed, what the core says - under the same ids.
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
        var core = new WorkspaceCore<String, Fake>(Map.of("books-grid", kind, "book-jumbotron", kind), new WorkspaceCore.Placement<>() {
            @Override public String lend(WorkspaceCore.Entry<?> e) { said.add("lend " + e.id()); return "slot-" + e.id(); }
            @Override public void release(WorkspaceCore.Entry<?> e) { said.add("release " + e.id()); }
        });
        core.on(n -> said.add(switch (n) {
            case WorkspaceCore.Notice.WidgetOpened o -> "opened " + o.entry().id();
            case WorkspaceCore.Notice.WidgetClosing c -> "closing " + c.entry().id();
            case WorkspaceCore.Notice.WidgetClosed c -> "closed " + c.id() + " " + c.kind();
        }));
        core.open("books-grid", Map.of());
        core.open("books-grid", Map.of("columns", "title,rating"));
        core.open("books-grid", Map.of());
        core.close(WidgetId.of("books-grid-1"));
        core.open("books-grid", Map.of());
        core.open("book-jumbotron", Map.of(), WidgetId.of("book-jumbotron-5"));
        core.open("book-jumbotron", Map.of());
        try { core.open("book-jumbotron", Map.of("mode", "fail")); } catch (IllegalStateException e) { said.add("refused"); }
        core.open("book-jumbotron", Map.of("mode", "fail-not"));
        core.spend("books-grid", 7);
        core.spend("books-grid", 2);
        core.open("books-grid", Map.of());
        said.add("roster " + String.join(" ", core.entries().stream().map(e -> e.id().value()).toList()));
        return said;
    }

    private static final String SCENARIO = """
        var said = [];
        class Fake {
            constructor(container, params) { if (params.mode === "fail") throw new Error("cannot be made"); said.push("make in " + container); this.container = container; }
            dispose() { said.push("dispose " + this.container); }
        }
        var core = new WorkspaceCore({ kinds: { "books-grid": { Widget: Fake }, "book-jumbotron": { Widget: Fake } },
            placement: { lend: function (e) { said.push("lend " + e.id); return "slot-" + e.id; }, release: function (e) { said.push("release " + e.id); } } });
        core.on(function (n) {
            said.push(n.kind === "WidgetOpened" ? "opened " + n.entry.id : n.kind === "WidgetClosing" ? "closing " + n.entry.id : "closed " + n.id + " " + n.widgetKind);
        });
        core.open("books-grid", {});
        core.open("books-grid", { columns: "title,rating" });
        core.open("books-grid", {});
        core.close("books-grid-1");
        core.open("books-grid", {});
        core.open("book-jumbotron", {}, "book-jumbotron-5");
        core.open("book-jumbotron", {});
        try { core.open("book-jumbotron", { mode: "fail" }); } catch (e) { said.push("refused"); }
        core.open("book-jumbotron", { mode: "fail-not" });
        core.spend("books-grid", 7);
        core.spend("books-grid", 2);
        core.open("books-grid", {});
        said.push("roster " + core.entries().map(function (e) { return e.id; }).join(" "));
        """;

    @Test
    void theSameScenario_saysTheSameThings_underTheSameIds() {
        loadModule(DIR + "WidgetIdsModule.js");
        loadModule(DIR + "WorkspaceCoreModule.js");
        js.eval("js", SCENARIO);
        List<String> inJs = js.eval("js", "said").as(List.class);
        List<String> java = inJava();
        assertEquals(java, inJs);
        assertTrue(java.contains("opened books-grid-3"), "a closed widget's id is never given again: " + java);
        assertTrue(java.contains("opened book-jumbotron-6"), "the sequence goes on past an id given: " + java);
        assertTrue(java.contains("release book-jumbotron_fail-1"), "a widget that could not be made gives its container back, its id spent: " + java);
        assertTrue(java.contains("opened books-grid-8"), "spent up to 7, never back: the next is past it: " + java);
        assertEquals("roster books-grid_title-rating-1 books-grid-2 books-grid-3 book-jumbotron-5 book-jumbotron-6 book-jumbotron_fail-not-1 books-grid-8", java.get(java.size() - 1));
    }

    @Test
    void anIdGivenMustBeOneItsKindAndParamsMake_andNotHeld() {
        var core = new WorkspaceCore<String, Fake>(Map.of("note", new WorkspaceCore.Kind<>() {
            @Override public Fake make(String c, Map<String, String> p) { return new Fake(c); }
            @Override public void dispose(Fake w) {}
        }), new WorkspaceCore.Placement<>() {
            @Override public String lend(WorkspaceCore.Entry<?> e) { return "slot"; }
            @Override public void release(WorkspaceCore.Entry<?> e) {}
        });
        core.open("note", Map.of(), WidgetId.of("note-2"));
        assertThrows(IllegalArgumentException.class, () -> core.open("note", Map.of(), WidgetId.of("note-2")), "held already");
        assertThrows(IllegalArgumentException.class, () -> core.open("note", Map.of("t", "x"), WidgetId.of("note-3")), "not what its params make");
        assertThrows(IllegalArgumentException.class, () -> core.open("sheet", Map.of()), "no such kind");
        assertThrows(IllegalArgumentException.class, () -> core.close(WidgetId.of("note-9")), "no such widget");
        assertEquals("note-3", core.open("note", Map.of()).id().value());
    }
}
