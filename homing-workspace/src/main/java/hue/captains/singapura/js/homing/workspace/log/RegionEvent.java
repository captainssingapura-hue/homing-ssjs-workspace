package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.SplitPath;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** What happens to the grid's regions: one is parted, one is removed, a split's tracks are re-shared. */
public sealed interface RegionEvent extends WorkspaceEvent {

    /** The side of a region a new one is parted off on. */
    enum Side { LEFT, RIGHT, TOP, BOTTOM }

    /** A region was parted: a new one, named, taken off one side of it. */
    record RegionParted(RegionId region, RegionId newRegion, Side side) implements RegionEvent {
        public RegionParted {
            Objects.requireNonNull(region, "RegionParted.region");
            Objects.requireNonNull(newRegion, "RegionParted.newRegion");
            Objects.requireNonNull(side, "RegionParted.side");
            if (region.equals(newRegion)) throw new IllegalArgumentException("RegionParted: the new region is " + region + " itself");
        }
    }

    /**
     * A region was removed from the grid, its tabs already moved; its room went
     * toward the region named, as the grid was told - the one it merged into -
     * or, none named, to the neighbour beside it. The layout after is the grid's
     * algebra on the layout before: the log need not say it.
     */
    record RegionRemoved(RegionId region, Optional<RegionId> toward) implements RegionEvent {
        public RegionRemoved {
            Objects.requireNonNull(region, "RegionRemoved.region");
            Objects.requireNonNull(toward, "RegionRemoved.toward");
        }
    }

    /**
     * A split's children were re-shared: each child's share of the split, in
     * order, exact decimals of one scale adding up to exactly one.
     */
    record TracksChanged(SplitPath path, List<Scaled> shares) implements RegionEvent {
        public TracksChanged {
            Objects.requireNonNull(path, "TracksChanged.path");
            Objects.requireNonNull(shares, "TracksChanged.shares");
            shares = List.copyOf(shares);
            if (shares.size() < 2) throw new IllegalArgumentException("TracksChanged.shares — two or more");
            int scale = shares.get(0).scale();
            long sum = 0;
            for (Scaled s : shares) {
                if (s.scale() != scale) throw new IllegalArgumentException("TracksChanged.shares — one scale for all, " + scale + " and " + s.scale());
                if (s.units() <= 0) throw new IllegalArgumentException("TracksChanged.shares — each above zero, got " + s);
                sum += s.units();
            }
            if (sum != pow10(scale)) throw new IllegalArgumentException("TracksChanged.shares — they add up to " + Scaled.of(sum, scale) + ", not 1");
        }

        private static long pow10(int n) {
            long p = 1;
            for (int i = 0; i < n; i++) p *= 10;
            return p;
        }
    }
}
