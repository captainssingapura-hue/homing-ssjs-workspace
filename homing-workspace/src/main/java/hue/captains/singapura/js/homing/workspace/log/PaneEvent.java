package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;

import java.util.Objects;
import java.util.Optional;

/**
 * What happens in a workspace of one pane - the placement that shows one of
 * the roster's widgets at a time, or none: which it shows. The simplest of the
 * placements, over the same roster as the split grid's; the workspace shows
 * one or the other by the display it is on, and the log keeps both.
 */
public sealed interface PaneEvent extends WorkspaceEvent {

    /** The pane came to show a widget of the roster - or nothing. */
    record PaneShown(Optional<WidgetId> widget) implements PaneEvent {
        public PaneShown {
            Objects.requireNonNull(widget, "PaneShown.widget");
        }
    }
}
