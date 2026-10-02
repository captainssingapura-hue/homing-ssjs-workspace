// =============================================================================
// ContentBenchApp — content parties, on the bench. The page holds the root
// parties - a note party and a flow party, each with the content secretary and
// the steward it hires - and two panels, each a scope of its own: content
// parties of both types, with no steward, linked under the page's. Every card
// asks for its note with its params; every flow asks for its parts, and its
// parts - cards - ask in turn, in the flow's panel. The panels ask for some
// of the same notes, and flows in both ask for the same parts: each item is
// fetched once, by the one steward of its type's hierarchy, and every asker is
// answered at every level. One note the bench does not have: it fails once,
// and every card that asks is told it is unavailable.
//
// The traffic under them says what passes - who asks whom for what, what goes
// up a link, what comes down - and what each steward fetched, and how often
// the server was asked for it.
// =============================================================================

const _contentBenchOwner = Object.freeze({ toString: () => "contentBench" });

/** The panels: what each shows, in order. */
var _CONTENT_PANELS = Object.freeze([
    { title: "Scope A", parts: [
        { type: "note-card", params: { key: "alpha" } },
        { type: "note-card", params: { key: "beta" } },
        { type: "flow", params: { key: "pair" } },
        { type: "note-card", params: { key: "missing" } }] },
    { title: "Scope B", parts: [
        { type: "note-card", params: { key: "alpha" } },
        { type: "note-card", params: { key: "gamma" } },
        { type: "flow", params: { key: "trio" } },
        { type: "flow", params: { key: "pair" } },
        { type: "note-card", params: { key: "missing" } }] }
]);

function appMain(el, params) {
    css.addClass(el, cb_page);
    var place = domOpsParty.createBranch("contentBench");
    place.activate(_contentBenchOwner);
    var intro = place.createElement("intro", "p");
    css.addClass(intro, cb_intro);
    intro.textContent = "Two scopes, each with content parties of its own linked under the page's. Every card asks for its note, and every flow "
        + "for its parts, with the params it was made with. The page's parties hire one steward each; each item is fetched once, and every "
        + "asker is answered where it asked.";
    var panels = place.createElement("panels", "div");
    css.addClass(panels, cb_panels);
    var trafficBox = place.createElement("traffic", "div");
    el.appendChild(intro);
    el.appendChild(panels);
    el.appendChild(trafficBox);

    var note = new MessagingParty(BENCH_NOTE, ContentSecretary, BenchNoteSteward);
    var flow = new MessagingParty(FLOW, ContentSecretary, BenchFlowSteward);
    var given = {};
    given[BENCH_NOTE.name] = note;
    given[FLOW.name] = flow;
    var kinds = { "note-card": NoteCard, "flow": Flow };

    var made = _CONTENT_PANELS.map(function (spec, i) {
        var box = place.createElement("panel" + i, "div");
        panels.appendChild(box);
        var panel = new ContentPanel(box, { title: spec.title, types: [BENCH_NOTE, FLOW], kinds: kinds, parts: spec.parts });
        place.graft("panel" + i, panel.roots.dom);
        focusParty.root.graft("panel" + i, panel.roots.focus);
        return panel;
    });

    // the traffic listens before anyone asks, so it hears everything
    var watched = [{ label: "page note", party: note }, { label: "page flow", party: flow }];
    made.forEach(function (panel, i) {
        var tag = String.fromCharCode(65 + i);
        watched.push({ label: tag + " note", party: panel.scope(BENCH_NOTE.name) }, { label: tag + " flow", party: panel.scope(FLOW.name) });
    });
    var traffic = new PartyTraffic(trafficBox, { parties: watched });
    place.graft("traffic", traffic.roots.dom);

    made.forEach(function (panel) { panel.join(given); });
}
