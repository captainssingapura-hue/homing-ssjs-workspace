package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;

/**
 * Where a tab is: a region's dock, or a float. Both are hosts of the desk's
 * tab-panes, and a tab afloat is as much somewhere as a tab docked — a float is
 * a state the workspace comes back to, so the log says which.
 */
public sealed interface Host {

    /** In a region of the grid: the dock in that cell. */
    record InRegion(RegionId id) implements Host {
        public InRegion { Objects.requireNonNull(id, "InRegion.id"); }
    }

    /** In a float on the desk. */
    record InFloat(FloatId id) implements Host {
        public InFloat { Objects.requireNonNull(id, "InFloat.id"); }
    }

    static Host region(String id) { return new InRegion(RegionId.of(id)); }

    static Host floating(String id) { return new InFloat(FloatId.of(id)); }
}
