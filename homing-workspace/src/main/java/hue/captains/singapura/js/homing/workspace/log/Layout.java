package hue.captains.singapura.js.homing.workspace.log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The workspace's regions as a tree: a cell holding one region, or a split
 * laying two or more tracks along an axis, their shares adding up to exactly
 * one. The grid's own arrangement, declared here with exact shares, where the
 * grid keeps doubles: what the log folds to and a restore lays out.
 */
public sealed interface Layout {

    /** One region. */
    record Cell(RegionId region) implements Layout {
        public Cell { Objects.requireNonNull(region, "Cell.region"); }
    }

    /** Two or more tracks along an axis, their shares adding up to exactly one. */
    record Split(Axis axis, List<Track> tracks) implements Layout {
        public Split {
            Objects.requireNonNull(axis, "Split.axis");
            tracks = List.copyOf(Objects.requireNonNull(tracks, "Split.tracks"));
            if (tracks.size() < 2) throw new IllegalArgumentException("Split.tracks — two or more");
            long sum = 0;
            for (Track t : tracks) sum += t.share().units();
            if (sum != WHOLE) throw new IllegalArgumentException("Split.tracks — shares add up to " + Scaled.of(sum, Track.SCALE) + ", not 1");
        }
    }

    /** One, in millionths. */
    long WHOLE = 1_000_000L;

    /** The regions, in the tree's order: left to right, top to bottom, depth first. */
    static List<RegionId> regions(Layout node) {
        var out = new ArrayList<RegionId>();
        collect(node, out);
        return out;
    }

    private static void collect(Layout node, List<RegionId> out) {
        switch (node) {
            case Cell c  -> out.add(c.region());
            case Split s -> { for (Track t : s.tracks()) collect(t.node(), out); }
        }
    }

    /** Every region once, or the tree is refused. */
    static Layout checked(Layout node) {
        var seen = new HashSet<RegionId>();
        for (RegionId r : regions(node)) {
            if (!seen.add(r)) throw new IllegalArgumentException("Layout — the region " + r + " is in it twice");
        }
        return node;
    }
}
