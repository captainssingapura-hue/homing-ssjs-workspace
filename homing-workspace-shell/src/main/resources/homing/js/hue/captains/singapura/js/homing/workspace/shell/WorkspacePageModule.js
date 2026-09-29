// =============================================================================
// WorkspacePage — a split-grid workspace as a page of a standard MPA, for any
// workspace declared in Java. The shell knows no widget: a widget set's own
// app hands its manifest in (WorkspaceManifest, from its WorkspaceDeclaration)
// with the slot and the params the MPA gave it, and the page is built here.
//
//   WorkspacePage.main(el, params, manifest, opts?)
//     el        the MPA's slot, which the workspace fills
//     params    the page's: ws_id, which workspace of the kind - its own unless
//               said; ws_server "on" when the server keeps its states; and the
//               keyboard and menus the chrome made for the document
//     manifest  the workspace's: its name is its log's kind
//     opts.fresh  ({ ws_id, ws_server }) → the app's own link to a workspace of the kind -
//               nav.<its app>(p) - offered on the log bar while another page writes this one
//     opts.attach  (ws, here) → detach? - the app's own, each time the workspace is built:
//               what it adds to it, a member of its root parties - here is { workspaceKind,
//               workspaceId }, the workspace the page shows; detach, called before the
//               workspace is disposed, undoes it (a take-over builds it again)
//
// The log is the workspace's: kept in IndexedDB under its kind and its id,
// typed, the workspace listed beside it under a name of its own the first time
// a page writes it. The page comes back to what the log folds to — from its
// latest checkpoint, folding only what came after — every layer at once, the
// roster first (GridWorkspace), and goes on logging, a checkpoint folded every
// so many events in a worker, posted to the server when the params say it
// keeps them. One page writes a log at a time: the one holding its write lock;
// another reads it only, until it takes the workspace over. A log that cannot
// be read or folded is set aside whole, said, and the page starts afresh
// (WorkspaceLoad); the bar exports what was set aside.
// =============================================================================

/** How many tabs the desk holds at once: the desk's budget. */
var _PAGE_BUDGET = 16;

class WorkspacePage {
    static main(el, params, manifest, opts) {
        var p = params || {};
        if (!manifest || !manifest.name || !manifest.kinds) throw new Error("[WorkspacePage] a workspace's manifest is required");
        css.addClass(el, mpa_main_full);
        var backend = new IndexedDbLog();
        var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(manifest.name, p.ws_id || null), backend: backend });
        var key = new LogKey(log.header.kind, log.header.workspaceId);
        var catalogue = new WorkspaceCatalogue({ backend: backend });
        var lock = new WorkspaceWriteLock({ log: key, onChange: lockSaid });
        var ws = null, detach = null;
        function writes() { return WorkspaceWriteLock.writes(lock.state); }
        // What the workspace is called: listed the first time a page writes it; a page that only reads it looks it up.
        function named() {
            var entry = writes() ? catalogue.opened(key)
                : catalogue.list(key.kind).then(function (all) { return all.filter(function (e) { return e.log.workspace.id === key.workspace.id; })[0] || null; });
            entry.then(function (e) { if (ws && ws.logBar) ws.logBar.named(e); },
                       function (e) { console.warn("[WorkspacePage] the workspace is not listed, so not named: " + (e && e.message)); });
        }
        // Taken by another page: nothing more is recorded here, and the bar says so.
        function lockSaid(writeLock) {
            if (!ws) return;
            if (!WorkspaceWriteLock.writes(writeLock)) ws.stopRecording();
            if (ws.logBar) ws.logBar.lock(writeLock, takeOver);
        }
        // Taken over: the workspace built again from the log, as the page that wrote it left it, and this page writes on.
        function takeOver() {
            lock.takeOver().then(function () {
                if (detach) { detach(); detach = null; }
                if (ws) ws.dispose();
                ws = null;
                load();
            });
        }
        function checkpointer() {
            if (typeof Worker === "undefined") return null;
            try {
                return new WorkspaceCheckpointer({ store: log, worker: new Worker(WORKSPACE_ADDRESSES.checkpointWorker, { type: "module" }),
                                                   upload: p.ws_server === "on" ? WORKSPACE_ADDRESSES.checkpoints : null });
            } catch (e) {
                console.warn("[WorkspacePage] no checkpoint worker, so no checkpoints this session: " + e.message);
                return null;
            }
        }
        function build(state, logged) {
            ws = new GridWorkspace(domOpsParty.createBranch("workspace"), { host: el, manifest: manifest, keyboard: p.keyboard, menus: p.menus,
                                                                            budget: _PAGE_BUDGET, log: log, state: state, logged: logged,
                                                                            readOnly: !writes(), checkpointer: writes() ? checkpointer() : null,
                                                                            server: p.ws_server === "on", fresh: opts && opts.fresh });
            if (ws.logBar) ws.logBar.lock(lock.state, takeOver);
            attached();
            named();
        }
        // The app's own, added to the workspace built: said, not thrown, when it fails - the workspace stands without it.
        function attached() {
            if (!opts || typeof opts.attach !== "function") return;
            try {
                var undo = opts.attach(ws, Object.freeze({ workspaceKind: key.kind.value, workspaceId: key.workspace.id }));
                detach = typeof undo === "function" ? undo : null;
            } catch (e) { console.error("[WorkspacePage] the app's attach threw:", e); }
        }
        function load() {
            WorkspaceLoad.load(log, { writes: writes() }).then(function (r) {
                log = r.log;
                build(r.folded ? r.folded.state : null, r.logged);
            });
        }
        lock.acquire().then(load);
    }
}
