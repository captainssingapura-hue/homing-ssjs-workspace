// =============================================================================
// MonitorsWorkspaceApp — the monitors' workspace as a page: the shell's
// workspace page (WorkspacePage), handed this set's manifest,
// MONITORS_WORKSPACE, generated from its declaration in Java
// (MonitorsWorkspace). The shell knows no widget; this is where the monitors
// meet it.
// =============================================================================

function appMain(el, params) {
    WorkspacePage.main(el, params, MONITORS_WORKSPACE, { fresh: function (p) { return nav.MonitorsWorkspaceApp(p); } });
}
