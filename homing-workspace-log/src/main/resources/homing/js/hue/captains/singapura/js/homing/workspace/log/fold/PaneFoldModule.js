// =============================================================================
// PaneFold — the one pane's layer of the fold: what it shows, a widget or
// nothing. It folds its own events and no other: a placement is handed widgets
// to show and never asks the roster whether they are, and while it places it
// records every consequence of the roster itself — a widget it shows, closed,
// is preceded by the pane's own PaneShown of the next (RFC 0066 E3, the
// workspace detour: placement engines, each folding only its own). Java's
// PaneFold, transcribed: the two agree to the byte.
//
//   PaneFold.empty()           → the PaneState every log starts from
//   PaneFold.apply(state, e)   → the PaneState after a pane event
// =============================================================================

function _paneNo(why) { throw new Error("[PaneFold] " + why); }

class PaneFold {
    static empty() { return new PaneState(null); }

    static apply(state, e) {
        if (!(e instanceof PaneShown)) _paneNo("not the pane's: " + (e && e.constructor ? e.constructor.name : JSON.stringify(e)));
        return new PaneState(e.widget);
    }
}
