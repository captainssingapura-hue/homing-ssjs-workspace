// =============================================================================
// WorkspaceInstances — the workspaces of the chosen kind this browser keeps,
// as a table: each one's name, when it was last opened, when it was made; the
// latest opened first; and last a new row. What it reads is the catalogue
// (WorkspaceCatalogue) the workspace pages write, kept beside their logs; it
// never writes it — what it asks of the workspaces, whoever keeps them does.
// Its rows and their cells are its relation's (WorkspaceRows).
//
// A self-contained widget (the Workspace & Widgets doctrines): made with the
// container its page lends it and its params, and nothing else; its DomOps
// and focus parties its own, offered as roots for its host to graft; the
// relation grid's cells natively focused inside it.
//
// Joined to a workspace choice party (Messaging Parties Are Joined Top-Down),
// what it shows is the kind the party says is chosen, read again each time it
// says so; on joining it asks what is chosen. A read that answers late for a
// kind no longer chosen is passed over. Not joined, it shows nothing chosen,
// and works alone.
//
// What it asks, of the workspace at the cursor:
//   Enter, or a double press   to open it (Open) — on the NEW ROW, "+ New
//                              workspace", its name taken instead: typed over
//                              what it offers, Enter to have one made of the kind
//                              (OpenNew), under that name, or left, the next
//   F2                         to call it otherwise: its name taken — the only
//                              edit a workspace's name allows, and only so asked
//                              (WorkspaceNameCell) — Enter to have it (Rename)
//   Delete                     to delete it (Delete) — softly: out of the list
// How a keeping went, whoever keeps the workspaces says (Reported): its note is
// shown, and the list read again when it changed — a name another workspace of
// the kind has, for one, refused, and said so.
//
// The keys: the grid's — ↑ ↓ and the rest walk it — and the three above. ← at
// the grid's left edge goes past it: its host, told (edge), may hand the keys
// on. Escape the grid did not take gives the keys back.
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
//   table.rename()  table.remove()   F2 and Delete, as a host's verbs: false when nothing is to be done
//   table.activate()   the keys claimed, and on into the grid
//   table.dispose()
// =============================================================================

const _workspaceInstancesOwner = Object.freeze({ toString: () => "workspaceInstances" });
var _workspaceInstancesMade = 0;

class WorkspaceInstances {
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
        // the workspace choice party, while joined; the kind shown, what was read of it, and what was told of it
        this._choice = null;
        this._joined = false;
        this._edge = null;
        this._kind = null;
        this._state = "none";
        this._failure = "";
        this._reads = 0;
        this._disposed = false;
        this._preferred = null;
        this._landedOn = null;
        this._told = "";
        // its data: the catalogue this browser keeps, read and never written; its rows, the grid's relation
        this._catalogue = new WorkspaceCatalogue({ backend: new IndexedDbLog() });
        this._rows = new WorkspaceRows({ branch: this._dom.createBranch("cells"),
                                         onRename: function (row, text) { self._rename(row, text); },
                                         onNewName: function (kind, text) { self._openNew(kind, text); } });
        this._grid = new RelGrid({
            container: this._box, branch: this._dom.createBranch("grid"), label: "Workspaces of the chosen kind",
            header: { show: true, sticky: true }, relation: this._rows,
            onEdge: function (direction) { if (direction === "left" && self._edge) self._edge("left"); }
        });
        // Enter, F2 and Delete, as the browser's focus in a cell has them; a double press opens
        this._box.addEventListener("keydown", function (ev) { if (self._key(ev.key)) ev.preventDefault(); });
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
        this._choice = party.join("workspaceInstances", { Chosen: function (m) { self._read(m.workspaceKind); }, Reported: function (m) { self._heard(m); } });
        this._choice.tell({ kind: "CurrentRequested" });
    }

    leave() {
        if (this._choice) { this._choice.leave(); this._choice = null; }
        this._joined = false;
    }

    shown() {
        return Object.freeze({ kind: this._kind, state: this._state,
                               workspaces: this._rows.kept().map(function (r) { return Object.freeze({ id: r.id, name: r.name, opened: r.opened, created: r.created }); }) });
    }

    edge(fn) { this._edge = typeof fn === "function" ? fn : null; }

    cursor() {
        var row = this._rowAt();
        return row ? Object.freeze({ workspaceKind: row.kind, workspaceId: row.id, fresh: row.fresh === true }) : null;
    }

    prefer(workspaceId) { this._preferred = workspaceId ? String(workspaceId) : null; this._land(); }

    /** The workspace at the cursor, its name taken - F2, or Rename. */
    rename() {
        var row = this._rowAt();
        if (!row || row.fresh) return false;
        this._grid.selectCell(row.pk, "name");
        this._rows.arm(row.pk);
        this._grid.takeControlAtCursor();
        return true;
    }

    /** The workspace at the cursor, asked to be deleted - Delete; whoever keeps the workspaces confirms, or says why not. */
    remove() {
        var row = this._rowAt();
        if (!row || row.fresh || !this._choice) return false;
        this._choice.tell({ kind: "Delete", workspaceKind: row.kind, workspaceId: row.id });
        return true;
    }

    /** Asked for the keys: they are claimed, and go on into the grid. */
    activate() { Keys.claim(this.focus); }

    /** Given the keys: into the grid - unless the browser's focus arriving in a cell is what gave them. */
    granted(by) { if (by !== "native") this._grid.focus(); }

    /** Escape the grid did not take gives the keys back; the table's own keys, when they came without the browser's focus. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return this._key(ev.key);
    }

    // ── the chosen kind, read ──────────────────────────────────────────────

    /** The kind's workspaces read from the catalogue, and shown; a read that answers for a kind no longer shown, passed over. */
    _read(kind) {
        var at = ++this._reads, self = this;
        if (kind !== this._kind) this._told = "";
        this._kind = kind;
        this._state = "reading";
        this._shown();
        var wk;
        try { wk = new WorkspaceKind(kind); }
        catch (e) { this._failed(e); return; }
        this._catalogue.list(wk).then(function (entries) {
            if (at !== self._reads || self._disposed) return;
            self._state = "read";
            self._rows.show(kind, entries);
            self._redrawn();
        }, function (e) {
            if (at !== self._reads || self._disposed) return;
            self._failed(e);
        });
    }

    _failed(e) {
        this._state = "failed";
        this._failure = (e && e.message) || String(e);
        this._rows.clear();
        this._redrawn();
    }

    /** The grid told its rows changed - its cursor kept on the row it was on; the cells no longer shown, disposed; the head, the note. */
    _redrawn() {
        this._grid.tell(new RelGridViewChanged());
        this._rows.sweep();
        this._shown();
        // the preferred workspace landed on when a kind is first shown - never again on a refresh, where the cursor stays put
        if (this._landedOn !== this._kind) { this._landedOn = this._kind; this._land(); }
    }

    /** The cursor on the preferred workspace, when it is among the rows shown. */
    _land() {
        var id = this._preferred, row = id ? this._rows.all().filter(function (r) { return r.id === id; })[0] : null;
        if (row) this._grid.selectCell(row.pk, "name");
    }

    // ── what it asks ───────────────────────────────────────────────────────

    /** The table's own keys, wherever they came from: Enter opens, F2 renames, Delete deletes. */
    _key(key) {
        if (key === "Enter") return this._openAtCursor();
        if (key === "F2") return this.rename();
        if (key === "Delete") return this.remove();
        return false;
    }

    _rowAt() { var at = this._grid.cursor(); return at ? this._rows.row(at.pk) : null; }

    /** The workspace at the cursor, asked to open: of its own kind, whatever is being read meanwhile - the new row, named. */
    _openAtCursor() {
        var row = this._rowAt();
        if (!row || !this._choice) return false;
        if (!row.fresh) { this._choice.tell({ kind: "Open", workspaceKind: row.kind, workspaceId: row.id }); return true; }
        var at = this._grid.cursor();
        if (!at || at.column !== "name") this._grid.selectCell(row.pk, "name");
        this._grid.takeControlAtCursor();
        return true;
    }

    /** A new one, named - or, left as it was offered or blank, the catalogue's next name - asked for, here. */
    _openNew(kind, text) {
        if (!this._choice) return;
        var name = String(text == null ? "" : text).trim();
        if (name === WorkspaceRows.NEW_NAME) name = "";
        this._choice.tell({ kind: "OpenNew", workspaceKind: kind, workspaceName: name, newTab: false });
    }

    /** A workspace's name, changed: asked of whoever keeps the workspaces - blank, or as it was, nothing. */
    _rename(row, text) {
        var name = String(text == null ? "" : text).trim();
        if (!name || name === row.name || !this._choice) return;
        this._choice.tell({ kind: "Rename", workspaceKind: row.kind, workspaceId: row.id, workspaceName: name });
    }

    /** How a keeping went, for the kind shown: its note said; its list, changed, read again. */
    _heard(m) {
        if (m.workspaceKind !== this._kind) return;
        if (m.note) this._told = m.note;
        if (m.changed) this._read(this._kind);
        else this._shown();
    }

    /** The head: the kind's title and how many; a note saying what there is to do, what was done, or why there is nothing. */
    _shown() {
        var found = this._kind ? WorkspaceDirectory.find(this._kind) : null, kept = this._rows.kept().length, nw = WorkspaceRows.NEW_NAME;
        this._title.textContent = (found ? found.workspace.title : this._kind) || "Workspaces";
        this._count.textContent = this._state === "read" && kept ? String(kept) : "";
        this._note.textContent = this._told ? this._told
                 : this._state === "none" ? "Choose a kind of workspace."
                 : this._state === "reading" ? "Reading the workspaces this browser keeps…"
                 : this._state === "failed" ? "The workspaces could not be read: " + this._failure
                 : kept ? "Enter opens one, F2 renames it, Delete deletes it - Enter on " + nw + " names a new one."
                 : "None of this kind is kept in this browser yet - Enter on " + nw + " names a new one.";
        css.toggleClass(this._box, sw_hidden, this._rows.all().length === 0);
    }

    dispose() {
        this._disposed = true;
        this.leave();
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._grid.destroy();
        this._rows.dispose();
        this._dom.dissolve();
    }
}
