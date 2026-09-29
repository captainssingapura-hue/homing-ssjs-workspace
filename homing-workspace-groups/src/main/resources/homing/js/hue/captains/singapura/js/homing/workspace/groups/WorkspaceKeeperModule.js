// =============================================================================
// WorkspaceKeeper — the page's member of its workspace choice party that keeps
// the workspaces: what the party says is asked of them — a workspace called
// otherwise (Renaming), one deleted (Deleting) — it has the page do, and says
// how it went (Report): whether the kind's list changed, and a note for every
// view of that kind to show — what was done, or why it was not. A page with
// more than one party has a keeper on each; what one keeper's page changed, it
// tells the others' parties too (report), so every view of the kind re-reads.
//
// Pure: it keeps nothing itself. The page hands it the doing, and keeps the
// catalogue, the confirming and the refusing its own.
//
//   new WorkspaceKeeper({ party, rename?, remove?, done? })
//     party    the workspace choice party it joins
//     rename   (workspaceKind, workspaceId, workspaceName) → Promise<{ changed, note }>
//     remove   (workspaceKind, workspaceId) → Promise<{ changed, note }>
//              none, and what is asked is said not done
//     done     (workspaceKind, changed, keeper) → nothing, told after every asking it reported
//   keeper.report(workspaceKind, note, changed)   told on its party: Report
//   keeper.reported()   what it reported - the last few, the newest last
//   keeper.leave()
// =============================================================================

class WorkspaceKeeper {

    /** How many reports are kept. */
    static KEPT = 5;

    constructor(opts) {
        var o = opts || {}, self = this;
        if (!o.party || typeof o.party.join !== "function") throw new TypeError("[WorkspaceKeeper] opts.party is required: the workspace choice party it joins");
        this._rename = typeof o.rename === "function" ? o.rename : null;
        this._remove = typeof o.remove === "function" ? o.remove : null;
        this._done = typeof o.done === "function" ? o.done : null;
        this._reported = [];
        this._member = o.party.join("workspaceKeeper", {
            Renaming: function (m) { self._keep(m.workspaceKind, self._rename, [m.workspaceKind, m.workspaceId, m.workspaceName], "renamed"); },
            Deleting: function (m) { self._keep(m.workspaceKind, self._remove, [m.workspaceKind, m.workspaceId], "deleted"); }
        });
    }

    report(kind, note, changed) {
        this._reported.push(Object.freeze({ workspaceKind: kind, note: String(note || ""), changed: changed === true }));
        if (this._reported.length > WorkspaceKeeper.KEPT) this._reported.shift();
        if (this._member) this._member.tell({ kind: "Report", workspaceKind: kind, note: String(note || ""), changed: changed === true });
    }

    reported() { return this._reported.slice(); }

    leave() {
        if (this._member) { this._member.leave(); this._member = null; }
    }

    /** An asking kept: done by the page, and said how it went - refused, or failed, said too. */
    _keep(kind, doing, args, what) {
        var self = this;
        if (!doing) { this._said(kind, "this page cannot have a workspace " + what, false); return; }
        Promise.resolve().then(function () { return doing.apply(null, args); }).then(function (o) {
            self._said(kind, o && o.note, !!(o && o.changed));
        }, function (e) {
            self._said(kind, (e && e.message) || String(e), false);
        });
    }

    _said(kind, note, changed) {
        this.report(kind, note, changed);
        if (this._done) this._done(kind, changed, this);
    }
}
