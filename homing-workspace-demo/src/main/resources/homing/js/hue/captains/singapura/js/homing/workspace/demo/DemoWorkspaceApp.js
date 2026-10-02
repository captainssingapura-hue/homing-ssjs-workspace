// =============================================================================
// DemoWorkspaceApp — the demo's workspaces as a page: the grouped workspace page
// (GroupedWorkspacePage), handed the demo's manifests, DEMO_WORKSPACES - the
// two widget sets together, the books, the monitors, each generated from its
// declaration in Java - and the groups they are filed in, DEMO_GROUPS. The
// route names the group, the anchor the kind; the page opens what its
// switcher asks to open - summoned, never a widget of the workspace's; and
// each workspace starts, the first time, as DEMO_ARRANGEMENTS has it - its
// own widgets, the whole floor theirs.
// =============================================================================

function appMain(el, params) {
    GroupedWorkspacePage.main(el, params, DEMO_WORKSPACES, DEMO_GROUPS, DEMO_ARRANGEMENTS);
}
