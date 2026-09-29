// =============================================================================
// WorkspaceChoiceSecretary — the secretary of a workspace choice party: which
// kind of workspace is chosen, said to every member when it changes and to a
// member that asks; and each workspace asked to open, said to every member —
// for whoever opens workspaces, a page, to hear. Browsing and opening are
// different acts: a kind chosen opens nothing. Diligent (Diligent
// Secretaries): its state answers an operator's questions, and every kind it
// handles is tested.
//
//   state  { chosen: a kind | null, lastChangedBy: a member's id | null, changes: n,
//            opened: n, lastOpen: { workspaceKind, workspaceId, by } | null,
//            made: n, lastNew: { workspaceKind, workspaceName, newTab, by } | null,
//            recentUnknown: [{ kind, from }] — the last few }
//
//   Choose { workspaceKind }         chosen := that kind, unless it is the one chosen;
//                                    Chosen to every member. The same kind again is nothing —
//                                    so a member that shows what it hears and tells what it
//                                    shows does not echo
//   CurrentRequested                 Chosen, to the member that asked, alone — when one is
//   Open { workspaceKind, workspaceId }   Opening, as it was asked, to every member; counted,
//                                    and who asked
//   OpenNew { workspaceKind, workspaceName, newTab }   OpeningNew, as it was asked, to every
//                                    member - for whoever opens workspaces to make it; counted,
//                                    and who asked
//   anything else                    kept in recentUnknown, nothing done: Chosen, Opening and
//                                    OpeningNew are the party's own words, never a member's
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var WorkspaceChoiceSecretary = {

    initial: { chosen: null, lastChangedBy: null, changes: 0, opened: 0, lastOpen: null, made: 0, lastNew: null, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    behavior: function (state, envelope) {
        var m = envelope.message;
        switch (m.kind) {
            case "Choose":
                if (state.chosen === m.workspaceKind) return { newState: state, actions: [] };
                return { newState: WorkspaceChoiceSecretary.with(state, { chosen: m.workspaceKind, lastChangedBy: envelope.from, changes: state.changes + 1 }),
                         actions: [{ kind: "BroadcastToMembers", message: { kind: "Chosen", workspaceKind: m.workspaceKind } }] };

            case "CurrentRequested":
                return { newState: state, actions: state.chosen === null ? []
                         : [{ kind: "SendToMember", to: envelope.from, message: { kind: "Chosen", workspaceKind: state.chosen } }] };

            case "Open":
                return { newState: WorkspaceChoiceSecretary.with(state, { opened: state.opened + 1,
                                                                         lastOpen: { workspaceKind: m.workspaceKind, workspaceId: m.workspaceId, by: envelope.from } }),
                         actions: [{ kind: "BroadcastToMembers", message: { kind: "Opening", workspaceKind: m.workspaceKind, workspaceId: m.workspaceId } }] };

            case "OpenNew":
                return { newState: WorkspaceChoiceSecretary.with(state, { made: state.made + 1,
                                                                         lastNew: { workspaceKind: m.workspaceKind, workspaceName: m.workspaceName, newTab: m.newTab, by: envelope.from } }),
                         actions: [{ kind: "BroadcastToMembers", message: { kind: "OpeningNew", workspaceKind: m.workspaceKind, workspaceName: m.workspaceName, newTab: m.newTab } }] };

            default: {
                var unknown = state.recentUnknown.concat([{ kind: m.kind, from: envelope.from }]).slice(-WorkspaceChoiceSecretary.UNKNOWN_KEPT);
                return { newState: WorkspaceChoiceSecretary.with(state, { recentUnknown: unknown }), actions: [] };
            }
        }
    },

    /** The state with some of it changed: a new object, the old one untouched. */
    with: function (state, changes) { return Object.assign({}, state, changes); }
};
