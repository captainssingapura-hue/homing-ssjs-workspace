// =============================================================================
// BookSelectionSecretary — the secretary of a book selection party: which
// book is chosen, said to every member when it changes, and to a member that
// asks. Diligent (Diligent Secretaries): its state answers an operator's
// questions — what is chosen, who chose it last, how often it changed, what
// came that it does not handle — and every kind it handles is tested.
//
//   state  { selected: { id, title, author } | null, lastChangedBy: a member's id | null,
//            changes: n, recentUnknown: [{ kind, from }] — the last few }
//
//   Select { id, title, author }  selected := that book, unless it is the one chosen, as
//                                  it is; Selected to every member. The same book again is
//                                  nothing — so a member that shows what it hears and tells
//                                  what it shows does not echo for ever
//   Clear                          selected := null, if a book was chosen; Cleared to every member
//   CurrentRequested               Selected, or Cleared, to the member that asked, alone
//   anything else                  kept in recentUnknown, nothing done: Selected and Cleared
//                                  are the party's own words, never a member's
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var BookSelectionSecretary = {

    initial: { selected: null, lastChangedBy: null, changes: 0, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message, s = state.selected;
        switch (m.kind) {

            case "Select": {
                if (s && s.id === m.id && s.title === m.title && s.author === m.author) return { newState: state, actions: [] };
                var book = { id: m.id, title: m.title, author: m.author };
                return { newState: BookSelectionSecretary.changed(state, book, envelope.from),
                         actions: [{ kind: "BroadcastToMembers", message: BookSelectionSecretary.said(book) }] };
            }

            case "Clear": {
                if (!s) return { newState: state, actions: [] };
                return { newState: BookSelectionSecretary.changed(state, null, envelope.from),
                         actions: [{ kind: "BroadcastToMembers", message: BookSelectionSecretary.said(null) }] };
            }

            case "CurrentRequested":
                return { newState: state, actions: [{ kind: "SendToMember", to: envelope.from, message: BookSelectionSecretary.said(s) }] };

            default: {
                var unknown = state.recentUnknown.concat([{ kind: m.kind, from: envelope.from }]).slice(-BookSelectionSecretary.UNKNOWN_KEPT);
                return { newState: { selected: s, lastChangedBy: state.lastChangedBy, changes: state.changes, recentUnknown: unknown }, actions: [] };
            }
        }
    },

    /** The state after a change: the book, who changed it, one more change. */
    changed: function (state, book, by) {
        return { selected: book, lastChangedBy: by, changes: state.changes + 1, recentUnknown: state.recentUnknown };
    },

    /** What the party says of a book, or of none. */
    said: function (book) {
        return book ? { kind: "Selected", id: book.id, title: book.title, author: book.author } : { kind: "Cleared" };
    }
};
