// =============================================================================
// WorkspaceApp - the workspace as a page of a standard MPA.
//
// The MPA's chrome made the page and handed us the slot; we fill it: say the
// slot lays itself out (a workspace is not a reading column), pick the spec the
// address asked for, and build the Workspace in it - the desk and its docks,
// as the gallery's docking page builds them (RFC 0066 E3, the workspace
// detour). Its log is kept in IndexedDB, typed; with no replay yet, a visit's
// log is that visit's, cleared as the page starts. What a tab may hold is, for
// now, a fake.
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
    // The page made one keyboard steward for the document and handed it in the
    // params; everything under here that takes keys takes THAT one.
    var log = new WorkspaceLogStore({ header: WorkspaceLogIdentity.header(kind, ""), backend: new IndexedDbLog() });
    log.clear();
    new Workspace(domOpsParty.createBranch("workspace"), { host: el, kinds: _KINDS, keyboard: params && params.keyboard, log: log,
                                                           menus: params && params.menus, budget: spec.maxTabs || 16 });
}
