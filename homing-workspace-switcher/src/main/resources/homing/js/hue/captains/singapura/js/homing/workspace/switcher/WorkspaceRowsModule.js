// =============================================================================
// WorkspaceRows — the switcher table's relation, a relation grid's root (view,
// columns, cellFor): the workspaces of one kind as the catalogue lists them,
// the latest opened first, each a row — its name, when it was last opened,
// when it was made — and last the NEW ROW, "+ New workspace": its name the one
// thing in it anyone may say, its other two derived, "now", as it will be made
// and opened. Only the name column is open to editing; the two dates never.
//
// A cell is a noun of its own, on a branch of the relation's: a workspace's
// name a WorkspaceNameCell — edited only when armed, so Enter keeps opening —
// the new row's name a text cell that commits, every other cell one that only
// shows. What is committed is the owner's to have done: onRename, onNewName.
//
//   new WorkspaceRows({ branch, onRename(row, text), onNewName(kind, text) })
//   rows.view(intent)  rows.columns()  rows.readOnlyColumns()  rows.labels()  rows.cellFor(pk, column)
//   rows.show(kind, entries)   the kind's WorkspaceEntries, and the new row; a cell still
//                              shown takes its value
//   rows.clear()     no rows
//   rows.sweep()     every cell no longer shown, disposed - once its grid has been told
//   rows.row(pk)     rows.all()    rows.kept() - the workspaces, the new row aside
//   rows.arm(pk)     the next edit of that workspace's name, taken
//   rows.dispose()
//   WorkspaceRows.NEW_NAME    WorkspaceRows.text(row, column)    WorkspaceRows.when(ms)
// =============================================================================

const _workspaceRowsOwner = Object.freeze({ toString: () => "workspaceRows" });

class WorkspaceRows {

    /** The columns, and what the header calls them. */
    static COLUMNS = Object.freeze(["name", "opened", "created"]);
    static LABELS = Object.freeze({ name: "Name", opened: "Last opened", created: "Made" });
    /** Derived: never a person's to say - so the name column is open, and only a name commits. */
    static DERIVED = Object.freeze(["opened", "created"]);
    /** The new row's name, as it is offered: replaced by what a person types, or, left, the catalogue's next name. */
    static NEW_NAME = "+ New workspace";

    constructor(opts) {
        var o = opts || {};
        if (!o.branch) throw new Error("[WorkspaceRows] opts.branch is required: the relation's own");
        this._branch = o.branch;
        this._branch.activate(_workspaceRowsOwner);
        this._onRename = typeof o.onRename === "function" ? o.onRename : function () {};
        this._onNewName = typeof o.onNewName === "function" ? o.onNewName : function () {};
        this._list = [];
        this._byPk = new Map();
        this._cells = new Map();
        this._seq = 0;
    }

    view(intent) { return intent ? null : this._list.map(function (r) { return r.pk; }); }
    columns() { return WorkspaceRows.COLUMNS.slice(); }
    readOnlyColumns() { return WorkspaceRows.DERIVED.slice(); }
    labels() { return WorkspaceRows.LABELS; }

    cellFor(pk, col) {
        var row = this._byPk.get(pk);
        if (!row) throw new Error("[WorkspaceRows] no such workspace: " + pk);
        var k = pk + " " + col, c = this._cells.get(k), self = this;
        if (!c) {
            var branch = this._branch.createBranch("c" + (++this._seq));
            if (col === "name" && !row.fresh) c = new WorkspaceNameCell({ branch: branch, value: row.name, onCommit: function (text) { self._onRename(row, text); } });
            else c = new RelGridTextCell({ branch: branch, value: WorkspaceRows.text(row, col),
                                           onCommit: row.fresh && col === "name" ? function (text) { self._onNewName(row.kind, text); } : undefined });
            this._cells.set(k, c);
        }
        return c;
    }

    show(kind, entries) {
        var rows = entries.map(function (e) {
            return { pk: kind + "/" + e.log.workspace.id, kind: kind, id: e.log.workspace.id, name: e.name.value, opened: e.opened, created: e.created };
        }).concat([{ pk: kind + "/+new", kind: kind, id: "", fresh: true, name: WorkspaceRows.NEW_NAME }]);
        this._set(rows);
    }

    clear() { this._set([]); }

    sweep() {
        var self = this;
        this._cells.forEach(function (c, k) { if (!self._byPk.has(k.slice(0, k.lastIndexOf(" ")))) { c.dispose(); self._cells.delete(k); } });
    }

    row(pk) { return this._byPk.get(pk) || null; }
    all() { return this._list.slice(); }
    kept() { return this._list.filter(function (r) { return !r.fresh; }); }

    arm(pk) { var c = this._cells.get(pk + " name"); if (c && typeof c.arm === "function") c.arm(); }

    dispose() {
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._branch.dissolve();
    }

    /** The rows now; a cell still shown takes its value. */
    _set(rows) {
        var self = this;
        this._list = rows;
        this._byPk = new Map(rows.map(function (r) { return [r.pk, r]; }));
        rows.forEach(function (r) {
            WorkspaceRows.COLUMNS.forEach(function (col) { var c = self._cells.get(r.pk + " " + col); if (c) c.set(WorkspaceRows.text(r, col)); });
        });
    }

    /** A cell's text: the name; or when, derived - for the new row, now, when it will be made and opened. */
    static text(row, col) {
        if (col === "name") return row.name;
        return row.fresh ? "now" : WorkspaceRows.when(row[col]);
    }

    /** A moment, as a person reads it here: the day, and the minute. */
    static when(ms) {
        var d = new Date(ms);
        function two(n) { return (n < 10 ? "0" : "") + n; }
        return d.getFullYear() + "-" + two(d.getMonth() + 1) + "-" + two(d.getDate()) + " " + two(d.getHours()) + ":" + two(d.getMinutes());
    }
}
