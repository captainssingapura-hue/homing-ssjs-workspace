package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;

import java.util.Objects;
import java.util.Optional;

/**
 * The one pane's layer of what a workspace log folds to: which widget it
 * shows, if any - its own events alone folded into it.
 *
 * @param shown the widget shown, or none
 */
public record PaneState(Optional<WidgetId> shown) {

    public PaneState {
        Objects.requireNonNull(shown, "PaneState.shown");
    }

    /** Where every log starts: nothing shown. */
    public static PaneState empty() { return new PaneState(Optional.empty()); }
}
