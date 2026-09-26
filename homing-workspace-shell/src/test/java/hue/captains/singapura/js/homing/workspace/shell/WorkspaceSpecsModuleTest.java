package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The registry stamped into one module: every registered spec under its kind,
 * in registration order, each told the two things no single spec can know —
 * every kind the switcher offers, and where a change of kind goes.
 */
class WorkspaceSpecsModuleTest extends JsModuleTestBase {

    @BeforeEach
    void twoSpecs() {
        WorkspaceSpecRegistry.INSTANCE.resetForTesting();
        WorkspaceSpecRegistry.INSTANCE.register(new Fixture("specs-notes", "Notes", "Writing"));
        WorkspaceSpecRegistry.INSTANCE.register(new Fixture("specs-books", "Books", null));
        js = buildContext();
        js.eval("js", String.join("\n", WorkspaceSpecsModule.INSTANCE.selfContent(null)));
    }

    @AfterEach
    void clean() { WorkspaceSpecRegistry.INSTANCE.resetForTesting(); }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void everySpecUnderItsKind_inRegistrationOrder() {
        assertEquals("specs-notes,specs-books", eval("Object.keys(SPECS).join(',')").asString());
        assertEquals("Notes|Writing|Books|Workspaces", eval("[SPECS['specs-notes'].title, SPECS['specs-notes'].section, SPECS['specs-books'].title, SPECS['specs-books'].section].join('|')").asString(),
                "a spec that names no section is under the default one");
        assertEquals("9|1", eval("SPECS['specs-notes'].maxTabs + '|' + SPECS['specs-notes'].entries.length").asString(), "what the spec declares, carried as it is");
    }

    @Test
    void eachSpecIsToldEveryKind_andWhereAKindChangeGoes() {
        assertEquals("specs-notes:Notes:Writing,specs-books:Books:Workspaces",
                eval("SPECS['specs-books'].availableKinds.map(function (k) { return k.kind + ':' + k.title + ':' + k.section; }).join(',')").asString());
        assertTrue(eval("SPECS['specs-notes'].availableKinds === SPECS['specs-books'].availableKinds").asBoolean(), "one catalogue, shared");
        assertEquals(WorkspaceSpecsModule.switchBase(), eval("SPECS['specs-notes'].switchBase").asString(), "the app's flat address, minted");
        assertEquals(eval("SPECS['specs-notes'].switchBase").asString(), eval("SPECS['specs-books'].switchBase").asString());
    }

    @Test
    void theModuleExportsSpecs_andImportsNothing() {
        assertEquals(List.of("SPECS"), WorkspaceSpecsModule.INSTANCE.exports().exports().stream().map(e -> e.getClass().getSimpleName()).toList());
        assertTrue(WorkspaceSpecsModule.INSTANCE.imports().getAllImports().isEmpty());
    }

    private static final class Fixture implements WorkspaceSpec {
        private final String kind, title, section;
        Fixture(String kind, String title, String section) { this.kind = kind; this.title = title; this.section = section; }
        @Override public String kind() { return kind; }
        @Override public String title() { return title; }
        @Override public String section() { return section == null ? WorkspaceSpec.super.section() : section; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(WidgetEntry.of(NoteWidget.class, WidgetLabel.of("Note"))); }
        @Override public int maxTabs() { return 9; }
    }

    /** A widget as the picker's registry wants one: a singleton, its params, its title and its construct. */
    public static final class NoteWidget extends WorkspaceWidget<WorkspaceWidget._None, NoteWidget> {
        public static final NoteWidget INSTANCE = new NoteWidget();
        private NoteWidget() {}
        private record construct() implements WorkspaceWidget._Construct<_None, NoteWidget> {}
        @Override protected _Construct<_None, NoteWidget> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "Note"; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }
}
