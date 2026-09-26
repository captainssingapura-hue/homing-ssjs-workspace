// =============================================================================
// WorkspaceApp - the workspace as a page of a standard MPA.
//
// The MPA's chrome made the page and handed us the slot; we fill it: say the
// slot lays itself out (a workspace is not a reading column) and build the
// Workspace in it - the desk and its docks, as the gallery's docking page
// builds them (RFC 0066 E3, the workspace detour). The address names the kind,
// and the kind names the log: kept in IndexedDB, typed; the page comes back to
// what the log folds to, and goes on logging. A log that cannot be read or
// folded - an older format, a gap - is set aside whole, said, and the page
// starts afresh; the bar exports what was set aside. What a tab may hold is,
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
        // or not kind-shaped - and there is then no log to keep.
        el.textContent = "No workspace kind: the address names none, or none a log can be kept under.";
        console.error("[workspaceApp] no ws_kind in the page's params");
        return;
    }
    css.addClass(el, mpa_main_full);
    var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(kind, ""), backend: new IndexedDbLog() });
    // The page made one keyboard steward for the document and handed it in the
    // params; everything under here that takes keys takes THAT one.
    function build(state, logged) {
        new Workspace(domOpsParty.createBranch("workspace"), { host: el, kinds: _KINDS, keyboard: params && params.keyboard, menus: params && params.menus,
                                                               budget: _BUDGET, log: log, state: state, logged: logged });
    }
    // A log that will not read or fold is set aside, not cleared: kept whole, to be
    // exported from the bar, and the page starts afresh. Should even that fail, the
    // stored log is left as it is and this session is kept in memory only.
    function afresh(why) {
        log.setAside(why).then(function (aside) {
            console.warn("[workspaceApp] the stored log does not read and is set aside" + (aside ? ", " + aside.lines.length + " lines" : "") + ": " + why);
            build(null, 0);
        }, function (e) {
            console.error("[workspaceApp] the stored log does not read and could not be set aside, so it is left as it is and this session is kept in memory only: "
                          + (e && e.message));
            log = new WorkspaceLogStore({ header: log.header, backend: new MemoryLog() });
            build(null, 0);
        });
    }
    // what the log folds to, and the page built back to it; a log that will not fold starts the page afresh
    log.events().then(function (events) {
        var folded;
        try { folded = WorkspaceFold.fold(log.header, events); }
        catch (e) { afresh(e.message); return; }
        build(folded.state, events.length);
    }, function (e) { afresh(e && e.message); });
}
