// =============================================================================
// ContentSecretary — the secretary of every content party, at every level: a
// page's root party and every scope linked below it alike. It never knows
// which it is: it sends to "the steward", and the party's runtime takes the
// send up through the link, or to the steward the party hired.
//
// It keeps what was loaded and answers a repeated want from it; it keeps who
// waits for what, and asks the steward once for many askers. An asker is a
// widget, which said Wanted and is answered Content or Unavailable; or a scope
// linked below, which said Fetch and is answered Loaded or Failed - to it, this
// party is its steward. A send to the steward that went nowhere comes back
// from "unrouted", and its askers are told Unavailable: no steward. Diligent
// (Diligent Secretaries): its state answers an operator's questions - what is
// held, what is pending and for whom, what failed and why, how many items it
// asked for - and every kind it handles is tested.
//
//   state  { held: { [key]: { params, content } }, failed: { [key]: { params, why } },
//            pending: { [key]: { params, askers: [{ to, as }] } }, asked: n,
//            recentUnknown: [{ kind, from }] - the last few }   key: ContentParams.key
//
//   Wanted { params }    from a widget: Content, if held; Unavailable, if it failed; else it
//                        waits, and the steward is sent a Fetch - unless the item is pending
//   Fetch { items }      from a scope below: each item as a Wanted is, answered Loaded or
//                        Failed; the items not held nor pending sent up in one Fetch.
//                        From "unrouted": each item's askers told there is no steward
//   Loaded { params, content }   held; every asker answered; any failure forgotten.
//                        Loaded for an item no one asked is kept: a steward may load ahead
//   Failed { params, why }       kept; every asker told
//   anything else        kept in recentUnknown: Content and Unavailable are the party's words
//
// Pure: no DOM, no clock, no console; the state handed in is never changed.
// =============================================================================

var ContentSecretary = {

    initial: { held: {}, failed: {}, pending: {}, asked: 0, recentUnknown: [] },

    /** How many unknown messages are kept. */
    UNKNOWN_KEPT: 5,

    /** Why a send to the steward came back. */
    NO_STEWARD: "no steward: nothing above to ask, and none hired",

    behavior: function (state, envelope) {
        var m = envelope.message, from = envelope.from;
        if (m.kind === "Wanted") return ContentSecretary._wanted(state, from, [{ params: m.params }], "Content");
        if (m.kind === "Fetch" && from === "unrouted") return ContentSecretary._unrouted(state, m.items);
        if (m.kind === "Fetch" && from !== "upstream") return ContentSecretary._wanted(state, from, m.items, "Loaded");
        if (m.kind === "Loaded") return ContentSecretary._settled(state, m.params, true, m.content);
        if (m.kind === "Failed") return ContentSecretary._settled(state, m.params, false, m.why);
        var unknown = state.recentUnknown.concat([{ kind: m.kind, from: from }]).slice(-ContentSecretary.UNKNOWN_KEPT);
        return { newState: ContentSecretary._with(state, { recentUnknown: unknown }), actions: [] };
    },

    /** Items wanted by one asker - a widget's one, a scope's several: each answered now, or waited for; the new ones fetched in one. */
    _wanted: function (state, from, items, as) {
        var pending = Object.assign({}, state.pending), actions = [], fetch = [];
        items.forEach(function (item) {
            var key = ContentParams.key(item.params), held = state.held[key], failed = state.failed[key], waiting = pending[key];
            if (held) actions.push(ContentSecretary._answer(from, as, held.params, true, held.content));
            else if (failed) actions.push(ContentSecretary._answer(from, as, failed.params, false, failed.why));
            else if (waiting) pending[key] = { params: waiting.params, askers: waiting.askers.concat([{ to: from, as: as }]) };
            else { pending[key] = { params: item.params, askers: [{ to: from, as: as }] }; fetch.push({ params: item.params }); }
        });
        if (fetch.length) actions.push({ kind: "SendToSteward", message: { kind: "Fetch", items: fetch } });
        return { newState: ContentSecretary._with(state, { pending: pending, asked: state.asked + fetch.length }), actions: actions };
    },

    /** An item fetched, or failed: kept, and every asker answered as it asked. */
    _settled: function (state, params, ok, value) {
        var key = ContentParams.key(params), waiting = state.pending[key];
        var pending = Object.assign({}, state.pending), held = Object.assign({}, state.held), failed = Object.assign({}, state.failed);
        delete pending[key];
        if (ok) { held[key] = { params: params, content: value }; delete failed[key]; }
        else failed[key] = { params: params, why: value };
        var actions = waiting ? waiting.askers.map(function (a) { return ContentSecretary._answer(a.to, a.as, params, ok, value); }) : [];
        return { newState: ContentSecretary._with(state, { held: held, failed: failed, pending: pending }), actions: actions };
    },

    /** A send to the steward that went nowhere: its items' askers told there is none. Not kept as failed - a steward may come. */
    _unrouted: function (state, items) {
        var pending = Object.assign({}, state.pending), actions = [];
        items.forEach(function (item) {
            var key = ContentParams.key(item.params), waiting = pending[key];
            if (!waiting) return;
            delete pending[key];
            waiting.askers.forEach(function (a) { actions.push(ContentSecretary._answer(a.to, a.as, waiting.params, false, ContentSecretary.NO_STEWARD)); });
        });
        return { newState: ContentSecretary._with(state, { pending: pending }), actions: actions };
    },

    /** The answer to one asker: a widget is told Content or Unavailable; a scope below, Loaded or Failed. */
    _answer: function (to, as, params, ok, value) {
        var message = as === "Content"
            ? (ok ? { kind: "Content", params: params, content: value } : { kind: "Unavailable", params: params, why: value })
            : (ok ? { kind: "Loaded", params: params, content: value } : { kind: "Failed", params: params, why: value });
        return { kind: "SendToMember", to: to, message: message };
    },

    _with: function (state, changes) { return Object.assign({}, state, changes); }
};
