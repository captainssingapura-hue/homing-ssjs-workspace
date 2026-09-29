// =============================================================================
// DemoWorkspaceApp — the demo's workspaces as a page: the grouped workspace page
// (GroupedWorkspacePage), handed the demo's manifests, DEMO_WORKSPACES - the
// three widget sets together, the books, the monitors, each generated from its
// declaration in Java - and the groups they are filed in, DEMO_GROUPS. The
// route names the kind; the page opens what its switcher asks to open.
// =============================================================================

function appMain(el, params) {
    GroupedWorkspacePage.main(el, params, DEMO_WORKSPACES, DEMO_GROUPS);
}
