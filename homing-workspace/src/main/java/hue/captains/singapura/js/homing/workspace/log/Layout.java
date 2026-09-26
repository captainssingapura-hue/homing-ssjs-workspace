package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The workspace's regions as a tree: a cell holding one region, or a split
 * laying two or more tracks along an axis, their shares adding up to exactly
 * one. The grid's own arrangement, declared here with exact shares, where the
 * grid keeps doubles: what the log folds to and a restore lays out.
 *
 * <p>The tree is recursive — a split's tracks hold layouts — so everything it
 * is made of is declared here, in one file: its JavaScript is one module, and
 * the codecs that call each other round the tree import nothing but what is
 * below them.</p>
 */
public sealed interface Layout permits Layout.Cell, Layout.Split {

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

    /** Which way a split lays its children: HORIZONTAL in a row, VERTICAL in a stack. */
    enum Axis { HORIZONTAL, VERTICAL }

    /**
     * One child of a split and its share of the split's room: an exact number of
     * millionths, {@link #SCALE} decimal places, above zero.
     *
     * @param node  what the track holds: a cell or another split
     * @param share its share of the split
     */
    record Track(Layout node, Scaled share) {

        /** The shares' scale: millionths. */
        public static final int SCALE = 6;

        public Track {
            Objects.requireNonNull(node, "Track.node");
            Objects.requireNonNull(share, "Track.share");
            if (share.scale() != SCALE) throw new IllegalArgumentException("Track.share " + share + " — at scale " + SCALE);
            if (share.units() <= 0) throw new IllegalArgumentException("Track.share " + share + " — above zero");
        }

        public static Track of(Layout node, long millionths) { return new Track(node, Scaled.of(millionths, SCALE)); }
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
