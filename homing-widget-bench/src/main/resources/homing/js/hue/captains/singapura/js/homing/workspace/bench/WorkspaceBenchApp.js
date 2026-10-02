// =============================================================================
// WorkspaceBenchApp — a workspace of one pane, on the bench, beside the page
// of one widget (which stays as it is). The workspace (SinglePaneWorkspace) is
// self-contained: made first, its own parties strays, and mounted last — its
// roots grafted into the page's, at the bench's place in the page's DomOps
// party and at the page's focus root, where the steward reaches them. What it
// is — its kinds, its root parties — is its declaration's, in Java
// (BenchWorkspace), as BENCH_WORKSPACE.
//
// It is LOGGED: one log, in IndexedDB, under the workspace's name, of every
// layer it has — its roster and its pane. The page comes back to what the log
// folds to (WorkspaceLoad): the roster first, every widget made again under
// its id, then the widget the pane showed; and records from then on. One page
// writes the log at a time — the one holding its lock; another reads it only.
// A log that will not fold is set aside, and the page starts afresh.
//
// Afresh, it asks for a books grid, shown, and a book jumbotron behind it: choose a
// book in it, then show the jumbotron — it heard the choice while nothing
// showed it.
//
// The bench's tools float over the page: the monitors, and a simulator of
// each root party the workspace hosts — its secretary real.
// =============================================================================

const _workspaceBenchOwner = Object.freeze({ toString: () => "workspaceBench" });

function appMain(el, params) {
    // the page's directory of workspace groups: the bench's, provided before anything reads it
    WorkspaceDirectory.provide(BENCH_GROUPS);
    css.addClass(el, wb_page);
    // the page's own: its place, and its tools
    var place = domOpsParty.createBranch("workspaceBench");
    place.activate(_workspaceBenchOwner);
    var tools = new BenchMonitors(place.createBranch("tools"), { host: el, monitors: BENCH_MONITORS });
    // THE LOG: the workspace's kind is its name; one page writes it at a time
    var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(BENCH_WORKSPACE.name, null), backend: new IndexedDbLog() });
    var lock = new WorkspaceWriteLock({ log: new LogKey(log.header.kind, log.header.workspaceId), onChange: taken });
    var stop = null;
    function taken(writeLock) {
        if (stop && !WorkspaceWriteLock.writes(writeLock)) { stop(); stop = null; console.warn("[workspaceBench] another page writes the log now: nothing more is recorded here"); }
    }
    lock.acquire().then(function (held) {
        var writes = WorkspaceWriteLock.writes(held);
        return WorkspaceLoad.load(log, { writes: writes }).then(function (r) {
            log = r.log;
            // THE WORKSPACE: made first, its roots strays, its root parties made with it
            var ws = new SinglePaneWorkspace(el, { manifest: BENCH_WORKSPACE });
            ws.parties.names().forEach(function (name, i) {
                tools.add({ kind: "party-" + name, title: "Party " + name, mark: name.charAt(0).toUpperCase(),
                            rect: { x: 24 + i * 28, y: Math.max(56, el.clientHeight - 340 - i * 28), w: 560, h: 300 },
                            make: function (branch, tab) { return new PartySimulator(branch, tab, ws.parties.party(name)); } }, false);
            });
            // COMING BACK, not recorded; then recorded, when this page writes the log
            if (r.folded) ws.restore(r.folded.state);
            if (writes) stop = ws.record(log);
            if (writes && r.logged === 0) {   // afresh: what a user would ask, asked
                ws.open("books-grid", {}, "shown");
                ws.open("book-jumbotron", {}, "behind");
            }
            // MOUNTED LAST: its roots grafted into the page's
            place.graft("workspace", ws.roots.dom);
            focusParty.root.graft("workspace", ws.roots.focus);
        });
    }).then(null, function (e) { console.error("[workspaceBench] the workspace could not be made: " + (e && e.message)); });
}
