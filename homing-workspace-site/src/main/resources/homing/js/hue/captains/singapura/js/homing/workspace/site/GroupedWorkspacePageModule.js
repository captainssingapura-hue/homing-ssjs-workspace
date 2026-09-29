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
// Switching is the page's, never a workspace's: whatever the workspace holds,
// the page keeps a workspace choice party of its own, its opener on it, and
// summons the switcher (WorkspaceSwitcherDialog) in the system dialog - by its
// key, Ctrl+Shift+K (Cmd+Shift+K on a Mac), wherever the hand is, or by the
// workspace's name on the log bar - at the kind and the workspace it shows.
// The key again, or Escape, closes it; no log keeps it. A new workspace asked
// for - the switcher's new row - the page makes: a fresh id, listed under the
// name asked for, and goes to it, where its arrangement is laid out.
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

const _groupedPageOwner = Object.freeze({ toString: () => "groupedWorkspacePage" });

class GroupedWorkspacePage {

    /** The letter of the page's key for the switcher, with Ctrl - or Cmd - and Shift. */
    static SWITCH_KEY = "K";

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
        var here = Object.freeze({ workspaceKind: kind, workspaceId: p.ws_id || WorkspaceLogIdentity.placeholder(kind), own: own });
        var go = function (address) { HrefManagerInstance.navigate(address); };
        var goNew = function (address) { HrefManagerInstance.openNew(address); };
        var catalogue = new WorkspaceCatalogue({ backend: new IndexedDbLog() });
        var create = function (k, name) { return GroupedWorkspacePage._create(catalogue, k, name); };
        // THE PAGE'S OWN CHOICE: a workspace choice party of the page's, whatever its workspace holds; its opener,
        // and the switcher summoned in the system dialog by the page's key, or by the workspace's name on the log bar
        var choice = new MessagingParty(WORKSPACE_CHOICE, WorkspaceChoiceSecretary);
        new WorkspaceOpener({ party: choice, here: here, addressOf: GroupedWorkspacePage.address, go: go, goNew: goNew, create: create });
        var summon = GroupedWorkspacePage._summoner(choice, p.keyboard, here);
        if (p.keyboard && typeof p.keyboard.shortcut === "function") {
            p.keyboard.shortcut(function (ev) { if (!GroupedWorkspacePage.isSwitchKey(ev)) return false; summon(); return true; });
        }
        WorkspacePage.main(el, p, manifest, {
            fresh: function (q) { return GroupedWorkspacePage.address(kind, q && q.ws_id ? q.ws_id : ""); },
            arrangement: arrangements && Object.prototype.hasOwnProperty.call(arrangements, kind) ? arrangements[kind] : null,
            attach: function (ws, here) {
                if (ws.logBar) ws.logBar.switching({ hint: "Switch workspace (" + GroupedWorkspacePage.switchKeyName() + ")", open: summon });
                var party = ws.parties.party(WORKSPACE_CHOICE.name);
                if (!party) return null;    // a workspace none of whose widgets choose one
                var opener = new WorkspaceOpener({
                    party: party,
                    here: { workspaceKind: here.workspaceKind, workspaceId: here.workspaceId, own: own },
                    addressOf: GroupedWorkspacePage.address,
                    go: go, goNew: goNew, create: create
                });
                return function () { opener.leave(); };
            }
        });
    }

    /** The switcher, summoned in the system dialog - or, open already, closed: one at a time, on a branch of the page's own. */
    static _summoner(choice, keyboard, here) {
        var place = domOpsParty.createBranch("workspaceSwitching"), dialog = null, made = 0;
        place.activate(_groupedPageOwner);
        return function () {
            if (dialog && dialog.isOpen()) { dialog.close(); return; }
            dialog = new WorkspaceSwitcherDialog(place.createBranch("dialog" + (++made)), {
                party: choice, keyboard: keyboard, here: here, addressOf: GroupedWorkspacePage.address,
                title: "Switch workspace"
            });
        };
    }

    /** The page's key for the switcher: Ctrl+Shift+K - on a Mac, Cmd+Shift+K; said once, read by the shortcut and the hint. */
    static isSwitchKey(ev) {
        if (!ev || ev.altKey || !ev.shiftKey || !(ev.ctrlKey || ev.metaKey)) return false;
        return ev.code === "Key" + GroupedWorkspacePage.SWITCH_KEY || String(ev.key).toUpperCase() === GroupedWorkspacePage.SWITCH_KEY;
    }

    /** The key as this platform says it. */
    static switchKeyName() {
        return (GroupedWorkspacePage._mac() ? "⌘⇧" : "Ctrl+Shift+") + GroupedWorkspacePage.SWITCH_KEY;
    }

    static _mac() {
        var n = typeof navigator !== "undefined" ? navigator : null;
        return /mac/i.test(n ? ((n.userAgentData && n.userAgentData.platform) || n.platform || "") : "");
    }

    /**
     * A new workspace of the kind, made: a fresh id, listed in the catalogue - under the name asked for,
     * or, none, the kind's next - and its address; null for a kind the site does not serve. Its log is
     * empty until its page writes it, and so its page lays out its arrangement.
     */
    static _create(catalogue, kind, name) {
        if (!WorkspaceDirectory.find(kind)) return Promise.resolve(null);
        var id = WorkspaceLogIdentity.fresh(), key = new LogKey(new WorkspaceKind(kind), id);
        return catalogue.opened(key)
            .then(function () { return name ? catalogue.rename(key, name) : null; })
            .then(function () { return GroupedWorkspacePage.address(kind, id.id); });
    }

    static address(kind, id) {
        var found = WorkspaceDirectory.find(kind);
        if (!found) return null;
        var named = id && id !== WorkspaceLogIdentity.placeholder(kind);
        return "/" + found.workspace.path + (named ? "?ws_id=" + encodeURIComponent(id) : "");
    }
}
