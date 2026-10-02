// =============================================================================
// BenchSecretary — the bench's manual secretary: at the root of every party a
// widget on the bench joins, it decides nothing. What a member tells it is
// heard and kept, and nothing is done; what goes down is sent by a hand, from
// the party's simulator (PartySimulator). So a widget is tried against any
// scenario a person can type, and every word it says is seen.
//
//   state  { heard: n, lastFrom: a member's id | null, recent: [{ kind, from }] — the last few }
//   any    heard, kept; no action
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var BenchSecretary = {

    initial: { heard: 0, lastFrom: null, recent: [] },

    /** How many of what it heard are kept. */
    KEPT: 10,

    behavior: function (state, envelope) {
        var recent = state.recent.concat([{ kind: envelope.message.kind, from: envelope.from }]).slice(-BenchSecretary.KEPT);
        return { newState: { heard: state.heard + 1, lastFrom: envelope.from, recent: recent }, actions: [] };
    }
};
