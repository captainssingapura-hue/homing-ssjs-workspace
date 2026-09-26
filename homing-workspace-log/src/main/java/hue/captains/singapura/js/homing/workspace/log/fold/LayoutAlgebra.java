package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.Axis;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.RegionId;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.Side;
import hue.captains.singapura.js.homing.workspace.log.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The grid's algebra on the log's {@link Layout} — {@code SplitGridTree}'s,
 * move for move: the same subdivision, the same removal with its room going
 * the same way, the same splits giving way. Where the grid computes a share
 * with a double, this computes it exactly ({@link ExactShare}) and brings each
 * split back to whole millionths by one rule; so the structure is the grid's,
 * the shares are the grid's to the millionth, and the result is the same in
 * Java and in the JavaScript that transcribes this.
 */
public final class LayoutAlgebra {

    private LayoutAlgebra() {}

    /** A refusal: the layout cannot do what the event says it did. */
    public static final class Refused extends IllegalArgumentException {
        public Refused(String why) { super(why); }
    }

    // ── the tree being worked on ─────────────────────────────────────────

    private static final class Node {
        RegionId cell;          // a cell when set
        Axis axis;
        List<Kid> kids;
        Node(RegionId cell) { this.cell = cell; }
        Node(Axis axis, List<Kid> kids) { this.axis = axis; this.kids = kids; }
    }

    private static final class Kid {
        Node node;
        ExactShare share;
        Kid(Node node, ExactShare share) { this.node = node; this.share = share; }
    }

    private record Hit(Node node, Node parent, int index) {}
    private record Link(Node split, int index) {}
    private record Over(boolean before, String id, int at) {}

    private static Node work(Layout l) {
        return switch (l) {
            case Layout.Cell c -> new Node(c.region());
            case Layout.Split s -> {
                var kids = new ArrayList<Kid>();
                for (Track t : s.tracks()) kids.add(new Kid(work(t.node()), ExactShare.millionthsOf(t.share().units())));
                yield new Node(s.axis(), kids);
            }
        };
    }

    private static Layout done(Node n) {
        if (n.cell != null) return new Layout.Cell(n.cell);
        var shares = new ArrayList<ExactShare>();
        for (Kid k : n.kids) shares.add(k.share);
        long[] units = ExactShare.millionths(shares);
        var tracks = new ArrayList<Track>();
        for (int i = 0; i < n.kids.size(); i++) tracks.add(new Track(done(n.kids.get(i).node), Scaled.of(units[i], Track.SCALE)));
        return new Layout.Split(n.axis, tracks);
    }

    private static List<String> cells(Node n) {
        var out = new ArrayList<String>();
        if (n.cell != null) { out.add(n.cell.value()); return out; }
        for (Kid k : n.kids) out.addAll(cells(k.node));
        return out;
    }

    private static Hit find(Node n, String id, Node parent, int index) {
        if (n.cell != null) return n.cell.value().equals(id) ? new Hit(n, parent, index) : null;
        for (int i = 0; i < n.kids.size(); i++) {
            Hit h = find(n.kids.get(i).node, id, n, i);
            if (h != null) return h;
        }
        return null;
    }

    private static List<Link> chain(Node n, String id, List<Link> acc) {
        if (n.cell != null) return n.cell.value().equals(id) ? acc : null;
        for (int i = 0; i < n.kids.size(); i++) {
            var next = new ArrayList<>(acc);
            next.add(new Link(n, i));
            var hit = chain(n.kids.get(i).node, id, next);
            if (hit != null) return hit;
        }
        return null;
    }

    private static Link parentOf(Node n, Node target, Node parent, int index) {
        if (n == target) return parent != null ? new Link(parent, index) : null;
        if (n.cell != null) return null;
        for (int i = 0; i < n.kids.size(); i++) {
            Link hit = parentOf(n.kids.get(i).node, target, n, i);
            if (hit != null) return hit;
        }
        return null;
    }

    /** The node takes the place of the split it was the last child of: at the root, or in the split above. */
    private static Node giveWay(Node root, Node split) {
        Node only = split.kids.get(0).node;
        Link above = parentOf(root, split, null, -1);
        if (above == null) return only;
        above.split().kids.get(above.index()).node = only;
        return root;
    }

    // ── subdivide ────────────────────────────────────────────────────────

    /** A new, empty region beside one, on a side: a sibling when the parent lies that way, else the two a split. */
    public static Layout subdivide(Layout layout, RegionId id, Side side, RegionId newId) {
        Node root = work(layout);
        if (cells(root).contains(newId.value())) throw new Refused("the region " + newId + " already exists");
        Hit hit = find(root, id.value(), null, -1);
        if (hit == null) throw new Refused("no region " + id);
        Axis axis = side == Side.LEFT || side == Side.RIGHT ? Axis.HORIZONTAL : Axis.VERTICAL;
        boolean before = side == Side.LEFT || side == Side.TOP;
        Node fresh = new Node(newId);
        if (hit.parent() != null && hit.parent().axis == axis) {
            Kid k = hit.parent().kids.get(hit.index());
            ExactShare half = k.share.times(ExactShare.half());
            k.share = half;
            hit.parent().kids.add(before ? hit.index() : hit.index() + 1, new Kid(fresh, half));
            return done(root);
        }
        var pair = new ArrayList<Kid>();
        if (before) { pair.add(new Kid(fresh, ExactShare.half())); pair.add(new Kid(hit.node(), ExactShare.half())); }
        else { pair.add(new Kid(hit.node(), ExactShare.half())); pair.add(new Kid(fresh, ExactShare.half())); }
        Node split = new Node(axis, pair);
        if (hit.parent() == null) return done(split);
        hit.parent().kids.get(hit.index()).node = split;
        return done(root);
    }

    // ── remove ───────────────────────────────────────────────────────────

    /** The region gone, its room toward the region named — over a splitter of its own when they share one, else to the neighbour holding it, else beside it. */
    public static Layout remove(Layout layout, RegionId id, Optional<RegionId> toward) {
        Node root = work(layout);
        Hit hit = find(root, id.value(), null, -1);
        if (hit == null) throw new Refused("no region " + id);
        if (hit.parent() == null) throw new Refused("the last region cannot be removed");
        String to = toward.map(RegionId::value).orElse(null);
        if (to != null && !to.equals(id.value())) {
            for (Over o : splitters(root, id.value())) if (o.id().equals(to)) return done(across(root, id.value(), o));
        }
        List<Kid> siblings = hit.parent().kids;
        Kid gone = siblings.get(hit.index());
        int lean = lean(hit, to);
        siblings.remove(hit.index());
        Kid heir = siblings.get(lean > hit.index() ? hit.index() : lean);
        heir.share = heir.share.plus(gone.share);
        if (siblings.size() > 1) return done(root);
        return done(giveWay(root, hit.parent()));
    }

    private static int lean(Hit hit, String toward) {
        List<Kid> kids = hit.parent().kids;
        int i = hit.index();
        if (toward != null) {
            for (int d = 0; d < 2; d++) {
                int j = d == 1 ? i + 1 : i - 1;
                if (j >= 0 && j < kids.size() && cells(kids.get(j).node).contains(toward)) return j;
            }
        }
        return i > 0 ? i - 1 : i + 1;
    }

    private static List<Over> splitters(Node root, String id) {
        List<Link> chain = chain(root, id, new ArrayList<>());
        var out = new ArrayList<Over>();
        Axis axis = null;
        boolean before = true, after = true;
        for (int k = chain.size() - 1; k >= 0 && (before || after); k--) {
            Node s = chain.get(k).split();
            int i = chain.get(k).index(), last = s.kids.size() - 1;
            if (axis == null) axis = s.axis;
            else if (s.axis != axis) break;
            if (before && i > 0) {
                String far = facing(s.kids.get(i - 1).node, axis, false);
                if (far != null) out.add(new Over(true, far, k));
            }
            if (after && i < last) {
                String far = facing(s.kids.get(i + 1).node, axis, true);
                if (far != null) out.add(new Over(false, far, k));
            }
            if (i > 0) before = false;
            if (i < last) after = false;
        }
        return out;
    }

    private static String facing(Node n, Axis axis, boolean first) {
        while (n.cell == null) {
            if (n.axis != axis) return null;
            n = n.kids.get(first ? 0 : n.kids.size() - 1).node;
        }
        return n.cell.value();
    }

    private static Node across(Node root, String id, Over over) {
        List<Link> chain = chain(root, id, new ArrayList<>());
        Node s = chain.get(over.at()).split();
        int i = chain.get(over.at()).index(), j = over.before() ? i - 1 : i + 1;
        Kid mine = s.kids.get(i), theirs = s.kids.get(j);
        ExactShare share = mine.share;
        for (int k = over.at() + 1; k < chain.size(); k++) share = share.times(chain.get(k).split().kids.get(chain.get(k).index()).share);
        gains(theirs.node, theirs.share, share, over.id());
        theirs.share = theirs.share.plus(share);
        Node left = loses(mine.node, mine.share, share, id);
        if (left != null) { mine.node = left; mine.share = mine.share.minus(share); }
        else s.kids.remove(i);
        if (s.kids.size() > 1) return root;
        return giveWay(root, s);
    }

    private static void gains(Node n, ExactShare have, ExactShare extra, String id) {
        if (n.cell != null) return;
        ExactShare total = have.plus(extra);
        int i = holding(n.kids, id);
        ExactShare at = have.times(n.kids.get(i).share);
        for (int j = 0; j < n.kids.size(); j++) {
            Kid k = n.kids.get(j);
            k.share = (j == i ? at.plus(extra) : have.times(k.share)).over(total);
        }
        gains(n.kids.get(i).node, at, extra, id);
    }

    private static Node loses(Node n, ExactShare have, ExactShare share, String id) {
        if (n.cell != null) return null;
        ExactShare total = have.minus(share);
        int i = holding(n.kids, id);
        ExactShare at = have.times(n.kids.get(i).share);
        Node inner = loses(n.kids.get(i).node, at, share, id);
        var sizes = new ArrayList<ExactShare>();
        for (int j = 0; j < n.kids.size(); j++) sizes.add(j == i ? at.minus(share) : have.times(n.kids.get(j).share));
        if (inner != null) n.kids.get(i).node = inner;
        else { n.kids.remove(i); sizes.remove(i); }
        for (int m = 0; m < n.kids.size(); m++) n.kids.get(m).share = sizes.get(m).over(total);
        return n.kids.size() > 1 ? n : n.kids.get(0).node;
    }

    private static int holding(List<Kid> kids, String id) {
        for (int i = 0; i < kids.size(); i++) if (cells(kids.get(i).node).contains(id)) return i;
        return -1;
    }

    // ── tracks ───────────────────────────────────────────────────────────

    /** A split's shares set: the split at the path — child indexes from the root, "" the root — with as many tracks as shares. */
    public static Layout tracks(Layout layout, String path, List<Scaled> shares) {
        Node root = work(layout);
        Node n = root;
        if (!path.isEmpty()) {
            for (String step : path.split("/")) {
                int k = Integer.parseInt(step);
                if (n.cell != null || k >= n.kids.size()) throw new Refused("no split at " + path);
                n = n.kids.get(k).node;
            }
        }
        if (n.cell != null) throw new Refused("the node at '" + path + "' is a region, not a split");
        if (n.kids.size() != shares.size()) throw new Refused("the split at '" + path + "' has " + n.kids.size() + " tracks, not " + shares.size());
        for (int i = 0; i < shares.size(); i++) {
            if (shares.get(i).scale() != Track.SCALE) throw new Refused("a share at scale " + shares.get(i).scale() + ": tracks are in millionths");
            n.kids.get(i).share = ExactShare.millionthsOf(shares.get(i).units());
        }
        return done(root);
    }
}
