package hue.captains.singapura.js.homing.workspace.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One of the captured axes of {@link WorkspaceState}: pure split structure.
 * The captured tree mirrors the live grid's — splits at internal nodes, leaf
 * panes (addressable by {@link PaneId}) at the leaves.
 *
 * <p>Sealed so the runtime can pattern-match exhaustively when rebuilding the
 * live tree on restore. {@link Leaf}s carry the {@link PaneId} that each
 * widget's {@link WidgetLocation.InPane} references; {@link Split}s carry an
 * orientation and two or more children, each with its share.</p>
 *
 * <h2>Why a split has N children and not two</h2>
 *
 * <p>Schema 2. A split used to be binary with one ratio — the first child's
 * share — and a row of three was a split inside a split. The grid does not
 * arrange itself that way: subdividing a cell adds a SIBLING when the parent's
 * orientation already matches, and nests only when it does not. The two shapes
 * render alike and are not the same arrangement.</p>
 *
 * <p>A flat row of three has two dividers that move independently; the nested
 * pair has an outer divider that moves two panes together. And a pane can be
 * merged only across a WHOLE divider — one pane alone on each side — so in the
 * nested shape the outer pane cannot merge with its neighbour at all, because
 * that divider has two panes behind it. Writing a flat row down as a nested one
 * does not lose a spelling; it loses legal merges and changes what a drag does.</p>
 *
 * <p>So the capture is flat where the grid is flat. Every schema-1 split is a
 * two-child split with shares {@code [ratio, 1 - ratio]}, which is why the
 * migration cannot fail; the other direction is the one that loses, and is
 * exactly what this stops doing.</p>
 *
 * @since RFC 0029 cycle 1; N-ary at schema 2
 */
public sealed interface LayoutNode permits LayoutNode.Leaf, LayoutNode.Split {

    /**
     * Leaf pane — the addressable terminal of the layout tree. Its
     * {@link PaneId} is what widgets reference via
     * {@link WidgetLocation.InPane#paneId()}.
     *
     * @param paneId stable pane identifier (workspace-scoped)
     */
    record Leaf(PaneId paneId) implements LayoutNode {
        public Leaf {
            Objects.requireNonNull(paneId, "LayoutNode.Leaf.paneId");
        }
    }

    /**
     * One track of a split: a sub-tree and its share of the split's space.
     *
     * @param node  the sub-tree in this track
     * @param ratio its share, positive; the shares of a split are normalised to sum to one
     */
    record Child(LayoutNode node, double ratio) {
        public Child {
            Objects.requireNonNull(node, "LayoutNode.Child.node");
            if (!(ratio > 0.0) || Double.isInfinite(ratio)) {
                throw new IllegalArgumentException(
                        "LayoutNode.Child.ratio must be a positive finite number (got " + ratio + ")");
            }
        }
    }

    /**
     * Internal split node — two or more child sub-trees laid out along the
     * orientation axis, each taking its share.
     *
     * <p>The shares are normalised to sum to one on construction, as the grid
     * normalises its tracks: a caller may write {@code 2} and {@code 1} for
     * two-thirds and one-third and be understood.</p>
     *
     * @param orientation horizontal (a row) or vertical (a column)
     * @param children    the tracks in order, two or more
     */
    record Split(Orientation orientation, List<Child> children) implements LayoutNode {
        public Split {
            Objects.requireNonNull(orientation, "LayoutNode.Split.orientation");
            Objects.requireNonNull(children,    "LayoutNode.Split.children");
            if (children.size() < 2) {
                throw new IllegalArgumentException(
                        "LayoutNode.Split.children: a split needs two or more (got " + children.size() + ")");
            }
            children = normalised(children);
        }

        /** The shares as given, rescaled to sum to one. */
        private static List<Child> normalised(List<Child> children) {
            double sum = 0.0;
            for (Child c : children) {
                Objects.requireNonNull(c, "LayoutNode.Split.children element");
                sum += c.ratio();
            }
            if (!(sum > 0.0) || Double.isInfinite(sum)) {
                throw new IllegalArgumentException("LayoutNode.Split.children: shares must sum to a positive finite number");
            }
            var out = new ArrayList<Child>(children.size());
            for (Child c : children) out.add(new Child(c.node(), c.ratio() / sum));
            return List.copyOf(out);
        }

        /**
         * The two-child split schema 1 could express: {@code ratio} is the
         * first child's share, as it was. Kept because a binary split is still
         * the common one to write, and because the migration is this call.
         */
        public static Split of(Orientation orientation, double ratio, LayoutNode first, LayoutNode second) {
            if (!(ratio > 0.0 && ratio < 1.0)) {
                throw new IllegalArgumentException(
                        "LayoutNode.Split.of: ratio must be strictly between 0.0 and 1.0 (got " + ratio + ")");
            }
            return new Split(orientation, List.of(new Child(first, ratio), new Child(second, 1.0 - ratio)));
        }

        /** The sub-tree in track {@code i}. */
        public LayoutNode child(int i) { return children.get(i).node(); }

        /** The share of track {@code i}, normalised. */
        public double ratio(int i) { return children.get(i).ratio(); }
    }
}
