package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.PaneEvent;
import hue.captains.singapura.js.homing.workspace.log.PaneState;

/**
 * The one pane's layer of the fold: what it shows - a widget, or nothing. It
 * folds its own events and no other: a placement is handed widgets to show and
 * never asks the roster whether they are, and while it places it records every
 * consequence of the roster itself - a widget it shows, closed, is preceded by
 * the pane's own {@code PaneShown} of the next (RFC 0066 E3, the workspace
 * detour: placement engines, each folding only its own). The JavaScript is
 * this, line for line (PaneFoldModule.js), and the two agree to the byte.
 */
public final class PaneFold {

    private PaneFold() {}

    /** One of the pane's events on its state: the state after it. */
    public static PaneState apply(PaneState state, PaneEvent event) {
        return switch (event) {
            case PaneEvent.PaneShown e -> new PaneState(e.widget());
        };
    }
}
