// =============================================================================
// PaneFold — the one pane's layer of the fold: what it shows, over the roster.
// It shows a widget the roster holds, or nothing; one the roster does not hold
// is refused. And it follows the roster: a widget closed is shown nowhere — so
// a pane that showed it shows nothing. A live pane says what it shows next
// before the widget it showed is closed; this is what a pane that was not
// showing then — the workspace laid out by another placement, on another
// display — comes back to. Java's PaneFold, transcribed: the two agree to the
// byte.
//
//   PaneFold.empty()                   → the PaneState every log starts from
//   PaneFold.apply(state, e, roster)   → the PaneState after a pane event, over the RosterState
//   PaneFold.follow(state, e)          → the PaneState after a roster event
// =============================================================================

function _paneNo(why) { throw new Error("[PaneFold] " + why); }

class PaneFold {
    static empty() { return new PaneState(null); }

    static apply(state, e, roster) {
        if (!(e instanceof PaneShown)) _paneNo("not the pane's: " + (e && e.constructor ? e.constructor.name : JSON.stringify(e)));
        if (e.widget !== null && !RosterFold.holds(roster, e.widget)) _paneNo("the roster does not hold " + e.widget.value);
        return new PaneState(e.widget);
    }

    static follow(state, e) {
        if (e instanceof WidgetClosed && state.shown !== null && state.shown.value === e.id.value) return PaneFold.empty();
        return state;
    }
}
