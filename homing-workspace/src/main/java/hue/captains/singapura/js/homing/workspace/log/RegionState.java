package hue.captains.singapura.js.homing.workspace.log;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A region: its tabs in the order its strip shows them, and the one it shows, if any. */
public record RegionState(RegionId id, List<TabId> tabs, Optional<TabId> shown) {
    public RegionState {
        Objects.requireNonNull(id, "RegionState.id");
        tabs = List.copyOf(Objects.requireNonNull(tabs, "RegionState.tabs"));
        Objects.requireNonNull(shown, "RegionState.shown");
        HostTabs.check("region " + id, tabs, shown);
    }
}
