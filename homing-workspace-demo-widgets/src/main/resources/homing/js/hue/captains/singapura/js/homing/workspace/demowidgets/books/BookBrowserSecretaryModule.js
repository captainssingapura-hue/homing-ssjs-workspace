// =============================================================================
// BookBrowserSecretary — the secretary of a book browser's own book selection
// party: the scope where its grid and its jumbotron meet. Within the scope it
// is the book selection secretary (BookSelectionSecretary), whose state it
// keeps whole; what it adds is the scope's edge — what goes up to the party
// the browser joined, and what is taken from it.
//
// What goes up is declared, kind by kind (BUBBLES): a choice made in the
// scope goes up — Select, Clear — when it changed what is chosen; a question
// never does, answered here (CurrentRequested). What comes down from above is
// taken as the scope's own choice: Selected, Cleared — told to every member of
// the scope, never sent back up. Anything else from above is kept as unknown.
// Diligent (Diligent Secretaries): how often it sent up, kept to itself, and
// took from above, beside the scope's selection.
//
//   state  { selection: BookSelectionSecretary's state, bubbled: n, kept: n, adopted: n }
//
//   from a member   as BookSelectionSecretary; and, when BUBBLES says so and the
//                   choice changed, the message sent up as it was: SendToParent
//   from upstream   Selected → as a Select from "upstream"; Cleared → as a Clear;
//                   told to the scope when it changed, never sent up
//                   anything else → recentUnknown, nothing done
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var BookBrowserSecretary = {

    initial: { selection: BookSelectionSecretary.initial, bubbled: 0, kept: 0, adopted: 0 },

    /** What a member's word does at the scope's edge: up, or kept here. */
    BUBBLES: Object.freeze({ Select: true, Clear: true, CurrentRequested: false }),

    behavior: function (state, envelope) {
        var m = envelope.message;
        if (envelope.from === "upstream") return BookBrowserSecretary.fromAbove(state, envelope);
        var step = BookSelectionSecretary.behavior(state.selection, envelope);
        var up = BookBrowserSecretary.BUBBLES[m.kind] === true && step.newState.changes !== state.selection.changes;
        return {
            newState: { selection: step.newState, bubbled: state.bubbled + (up ? 1 : 0), kept: state.kept + (up ? 0 : 1), adopted: state.adopted },
            actions: up ? step.actions.concat([{ kind: "SendToParent", message: m }]) : step.actions
        };
    },

    /** What the party above says: taken as the scope's choice when it is one, told to the scope, never sent back up. */
    fromAbove: function (state, envelope) {
        var m = envelope.message, as = m.kind === "Selected" ? { kind: "Select", id: m.id, title: m.title, author: m.author }
                                     : m.kind === "Cleared" ? { kind: "Clear" } : null;
        if (!as) {
            var s = state.selection, unknown = s.recentUnknown.concat([{ kind: m.kind, from: "upstream" }]).slice(-BookSelectionSecretary.UNKNOWN_KEPT);
            return { newState: { selection: { selected: s.selected, lastChangedBy: s.lastChangedBy, changes: s.changes, recentUnknown: unknown },
                                 bubbled: state.bubbled, kept: state.kept, adopted: state.adopted }, actions: [] };
        }
        var step = BookSelectionSecretary.behavior(state.selection, { from: "upstream", message: as });
        var took = step.newState.changes !== state.selection.changes;
        return { newState: { selection: step.newState, bubbled: state.bubbled, kept: state.kept, adopted: state.adopted + (took ? 1 : 0) },
                 actions: step.actions };
    }
};
