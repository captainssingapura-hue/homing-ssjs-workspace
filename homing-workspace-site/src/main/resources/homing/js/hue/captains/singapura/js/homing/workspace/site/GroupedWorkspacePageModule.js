// =============================================================================
// GroupedWorkspacePage — a grouped site's page of one group: the shell's page
// (WorkspacePage) of the kind of workspace its anchor names, from the site's
// manifests — each generated from its declaration in Java — and the page's
// directory provided from the site's groups (WorkspaceDirectory), so a
// switcher on it shows them.
//
// THE AUTHENTIC PATH (RFC 0058). The group is the page, reached from outside
// by its address, "/<group>"; everything inside it is the group's own:
//   /<group>#ws/<section>/<kind>              a kind, its own workspace
//   /<group>?ws_id=<id>#ws/<section>/<kind>   one workspace of the kind, by its id
//   /<group>?ws_name=<name>#ws/<section>/<kind>   ... by what it is called - its
//                                             case aside; listed, else deleted and
//                                             so brought back
// The kind is a position, in the anchor (WorkspaceAnchor), which never reaches
// the server; which workspace of it is a parameter, in the query. No anchor is
// the group's default; a kind under another path is moved to its own, in
// place; a workspace's anchor naming nothing of the group is the default, and
// the log bar says so - as it does a name that calls none, the kind's own
// shown. The id outranks the name. The trail the server wrote ends at the
// group; the page carries it on - the section, the workspace.
//
// Another anchor once the page stands: the same kind's, said as it is;
// another kind's, that kind's page, its own workspace - the query named one of
// this kind. A heading's anchor is not the workspace's, and left alone.
//
// The page is whoever opens workspaces. Its workspace's root workspace choice
// party, when it has one, is joined by an opener (WorkspaceOpener) each time
// the workspace is built: it chooses the kind the page shows, and what the
// party says is asked to open, the page goes to - its address - unless it is
// the workspace the page shows. The log bar's new workspace of the kind is at
// its address too.
//
// Switching is the page's, never a workspace's: whatever the workspace holds,
// the page keeps a workspace choice party of its own, its opener on it, and
// summons the switcher (WorkspaceSwitcherDialog) in the system dialog - by its
// key, Ctrl+Shift+K (Cmd+Shift+K on a Mac), wherever the hand is, or by the
// workspace's name on the log bar - at the kind and the workspace it shows.
// The key again, or Escape, closes it; no log keeps it. A new workspace asked
// for - the switcher's new row - the page makes (WorkspaceKeeping): a fresh id, listed under the
// name asked for, and goes to it, where its arrangement is laid out; a
// workspace called otherwise, or deleted - softly - the page does too, and its
// keeper on each of its parties says how it went to every view of the kind.
//
// A workspace of a kind the site has arranged starts as its arrangement has it,
// the first time: handed to the shell, which lays it out on a log that holds
// nothing yet (GridArrangement).
//
//   GroupedWorkspacePage.main(el, params, workspaces, groups, arrangements?)
//     el          the MPA's slot
//     params      the page's: ws_group, the route's; ws_id or ws_name, which workspace
//                 of the kind; ws_server, a workspace page's; the trail, carried on
//     workspaces  the site's manifests, by kind
//     groups      the site's groups, as the directory is provided them
//     arrangements  the split grid's, by kind: each workspace's first state, laid out by
//                 the shell on a log that holds nothing yet; a kind with none starts empty
//   GroupedWorkspacePage.address(kind, id) → where a workspace of the site is: its group's
//               page, its id unless it is the kind's own - "", or the own's id - and its
//               kind's anchor, so one workspace has one address; null for a kind not filed
// =============================================================================

const _groupedPageOwner = Object.freeze({ toString: () => "groupedWorkspacePage" });

class GroupedWorkspacePage {

    /** The letter of the page's key for the switcher, with Ctrl - or Cmd - and Shift. */
    static SWITCH_KEY = "K";

    static main(el, params, workspaces, groups, arrangements) {
        var p = params || {};
        WorkspaceDirectory.provide(groups);
        var group = p.ws_group ? WorkspaceDirectory.group(p.ws_group) : null;
        if (!group) {
            // The MPA's flat /app hands the app no params when the codec refused the address's.
            el.textContent = "No workspace: the address names no group this site serves.";
            console.error("[GroupedWorkspacePage] no group " + JSON.stringify(p.ws_group) + " on this site");
            return;
        }
        // THE ANCHOR: which kind of the group - said in the address as it resolved, and in the trail
        var read = WorkspaceAnchor.read(group, HrefManagerInstance.hash()), kind = read.kind, notices = [];
        if (read.said === "unknown") notices.push("No workspace at “#" + read.asked + "” in " + group.title + " - " + read.workspace.title + " shown");
        if (read.said === "moved" || read.said === "unknown" || (read.said === "none" && !read.asked)) HrefManagerInstance.replaceHash(read.anchor);
        if (p.trail && typeof p.trail.extend === "function") p.trail.extend([{ text: read.section.title }, { text: read.workspace.title }]);
        var manifest = Object.prototype.hasOwnProperty.call(workspaces, kind) ? workspaces[kind] : null;
        if (!manifest) {
            el.textContent = "No workspace: " + group.title + " files one this site does not serve.";
            console.error("[GroupedWorkspacePage] no workspace of the kind " + JSON.stringify(kind) + " on this site");
            return;
        }
        GroupedWorkspacePage._follow(group, kind);
        // THE QUERY: which workspace of the kind
        var catalogue = new WorkspaceCatalogue({ backend: new IndexedDbLog() });
        GroupedWorkspacePage._which(catalogue, read, p).then(function (which) {
            if (which.note) notices.push(which.note);
            GroupedWorkspacePage._open(el, p, { manifest: manifest, kind: kind, id: which.id, byName: which.byName, notices: notices,
                                                catalogue: catalogue, arrangement: arrangements && Object.prototype.hasOwnProperty.call(arrangements, kind) ? arrangements[kind] : null });
        });
    }

    /**
     * Which workspace of the kind: the query's id; else the one its name calls so - listed, or deleted and so
     * brought back when opened - the name then kept in the address as the workspace is called; else the kind's own.
     */
    static _which(catalogue, read, p) {
        if (p.ws_id || !p.ws_name) return Promise.resolve({ id: p.ws_id || "", byName: false });
        var none = function (why) {
            HrefManagerInstance.replaceParam("ws_name", null);
            return { id: "", byName: false, note: why };
        };
        return catalogue.find(new WorkspaceKind(read.kind), p.ws_name).then(function (entry) {
            return entry ? { id: entry.log.workspace.id, byName: true }
                         : none("No " + read.workspace.title + " workspace is called “" + p.ws_name + "” - its own shown");
        }, function (e) {
            console.warn("[GroupedWorkspacePage] the workspaces of " + read.kind + " do not read: " + (e && e.message));
            return none("The workspaces of " + read.workspace.title + " do not read - its own shown");
        });
    }

    /** Another anchor, once the page stands: the same kind's, as it is; another kind's, its page - that kind's own workspace. */
    static _follow(group, kind) {
        HrefManagerInstance.onHashChange(function (h) {
            if (h && !WorkspaceAnchor.isWorkspace(h)) return;
            var next = WorkspaceAnchor.read(group, h);
            if (next.kind === kind) { if (next.anchor !== h) HrefManagerInstance.replaceHash(next.anchor); return; }
            var to = GroupedWorkspacePage.address(next.kind, "");
            if (to.split("#")[0] !== HrefManagerInstance.current()) { HrefManagerInstance.navigate(to); return; }
            HrefManagerInstance.replaceHash(next.anchor);
            HrefManagerInstance.reload();
        });
    }

    /** The workspace the page shows, built: its keeping, the page's own choice, and the shell's page. */
    static _open(el, p, o) {
        var kind = o.kind, own = !o.id || o.id === WorkspaceLogIdentity.placeholder(kind);
        var here = Object.freeze({ workspaceKind: kind, workspaceId: own ? WorkspaceLogIdentity.placeholder(kind) : o.id, own: own });
        var go = function (address) { HrefManagerInstance.navigate(address); };
        var goNew = function (address) { HrefManagerInstance.openNew(address); };
        // THE KEEPING of the site's workspaces: made, renamed, deleted - by the page, as its parties ask
        var current = null, keepers = [];
        var keeping = new WorkspaceKeeping({ catalogue: o.catalogue, here: here, keyboard: p.keyboard,
                                             addressOf: GroupedWorkspacePage.address,
                                             named: function (entry) {
                                                 if (current && current.logBar) current.logBar.named(entry);
                                                 if (o.byName) HrefManagerInstance.replaceParam("ws_name", entry.name.value);
                                             } });
        var create = function (k, name) { return keeping.create(k, name); };
        // a keeper on each of the page's parties; what one's asking changed, the others say too, for every view of the kind
        var relay = function (k, changed, from) { if (changed) keepers.forEach(function (x) { if (x !== from) x.report(k, "", true); }); };
        var keep = function (party) {
            var keeper = new WorkspaceKeeper({ party: party, done: relay,
                                               rename: function (k, id, name) { return keeping.rename(k, id, name); },
                                               remove: function (k, id) { return keeping.remove(k, id); } });
            keepers.push(keeper);
            return keeper;
        };
        // THE PAGE'S OWN CHOICE: a workspace choice party of the page's, whatever its workspace holds; its opener and
        // keeper, and the switcher summoned in the system dialog by the page's key, or by the workspace's name on the log bar
        var choice = new MessagingParty(WORKSPACE_CHOICE, WorkspaceChoiceSecretary);
        new WorkspaceOpener({ party: choice, here: here, addressOf: GroupedWorkspacePage.address, go: go, goNew: goNew, create: create });
        keep(choice);
        var summon = GroupedWorkspacePage._summoner(choice, p.keyboard, here);
        if (p.keyboard && typeof p.keyboard.shortcut === "function") {
            p.keyboard.shortcut(function (ev) { if (!GroupedWorkspacePage.isSwitchKey(ev)) return false; summon(); return true; });
        }
        WorkspacePage.main(el, Object.assign({}, p, { ws_id: own ? null : o.id }), o.manifest, {
            fresh: function (q) { return GroupedWorkspacePage.address(kind, q && q.ws_id ? q.ws_id : ""); },
            arrangement: o.arrangement,
            attach: function (ws, here) {
                current = ws;
                if (ws.logBar) {
                    ws.logBar.switching({ hint: "Switch workspace (" + GroupedWorkspacePage.switchKeyName() + ")", open: summon });
                    ws.logBar.notice(o.notices.join(" · "));
                }
                var party = ws.parties.party(WORKSPACE_CHOICE.name);
                if (!party) return function () { current = null; };    // a workspace none of whose widgets choose one
                var opener = new WorkspaceOpener({
                    party: party,
                    here: { workspaceKind: here.workspaceKind, workspaceId: here.workspaceId, own: own },
                    addressOf: GroupedWorkspacePage.address,
                    go: go, goNew: goNew, create: create
                });
                var keeper = keep(party);
                return function () {
                    opener.leave();
                    keeper.leave();
                    keepers.splice(keepers.indexOf(keeper), 1);
                    current = null;
                };
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

    /** Its group's page - "/<group>", as the site places it - its id unless the kind's own, and its kind's anchor. */
    static address(kind, id) {
        var found = WorkspaceDirectory.find(kind);
        if (!found) return null;
        var named = id && id !== WorkspaceLogIdentity.placeholder(kind);
        return "/" + encodeURIComponent(found.group.id) + (named ? "?ws_id=" + encodeURIComponent(id) : "") + "#" + WorkspaceAnchor.of(found.group, kind);
    }
}
