// =============================================================================
// GroupedWorkspacePage — a grouped site's page of one group: the shell's page
// (WorkspacePage) of the kind of workspace its anchor names, from the site's
// manifests — each generated from its declaration in Java — and the page's
// directory provided from the site's groups (WorkspaceDirectory), so a
// switcher on it shows them.
//
// THE AUTHENTIC PATH (RFC 0058). The group is the page, reached from outside
// by its address, "/<group>" - "<under>/<group>" when the site places its
// groups under an address of its own; everything inside it is the group's own, in the
// anchor (WorkspaceAnchor), which never reaches the server:
//   /<group>#ws/<section>/<kind>                    a kind, its own workspace
//   /<group>#ws/<section>/<kind>?ws_name=<name>     one of its others, by what it is
//                                                   called - its case aside; listed, else
//                                                   deleted and so brought back
//   /<group>#ws/<section>/<kind>?ws_id=<id>         ... by its id - a new one, not named yet
// The kind is a position; which workspace of it is a parameter - the anchor's
// query, at its end. The id outranks the name, and the address says the name
// once the catalogue has one: a workspace is called by what it is called, and
// called again so when it is renamed. No anchor is the group's default; a
// kind under another path is moved to its own, in place; a workspace's anchor
// naming nothing of the group is the default, and the log bar says so - as it
// does a name that calls none, the kind's own shown. The trail the server
// wrote ends at the group; the page carries it on - the section, the workspace.
//
// Another anchor once the page stands: the workspace it shows, said as it is;
// any other - another kind's, another of this kind - its page, loaded. A
// heading's anchor is not the workspace's, and left alone.
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
// nothing yet (GridArrangement) - and again whenever a person resets it: the
// log bar's Reset…, asked in the system dialog, its log set aside, not lost.
//
//   GroupedWorkspacePage.main(el, params, workspaces, groups, arrangements?)
//     el          the MPA's slot
//     params      the page's: ws_group, the route's; ws_server, a workspace page's;
//                 ws_under, where the site places its groups, "" for its root; the
//                 trail, carried on. Which workspace is the anchor's, never a param
//     workspaces  the site's manifests, by kind
//     groups      the site's groups, as the directory is provided them
//     arrangements  the split grid's, by kind: each workspace's first state, laid out by
//                 the shell on a log that holds nothing yet; a kind with none starts empty
//   GroupedWorkspacePage.address(kind, id, name?) → where a workspace of the site is: its
//               group's page and its kind's anchor, and - unless it is the kind's own, "" or
//               the own's id - its name when said, else its id; null for a kind not filed
// =============================================================================

const _groupedPageOwner = Object.freeze({ toString: () => "groupedWorkspacePage" });

class GroupedWorkspacePage {

    /** The letter of the page's key for the switcher, with Ctrl - or Cmd - and Shift. */
    static SWITCH_KEY = "K";

    /** Where the site places its groups: "" for its root - said by the page's params, one page to a document. */
    static _under = "";

    static main(el, params, workspaces, groups, arrangements) {
        var p = params || {};
        GroupedWorkspacePage._under = p.ws_under || "";
        WorkspaceDirectory.provide(groups);
        var group = p.ws_group ? WorkspaceDirectory.group(p.ws_group) : null;
        if (!group) {
            // The MPA's flat /app hands the app no params when the codec refused the address's.
            el.textContent = "No workspace: the address names no group this site serves.";
            console.error("[GroupedWorkspacePage] no group " + JSON.stringify(p.ws_group) + " on this site");
            return;
        }
        // THE ANCHOR: which kind of the group - its path said as it resolved, and in the trail
        var read = WorkspaceAnchor.read(group, HrefManagerInstance.hash()), kind = read.kind, notices = [];
        if (read.said === "unknown") notices.push("No workspace at “#" + read.asked + "” in " + group.title + " - " + read.workspace.title + " shown");
        if (read.said === "moved" || read.said === "unknown" || (read.said === "none" && !read.asked)) HrefManagerInstance.replaceHash(read.anchor + read.query);
        if (p.trail && typeof p.trail.extend === "function") p.trail.extend([{ text: read.section.title }, { text: read.workspace.title }]);
        var manifest = Object.prototype.hasOwnProperty.call(workspaces, kind) ? workspaces[kind] : null;
        if (!manifest) {
            el.textContent = "No workspace: " + group.title + " files one this site does not serve.";
            console.error("[GroupedWorkspacePage] no workspace of the kind " + JSON.stringify(kind) + " on this site");
            return;
        }
        // ITS QUERY: which workspace of the kind
        var catalogue = new WorkspaceCatalogue({ backend: new IndexedDbLog() });
        GroupedWorkspacePage._which(catalogue, read).then(function (which) {
            if (which.note) notices.push(which.note);
            GroupedWorkspacePage._open(el, p, { group: group, manifest: manifest, kind: kind, id: which.id, notices: notices, catalogue: catalogue,
                                                arrangement: arrangements && Object.prototype.hasOwnProperty.call(arrangements, kind) ? arrangements[kind] : null });
        });
    }

    /**
     * Which workspace of the kind: the one the anchor's id says; else the one its name calls so - listed, or
     * deleted and so brought back when opened; else the kind's own - a name that calls none said, and let go.
     */
    static _which(catalogue, read) {
        if (read.workspaceId || !read.workspaceName) return Promise.resolve({ id: read.workspaceId || "" });
        var none = function (why) {
            HrefManagerInstance.replaceHash(read.anchor);
            return { id: "", note: why };
        };
        return catalogue.find(new WorkspaceKind(read.kind), read.workspaceName).then(function (entry) {
            return entry ? { id: entry.log.workspace.id } : none("No " + read.workspace.title + " workspace is called “" + read.workspaceName + "” - its own shown");
        }, function (e) {
            console.warn("[GroupedWorkspacePage] the workspaces of " + read.kind + " do not read: " + (e && e.message));
            return none("The workspaces of " + read.workspace.title + " do not read - its own shown");
        });
    }

    /**
     * Another anchor, once the page stands: the workspace it shows, said as it is - its path, and its name;
     * any other workspace, another kind's or another of this kind, its page, loaded.
     */
    static _follow(group, shown) {
        HrefManagerInstance.onHashChange(function (h) {
            if (h && !WorkspaceAnchor.isWorkspace(h)) return;
            var next = WorkspaceAnchor.read(group, h);
            if (!GroupedWorkspacePage._shows(next, shown)) { HrefManagerInstance.reload(); return; }
            var truth = GroupedWorkspacePage._anchor(group, shown);
            if (truth !== h) HrefManagerInstance.replaceHash(truth);
        });
    }

    /** Whether an anchor names the workspace the page shows: its kind, and it - by id, by name, or its own by neither. */
    static _shows(next, shown) {
        if (next.kind !== shown.kind) return false;
        if (next.workspaceId) return next.workspaceId === shown.id;
        if (next.workspaceName) return !!shown.name && next.workspaceName.trim().toLowerCase() === shown.name.trim().toLowerCase();
        return shown.own;
    }

    /** What the address says of the workspace the page shows: its kind's anchor, and its name - by its id until it has one; nothing more, the kind's own. */
    static _anchor(group, shown) {
        return WorkspaceAnchor.of(group, shown.kind, shown.own ? null : shown.name ? { name: shown.name } : { id: shown.id });
    }

    /** The workspace the page shows, built: its keeping, the page's own choice, and the shell's page. */
    static _open(el, p, o) {
        var kind = o.kind, own = !o.id || o.id === WorkspaceLogIdentity.placeholder(kind);
        var here = Object.freeze({ workspaceKind: kind, workspaceId: own ? WorkspaceLogIdentity.placeholder(kind) : o.id, own: own });
        var shown = { kind: kind, id: here.workspaceId, own: own, name: null };
        // the address calls the workspace by what it is called - once it is, and again when it is called otherwise
        var called = function (entry) {
            shown.name = entry.name.value;
            var truth = GroupedWorkspacePage._anchor(o.group, shown);
            if (truth !== HrefManagerInstance.hash()) HrefManagerInstance.replaceHash(truth);
        };
        GroupedWorkspacePage._follow(o.group, shown);
        var go = function (address) { HrefManagerInstance.navigate(address); };
        var goNew = function (address) { HrefManagerInstance.openNew(address); };
        // THE KEEPING of the site's workspaces: made, renamed, deleted - by the page, as its parties ask
        var current = null, keepers = [];
        var keeping = new WorkspaceKeeping({ catalogue: o.catalogue, here: here, keyboard: p.keyboard,
                                             addressOf: GroupedWorkspacePage.address,
                                             named: function (entry) {
                                                 if (current && current.logBar) current.logBar.named(entry);
                                                 called(entry);
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
            named: called,
            confirm: function (q) { return keeping.ask(q); },
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

    /** Its group's page - "<under>/<group>", as the site places it - and its kind's anchor, ending with its name, else its id; nothing, the kind's own. */
    static address(kind, id, name) {
        var found = WorkspaceDirectory.find(kind);
        if (!found) return null;
        var own = !id || id === WorkspaceLogIdentity.placeholder(kind);
        return GroupedWorkspacePage._under + "/" + encodeURIComponent(found.group.id) + "#" + WorkspaceAnchor.of(found.group, kind, own ? null : name ? { name: name } : { id: id });
    }
}
