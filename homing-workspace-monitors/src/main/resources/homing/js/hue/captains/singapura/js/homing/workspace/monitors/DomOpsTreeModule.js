// =============================================================================
// DomOpsTree — the page's DomOps party as a tree, a monitor widget (Monitor):
// one row per branch, indented by its level on the page, with the elements it
// holds; a grafted party marked where it stands — the proxy's name, and the
// party's — and the parties no one has grafted named below the tree. Read from
// the party's own snapshot: plain data, nothing live. The DomOps party says
// nothing when it changes, so the monitor reads it every EVERY milliseconds
// and redraws only when what it read has changed. Its own party it names and
// does not open: the rows it draws would change what it reads.
//
//   new DomOpsTree(container, params)   params: none
//     .refresh()   read now; redrawn if it changed
// =============================================================================

const _domOpsTreeOwner = Object.freeze({ toString: () => "domOpsTree" });
var _domOpsTrees = 0;

class DomOpsTree extends Monitor {
    constructor(container, params) {
        super(container, "domOpsTree-" + (++_domOpsTrees), "DomOps party");
        var self = this;
        var tree = this._dom.createElement("tree", "div");
        css.addClass(tree, fm_tree);
        tree.setAttribute("role", "tree");
        tree.setAttribute("aria-label", "DomOps party");
        this.box.appendChild(tree);
        this._tree = tree;
        this._read = null;
        this._rows = null;
        this._timer = setInterval(function () { self.refresh(); }, DomOpsTree.EVERY);
        this.refresh();
    }

    /** Given the keys: read at once. */
    granted() { this.refresh(); }

    refresh() {
        var me = this._dom.name, page = domOpsParty.snapshot(), strays = domOpsParties.strays();
        var read = JSON.stringify([DomOpsTree._shape(page, me), strays.map(function (s) { return DomOpsTree._shape(s, me); })]);
        if (read === this._read) return this;
        this._read = read;
        if (this._rows) this._dom.dissolveBranch(this._rows.name);
        var rows = this._dom.createBranch("rows");
        rows.activate(_domOpsTreeOwner);
        this._rows = rows;
        var tree = this._tree, seq = 0;
        while (tree.firstChild) tree.removeChild(tree.firstChild);
        function span(cls, text) {
            var s = rows.createElement("s" + (++seq), "span");
            css.addClass(s, cls);
            s.textContent = text;
            return s;
        }
        (function draw(node) {
            var mine = node.mobile === me;
            var el = rows.createElement("r" + (++seq), "div");
            css.addClass(el, fm_row);
            el.setAttribute("role", "treeitem");
            el.setAttribute("aria-level", String(node.depth + 1));
            el.style.setProperty("--fm-depth", String(node.depth));
            el.appendChild(span(fm_kind, mine ? "this" : node.mobile ? "graft" : node.depth === 0 ? "party" : "branch"));
            el.appendChild(span(fm_name, node.name));
            el.appendChild(span(fm_component, DomOpsTree._detail(node, mine)));
            tree.appendChild(el);
            if (!mine) node.branches.forEach(draw);
        })(page);
        strays.forEach(function (s) {
            var out = span(fm_outside, "not grafted: " + s.name + (s.mobile === me ? " — this monitor" : ""));
            tree.appendChild(out);
        });
        return this;
    }

    /** What a row says after the name: the elements it holds, and the party a graft is of. */
    static _detail(node, mine) {
        if (mine) return "party " + node.mobile + " — this monitor, not opened";
        var n = node.elements.length, said = n + (n === 1 ? " element" : " elements");
        return node.mobile ? said + " · party " + node.mobile : said;
    }

    /** What is compared between readings: every name and count, but none inside this monitor's own party. */
    static _shape(node, me) {
        if (node.mobile === me) return [node.name, me];
        return [node.name, node.mobile, node.elements.length, node.branches.map(function (b) { return DomOpsTree._shape(b, me); })];
    }

    dispose() {
        clearInterval(this._timer);
        super.dispose();
    }
}

/** How often the party is read, in milliseconds. */
DomOpsTree.EVERY = 500;
