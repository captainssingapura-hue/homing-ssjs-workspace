package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

import java.util.List;
import java.util.Objects;

/**
 * What the workspace log records: every change of the workspace's arrangement
 * that outlives the page, one variant apiece, spelled as the components report
 * it — a tab opened, turned into another kind, moved, shown or closed; a region
 * parted, removed; a split's tracks re-shared. Declared here and only here: the
 * JavaScript classes and both languages' codecs are generated from these records.
 *
 * <p>A float is not a place the log knows: floating is transient, and a tab
 * afloat is, as far as the log goes, still where it left. A merge is the moves
 * the merge made, then the region it emptied removed.</p>
 */
public sealed interface WorkspaceEvent {

    /** A tab was opened in a region, at an index there, holding a widget of a kind, under a title. */
    record TabOpened(TabId id, WidgetKind kind, WidgetTitle title, RegionId region, int index) implements WorkspaceEvent {
        public TabOpened {
            Objects.requireNonNull(id, "TabOpened.id");
            Objects.requireNonNull(kind, "TabOpened.kind");
            Objects.requireNonNull(title, "TabOpened.title");
            Objects.requireNonNull(region, "TabOpened.region");
            if (index < 0) throw new IllegalArgumentException("TabOpened.index " + index + " — non-negative");
        }
    }

    /** A tab became a tab of another kind, in place: the same tab, a new widget, a new title. */
    record TabBecame(TabId id, WidgetKind kind, WidgetTitle title) implements WorkspaceEvent {
        public TabBecame {
            Objects.requireNonNull(id, "TabBecame.id");
            Objects.requireNonNull(kind, "TabBecame.kind");
            Objects.requireNonNull(title, "TabBecame.title");
        }
    }

    /** A tab moved to an index in a region: another region's, or its own. */
    record TabMoved(TabId id, RegionId region, int index) implements WorkspaceEvent {
        public TabMoved {
            Objects.requireNonNull(id, "TabMoved.id");
            Objects.requireNonNull(region, "TabMoved.region");
            if (index < 0) throw new IllegalArgumentException("TabMoved.index " + index + " — non-negative");
        }
    }

    /** A region came to show a tab. */
    record TabShown(RegionId region, TabId id) implements WorkspaceEvent {
        public TabShown {
            Objects.requireNonNull(region, "TabShown.region");
            Objects.requireNonNull(id, "TabShown.id");
        }
    }

    /** A tab was closed. */
    record TabClosed(TabId id) implements WorkspaceEvent {
        public TabClosed {
            Objects.requireNonNull(id, "TabClosed.id");
        }
    }

    /** A region was parted: a new one, named, taken off one side of it. */
    record RegionParted(RegionId region, RegionId newRegion, Side side) implements WorkspaceEvent {
        public RegionParted {
            Objects.requireNonNull(region, "RegionParted.region");
            Objects.requireNonNull(newRegion, "RegionParted.newRegion");
            Objects.requireNonNull(side, "RegionParted.side");
            if (region.equals(newRegion)) throw new IllegalArgumentException("RegionParted: the new region is " + region + " itself");
        }
    }

    /** A region was removed from the grid, its neighbours taking its room; its tabs had already been moved. */
    record RegionRemoved(RegionId region) implements WorkspaceEvent {
        public RegionRemoved {
            Objects.requireNonNull(region, "RegionRemoved.region");
        }
    }

    /**
     * A split's children were re-shared: each child's share of the split, in
     * order, exact decimals of one scale adding up to exactly one.
     */
    record TracksChanged(SplitPath path, List<Scaled> shares) implements WorkspaceEvent {
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
