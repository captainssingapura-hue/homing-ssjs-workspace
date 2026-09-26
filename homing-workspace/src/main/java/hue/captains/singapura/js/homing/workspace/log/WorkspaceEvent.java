package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

import java.util.List;
import java.util.Objects;

/**
 * What the workspace log records: every change of the workspace's arrangement
 * that outlives the page, one variant apiece, spelled as the components report
 * it — a tab opened, turned into another kind, renamed, moved, shown or closed;
 * a region parted, removed; a split's tracks re-shared; a float opened, moved,
 * resized, raised, closed. Declared here and only here: the JavaScript classes
 * and both languages' codecs are generated from these records.
 *
 * <p>A tab is always somewhere — a {@link Host}: a region's dock or a float. A
 * float is a state the workspace comes back to, recorded like a region: where
 * it lies and how big, in whole pixels of the desk, and which is on top. A
 * merge is the moves the merge made, then the region it emptied removed; a
 * float that empties closes, after the move that emptied it.</p>
 */
public sealed interface WorkspaceEvent {

    /** A tab was opened in a host, at an index there, holding a widget of a kind, under a title. */
    record TabOpened(TabId id, WidgetKind kind, WidgetTitle title, Host host, int index) implements WorkspaceEvent {
        public TabOpened {
            Objects.requireNonNull(id, "TabOpened.id");
            Objects.requireNonNull(kind, "TabOpened.kind");
            Objects.requireNonNull(title, "TabOpened.title");
            Objects.requireNonNull(host, "TabOpened.host");
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

    /** A tab was renamed where it is: by its widget, its holder or its pane. */
    record TabRenamed(TabId id, WidgetTitle title) implements WorkspaceEvent {
        public TabRenamed {
            Objects.requireNonNull(id, "TabRenamed.id");
            Objects.requireNonNull(title, "TabRenamed.title");
        }
    }

    /** A tab moved to an index in a host: another host — a region, a float — or its own. */
    record TabMoved(TabId id, Host host, int index) implements WorkspaceEvent {
        public TabMoved {
            Objects.requireNonNull(id, "TabMoved.id");
            Objects.requireNonNull(host, "TabMoved.host");
            if (index < 0) throw new IllegalArgumentException("TabMoved.index " + index + " — non-negative");
        }
    }

    /** A host came to show a tab. */
    record TabShown(Host host, TabId id) implements WorkspaceEvent {
        public TabShown {
            Objects.requireNonNull(host, "TabShown.host");
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

    /** A float was opened on the desk: where it lies and how big, in whole pixels of the desk; on top. Its tabs come to it after. */
    record FloatOpened(FloatId id, int x, int y, int w, int h) implements WorkspaceEvent {
        public FloatOpened {
            Objects.requireNonNull(id, "FloatOpened.id");
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatOpened " + w + "×" + h + " — a measure above zero");
        }
    }

    /** A float came to rest somewhere else. */
    record FloatMoved(FloatId id, int x, int y) implements WorkspaceEvent {
        public FloatMoved {
            Objects.requireNonNull(id, "FloatMoved.id");
        }
    }

    /** A float has another measure. */
    record FloatResized(FloatId id, int w, int h) implements WorkspaceEvent {
        public FloatResized {
            Objects.requireNonNull(id, "FloatResized.id");
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatResized " + w + "×" + h + " — a measure above zero");
        }
    }

    /** A float came on top of the others. */
    record FloatRaised(FloatId id) implements WorkspaceEvent {
        public FloatRaised {
            Objects.requireNonNull(id, "FloatRaised.id");
        }
    }

    /** A float was closed: emptied by a move, or closed with what it held, those closes said first. */
    record FloatClosed(FloatId id) implements WorkspaceEvent {
        public FloatClosed {
            Objects.requireNonNull(id, "FloatClosed.id");
        }
    }
}
