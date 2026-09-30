package hue.captains.singapura.js.homing.workspace.groups.core.models;

import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Label;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Name;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Node;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Run;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tree placement: headings as structure, nameless leaves before named children, the
 * path a node's locator and the path and position a leaf's; every invariant refused with
 * its reason; and an arrangement over a tree held to its own checks.
 */
class TreePlacementTest {

    /** An introduction; a section with a lead and a subsection; a section of two leaves; a section of none. */
    static final TreePlacement TREE = TreePlacement.of(TreePlacement.root("The doc", List.of("intro"),
            TreePlacement.node("design", "Design", List.of("design-lead"),
                    TreePlacement.node("keys", "Keys", "keys-body")),
            TreePlacement.node("plan", "Plan", "plan-body", "plan-table"),
            TreePlacement.node("empty", "Nothing under it")));

    private static List<String> names(List<WidgetRef> refs) { return refs.stream().map(WidgetRef::value).toList(); }

    @Test
    void itPlacesInReadingOrder_aNodesLeavesBeforeItsChildren() {
        assertEquals(TreePlacement.ENGINE, TREE.engine());
        assertEquals("tree", TREE.engine().value());
        assertEquals(List.of("intro", "design-lead", "keys-body", "plan-body", "plan-table"), names(TREE.placed()));
    }

    @Test
    void aNodeIsLocatedByItsPath_aLeafByItsNodesPathAndPosition() {
        assertEquals(List.of("", "design", "design/keys", "plan", "empty"), TREE.paths());
        assertEquals(List.of(":0", "design:0", "design/keys:0", "plan:0", "plan:1"), TREE.leaves().stream().map(TreePlacement.Leaf::locator).toList());
        assertEquals("Keys", TREE.node("design/keys").orElseThrow().label().text());
        assertEquals("The doc", TREE.node("").orElseThrow().label().text());
        assertTrue(TREE.node("design/nothing").isEmpty());
        assertTrue(TREE.node("keys").isEmpty(), "a path is from the root");
        assertEquals("plan:1", TREE.leafOf(WidgetRef.of("plan-table")).orElseThrow().locator());
        assertTrue(TREE.leafOf(WidgetRef.of("nowhere")).isEmpty());
        assertEquals(2, TREE.depth());
    }

    @Test
    void aNodeMayHoldNoLeaf_oneOrSeveral() {
        assertEquals(0, TREE.node("empty").orElseThrow().leaves().size());
        assertEquals(1, TREE.node("design/keys").orElseThrow().leaves().size());
        assertEquals(2, TREE.node("plan").orElseThrow().leaves().size());
        assertEquals(1, TREE.node("design").orElseThrow().leaves().size(), "a lead before a named child");
    }

    @Test
    void theRootAloneIsNameless() {
        var e = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.node("named", "A named root")));
        assertTrue(e.getMessage().contains("the root has no name"), e.getMessage());
        var nameless = new Node(Optional.empty(), Label.of("no name"), List.of(), List.of());
        var e2 = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of(), nameless)));
        assertTrue(e2.getMessage().contains("only the root is nameless"), e2.getMessage());
    }

    @Test
    void siblingsAreNamedOnce_namesMayRepeatElsewhere() {
        var e = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of(),
                TreePlacement.node("a", "A"), TreePlacement.node("a", "A again"))));
        assertTrue(e.getMessage().contains("two children named a"), e.getMessage());
        TreePlacement.of(TreePlacement.root("r", List.of(),
                TreePlacement.node("a", "A", List.of(), TreePlacement.node("x", "X")),
                TreePlacement.node("b", "B", List.of(), TreePlacement.node("x", "X under B"))));
    }

    @Test
    void aWidgetIsPlacedOnce_inTheWholeTree() {
        var e = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of("w"),
                TreePlacement.node("a", "A", "w"))));
        assertTrue(e.getMessage().contains("places w twice"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of("w", "w"))));
    }

    @Test
    void theTreeIsNoDeeperThanTheRigidTreesLevels() {
        Node deep = TreePlacement.node("n19", "19");
        for (int i = 18; i >= 1; i--) deep = TreePlacement.node("n" + i, String.valueOf(i), List.of(), deep);
        Node tooDeep = deep;
        var e = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of(), tooDeep)));
        assertTrue(e.getMessage().contains("at most 18"), e.getMessage());
        Node ok = TreePlacement.node("n18", "18");
        for (int i = 17; i >= 1; i--) ok = TreePlacement.node("n" + i, String.valueOf(i), List.of(), ok);
        assertEquals(18, TreePlacement.of(TreePlacement.root("r", List.of(), ok)).depth());
    }

    @Test
    void aNodeAtTheCapHoldsNoLeavesForTheyWouldSitBelowIt() {
        Node atCap = TreePlacement.node("n18", "18", "w18");
        for (int i = 17; i >= 1; i--) atCap = TreePlacement.node("n" + i, String.valueOf(i), List.of(), atCap);
        Node holding = atCap;
        var e = assertThrows(IllegalArgumentException.class, () -> TreePlacement.of(TreePlacement.root("r", List.of(), holding)));
        assertTrue(e.getMessage().contains("at most 17"), e.getMessage());
        Node above = TreePlacement.node("n17", "17", "w17");
        for (int i = 16; i >= 1; i--) above = TreePlacement.node("n" + i, String.valueOf(i), List.of(), above);
        assertEquals(17, TreePlacement.of(TreePlacement.root("r", List.of(), above)).depth());
    }

    @Test
    void aNameIsASafePathSegment() {
        Name.of("design.keys_2-b");
        assertThrows(IllegalArgumentException.class, () -> Name.of(""));
        assertThrows(IllegalArgumentException.class, () -> Name.of("a/b"));
        assertThrows(IllegalArgumentException.class, () -> Name.of("a:b"));
        assertThrows(IllegalArgumentException.class, () -> Name.of("x".repeat(49)));
    }

    @Test
    void aLabelsRunsSpellItsText() {
        var label = Label.of(new Run.Text("The "), new Run.Code("Doc"), new Run.Text(" type"));
        assertEquals("The Doc type", label.text());
        assertEquals(3, label.runs().size());
        assertTrue(Label.of("plain").runs().isEmpty());
        var e = assertThrows(IllegalArgumentException.class, () -> new Label("The Doc type", List.of(new Run.Text("The "), new Run.Code("Doc"))));
        assertTrue(e.getMessage().contains("its runs spell"), e.getMessage());
    }

    /** A workspace that offers the widgets a tree places, for the arrangement's own checks. */
    record Spec() implements WorkspaceSpec {
        @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of("tree-bench"); }
        @Override public java.util.Set<WidgetKind> widgetKinds() { return java.util.Set.of(WidgetKind.of("note"), WidgetKind.of("grid")); }
    }

    @Test
    void anArrangementOverATreeHoldsItsOwnChecks() {
        var tree = TreePlacement.of(TreePlacement.root("r", List.of("a"), TreePlacement.node("s", "S", "b")));
        var ok = Arrangement.of(new Spec(), tree, ArrangedWidget.of("a", "note"), ArrangedWidget.of("b", "grid"));
        assertEquals(TreePlacement.ENGINE, ok.engine());
        var unplaced = assertThrows(IllegalArgumentException.class,
                () -> Arrangement.of(new Spec(), tree, ArrangedWidget.of("a", "note"), ArrangedWidget.of("b", "grid"), ArrangedWidget.of("c", "note")));
        assertTrue(unplaced.getMessage().contains("places it nowhere"), unplaced.getMessage());
        var unnamed = assertThrows(IllegalArgumentException.class, () -> Arrangement.of(new Spec(), tree, ArrangedWidget.of("a", "note")));
        assertTrue(unnamed.getMessage().contains("which it does not open"), unnamed.getMessage());
        var unoffered = assertThrows(IllegalArgumentException.class,
                () -> Arrangement.of(new Spec(), tree, ArrangedWidget.of("a", "note"), ArrangedWidget.of("b", "video")));
        assertTrue(unoffered.getMessage().contains("which the workspace does not offer"), unoffered.getMessage());
    }
}
