package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.PaneEvent;
import hue.captains.singapura.js.homing.workspace.log.PaneState;
import hue.captains.singapura.js.homing.workspace.log.RosterEvent;
import hue.captains.singapura.js.homing.workspace.log.RosterState;

/**
 * The one pane's layer of the fold: what it shows, over the roster. It shows a
 * widget the roster holds, or nothing; one the roster does not hold is refused.
 * And it follows the roster: a widget closed is shown nowhere - so a pane that
 * showed it shows nothing. A live pane says what it shows next before the
 * widget it showed is closed, so this is what a pane that was not showing when
 * the widget closed - the workspace laid out by another placement, on another
 * display - comes back to. The JavaScript is this, line for line
 * (PaneFoldModule.js), and the two agree to the byte.
 */
public final class PaneFold {

    private PaneFold() {}

    /** One of the pane's events on its state, over the roster: the state after it. */
    public static PaneState apply(PaneState state, PaneEvent event, RosterState roster) {
        return switch (event) {
            case PaneEvent.PaneShown e -> {
                e.widget().ifPresent(w -> {
                    if (!roster.holds(w)) throw new WorkspaceFold.Refused("the roster does not hold " + w);
                });
                yield new PaneState(e.widget());
            }
        };
    }

    /** The pane after one of the roster's events: a widget closed is shown nowhere. */
    public static PaneState follow(PaneState state, RosterEvent event) {
        if (event instanceof RosterEvent.WidgetClosed c && state.shown().map(c.id()::equals).orElse(false)) return PaneState.empty();
        return state;
    }
}
