// =============================================================================
// WorkspaceSwitcherSecretary — the secretary of a switcher's own workspace
// choice party: the scope where its kinds and its instances meet. Within the
// scope it is the workspace choice secretary (WorkspaceChoiceSecretary), whose
// state it keeps whole; what it adds is the scope's edge — what goes up to the
// party the switcher joined, and what is taken from it.
//
// What goes up is declared, kind by kind (BUBBLES): a kind chosen in the scope
// goes up — Choose — when it changed what is chosen; a workspace asked to open
// goes up — Open — always, since opening is not the scope's to do, and so does a
// new one asked for — OpenNew; a question
// never does, answered here (CurrentRequested). What comes down from above:
// Chosen is taken as the scope's own choice, told to every member of the
// scope, never sent back up; Opening and OpeningNew are heard — the scope said its own
// already — and counted, nothing done. Anything else from above is kept as
// unknown. Diligent (Diligent Secretaries): how often it sent up, kept to
// itself, took from above and heard, beside the scope's choice.
//
//   state  { choice: WorkspaceChoiceSecretary's state, bubbled: n, kept: n, adopted: n, heard: n }
//
//   from a member   as WorkspaceChoiceSecretary; and, when BUBBLES says so - and, for a
//                   Choose, the choice changed - the message sent up as it was: SendToParent
//   from upstream   Chosen → as a Choose from "upstream"; told to the scope when it
//                   changed, never sent up
//                   Opening, OpeningNew → heard: counted, nothing done
//                   anything else → recentUnknown, nothing done
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var WorkspaceSwitcherSecretary = {

    initial: { choice: WorkspaceChoiceSecretary.initial, bubbled: 0, kept: 0, adopted: 0, heard: 0 },

    /** What a member's word does at the scope's edge: up, or kept here. */
    BUBBLES: Object.freeze({ Choose: true, Open: true, OpenNew: true, CurrentRequested: false }),

    behavior: function (state, envelope) {
        var m = envelope.message;
        if (envelope.from === "upstream") return WorkspaceSwitcherSecretary.fromAbove(state, envelope);
        var step = WorkspaceChoiceSecretary.behavior(state.choice, envelope);
        var up = WorkspaceSwitcherSecretary.BUBBLES[m.kind] === true
                 && (m.kind !== "Choose" || step.newState.changes !== state.choice.changes);
        return {
            newState: WorkspaceSwitcherSecretary.with(state, { choice: step.newState, bubbled: state.bubbled + (up ? 1 : 0), kept: state.kept + (up ? 0 : 1) }),
            actions: up ? step.actions.concat([{ kind: "SendToParent", message: m }]) : step.actions
        };
    },

    /** What the party above says: a kind chosen is taken as the scope's, told to it, never sent back up; an opening is heard. */
    fromAbove: function (state, envelope) {
        var m = envelope.message;
        if (m.kind === "Opening" || m.kind === "OpeningNew") return { newState: WorkspaceSwitcherSecretary.with(state, { heard: state.heard + 1 }), actions: [] };
        if (m.kind !== "Chosen") {
            var c = state.choice, unknown = c.recentUnknown.concat([{ kind: m.kind, from: "upstream" }]).slice(-WorkspaceChoiceSecretary.UNKNOWN_KEPT);
            return { newState: WorkspaceSwitcherSecretary.with(state, { choice: WorkspaceChoiceSecretary.with(c, { recentUnknown: unknown }) }), actions: [] };
        }
        var step = WorkspaceChoiceSecretary.behavior(state.choice, { from: "upstream", message: { kind: "Choose", workspaceKind: m.workspaceKind } });
        var took = step.newState.changes !== state.choice.changes;
        return { newState: WorkspaceSwitcherSecretary.with(state, { choice: step.newState, adopted: state.adopted + (took ? 1 : 0) }),
                 actions: step.actions };
    },

    /** The state with some of it changed: a new object, the old one untouched. */
    with: function (state, changes) { return Object.assign({}, state, changes); }
};
