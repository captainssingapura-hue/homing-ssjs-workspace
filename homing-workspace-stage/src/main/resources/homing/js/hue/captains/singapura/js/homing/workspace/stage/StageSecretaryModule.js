// =============================================================================
// StageSecretary — the secretary of a page's stage party: one stage, one
// widget on it at a time. A member asks for a widget to be shown (Present) or
// for what is shown to be brought back (Dismiss), and the steward is sent it;
// what the steward says - Shown, Returned, Refused - every member hears. A
// Present while a widget is on the stage, or on its way, is a bug: the stage is
// modal, and nothing behind it can be pressed. It is refused to the asker at
// once, and kept for whoever looks. A Present the steward cannot be reached for
// is refused too. Diligent: its state says what is on the stage, what is on its
// way, and the bugs it has seen.
//
//   state  { shown: { widget, title } | null, asked: widget | null, bugs: [{ widget, why }] - the last few }
//
//   Present { widget }        from a member: to the steward, and on its way - unless the stage is taken
//   Dismiss {}                from a member: to the steward, when something is shown
//   Shown { widget, title }   from the steward: shown; every member told
//   Returned { widget }       from the steward: the stage empty; every member told
//   Refused { widget, why }   from the steward: nothing on its way; every member told
//   anything else             nothing: the others are the steward's words, and the steward's words a member's
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var StageSecretary = {

    initial: { shown: null, asked: null, bugs: [] },

    /** How many bugs are kept. */
    BUGS_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message, fromSteward = envelope.name === "steward";
        if (envelope.from === "unrouted") return StageSecretary._unrouted(state, m);
        if (!fromSteward && m.kind === "Present") return StageSecretary._present(state, envelope.from, m);
        if (!fromSteward && m.kind === "Dismiss") return { newState: state, actions: state.shown ? [{ kind: "SendToSteward", message: m }] : [] };
        if (fromSteward && m.kind === "Shown") return StageSecretary._told(state, { shown: { widget: m.widget, title: m.title }, asked: null }, m);
        if (fromSteward && m.kind === "Returned") return StageSecretary._told(state, { shown: null }, m);
        if (fromSteward && m.kind === "Refused") return StageSecretary._told(state, { asked: null }, m);
        return { newState: state, actions: [] };
    },

    /** A widget asked for: on its way to the steward - or, the stage taken, refused to the asker and kept as the bug it is. */
    _present: function (state, from, m) {
        var taken = state.shown ? state.shown.widget : state.asked;
        if (taken === null) return { newState: StageSecretary._with(state, { asked: m.widget }), actions: [{ kind: "SendToSteward", message: m }] };
        var why = "the stage is taken by " + taken + ": one widget at a time, and nothing behind the stage can ask";
        var bugs = state.bugs.concat([{ widget: m.widget, why: why }]).slice(-StageSecretary.BUGS_KEPT);
        return { newState: StageSecretary._with(state, { bugs: bugs }), actions: [{ kind: "SendToMember", to: from, message: { kind: "Refused", widget: m.widget, why: why } }] };
    },

    /** What the steward said: kept, and every member told. */
    _told: function (state, changes, m) {
        return { newState: StageSecretary._with(state, changes), actions: [{ kind: "BroadcastToMembers", message: m }] };
    },

    /** A send to the steward that went nowhere: a Present refused - nothing will move it. */
    _unrouted: function (state, m) {
        if (m.kind !== "Present") return { newState: state, actions: [] };
        var refused = { kind: "Refused", widget: m.widget, why: "no steward: the host hired none for its stage" };
        return { newState: StageSecretary._with(state, { asked: null }), actions: [{ kind: "BroadcastToMembers", message: refused }] };
    },

    _with: function (state, changes) { return Object.assign({}, state, changes); }
};
