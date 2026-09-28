// =============================================================================
// WorkspaceApp - the workspace as a page of a standard MPA.
//
// The MPA's chrome made the page and handed us the slot; we fill it: say the
// slot lays itself out (a workspace is not a reading column) and build the
// Workspace in it - the desk and its docks, as the gallery's docking page
// builds them (RFC 0066 E3, the workspace detour). The address names the kind,
// and the workspace of that kind - its ws_id, or the kind's own - and the two
// name the log: kept in IndexedDB, typed, the workspace listed beside it under
// a name of its own the first time a page writes it; the page comes back to
// what the log folds to - from its latest checkpoint, folding only what came
// after - and goes on logging, a checkpoint folded every so many events in a
// worker, and posted to the server when the page's route says it keeps them.
// One page writes a log at a time: the one holding its write lock. Another page
// of the same workspace reads it only, until it takes the workspace over, or
// opens a new workspace of the kind instead. A log that cannot be read or
// folded - an older format, a gap - is set aside whole, said, and the page
// starts afresh (WorkspaceLoad, the load every placement shares); the bar exports
// what was set aside. What a tab may hold is,
// for now, a fake.
// =============================================================================

/** How many tabs the desk holds at once: the desk's budget, the same for every kind for now. */
var _BUDGET = 16;

/** What a tab may hold while the tabs are built: the fakes, as the tab source's kinds. */
var _KINDS = [
    { id: "note",    label: "A note",    title: "Note",    make: function (b, p) { return new FakeNote(b, p); } },
    { id: "counter", label: "A counter", title: "Counter", make: function (b, p) { return new FakeCounter(b, p); } },
    { id: "field",   label: "A field",   title: "Field",   make: function (b, p) { return new FakeField(b, p); } }
];

function appMain(el, params) {
    var kind = params && params.ws_kind;
    if (!kind) {
        // A routed page always stamps its kind. The MPA's flat /app hands the
        // app no params when the codec refused the address's - a kind absent,
        // or not kind-shaped, or a ws_id not a workspace's - and there is then
        // no log to keep.
        el.textContent = "No workspace: the address names no kind, or no kind or workspace a log can be kept under.";
        console.error("[workspaceApp] no ws_kind in the page's params");
        return;
    }
    css.addClass(el, mpa_main_full);
    // the workspace the address names, checked where the address was read; else the kind's own
    var backend = new IndexedDbLog();
    var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(kind, params.ws_id || null), backend: backend });
    var key = new LogKey(log.header.kind, log.header.workspaceId);
    var catalogue = new WorkspaceCatalogue({ backend: backend });
    // One writer per log: the page that holds its lock records into it and takes
    // its checkpoints; a page that does not reads it only, and writes nothing to it.
    var lock = new WorkspaceWriteLock({ log: key, onChange: lockSaid });
    var ws = null;
    function writes() { return WorkspaceWriteLock.writes(lock.state); }
    // What the workspace is called: listed under a name of its own the first time a
    // page writes it, and its opening noted; a page that only reads it looks it up.
    function named() {
        var entry = writes() ? catalogue.opened(key)
            : catalogue.list(key.kind).then(function (all) { return all.filter(function (e) { return e.log.workspace.id === key.workspace.id; })[0] || null; });
        entry.then(function (e) { if (ws && ws.logBar) ws.logBar.named(e); },
                   function (e) { console.warn("[workspaceApp] the workspace is not listed, so not named: " + (e && e.message)); });
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
            if (ws) ws.dispose();
            ws = null;
            load();
        });
    }
    // The page made one keyboard steward for the document and handed it in the
    // params; everything under here that takes keys takes THAT one.
    function build(state, logged) {
        ws = new Workspace(domOpsParty.createBranch("workspace"), { host: el, kinds: _KINDS, keyboard: params && params.keyboard, menus: params && params.menus,
                                                                    budget: _BUDGET, log: log, state: state, logged: logged,
                                                                    readOnly: !writes(), checkpointer: writes() ? checkpointer() : null,
                                                                    server: params.ws_server === "on" });
        if (ws.logBar) ws.logBar.lock(lock.state, takeOver);
        named();
    }
    // Checkpoints are folded in a module worker, off this thread; a page whose
    // route says the server keeps its states posts each one there too.
    function checkpointer() {
        if (typeof Worker === "undefined") return null;
        try {
            return new WorkspaceCheckpointer({ store: log, worker: new Worker(WORKSPACE_ADDRESSES.checkpointWorker, { type: "module" }),
                                               upload: params.ws_server === "on" ? WORKSPACE_ADDRESSES.checkpoints : null });
        } catch (e) {
            console.warn("[workspaceApp] no checkpoint worker, so no checkpoints this session: " + e.message);
            return null;
        }
    }
    // What the log folds to - its latest checkpoint and what came after, every
    // layer at once - and the page built back to the grid's; a log that will not
    // fold is set aside, and the page starts afresh (WorkspaceLoad).
    function load() {
        WorkspaceLoad.load(log, { writes: writes() }).then(function (r) {
            log = r.log;
            build(r.folded ? r.folded.state.grid : null, r.logged);
        });
    }
    lock.acquire().then(load);
}
