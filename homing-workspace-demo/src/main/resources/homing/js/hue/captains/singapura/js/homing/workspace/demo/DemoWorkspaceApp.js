// =============================================================================
// DemoWorkspaceApp — the demo workspace as a page: the shell's workspace page
// (WorkspacePage), handed the demo's manifest, DEMO_WORKSPACE, generated from
// its declaration in Java (DemoWorkspace) - the books, the switcher and the
// monitors, three widget sets that know nothing of each other or of the
// shell, put together - and its directory of workspace groups, DEMO_GROUPS,
// provided first, for the switcher to read.
// =============================================================================

function appMain(el, params) {
    // the page's directory of workspace groups: the demo's, provided before anything reads it
    WorkspaceDirectory.provide(DEMO_GROUPS);
    WorkspacePage.main(el, params, DEMO_WORKSPACE, { fresh: function (p) { return nav.DemoWorkspaceApp(p); } });
}
