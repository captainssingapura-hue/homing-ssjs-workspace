// =============================================================================
// WorkspaceInstances — the workspaces of the chosen kind this browser keeps,
// as a table: each one's name, when it was last opened, when it was made; the
// latest opened first. What it reads is the catalogue (WorkspaceCatalogue) the
// workspace pages write, kept beside their logs; it never writes it.
//
// A self-contained widget (the Workspace & Widgets doctrines): made with the
// container its page lends it and its params, and nothing else; its DomOps
// and focus parties its own, offered as roots for its host to graft; the
// relation grid's cells natively focused inside it.
//
// Joined to a workspace choice party (Messaging Parties Are Joined Top-Down),
// what it shows is the kind the party says is chosen, read again each time it
// says so; on joining it asks what is chosen. Enter on a workspace, or a
// double press, asks for it to open — its kind, and its id. A read that
// answers late for a kind no longer chosen is passed over. Not joined, it
// shows nothing chosen, and works alone.
//
// Last in the table, a NEW ROW: "+ New workspace". Its name is the one thing
// in it anyone may say — typed over what it offers, Enter to have it — and
// its other two are derived, "now", as it will be made and opened; the
// workspace is asked for, of the kind shown, under that name (OpenNew) —
// left as offered, or blank, under the catalogue's next — and whoever opens
// workspaces makes it. Enter, or a double press, anywhere on it takes its
// name.
//
// The keys: the grid's — ↑ ↓ and the rest walk it. Enter opens the workspace
// at the cursor. ← at the grid's left edge goes past it: its host, told
// (edge), may hand the keys on. Escape the grid did not take gives the keys
// back.
//
//   new WorkspaceInstances(container, params)   params: none
//   table.root      its root, in the container
//   table.roots     { dom, focus }: what its host grafts
//   (its kind declares, in Java, the types it joins for its full function: workspace-choice)
//   table.join(given)   given: { [type name]: party }; a second join without a leave is refused
//   table.leave()
//   table.shown()   { kind, state: "none" | "reading" | "read" | "failed", workspaces: [{ id, name, opened, created }] }
//   table.edge(fn)  fn("left") when ← goes past the grid — for a host that hands the keys on
//   table.cursor()  the workspace at the cursor, { workspaceKind, workspaceId, fresh }, or null -
//                   fresh on the new row, its id ""
//   table.prefer(workspaceId)   the workspace its cursor lands on whenever its kind is shown:
//                   the page's own, so a switcher opens where the page is
//   table.activate()   the keys claimed, and on into the grid
//   table.dispose()
// =============================================================================

const _workspaceInstancesOwner = Object.freeze({ toString: () => "workspaceInstances" });
var _workspaceInstancesMade = 0;

class WorkspaceInstances {

    /** The columns, as the relation has them, and what the header calls them. */
    static COLUMNS = Object.freeze(["name", "opened", "created"]);
    static LABELS = Object.freeze({ name: "Name", opened: "Last opened", created: "Made" });
    /** The new row's name, as it is offered: replaced by what a person types, or, left, the catalogue's next name. */
    static NEW_NAME = "+ New workspace";
    /** What a person may change: the new row's name alone - so the name column is open, and only that cell commits. */
    static DERIVED = Object.freeze(["opened", "created"]);

    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[WorkspaceInstances] a container is required: the one its page lends it");
        var name = "workspaceInstances-" + (++_workspaceInstancesMade), self = this;
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_workspaceInstancesOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        css.addClass(root, sw_column);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "Workspaces of the chosen kind");
        var head = this._dom.createElement("head", "div");
        css.addClass(head, sw_head);
        this._title = this._dom.createElement("title", "div");
        css.addClass(this._title, sw_title);
        this._count = this._dom.createElement("count", "div");
        css.addClass(this._count, sw_count);
        head.appendChild(this._title);
        head.appendChild(this._count);
        this._box = this._dom.createElement("box", "div");
        css.addClass(this._box, wg_scroll);
        this._note = this._dom.createElement("note", "p");
        css.addClass(this._note, sw_note);
        root.appendChild(head);
        root.appendChild(this._box);
        root.appendChild(this._note);
        container.appendChild(root);
        this.root = root;
        // the workspace choice party, while joined; the kind shown, and what was read of it
        this._choice = null;
        this._joined = false;
        this._edge = null;
        this._kind = null;
        this._state = "none";
        this._failure = "";
        this._reads = 0;
        this._rows = [];
        this._byPk = new Map();
        this._disposed = false;
        this._preferred = null;
        // its data: the catalogue this browser keeps, read and never written; a cell a value, on a branch of its own
        this._catalogue = new WorkspaceCatalogue({ backend: new IndexedDbLog() });
        this._cellsBranch = this._dom.createBranch("cells");
        this._cellsBranch.activate(_workspaceInstancesOwner);
        this._cells = new Map();
        this._cellSeq = 0;
        this._grid = new RelGrid({
            container: this._box, branch: this._dom.createBranch("grid"), label: "Workspaces of the chosen kind",
            header: { show: true, sticky: true },
            relation: {
                view: function (intent) { return intent ? null : self._rows.map(function (r) { return r.pk; }); },
                columns: function () { return WorkspaceInstances.COLUMNS.slice(); },
                readOnlyColumns: function () { return WorkspaceInstances.DERIVED.slice(); },
                labels: function () { return WorkspaceInstances.LABELS; },
                cellFor: function (pk, col) { return self._cellFor(pk, col); }
            },
            onEdge: function (direction) { if (direction === "left" && self._edge) self._edge("left"); }
        });
        // Enter, and a double press, open the workspace at the cursor
        this._box.addEventListener("keydown", function (ev) { if (ev.key === "Enter" && self._openAtCursor()) ev.preventDefault(); });
        this._box.addEventListener("dblclick", function () { self._openAtCursor(); });
        this._focusParty = focusParties.mobile(name);
        this.focus = this._focusParty.root.join("instances", this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._shown();
    }

    /** Joined to the parties it needs, given by type: the workspace choice party, whose chosen kind it shows. */
    join(given) {
        if (this._joined) throw new Error("[WorkspaceInstances] joined already: leave first");
        this._joined = true;
        var party = given && given[WORKSPACE_CHOICE.name], self = this;
        if (!party) return;
        this._choice = party.join("workspaceInstances", { Chosen: function (m) { self._read(m.workspaceKind); } });
        this._choice.tell({ kind: "CurrentRequested" });
    }

    leave() {
        if (this._choice) { this._choice.leave(); this._choice = null; }
        this._joined = false;
    }

    shown() {
        return Object.freeze({ kind: this._kind, state: this._state,
                               workspaces: this._rows.filter(function (r) { return !r.fresh; }).map(function (r) { return Object.freeze({ id: r.id, name: r.name, opened: r.opened, created: r.created }); }) });
    }

    edge(fn) { this._edge = typeof fn === "function" ? fn : null; }

    cursor() {
        var at = this._grid.cursor(), row = at ? this._byPk.get(at.pk) : null;
        return row ? Object.freeze({ workspaceKind: row.kind, workspaceId: row.id, fresh: row.fresh === true }) : null;
    }

    prefer(workspaceId) { this._preferred = workspaceId ? String(workspaceId) : null; this._land(); }

    /** Asked for the keys: they are claimed, and go on into the grid. */
    activate() { Keys.claim(this.focus); }

    /** Given the keys: into the grid - unless the browser's focus arriving in a cell is what gave them. */
    granted(by) { if (by !== "native") this._grid.focus(); }

    /** Escape the grid did not take gives the keys back; Enter opens, when the keys came without the browser's focus. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        if (ev.key === "Enter") return this._openAtCursor();
        return false;
    }

    // ── the chosen kind, read ──────────────────────────────────────────────

    /** The kind's workspaces read from the catalogue, and shown; a read that answers for a kind no longer shown, passed over. */
    _read(kind) {
        var at = ++this._reads, self = this;
        this._kind = kind;
        this._state = "reading";
        this._shown();
        var wk;
        try { wk = new WorkspaceKind(kind); }
        catch (e) { this._failed(e); return; }
        this._catalogue.list(wk).then(function (entries) {
            if (at !== self._reads || self._disposed) return;
            self._state = "read";
            self._showRows(entries.map(function (e) {
                return { pk: kind + "/" + e.log.workspace.id, kind: kind, id: e.log.workspace.id, name: e.name.value, opened: e.opened, created: e.created };
            }).concat([{ pk: kind + "/+new", kind: kind, id: "", fresh: true, name: WorkspaceInstances.NEW_NAME }]));
        }, function (e) {
            if (at !== self._reads || self._disposed) return;
            self._failed(e);
        });
    }

    _failed(e) {
        this._state = "failed";
        this._failure = (e && e.message) || String(e);
        this._showRows([]);
    }

    /** The rows shown now: a cell still shown takes its value; the grid told; a cell no longer shown, disposed. */
    _showRows(rows) {
        var self = this, cols = WorkspaceInstances.COLUMNS;
        this._rows = rows;
        this._byPk = new Map(rows.map(function (r) { return [r.pk, r]; }));
        rows.forEach(function (r) {
            cols.forEach(function (col) { var c = self._cells.get(r.pk + " " + col); if (c) c.set(WorkspaceInstances._text(r, col)); });
        });
        this._grid.tell(new RelGridViewChanged());
        this._cells.forEach(function (c, k) { if (!self._byPk.has(k.slice(0, k.lastIndexOf(" ")))) { c.dispose(); self._cells.delete(k); } });
        this._shown();
        this._land();
    }

    _cellFor(pk, col) {
        var row = this._byPk.get(pk);
        if (!row) throw new Error("[WorkspaceInstances] no such workspace: " + pk);
        var k = pk + " " + col, c = this._cells.get(k), self = this;
        if (!c) {
            // the new row's name is the one thing a person says of it: its cell commits; every other cell only shows
            var commit = row.fresh && col === "name" ? function (text) { self._openNew(row.kind, text); } : undefined;
            c = new RelGridTextCell({ branch: this._cellsBranch.createBranch("c" + (++this._cellSeq)), value: WorkspaceInstances._text(row, col), onCommit: commit });
            this._cells.set(k, c);
        }
        return c;
    }

    /** The cursor on the preferred workspace, when it is among the rows shown. */
    _land() {
        var id = this._preferred, row = null;
        for (var i = 0; id && i < this._rows.length; i++) if (this._rows[i].id === id) row = this._rows[i];
        if (row) this._grid.selectCell(row.pk, "name");
    }

    /** A cell's text: the name; or when, derived - for the new row, now, when it will be made and opened. */
    static _text(row, col) {
        if (col === "name") return row.name;
        return row.fresh ? "now" : WorkspaceInstances._when(row[col]);
    }

    /** A moment, as a person reads it here: the day, and the minute. */
    static _when(ms) {
        var d = new Date(ms);
        function two(n) { return (n < 10 ? "0" : "") + n; }
        return d.getFullYear() + "-" + two(d.getMonth() + 1) + "-" + two(d.getDate()) + " " + two(d.getHours()) + ":" + two(d.getMinutes());
    }

    /** The workspace at the cursor, asked to open: of its own kind, whatever is being read meanwhile. */
    _openAtCursor() {
        var at = this._grid.cursor(), row = at ? this._byPk.get(at.pk) : null;
        if (!row || !this._choice) return false;
        if (row.fresh) return this._nameAt(row);
        this._choice.tell({ kind: "Open", workspaceKind: row.kind, workspaceId: row.id });
        return true;
    }

    /** The new row, opened: its name taken, the one thing of it that is anyone's to say. */
    _nameAt(row) {
        var at = this._grid.cursor();
        if (!at || at.column !== "name") this._grid.selectCell(row.pk, "name");
        this._grid.takeControlAtCursor();
        return true;
    }

    /** A new one, named - or, left as it was offered or blank, the catalogue's next name - asked for, here. */
    _openNew(kind, text) {
        if (!this._choice) return;
        var name = String(text == null ? "" : text).trim();
        if (name === WorkspaceInstances.NEW_NAME) name = "";
        this._choice.tell({ kind: "OpenNew", workspaceKind: kind, workspaceName: name, newTab: false });
    }

    /** The head: the kind's title and how many; a note saying what there is to do, or why there is nothing. */
    _shown() {
        var found = this._kind ? WorkspaceDirectory.find(this._kind) : null;
        var kept = this._rows.filter(function (r) { return !r.fresh; }).length, rows = this._rows.length;
        var title = found ? found.workspace.title : this._kind;
        this._title.textContent = title || "Workspaces";
        this._count.textContent = this._state === "read" && kept ? String(kept) : "";
        var note = this._state === "none" ? "Choose a kind of workspace."
                 : this._state === "reading" ? "Reading the workspaces this browser keeps…"
                 : this._state === "failed" ? "The workspaces could not be read: " + this._failure
                 : kept ? "Enter, or a double press, opens one - on " + WorkspaceInstances.NEW_NAME + ", names a new one."
                 : "None of this kind is kept in this browser yet - Enter on " + WorkspaceInstances.NEW_NAME + " names a new one.";
        this._note.textContent = note;
        css.toggleClass(this._box, sw_hidden, rows === 0);
    }

    dispose() {
        this._disposed = true;
        this.leave();
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._grid.destroy();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._dom.dissolve();
    }
}
