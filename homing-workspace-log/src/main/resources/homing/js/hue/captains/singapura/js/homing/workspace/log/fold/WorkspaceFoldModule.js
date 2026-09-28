// =============================================================================
// WorkspaceFold — a workspace log's meaning: its events folded, from the
// workspace's opening, into the WorkspaceState they leave — one state per
// layer. The log is one sequence; the layers are made here, in the processing:
// each event is handed, by its family, to its layer's fold — the roster's
// (RosterFold), the one pane's (PaneFold), the split grid's (GridFold) — which
// folds it into that layer's state and refuses it, naming it, when it cannot
// be where it falls. A placement stands on the roster: the pane's fold reads
// it, and follows it when a widget is closed. Java's WorkspaceFold,
// transcribed: the two agree to the byte.
//
//   WorkspaceFold.opening()              → the WorkspaceState every log starts from
//   WorkspaceFold.start(header)          → a FoldedState: the opening, through no event
//   WorkspaceFold.apply(state, event)    → the WorkspaceState after it
//   WorkspaceFold.fold(header, events)   → a FoldedState: through the last event
//   WorkspaceFold.foldFrom(from, events) → the same, folded on from a FoldedState - a
//                                          checkpoint's - each event after the last it
//                                          went through; the fold of a prefix, folded on,
//                                          is the fold of the whole
// =============================================================================

function _foldNo(why) { throw new Error("[WorkspaceFold] " + why); }

class WorkspaceFold {
    static opening() {
        return new WorkspaceState(RosterFold.empty(), PaneFold.empty(), GridFold.opening());
    }

    static start(header) {
        return new FoldedState(header, new EventSeq(0), WorkspaceFold.opening());
    }

    static fold(header, events) {
        return WorkspaceFold.foldFrom(WorkspaceFold.start(header), events);
    }

    static foldFrom(from, events) {
        var s = from.state, through = from.through.value;
        for (var i = 0; i < events.length; i++) {
            var seq = events[i].seq.value;
            if (seq <= through) _foldNo("seq " + seq + " is not after " + through + ", the last folded");
            try { s = WorkspaceFold.apply(s, events[i].event); }
            catch (e) { throw new Error("[WorkspaceFold] seq " + seq + ": " + events[i].event.constructor.name + " cannot be: " + e.message); }
            through = seq;
        }
        return new FoldedState(from.header, new EventSeq(through), s);
    }

    static apply(state, e) {
        if (e instanceof RosterEvent) {
            return new WorkspaceState(RosterFold.apply(state.roster, e), PaneFold.follow(state.pane, e), state.grid);
        }
        if (e instanceof PaneEvent) return new WorkspaceState(state.roster, PaneFold.apply(state.pane, e, state.roster), state.grid);
        if (e instanceof TabEvent || e instanceof RegionEvent || e instanceof FloatEvent) {
            return new WorkspaceState(state.roster, state.pane, GridFold.apply(state.grid, e));
        }
        _foldNo("not a WorkspaceEvent: " + JSON.stringify(e));
    }
}
