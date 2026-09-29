// =============================================================================
// GroupedWorkspacePage — a grouped site's workspace page: the shell's page
// (WorkspacePage), the workspace chosen by the kind its route names, from the
// site's manifests — each generated from its declaration in Java — and the
// page's directory provided from the site's groups (WorkspaceDirectory), so a
// switcher on it shows them.
//
// The page is whoever opens workspaces. Its workspace's root workspace choice
// party, when it has one, is joined by an opener (WorkspaceOpener) each time
// the workspace is built: it chooses the kind the page shows, and what the
// party says is asked to open, the page goes to — at the place the groups file
// it, "/<section>/<kind>", and "?ws_id=<id>" unless it is the kind's own -
// unless it is the workspace the page shows. The log bar's new workspace of the
// kind is at that place too.
//
// A workspace of a kind the site has arranged starts as its arrangement has it,
// the first time: handed to the shell, which lays it out on a log that holds
// nothing yet (GridArrangement).
//
//   GroupedWorkspacePage.main(el, params, workspaces, groups, arrangements?)
//     el          the MPA's slot
//     params      the page's: ws_kind, the route's; ws_id and ws_server, a workspace page's
//     workspaces  the site's manifests, by kind
//     groups      the site's groups, as the directory is provided them
//     arrangements  the split grid's, by kind: each workspace's first state, laid out by
//                 the shell on a log that holds nothing yet; a kind with none starts empty
//   GroupedWorkspacePage.address(kind, id) → where a workspace of the site is: its place,
//               and its id unless it is the kind's own - "", or the own's id - so one
//               workspace has one address; null for a kind not filed
// =============================================================================

class GroupedWorkspacePage {
    static main(el, params, workspaces, groups, arrangements) {
        var p = params || {}, kind = p.ws_kind;
        WorkspaceDirectory.provide(groups);
        var manifest = kind && Object.prototype.hasOwnProperty.call(workspaces, kind) ? workspaces[kind] : null;
        if (!manifest || !WorkspaceDirectory.find(kind)) {
            // The MPA's flat /app hands the app no params when the codec refused the address's.
            el.textContent = "No workspace: the address names none this site serves.";
            console.error("[GroupedWorkspacePage] no workspace of the kind " + JSON.stringify(kind) + " on this site");
            return;
        }
        var own = !p.ws_id || p.ws_id === WorkspaceLogIdentity.placeholder(kind);
        WorkspacePage.main(el, p, manifest, {
            fresh: function (q) { return GroupedWorkspacePage.address(kind, q && q.ws_id ? q.ws_id : ""); },
            arrangement: arrangements && Object.prototype.hasOwnProperty.call(arrangements, kind) ? arrangements[kind] : null,
            attach: function (ws, here) {
                var party = ws.parties.party(WORKSPACE_CHOICE.name);
                if (!party) return null;    // a workspace none of whose widgets choose one
                var opener = new WorkspaceOpener({
                    party: party,
                    here: { workspaceKind: here.workspaceKind, workspaceId: here.workspaceId, own: own },
                    addressOf: GroupedWorkspacePage.address,
                    go: function (address) { HrefManagerInstance.navigate(address); }
                });
                return function () { opener.leave(); };
            }
        });
    }

    static address(kind, id) {
        var found = WorkspaceDirectory.find(kind);
        if (!found) return null;
        var named = id && id !== WorkspaceLogIdentity.placeholder(kind);
        return "/" + found.workspace.path + (named ? "?ws_id=" + encodeURIComponent(id) : "");
    }
}
