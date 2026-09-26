package hue.captains.singapura.js.homing.workspace.log;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A float: where it lies and how big, in whole pixels of the desk; its tabs in
 * the order its strip shows them, and the one it shows, if any.
 */
public record FloatState(FloatId id, int x, int y, int w, int h, List<TabId> tabs, Optional<TabId> shown) {
    public FloatState {
        Objects.requireNonNull(id, "FloatState.id");
        if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatState " + id + " " + w + "×" + h + " — a measure above zero");
        tabs = List.copyOf(Objects.requireNonNull(tabs, "FloatState.tabs"));
        Objects.requireNonNull(shown, "FloatState.shown");
        HostTabs.check("float " + id, tabs, shown);
    }
}
