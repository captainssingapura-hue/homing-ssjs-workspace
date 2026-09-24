package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.PaneDirection;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RFC 0060 — <b>geometry only</b>: named panes and how the space divides between
 * them. No widgets, and no opinion about which workspace it serves.
 *
 * <p>That separation is the point. A pane arrangement is about <i>shape</i>, so it
 * is reusable across every workspace kind — the same three-pane IDE layout serves
 * a document studio and a trading desk. Widget allocation is the part that
 * belongs to a kind, and it lives in {@link Arrangement}, which holds one of
 * these.</p>
 *
 * <p>Being a pure {@link ValueObject} of shape, it is also the thing you can
 * <b>draw</b> — a thumbnail is a walk over the layout with no workspace, no
 * widget registry and no session required.</p>
 *
 * <h2>Built as a playbook</h2>
 *
 * <pre>{@code
 * PaneArrangement.named("editor-and-output")
 *         .root("main")
 *         .splitWithRatio("main", PaneDirection.RIGHT, "side", 0.70)
 *         .splitEvenly("side", PaneDirection.DOWN, "output")
 *         .build();
 * }</pre>
 *
 * <p>The sequence of splits a person would have performed, in the vocabulary the
 * runtime already records when they do ({@code SplitCreated(paneId, orientation,
 * newRatio)}).</p>
 *
 * <h2>The ratio is the share the split pane KEEPS</h2>
 *
 * <p>The one thing an author can get wrong, so the method says it: above,
 * {@code main} keeps 0.70 of the whole and {@code side} takes 0.30 — and
 * splitting {@code side} evenly then gives two panes of <b>0.15 each of the
 * workspace</b>. A ratio is local to its split, exactly as the event stores it
 * and exactly as a person experiences dragging a divider, which means these
 * numbers read as absolute and are not.</p>
 *
 * <p>A split holds its tracks with their shares, so the ratio an author writes
 * is the share the split pane KEEPS of its own track; the new pane takes the
 * rest of that track, and every other track is left alone.</p>
 *
 * @param name   identifies the shape; shared ones are referenced by it
 * @param layout the pane tree, binary throughout (D1)
 *
 * @since RFC 0060
 */
public record PaneArrangement(String name, LayoutNode layout) implements ValueObject {

    public PaneArrangement {
        Objects.requireNonNull(name,   "PaneArrangement.name");
        Objects.requireNonNull(layout, "PaneArrangement.layout");
        if (name.isBlank()) throw new IllegalArgumentException("PaneArrangement.name must not be blank");
    }

    /** Every pane, in playbook order — the order panes were created. */
    public List<PaneId> panes() {
        var out = new ArrayList<PaneId>();
        collect(layout, out);
        return List.copyOf(out);
    }

    /** Does this shape have a pane by that name? */
    public boolean hasPane(PaneId pane) { return panes().contains(pane); }

    /** How many panes the shape divides into — and therefore {@code count - 1} dividers. */
    public int paneCount() { return panes().size(); }



    /**
     * This shape's pane by that name, validated now — the only way to obtain a
     * {@link ShapePane}.
     *
     * <p>A shipped shape publishes its panes as constants built through here, so
     * a typo fails at class initialisation naming the panes that exist, rather
     * than compiling and going wrong at boot.</p>
     */
    public ShapePane pane(String paneName) {
        var id = new PaneId(paneName);
        if (!hasPane(id)) {
            throw new IllegalArgumentException(
                    "pane: '" + paneName + "' is not in shape '" + name + "' — panes are "
                  + panes().stream().map(PaneId::value).toList());
        }
        return new ShapePane(name, paneName);
    }

    /** Every pane of this shape, as {@link ShapePane}s, in playbook order. */
    public List<ShapePane> shapePanes() {
        return panes().stream().map(p -> new ShapePane(name, p.value())).toList();
    }
    /**
     * Start allocating widgets into this shape — the step from geometry (shared)
     * to a full {@link Arrangement} (a workspace kind's own).
     */
    public Arrangement.Builder allocate() { return new Arrangement.Builder(this); }

    /** This shape with no widgets in it at all. */
    public Arrangement empty() { return Arrangement.of(this); }

    private static void collect(LayoutNode n, List<PaneId> out) {
        switch (n) {
            case LayoutNode.Leaf leaf -> out.add(leaf.paneId());
            case LayoutNode.Split s   -> { for (var c : s.children()) collect(c.node(), out); }
        }
    }

    // ── Playbook ─────────────────────────────────────────────────────────────

    /** Start a playbook. */
    public static Named named(String name) { return new Named(name); }

    /** Intermediate step: a shape needs its first pane before it can be split. */
    public record Named(String name) {
        public Named {
            Objects.requireNonNull(name, "PaneArrangement.named");
            if (name.isBlank()) throw new IllegalArgumentException("PaneArrangement.named must not be blank");
        }
        /** The whole workspace as one pane — every playbook starts here. */
        public Builder root(String paneName) {
            return new Builder(name, new LayoutNode.Leaf(new PaneId(paneName)));
        }
    }

    /** The playbook. Mutable while building; what it yields is not. */
    public static final class Builder {

        private final String name;
        private LayoutNode layout;

        private Builder(String name, LayoutNode layout) {
            this.name = name; this.layout = layout;
        }

        /**
         * Split {@code target} in half, putting the new pane in {@code direction}.
         * Sugar for {@link #splitWithRatio} at 0.5 — already what
         * {@code SplitPane.split} builds, so the even case needs no number (D5).
         */
        public Builder splitEvenly(String target, PaneDirection direction, String newPane) {
            return splitWithRatio(target, direction, newPane, 0.5);
        }

        /**
         * Split {@code target}, putting the new pane in {@code direction}.
         *
         * @param keep the share {@code target} KEEPS, strictly between 0 and 1 —
         *             see the class note; the new pane takes the remainder
         */
        public Builder splitWithRatio(String target, PaneDirection direction,
                                      String newPane, double keep) {
            Objects.requireNonNull(direction, "splitWithRatio.direction");
            var targetId = new PaneId(target);
            var newId    = new PaneId(newPane);
            if (!(keep > 0.0 && keep < 1.0)) {
                throw new IllegalArgumentException(
                        "splitWithRatio: '" + target + "' keeps " + keep
                      + " — must be strictly between 0 and 1");
            }
            if (find(layout, newId)) {
                throw new IllegalArgumentException("splitWithRatio: pane '" + newPane + "' already exists");
            }
            if (!find(layout, targetId)) {
                throw new IllegalArgumentException(
                        "splitWithRatio: no pane named '" + target + "' to split");
            }
            layout = replace(layout, targetId, direction, newId, keep);
            return this;
        }

        public PaneArrangement build() { return new PaneArrangement(name, layout); }

        /**
         * Carve a new pane off {@code target}, by the same rule the live grid
         * subdivides with: a SIBLING in the row or column already there when its
         * orientation matches the direction, and only otherwise a new split of the
         * two. Authoring a shape and dragging one out must land on the same tree,
         * or the arrangement a kind ships could not be reached by hand — and the
         * two shapes are not interchangeable: a flat row has a divider per pair,
         * where a nested one has an outer divider that moves two panes at once and
         * denies the merge across it.
         */
        private static LayoutNode replace(LayoutNode n, PaneId target,
                                          PaneDirection dir, PaneId added, double keep) {
            return switch (n) {
                case LayoutNode.Leaf leaf -> leaf.paneId().equals(target) ? carve(leaf, dir, added, keep) : leaf;
                case LayoutNode.Split s -> {
                    int at = s.orientation() == dir.orientation() ? trackOf(s, target) : -1;
                    if (at >= 0) yield beside(s, at, dir, added, keep);
                    var kids = new ArrayList<LayoutNode.Child>(s.children().size());
                    for (var c : s.children()) {
                        kids.add(new LayoutNode.Child(replace(c.node(), target, dir, added, keep), c.ratio()));
                    }
                    yield new LayoutNode.Split(s.orientation(), kids);
                }
            };
        }

        /** No row to join: the pane becomes a split of itself and the new one. */
        private static LayoutNode carve(LayoutNode.Leaf leaf, PaneDirection dir, PaneId added, double keep) {
            var kept  = new LayoutNode.Leaf(leaf.paneId());
            var fresh = new LayoutNode.Leaf(added);
            return dir.targetIsFirst()
                    ? LayoutNode.Split.of(dir.orientation(), keep, kept, fresh)
                    : LayoutNode.Split.of(dir.orientation(), 1.0 - keep, fresh, kept);
        }

        /**
         * A row or column already runs this way: the new pane joins it beside the
         * target, and the two share the TARGET'S track between them. Every other
         * track keeps what it had, which is what a reader means by "split that one".
         */
        private static LayoutNode beside(LayoutNode.Split s, int at,
                                         PaneDirection dir, PaneId added, double keep) {
            double share = s.ratio(at);
            var kids = new ArrayList<>(s.children());
            kids.set(at, new LayoutNode.Child(kids.get(at).node(), share * keep));
            kids.add(dir.targetIsFirst() ? at + 1 : at,
                     new LayoutNode.Child(new LayoutNode.Leaf(added), share * (1.0 - keep)));
            return new LayoutNode.Split(s.orientation(), kids);
        }

        /** Which track of this split IS the target pane, or -1 when none is. */
        private static int trackOf(LayoutNode.Split s, PaneId target) {
            for (int i = 0; i < s.children().size(); i++) {
                if (s.child(i) instanceof LayoutNode.Leaf leaf && leaf.paneId().equals(target)) return i;
            }
            return -1;
        }

        private static boolean find(LayoutNode n, PaneId id) {
            return switch (n) {
                case LayoutNode.Leaf leaf -> leaf.paneId().equals(id);
                case LayoutNode.Split s   -> {
                    for (var c : s.children()) if (find(c.node(), id)) yield true;
                    yield false;
                }
            };
        }
    }
}
