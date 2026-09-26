// =============================================================================
// WorkspaceApp - the workspace as a page of a standard MPA.
//
// The MPA's chrome made the page and handed us the slot; we fill it: say the
// slot lays itself out (a workspace is not a reading column), pick the spec the
// address asked for, and build the Workspace in it - the desk and its docks,
// as the gallery's docking page builds them (RFC 0066 E3, the workspace
// detour). Its log is kept in IndexedDB, typed; the page comes back to what the
// log folds to, and goes on logging. A log that cannot be read or folded - an
// older format, a gap - is cleared, said, and the page starts afresh. What a tab
// may hold is, for now, a fake.
// =============================================================================

/** What a tab may hold while the tabs are built: the fakes, as the tab source's kinds. */
var _KINDS = [
    { id: "note",    label: "A note",    title: "Note",    make: function (b, p) { return new FakeNote(b, p); } },
    { id: "counter", label: "A counter", title: "Counter", make: function (b, p) { return new FakeCounter(b, p); } },
    { id: "field",   label: "A field",   title: "Field",   make: function (b, p) { return new FakeField(b, p); } }
];

function appMain(el, params) {
    var kind = (params && params.ws_kind) || "";
    var spec = SPECS[kind];
    if (!spec) {
        // The codec refuses an absent kind, so getting here means a kind that
        // is not registered in THIS runtime - worth saying which are.
        el.textContent = "Unknown workspace kind: \"" + kind + "\""
                       + " (registered: " + Object.keys(SPECS).join(", ") + ")";
        console.error("[workspaceApp] no spec for kind '" + kind + "'");
        return;
    }
    css.addClass(el, mpa_main_full);
    var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(kind, ""), backend: new IndexedDbLog() });
    // The page made one keyboard steward for the document and handed it in the
    // params; everything under here that takes keys takes THAT one.
    function build(state, logged) {
        new Workspace(domOpsParty.createBranch("workspace"), { host: el, kinds: _KINDS, keyboard: params && params.keyboard, menus: params && params.menus,
                                                               budget: spec.maxTabs || 16, log: log, state: state, logged: logged });
    }
    function afresh(why) { console.warn("[workspaceApp] the stored log is cleared: " + why); log.clear(); build(null, 0); }
    // what the log folds to, and the page built back to it; a log that will not fold starts the page afresh
    log.events().then(function (events) {
        var folded;
        try { folded = WorkspaceFold.fold(log.header, events); }
        catch (e) { afresh(e.message); return; }
        build(folded.state, events.length);
    }, function (e) { afresh(e && e.message); });
}
