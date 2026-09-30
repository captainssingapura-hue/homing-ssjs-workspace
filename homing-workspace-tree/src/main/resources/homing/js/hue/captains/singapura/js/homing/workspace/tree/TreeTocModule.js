// =============================================================================
// TreeToc — a tree placement's table of contents: its nodes as a relation
// tree, a row each, showing the node's label - the root first, and every node
// with children open from the start. It reads the arrangement it is given and
// never the layout: the two meet through whoever hosts them. It says which
// section the reader picked; it follows the section in view.
//
// A self-contained widget: its DomOps and focus parties its own, offered as
// roots for its host to graft; the relation tree's rows natively focused
// inside it.
//
// The contents are how the reader goes about what they are of, so on the
// focus side they HOLD it: the host grafts the view's focus party under the
// contents (graft), and the widgets in it that take the keys - a grid, a
// diagram - take them locally, and give them back to the contents: keys given
// up below the contents come back to the contents, and into their tree.
//
// The cursor is the pick. A move onto a row - a press, the arrow keys - is
// told (onPick); Enter or a double press tells it too, where the cursor
// already is. Following the reader (follow) moves the cursor there, the
// sections that hold it unfolded, and tells no one: the reader's scrolling is
// never pulled back to a heading by its own reflection. A fold or an unfold -
// Left, Right, Space, a press on the caret - is told (onFold), for the host to
// fold the same section in the view the contents are of.
//
// The keys: the tree's - Up, Down, Home, End, PageUp, PageDown walk it; Left
// and Right fold and unfold; Space toggles. Escape the tree did not take
// gives the keys back.
//
//   new TreeToc(container, { arrangement, label? })   arrangement: TreeArrangementJs's
//   toc.root  toc.roots   { dom, focus }
//   toc.onPick(fn)   fn(path) - a section picked
//   toc.onFold(fn)   fn(path, open) - a section folded, or unfolded
//   toc.graft(name, focusParty) → the proxy: a mobile focus party held under the contents
//   toc.follow(path) the cursor to a section, telling no one
//   toc.picked() → the path the cursor is on, or null
//   toc.activate()   toc.dispose()
// =============================================================================

const _treeTocOwner = Object.freeze({ toString: () => "treeToc" });
var _treeTocs = 0;

class TreeToc {
    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[TreeToc] a container is required: the one its host lends it");
        var o = opts || {}, a = o.arrangement, self = this;
        if (!a || a.engine !== "tree" || !a.root) throw new Error("[TreeToc] a tree's arrangement is required, not one for " + JSON.stringify(a && a.engine));
        var name = "treeToc-" + (++_treeTocs);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_treeTocOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        css.addClass(root, tl_toc);
        root.setAttribute("role", "navigation");
        root.setAttribute("aria-label", o.label || "Contents");
        var box = this._dom.createElement("box", "div");
        css.addClass(box, wg_scroll);
        root.appendChild(box);
        container.appendChild(root);
        this.root = root;
        // the tree read into nodes, each keyed "/" + its path, so the root's key is never empty
        this._cellsBranch = this._dom.createBranch("cells");
        this._cellsBranch.activate(_treeTocOwner);
        this._cells = new Map();
        this._cellSeq = 0;
        this._nodes = new Map();      // key → { key, label, depth, children: [key], holders: [key] }
        this._roots = [];
        this._open = new Set();
        this._read(a.root, "/", 0, [], this._roots);
        this._onPick = null;
        this._onFold = null;
        this._quiet = null;
        this._tree = new RelTree({
            container: box, branch: this._dom.createBranch("tree"), label: o.label || "Contents", folder: true,
            relation: { view: function () { return self._places(); }, cellFor: function (key) { return self._cellFor(key); } },
            ask: function (q) { return self._answer(q); },
            onCursorMoved: function (key) { if (key !== self._quiet) self._tell(key); },
            onActivated: function (key) { self._tell(key); }
        });
        this._focusParty = focusParties.mobile(name);
        this._under = this._focusParty.root.createBranch("toc", this);
        this.focus = this._under.owner;
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
    }

    onPick(fn) { this._onPick = typeof fn === "function" ? fn : null; }

    onFold(fn) { this._onFold = typeof fn === "function" ? fn : null; }

    /** A mobile focus party held under the contents - what they are of: the keys its members give up come back here. */
    graft(name, party) { return this._under.graft(name, party); }

    /** The cursor to a section - the sections holding it unfolded first - telling no one. */
    follow(path) {
        var key = TreeToc._key(path), n = this._nodes.get(key), self = this;
        if (!n || this._tree.cursor() === key) return;
        var folded = n.holders.filter(function (h) { return !self._open.has(h); });
        if (folded.length) {
            folded.forEach(function (h) { self._open.add(h); });
            this._tree.tell(new RelTreeViewChanged());
        }
        this._quiet = key;
        try { this._tree.selectNode(key); } finally { this._quiet = null; }
    }

    picked() { var key = this._tree.cursor(); return key ? TreeToc._path(key) : null; }

    /** Asked for the keys: they are claimed, and go on into the tree. */
    activate() { Keys.claim(this.focus); }

    /** Given the keys: into the tree - unless the browser's focus arriving in a row is what gave them. */
    granted(by) { if (by !== "native") this._tree.focus(); }

    /** Keys given up below the contents come back to them. */
    wouldHold() { return true; }

    /** Escape the tree did not take gives the keys back. */
    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    dispose() {
        if (this._off) { this._off(); this._off = null; }
        this._tree.destroy();
        this._cells.forEach(function (c) { c.dispose(); });
        this._cells.clear();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    static _key(path) { return "/" + path; }

    static _path(key) { return key.slice(1); }

    _tell(key) { if (this._onPick && this._nodes.has(key)) this._onPick(TreeToc._path(key)); }

    _folding(key, open) { if (this._onFold && this._nodes.has(key)) this._onFold(TreeToc._path(key), open); }

    /** A node and those under it, each with what holds it; a node with children open from the start. */
    _read(node, key, depth, holders, into) {
        var n = { key: key, label: node.label.text, depth: depth, children: [], holders: holders }, self = this;
        this._nodes.set(key, n);
        into.push(key);
        node.children.forEach(function (c) {
            self._read(c, key === "/" ? "/" + c.name : key + "/" + c.name, depth + 1, holders.concat([key]), n.children);
        });
        if (n.children.length) this._open.add(key);
    }

    /** What the tree presents now: every node, those under a folded one left out. */
    _places() {
        var out = [], open = this._open, nodes = this._nodes;
        (function place(keys) {
            keys.forEach(function (key) {
                var n = nodes.get(key);
                out.push({ key: key, depth: n.depth, fold: n.children.length ? (open.has(key) ? "open" : "closed") : "leaf" });
                if (open.has(key)) place(n.children);
            });
        })(this._roots);
        return out;
    }

    _cellFor(key) {
        var n = this._nodes.get(key);
        if (!n) throw new Error("[TreeToc] no such node: " + key);
        var c = this._cells.get(key);
        if (!c) {
            c = new RelTreeTextCell({ branch: this._cellsBranch.createBranch("n" + (++this._cellSeq)), text: n.label });
            this._cells.set(key, c);
        }
        return c;
    }

    /** The tree's questions: a fold and an unfold, answered from the nodes and told; anything else, with nothing. */
    _answer(q) {
        if (q instanceof RelTreeUnfold) { this._open.add(q.key); this._folding(q.key, true); return Promise.resolve(new RelTreeView(this._places())); }
        if (q instanceof RelTreeFold) { this._open.delete(q.key); this._folding(q.key, false); return Promise.resolve(new RelTreeView(this._places())); }
        return Promise.resolve();
    }
}
