package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Where a fixed tree's widgets go: engine {@code tree}. A tree of named nodes - its
 * structure, as a split grid's tab bars are - each with at most one <b>leaf</b>, before its
 * named children: its own content, a list of <b>unnamed widgets</b> in the order they are
 * shown. A leaf is one thing: one entry in the contents - its node's - folded and unfolded
 * with it. Only a leaf holds widgets; a node never is one. A doc's headings are such a tree,
 * each section's prose, tables and code its leaf; so are a plan's phases.
 *
 * <p>A node, and its leaf, are located by the node's path: the names from the root, joined by
 * {@code /}. A widget in a leaf is where it is by its position there, {@code design/keys:1}.
 * Locators are the placement's and navigation's; a widget never learns its own - it is made
 * from its type and params alone.</p>
 *
 * <p>Fixed: made once from what it places, never rearranged. There is nothing to log and
 * nothing to persist; where the reader is belongs to the engine that shows it.</p>
 *
 * <pre>{@code
 * TreePlacement.of(TreePlacement.root("The doc", List.of("intro"),
 *         TreePlacement.node("design", "Design", List.of("design-lead"),
 *                 TreePlacement.node("keys", "Keys", "keys-body")),
 *         TreePlacement.node("plan", "Plan", "plan-body", "plan-table")))
 * }</pre>
 *
 * @param root the tree's root: no name, its label the whole's, its leaf the introduction
 */
public record TreePlacement(Node root) implements Placement {

    /** The tree placement. */
    public static final PlacementEngine ENGINE = PlacementEngine.of("tree");

    /**
     * How deep a tree goes below its root: the rigid tree's levels. A node's leaf sits a level
     * below it, so a node at this depth has none.
     */
    public static final int MAX_DEPTH = 18;

    /** A node's name in its path: letters, digits, dot, underscore, hyphen - at most 48, as the rigid tree's. */
    public record Name(String value) {
        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9._-]{1,48}");

        public Name {
            Objects.requireNonNull(value, "Name.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("Name.value '" + value + "' - letters, digits, dot, underscore, hyphen; 1 to 48");
            }
        }

        public static Name of(String value) { return new Name(value); }

        @Override public String toString() { return value; }
    }

    /**
     * What a node is shown as: its text, and optionally the runs that draw it - code as code,
     * strong as strong. The runs, when there are any, spell the text exactly, so the TOC's
     * text and the heading's runs can never disagree.
     */
    public record Label(String text, List<Run> runs) {
        public Label {
            Objects.requireNonNull(text, "Label.text");
            runs = List.copyOf(Objects.requireNonNull(runs, "Label.runs"));
            if (!runs.isEmpty()) {
                var spelled = new StringBuilder();
                for (Run r : runs) spelled.append(r.text());
                if (!spelled.toString().equals(text)) {
                    throw new IllegalArgumentException("the label '" + text + "' - its runs spell '" + spelled + "'");
                }
            }
        }

        /** Plain text, no runs. */
        public static Label of(String text) { return new Label(text, List.of()); }

        /** Drawn by these runs; its text is theirs. */
        public static Label of(Run... runs) {
            var text = new StringBuilder();
            for (Run r : runs) text.append(r.text());
            return new Label(text.toString(), List.of(runs));
        }
    }

    /** A run of a label. */
    public sealed interface Run permits Run.Text, Run.Code, Run.Strong, Run.Emphasis {
        String text();

        record Text(String text) implements Run { public Text { Objects.requireNonNull(text, "Run.Text.text"); } }
        record Code(String text) implements Run { public Code { Objects.requireNonNull(text, "Run.Code.text"); } }
        record Strong(String text) implements Run { public Strong { Objects.requireNonNull(text, "Run.Strong.text"); } }
        record Emphasis(String text) implements Run { public Emphasis { Objects.requireNonNull(text, "Run.Emphasis.text"); } }
    }

    /**
     * A node: its name in the path - none for the root, and only for the root - its label, its
     * leaf, and its named children after it. The leaf is the node's own content, the unnamed
     * widgets in the order they are shown; empty, the node has none.
     */
    public record Node(Optional<Name> name, Label label, List<WidgetRef> leaf, List<Node> children) {
        public Node {
            Objects.requireNonNull(name, "Node.name (use Optional.empty for the root)");
            Objects.requireNonNull(label, "Node.label");
            leaf = List.copyOf(Objects.requireNonNull(leaf, "Node.leaf"));
            children = List.copyOf(Objects.requireNonNull(children, "Node.children"));
        }

        /** Whether it has content of its own. */
        public boolean hasLeaf() { return !leaf.isEmpty(); }
    }

    /** A widget, where the placement has it: its node's path, and its position in the node's leaf. */
    public record Spot(String path, int position, WidgetRef widget) {
        public Spot {
            Objects.requireNonNull(path, "Spot.path");
            Objects.requireNonNull(widget, "Spot.widget");
            if (position < 0) throw new IllegalArgumentException("a position in a leaf " + position + " - zero or more");
        }

        /** Where it is, as text: {@code design/keys:1}; in the root's leaf {@code :0}, {@code :1}. */
        public String locator() { return path + ":" + position; }
    }

    public TreePlacement {
        Objects.requireNonNull(root, "TreePlacement.root");
        if (root.name().isPresent()) throw new IllegalArgumentException("the root is named " + root.name().get() + " - the root has no name");
        check(root, "", 0, new HashSet<>());
    }

    private static void check(Node node, String path, int depth, Set<WidgetRef> placed) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("the node " + shown(path) + " is " + depth + " deep - at most " + MAX_DEPTH);
        if (depth == MAX_DEPTH && node.hasLeaf()) {
            throw new IllegalArgumentException("the node " + shown(path) + " is " + depth + " deep and has a leaf - its widgets would be "
                    + (depth + 1) + " deep; a node with a leaf is at most " + (MAX_DEPTH - 1));
        }
        for (WidgetRef w : node.leaf()) {
            if (!placed.add(w)) throw new IllegalArgumentException("the tree places " + w + " twice");
        }
        var names = new HashSet<Name>();
        for (Node child : node.children()) {
            Name name = child.name().orElseThrow(() -> new IllegalArgumentException("a child of " + shown(path) + " has no name - only the root is nameless"));
            if (!names.add(name)) throw new IllegalArgumentException("the node " + shown(path) + " has two children named " + name);
            check(child, pathOf(path, name), depth + 1, placed);
        }
    }

    private static String pathOf(String parent, Name name) { return parent.isEmpty() ? name.value() : parent + "/" + name.value(); }

    private static String shown(String path) { return path.isEmpty() ? "the root" : "'" + path + "'"; }

    public static TreePlacement of(Node root) { return new TreePlacement(root); }

    /** The root: the whole's label, the widgets of its leaf - the introduction - its children. */
    public static Node root(String label, List<String> leaf, Node... children) {
        return new Node(Optional.empty(), Label.of(label), refs(leaf), List.of(children));
    }

    /** A named node: the widgets of its leaf, before its children. */
    public static Node node(String name, String label, List<String> leaf, Node... children) {
        return new Node(Optional.of(Name.of(name)), Label.of(label), refs(leaf), List.of(children));
    }

    /** A named node with a leaf of these widgets, and no children. */
    public static Node node(String name, String label, String... leaf) {
        return new Node(Optional.of(Name.of(name)), Label.of(label), refs(List.of(leaf)), List.of());
    }

    private static List<WidgetRef> refs(List<String> widgets) { return widgets.stream().map(WidgetRef::of).toList(); }

    @Override public PlacementEngine engine() { return ENGINE; }

    /** The widgets in reading order: a node's leaf, then its children's. */
    @Override
    public List<WidgetRef> placed() { return spots().stream().map(Spot::widget).toList(); }

    /** Every widget, in reading order, where it is. */
    public List<Spot> spots() {
        var out = new ArrayList<Spot>();
        walk(root, "", out);
        return List.copyOf(out);
    }

    private static void walk(Node node, String path, List<Spot> out) {
        for (int i = 0; i < node.leaf().size(); i++) out.add(new Spot(path, i, node.leaf().get(i)));
        for (Node child : node.children()) walk(child, pathOf(path, child.name().orElseThrow()), out);
    }

    /** Every node's path, in reading order, the root's ({@code ""}) first. */
    public List<String> paths() {
        var out = new ArrayList<String>();
        paths(root, "", out);
        return List.copyOf(out);
    }

    private static void paths(Node node, String path, List<String> out) {
        out.add(path);
        for (Node child : node.children()) paths(child, pathOf(path, child.name().orElseThrow()), out);
    }

    /** The node at a path, if the tree has one there. */
    public Optional<Node> node(String path) {
        Objects.requireNonNull(path, "path");
        Node at = root;
        if (path.isEmpty()) return Optional.of(at);
        for (String segment : path.split("/", -1)) {
            Node next = null;
            for (Node child : at.children()) if (child.name().orElseThrow().value().equals(segment)) { next = child; break; }
            if (next == null) return Optional.empty();
            at = next;
        }
        return Optional.of(at);
    }

    /** Where a widget is, if the tree places it. */
    public Optional<Spot> spotOf(WidgetRef widget) {
        for (Spot s : spots()) if (s.widget().equals(widget)) return Optional.of(s);
        return Optional.empty();
    }

    /** How deep the tree goes: 0 for a root alone. */
    public int depth() { return depth(root); }

    private static int depth(Node node) {
        int deepest = 0;
        for (Node child : node.children()) deepest = Math.max(deepest, 1 + depth(child));
        return deepest;
    }
}
