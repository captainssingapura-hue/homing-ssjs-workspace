package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Label;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Node;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Run;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetRef;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A tree's arrangement as a page has it: the engine, the widgets with their params, the tree - frozen through, safe to inline. */
class TreeArrangementJsTest {

    record Spec() implements WorkspaceSpec {
        @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of("tree-bench"); }
        @Override public Set<WidgetKind> widgetKinds() { return Set.of(WidgetKind.of("note")); }
    }

    /** An introduction, then one section whose label is drawn by runs. */
    static final TreePlacement TREE = TreePlacement.of(new Node(Optional.empty(), Label.of("The doc"), List.of(WidgetRef.of("intro")),
            List.of(new Node(Optional.of(TreePlacement.Name.of("keys")), Label.of(new Run.Text("The "), new Run.Code("Keys")),
                    List.of(WidgetRef.of("body")), List.of()))));

    static final Arrangement<Spec, TreePlacement> ARRANGEMENT = Arrangement.of(new Spec(), TREE,
            ArrangedWidget.of("intro", "note", Map.of("note", "the \"intro\" </script>")),
            ArrangedWidget.of("body", "note", Map.of("note", "body")));

    @Test
    void itIsTheEngineTheWidgetsAndTheTree() {
        String js = TreeArrangementJs.expression(ARRANGEMENT);
        assertTrue(js.startsWith("Object.freeze({ engine: \"tree\", workspace: \"tree-bench\""), js);
        assertTrue(js.contains("\"intro\": Object.freeze({ kind: \"note\", params: Object.freeze({ \"note\": \"the \\\"intro\\\" \\u003c/script>\" }) })"), js);
        assertTrue(js.contains("root: Object.freeze({ name: \"\", label: Object.freeze({ text: \"The doc\", runs: Object.freeze([]) }), leaves: Object.freeze([\"intro\"])"), js);
        assertTrue(js.contains("Object.freeze({ name: \"keys\", label: Object.freeze({ text: \"The Keys\", runs: Object.freeze([Object.freeze({ kind: \"text\", text: \"The \" }), Object.freeze({ kind: \"code\", text: \"Keys\" })]) }), leaves: Object.freeze([\"body\"]), children: Object.freeze([]) })"), js);
        assertTrue(!js.contains("</script>"), "a page can inline it");
    }

    @Test
    void aConstantIsNamedAsOne() {
        assertTrue(TreeArrangementJs.constant("BENCH_TREE", ARRANGEMENT).startsWith("const BENCH_TREE = Object.freeze({"));
        assertThrows(IllegalArgumentException.class, () -> TreeArrangementJs.constant("benchTree", ARRANGEMENT));
        assertEquals("\"a\\\\b\\u000a\"", TreeArrangementJs.quote("a\\b\n"));
    }
}
