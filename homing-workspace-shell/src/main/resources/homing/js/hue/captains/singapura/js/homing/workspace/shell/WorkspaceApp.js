// =============================================================================
// WorkspaceApp - the workspace as a page of a standard MPA.
//
// The MPA's chrome made the page and handed us the slot; we fill it. Three
// steps, and none of them is workspace-specific: say the slot lays itself out
// (a shell of panes is not a reading column), pick the spec the address asked
// for, and hand it to mountWorkspaceShell with a branch of our own. Everything
// that IS a workspace - the arrangement, the panes, the ribbon and footer, the
// parties, persistence, the event log, replay, checkpoints, the write lock -
// is behind that one call, exactly as it is behind the studio's mounting.
//
// The branch is ours and activated here because nothing above made one for us:
// under the studio a widget was handed an activated branch, and here there is
// no widget - the app is the thing.
// =============================================================================

const _owner = Object.freeze({ toString: () => "workspaceApp" });

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
    var branch = domOpsParty.createBranch("workspace");
    branch.activate(_owner);
    // The page made one keyboard steward for the document and handed it in the
    // params; everything under here that takes keys takes THAT one.
    mountWorkspaceShell(branch, el, spec, params && params.keyboard);
}
