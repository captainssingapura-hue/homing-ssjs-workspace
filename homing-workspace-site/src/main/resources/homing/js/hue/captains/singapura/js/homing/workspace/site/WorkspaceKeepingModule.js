// =============================================================================
// WorkspaceKeeping — what a grouped page does to its site's workspaces when
// its workspace choice party asks: makes a new one, calls one otherwise,
// deletes one — by the catalogue this browser keeps. The opener and the keeper
// ask it; it answers them.
//
//   make     a fresh id, listed under the name asked for — refused when another
//            workspace of the kind is called so, its case aside — or the kind's
//            next; its address. Its log is empty until its page writes it, and so
//            its page lays out its arrangement.
//   rename   the catalogue's, refused the same way; the page's own workspace
//            renamed, its log bar told the name (named).
//   delete   SOFT: out of the list, its log, checkpoint and address kept. Refused
//            for the workspace the page shows, for a kind's own - its address
//            always brings it back - and for one another page is writing now (the
//            browser's locks, as the write lock names them); else asked of a person
//            first (WorkspaceConfirm), in the system dialog.
// Each answer is { changed, note }: whether the kind's list changed, and what to
// say - done, or why not.
//
//   new WorkspaceKeeping({ catalogue, here, keyboard, addressOf, named? })
//     here       { workspaceKind, workspaceId } the page shows
//     addressOf  (kind, id, name?) → a workspace's address - by its name, when said - or null for a
//                kind the site does not serve
//     named      (WorkspaceEntry) → the page's own workspace called otherwise
//   keeping.create(kind, name) → Promise<address | null>; a name taken, refused
//   keeping.rename(kind, id, name) → Promise<{ changed, note }>
//   keeping.remove(kind, id)       → Promise<{ changed, note }>
//   WorkspaceKeeping.heldElsewhere(logKey) → Promise<whether a page holds its write lock>
// =============================================================================

const _workspaceKeepingOwner = Object.freeze({ toString: () => "workspaceKeeping" });

class WorkspaceKeeping {
    constructor(opts) {
        var o = opts || {};
        if (!o.catalogue || !o.here || typeof o.addressOf !== "function") throw new TypeError("[WorkspaceKeeping] opts.catalogue, opts.here and opts.addressOf are required");
        this._catalogue = o.catalogue;
        this._here = o.here;
        this._keyboard = o.keyboard || null;
        this._addressOf = o.addressOf;
        this._named = typeof o.named === "function" ? o.named : function () {};
        this._asks = null;
        this._asked = 0;
    }

    create(kind, name) {
        if (!this._addressOf(kind, "")) return Promise.resolve(null);
        var id = WorkspaceLogIdentity.fresh(), self = this;
        return this._catalogue.opened(new LogKey(new WorkspaceKind(kind), id), name || "").then(function (entry) { return self._addressOf(kind, id.id, entry.name.value); });
    }

    rename(kind, id, name) {
        var self = this;
        return this._catalogue.rename(WorkspaceKeeping._key(kind, id), name).then(function (entry) {
            if (self._isHere(kind, id)) self._named(entry);
            return { changed: true, note: "Renamed “" + entry.name.value + "”." };
        });
    }

    remove(kind, id) {
        var key = WorkspaceKeeping._key(kind, id), self = this;
        if (id === WorkspaceLogIdentity.placeholder(kind)) return Promise.resolve({ changed: false, note: "A kind's own workspace is always there - its address brings it back." });
        if (this._isHere(kind, id)) return Promise.resolve({ changed: false, note: "This page shows it - open another before deleting it." });
        return WorkspaceKeeping.heldElsewhere(key).then(function (held) {
            if (held) return { changed: false, note: "Another page is writing it - close that page first." };
            return self._catalogue.list(key.kind).then(function (all) {
                var entry = all.filter(function (e) { return e.log.workspace.id === id; })[0];
                if (!entry) return { changed: true, note: "" };
                return self._ask(entry).then(function (yes) {
                    if (!yes) return { changed: false, note: "" };
                    return self._catalogue.remove(key).then(function (gone) {
                        return { changed: true, note: "Deleted “" + gone.name.value + "” - its log is kept." };
                    });
                });
            });
        });
    }

    static heldElsewhere(key) {
        var locks = typeof navigator !== "undefined" ? navigator.locks : null;
        if (!locks || typeof locks.query !== "function") return Promise.resolve(false);
        var name = WorkspaceWriteLock.nameOf(key);
        return locks.query().then(function (s) { return (s.held || []).some(function (l) { return l.name === name; }); }, function () { return false; });
    }

    /** A person asked, in the system dialog, whether to delete it: yes only when Delete was pressed. */
    _ask(entry) {
        if (!this._asks) { this._asks = domOpsParty.createBranch("workspaceKeeping"); this._asks.activate(_workspaceKeepingOwner); }
        return new WorkspaceConfirm(this._asks.createBranch("confirm" + (++this._asked)), {
            title: "Delete workspace", act: "Delete", keyboard: this._keyboard,
            question: "Delete “" + entry.name.value + "”? It leaves the list; its log is kept, and opening its address brings it back."
        }).answered;
    }

    _isHere(kind, id) { return kind === this._here.workspaceKind && id === this._here.workspaceId; }

    static _key(kind, id) { return new LogKey(new WorkspaceKind(kind), new WorkspaceInstanceId(id)); }
}
