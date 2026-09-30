package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Label;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Name;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Node;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement.Run;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetRef;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceSpec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The tree bench's tree: a synthetic tree, three levels deep, for the tree placement to lay
 * out - nodes of one leaf, of several and of none; a label drawn by runs; stand-in widgets
 * that show only their params; and the books grid and the chosen book in different sections,
 * meeting in one party.
 */
public final class BenchTree {

    private BenchTree() {}

    /** What the tree bench offers: the stand-in card, and the books. */
    public record Spec() implements WorkspaceSpec {
        @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of("tree-bench"); }
        @Override public Set<WidgetKind> widgetKinds() { return Set.of(WidgetKind.of("params-card"), WidgetKind.of("books-grid"), WidgetKind.of("book-jumbotron")); }
    }

    static final TreePlacement TREE = TreePlacement.of(TreePlacement.root("A tree, placed", List.of("intro"),
            TreePlacement.node("sections", "Sections and their leaves", List.of("sections-lead"),
                    TreePlacement.node("one", "A section of one leaf", "one"),
                    TreePlacement.node("several", "A section of several leaves", "several-a", "several-b", "several-c"),
                    TreePlacement.node("none", "A section of no leaf", List.of(),
                            TreePlacement.node("deeper", "Three levels down", "deeper"))),
            TreePlacement.node("widgets", "Real widgets, one party", List.of("widgets-lead"),
                    TreePlacement.node("books", "The books", "grid"),
                    TreePlacement.node("chosen", "The chosen book", "jumbotron")),
            new Node(Optional.of(Name.of("runs")),
                    Label.of(new Run.Text("A label drawn by runs: "), new Run.Code("code"), new Run.Text(", "),
                             new Run.Strong("strong"), new Run.Text(" and "), new Run.Emphasis("emphasis")),
                    List.of(WidgetRef.of("runs")),
                    List.of())));

    /** The widgets, then where they go. */
    public static final Arrangement<Spec, TreePlacement> ARRANGEMENT = Arrangement.of(new Spec(), TREE,
            card("intro", "The root's own leaf: the introduction, before any section."),
            card("sections-lead", "A section's own leaf, shown before its named children."),
            card("one", "The one leaf of its section."),
            card("several-a", "The first of three leaves."),
            card("several-b", "The second of three leaves."),
            card("several-c", "The third of three leaves."),
            card("deeper", "A leaf three levels down, under a section with no leaf of its own."),
            card("widgets-lead", "Below, the books grid and the chosen book: two sections, one party. Choose a book."),
            ArrangedWidget.of("grid", "books-grid"),
            ArrangedWidget.of("jumbotron", "book-jumbotron"),
            card("runs", "The heading above is drawn from its label's runs."));

    /** A stand-in card: its params - a note and a content key - are all it knows, and all it shows. */
    private static ArrangedWidget card(String ref, String note) {
        return ArrangedWidget.of(ref, "params-card", Map.of("note", note, "key", "bench/" + ref));
    }
}
