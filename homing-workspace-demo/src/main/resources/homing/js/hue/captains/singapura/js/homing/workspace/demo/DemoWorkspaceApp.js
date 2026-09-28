// =============================================================================
// DemoWorkspaceApp — the demo workspace as a page: the shell's workspace page
// (WorkspacePage), handed the demo's manifest, DEMO_WORKSPACE, generated from
// its declaration in Java (DemoWorkspace) - the books and the monitors, two
// widget sets that know nothing of each other or of the shell, put together.
// =============================================================================

function appMain(el, params) {
    WorkspacePage.main(el, params, DEMO_WORKSPACE, { fresh: function (p) { return nav.DemoWorkspaceApp(p); } });
}
