// =============================================================================
// WorkspaceBenchApp — a workspace of one pane, on the bench, beside the page
// of one widget (which stays as it is). The workspace (SinglePaneWorkspace) is
// self-contained: made first, its own parties strays, and mounted last — its
// roots grafted into the page's, at the bench's place in the page's DomOps
// party and at the page's focus root, where the steward reaches them.
//
// It can open a widget of any kind the bench knows but its nasty ones, each
// under an id that says what it is; the roster's dropdown shows one at a
// time. It opens a books grid and a book jumbotron to begin with, the grid
// shown: choose a book in it, then show the jumbotron — it heard the choice
// while nothing showed it.
//
// The bench's tools float over the page: the monitors, and a simulator of
// each root party the workspace makes — the core's own, its secretary real.
// =============================================================================

const _workspaceBenchOwner = Object.freeze({ toString: () => "workspaceBench" });

function appMain(el, params) {
    css.addClass(el, wb_page);
    // the page's own: its place, and its tools
    var place = domOpsParty.createBranch("workspaceBench");
    place.activate(_workspaceBenchOwner);
    var tools = new BenchMonitors(place.createBranch("tools"), { host: el, monitors: BENCH_MONITORS });
    // what can be opened: every kind the bench knows but the nasty ones, which are the bench's to catch
    var kinds = {};
    Object.keys(BENCH_WIDGETS).forEach(function (k) { if (k.indexOf("nasty-") !== 0) kinds[k] = { Widget: BENCH_WIDGETS[k] }; });
    // THE WORKSPACE: made first, its roots strays
    var ws = new SinglePaneWorkspace(el, { kinds: kinds, secretaries: { "book-selection": BookSelectionSecretary } });
    // a simulator of each root party, as the workspace makes it
    ws.parties.on(function (n) {
        var i = ws.parties.names().length - 1;
        tools.add({ kind: "party-" + n.name, title: "Party " + n.name, mark: n.name.charAt(0).toUpperCase(),
                    rect: { x: 24 + i * 28, y: Math.max(56, el.clientHeight - 340 - i * 28), w: 560, h: 300 },
                    make: function (branch, tab) { return new PartySimulator(branch, tab, n.party); } }, false);
    });
    ws.open("books-grid");
    ws.open("book-jumbotron");
    ws.show("books-grid-1");
    // MOUNTED LAST: its roots grafted into the page's
    place.graft("workspace", ws.roots.dom);
    focusParty.root.graft("workspace", ws.roots.focus);
}
