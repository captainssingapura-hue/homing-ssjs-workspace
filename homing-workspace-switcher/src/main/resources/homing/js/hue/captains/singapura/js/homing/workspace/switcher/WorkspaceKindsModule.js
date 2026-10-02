// =============================================================================
// WorkspaceKinds — the kinds of workspace the page's directory files, as a
// tree: each section, and under it the kinds filed there — or, where the site
// has several groups, the groups above their sections. It reads the directory
// (WorkspaceDirectory), the page's as the steward is, and never writes it; a
// directory provided again is shown again, what was folded kept folded.
//
// A self-contained widget (the Workspace & Widgets doctrines): made with the
// container its page lends it and its params, and nothing else; its DomOps
// and focus parties its own, offered as roots for its host to graft; the
// relation tree's rows natively focused inside it.
//
// Joined to a workspace choice party (Messaging Parties Are Joined Top-Down),
// the tree's cursor is the choice: a move onto a kind tells the party it is
// chosen, and so does a press on the kind the cursor is on, when it is not the
// chosen one; what the party says is chosen, the cursor moves to — the
// sections that hold it unfolded, when they were folded. On joining it asks
// what is chosen; when nothing is, it chooses the group's default. Enter on a
// kind, or a double press, asks for it to open: the kind's own, its id "".
// Not joined, it works alone.
//
// The keys: the tree's — ↑ ↓ Home End PageUp PageDown walk it, ← → fold and
// unfold, Space toggles, Enter opens. → on a kind goes past the tree: its host,
// told (edge), may hand the keys on. Escape the tree did not take gives the
// keys back.
//
//   new WorkspaceKinds(container, params)   params: none
//   kinds.root      its root, in the container
//   kinds.roots     { dom, focus }: what its host grafts
//   (its kind declares, in Java, the types it joins for its full function: workspace-choice)
//   kinds.join(given)   given: { [type name]: party }; a second join without a leave is refused
//   kinds.leave()
//   kinds.chosen()  the kind last told or heard as chosen, while joined; or null
//   kinds.edge(fn)  fn("right") when → goes past a kind — for a host that hands the keys on
//   kinds.activate()   the keys claimed, and on into the tree
//   kinds.dispose()
// =============================================================================

const _workspaceKindsOwner = Object.freeze({ toString: () => "workspaceKinds" });
var _workspaceKindsMade = 0;

class WorkspaceKinds {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[WorkspaceKinds] a container is required: the one its page lends it");
        var name = "workspaceKinds-" + (++_workspaceKindsMade), self = this;
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_workspaceKindsOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        css.addClass(root, sw_column);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "Kinds of workspace");
        var head = this._dom.createElement("head", "div");
        css.addClass(head, sw_head);
        this._title = this._dom.createElement("title", "div");
        css.addClass(this._title, sw_title);
        this._count = this._dom.createElement("count", "div");
        css.addClass(this._count, sw_count);
        head.appendChild(this._title);
        head.appendChild(this._count);
        var box = this._dom.createElement("box", "div");
        css.addClass(box, wg_scroll);
        this._note = this._dom.createElement("note", "p");
        css.addClass(this._note, sw_note);
        root.appendChild(head);
        root.appendChild(box);
        root.appendChild(this._note);
        container.appendChild(root);
        this.root = root;
        // the workspace choice party, while joined: its membership, and the kind last told or heard
        this._choice = null;
        this._chosen = null;
        this._joined = false;
        this._edge = null;
        // its data: the directory read into nodes; a cell a node, on a branch of its own
        this._cellsBranch = this._dom.createBranch("cells");
        this._cellsBranch.activate(_workspaceKindsOwner);
        this._cells = new Map();
        this._cellSeq = 0;
        this._roots = [];
        this._byKey = new Map();
        this._open = new Set();
        this._read();
        this._tree = new RelTree({
            container: box, branch: this._dom.createBranch("tree"), label: "Kinds of workspace", folder: true,
            relation: { view: function () { return self._places(); }, cellFor: function (key) { return self._cellFor(key); } },
            ask: function (q) { return self._answer(q); },
            onCursorMoved: function (key) { var k = self._kindAt(key); if (k && self._choice && k !== self._chosen) self._tell(k); },
            onActivated: function (key) { var k = self._kindAt(key); if (k && self._choice) self._choice.tell({ kind: "Open", workspaceKind: k, workspaceId: "" }); }
        });
        // a press on the kind the cursor is on already chooses it too; → past a kind is the host's
        box.addEventListener("click", function () { var k = self._kindAt(self._tree.cursor()); if (k && self._choice && k !== self._chosen) self._tell(k); });
        box.addEventListener("keydown", function (ev) { if (ev.key === "ArrowRight" && self._edge && self._kindAt(self._tree.cursor())) self._edge("right"); });
        this._offDirectory = WorkspaceDirectory.subscribe(function () { self._again(); });
        this._focusParty = focusParties.mobile(name);
        this.focus = this._focusParty.root.join("kinds", this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._shown();
    }

    /** Joined to the parties it needs, given by type: the workspace choice party, which its cursor tells and follows. */
    join(given) {
        if (this._joined) throw new Error("[WorkspaceKinds] joined already: leave first");
        this._joined = true;
        var party = given && given[WORKSPACE_CHOICE.name], self = this;
        if (!party) return;
        this._choice = party.join("workspaceKinds", { Chosen: function (m) { self._follow(m.workspaceKind); } });
        this._choice.tell({ kind: "CurrentRequested" });
        this._defaultIfNone();
    }

    leave() {
        if (this._choice) { this._choice.leave(); this._choice = null; }
        this._chosen = null;
        this._joined = false;
    }

    chosen() { return this._chosen; }

    edge(fn) { this._edge = typeof fn === "function" ? fn : null; }

    /** Asked for the keys: they are claimed, and go on into the tree. */
    activate() { Keys.claim(this.focus); }

    /** Given the keys: into the tree - unless the browser's focus arriving in a row is what gave them. */
    granted(by) { if (by !== "native") this._tree.focus(); }

    /** Escape the tree did not take gives the keys back. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    // ── the directory, as nodes ────────────────────────────────────────────

    /** The directory read into nodes: a group's (when there are several), a section's, a kind's - each with what holds it. What was known keeps its fold; what is new opens. */
    _read() {
        var groups = WorkspaceDirectory.groups(), several = groups.length > 1, known = this._byKey, open = this._open;
        var byKey = new Map(), roots = [];
        function node(key, label, depth, kind, holders) {
            var n = Object.freeze({ key: key, label: label, depth: depth, kind: kind, holders: holders, children: [] });
            byKey.set(key, n);
            return n;
        }
        groups.forEach(function (g) {
            var into = roots, depth = 0, holders = [];
            if (several) {
                var gn = node("g:" + g.id, g.title, 0, null, []);
                roots.push(gn);
                into = gn.children; depth = 1; holders = [gn.key];
            }
            g.sections.forEach(function (s) {
                var sn = node("s:" + g.id + "/" + s.slug, s.title, depth, null, holders);
                into.push(sn);
                s.workspaces.forEach(function (w) { sn.children.push(node("k:" + w.kind, w.title, depth + 1, w.kind, holders.concat([sn.key]))); });
            });
        });
        byKey.forEach(function (n, key) { if (n.children.length && !known.has(key)) open.add(key); });
        this._roots = roots;
        this._byKey = byKey;
    }

    /** What the tree presents now: every node, those under a folded one left out. */
    _places() {
        var out = [], open = this._open;
        (function place(nodes) {
            nodes.forEach(function (n) {
                out.push({ key: n.key, depth: n.depth, fold: n.children.length ? (open.has(n.key) ? "open" : "closed") : "leaf" });
                if (open.has(n.key)) place(n.children);
            });
        })(this._roots);
        return out;
    }

    _cellFor(key) {
        var n = this._byKey.get(key);
        if (!n) throw new Error("[WorkspaceKinds] no such node: " + key);
        var c = this._cells.get(key);
        if (!c) {
            c = new RelTreeTextCell({ branch: this._cellsBranch.createBranch("n" + (++this._cellSeq)), text: n.label });
            this._cells.set(key, c);
        }
        return c;
    }

    /** The tree's questions: a fold and an unfold, answered from the nodes; its notifications, with nothing. */
    _answer(q) {
        if (q instanceof RelTreeUnfold) { this._open.add(q.key); return Promise.resolve(new RelTreeView(this._places())); }
        if (q instanceof RelTreeFold) { this._open.delete(q.key); return Promise.resolve(new RelTreeView(this._places())); }
        return Promise.resolve();
    }

    /** The directory provided again: read, shown - a cell's label told it, a cell for a node gone disposed - and, joined with nothing chosen, its default chosen. */
    _again() {
        var self = this;
        this._read();
        this._cells.forEach(function (c, key) { var n = self._byKey.get(key); if (n && c.text() !== n.label) c.set(n.label); });
        this._tree.tell(new RelTreeViewChanged());
        this._cells.forEach(function (c, key) { if (!self._byKey.has(key)) { c.dispose(); self._cells.delete(key); } });
        this._shown();
        this._defaultIfNone();
    }

    _kindAt(key) { var n = key == null ? null : this._byKey.get(key); return n ? n.kind : null; }

    /** The kind at the cursor, told as chosen. */
    _tell(kind) {
        this._chosen = kind;
        this._choice.tell({ kind: "Choose", workspaceKind: kind });
    }

    /** Joined, and nothing chosen: the group's default is. */
    _defaultIfNone() {
        if (!this._choice || this._chosen !== null) return;
        var g = WorkspaceDirectory.group();
        if (g) this._tell(g.defaultKind);
        this._follow(this._chosen);
    }

    /** The cursor moved to the kind the party says is chosen - what holds it unfolded first, when it is folded away. */
    _follow(kind) {
        if (kind == null) return;
        this._chosen = kind;
        var key = "k:" + kind, n = this._byKey.get(key), self = this;
        if (!n || this._tree.cursor() === key) return;
        if (this._tree.selectNode(key)) return;
        var folded = n.holders.filter(function (h) { return !self._open.has(h); });
        if (!folded.length) return;
        folded.forEach(function (h) { self._open.add(h); });
        this._tree.tell(new RelTreeViewChanged());
        this._tree.selectNode(key);
    }

    /** The head: the group's title - or, of several, none - and how many kinds; a note when there is nothing. */
    _shown() {
        var groups = WorkspaceDirectory.groups(), kinds = 0;
        this._byKey.forEach(function (n) { if (n.kind) kinds++; });
        this._title.textContent = groups.length === 1 ? groups[0].title : "Workspaces";
        this._count.textContent = kinds ? kinds + (kinds === 1 ? " kind" : " kinds") : "";
        this._note.textContent = groups.length ? "" : "No kinds of workspace: the page offers none.";
        css.toggleClass(this._note, sw_hidden, groups.length > 0);
    }

    dispose() {
        this.leave();
        if (this._offDirectory) { this._offDirectory(); this._offDirectory = null; }
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._tree.destroy();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._dom.dissolve();
    }
}
