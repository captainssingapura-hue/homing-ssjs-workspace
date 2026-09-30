// =============================================================================
// TreeLayout — the tree placement's engine (engine "tree"): a tree's
// arrangement laid out as a reading flow. Each node is a section: its own part
// - its heading, drawn from its label, then its nameless leaves, in order,
// each a box lent to a widget made from its type and its params and nothing
// else - then its named children, nested. A widget never learns where it sits.
//
// A section below the root is indented under its parent, a hairline down its
// left the length of the section, so the tree's depth reads as the contents'
// does. A section folds: its heading stays, its leaves and children go, and
// nothing in it is where the reader is until it unfolds. Folding is view
// state, as where the reader is; what folds is the host's to say - its
// contents', as a rule. The current section - where the reader is - is marked
// on its own part, in the design's current surface: its heading and leaves,
// not its children, as the contents mark its row and not the rows below.
//
// Its DomOps party is the tree: the root's section is the party's root, and
// every other section a branch of its parent's - the branch holding its
// heading, its leaves' boxes and the widgets in them, and its child sections'
// branches - so a section is one branch, whole, and a node at depth
// d is a branch at depth d. A widget made here is hosted as any host hosts
// one: its DomOps party grafted under its section's branch, a level below it
// (why the placement's deepest nodes hold no leaves), its focus party under the
// layout's, and - when the host gave parties - joined to them. A widget that
// makes widgets of its own - a flow - is offered the kinds the layout was
// offered (compose), before it joins. A widget whose class says
// SIZING = "flow" is lent a box as tall as its content; any other fills what it
// is lent, and is given a height the reader may drag. A type the host does not
// offer is said, in its box and on the console.
//
// It keeps where the reader is: the last shown section whose heading has
// reached the top of the view, reported when it changes; and it shows a
// section when asked - the folded sections above it unfolded first - which is
// then where the reader is while its heading is in view, so a short section at
// the end, which can never reach the top, is still where a reader who asked
// for it is. The placement itself never changes.
//
// The arrangement is generated in Java from a TreePlacement (TreeArrangementJs):
//   { engine: "tree", workspace, widgets: { [ref]: { kind, params } },
//     root: { name, label: { text, runs: [{ kind, text }] }, leaves: [ref], children: [node] } }
//
//   new TreeLayout(container, { arrangement, kinds, given?, onShown? })
//     kinds    { [type]: WidgetClass } - the types the host offers
//     given    { [party type name]: party } - joined by every widget that joins
//     onShown  function (path) - the section in view changed
//   layout.root  layout.roots   { dom, focus }
//   layout.show(path) → true when the tree has it   layout.shown() → the path in view
//   layout.fold(path, folded) → true when the tree has it   layout.folded(path)
//   layout.paths() → every node's path, in reading order   layout.widget(ref) → the widget, or null
//   layout.dispose()
// =============================================================================

const _treeLayoutOwner = Object.freeze({ toString: () => "treeLayout" });
var _treeLayouts = 0;
var _RUN_TAGS = Object.freeze({ text: "span", code: "code", strong: "strong", emphasis: "em" });

class TreeLayout {

    /** The engine an arrangement it lays out is for. */
    static ENGINE = "tree";

    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[TreeLayout] a container is required: the one its host lends it");
        var o = opts || {}, a = o.arrangement;
        if (!a || a.engine !== TreeLayout.ENGINE || !a.root) throw new Error("[TreeLayout] a tree's arrangement is required, not one for " + JSON.stringify(a && a.engine));
        this._arrangement = a;
        this._kinds = o.kinds || {};
        this._given = o.given || null;
        this._onShown = typeof o.onShown === "function" ? o.onShown : function () {};
        var name = "treeLayout-" + (++_treeLayouts);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_treeLayoutOwner);
        this._focusParty = focusParties.mobile(name);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        var scroll = this._dom.createElement("scroll", "div");
        css.addClass(scroll, wg_scroll);
        var column = this._dom.createElement("column", "div");
        css.addClass(column, tl_column);
        scroll.appendChild(column);
        root.appendChild(scroll);
        this.root = root;
        this._scroll = scroll;
        this._sections = [];          // { path, mark } in reading order: the element whose top says where the section starts
        this._byPath = new Map();     // path → { mark, own, folds }: the section's start, its own part, what folds away
        this._folded = new Set();
        this._placed = new Map();     // ref → the widget made for it
        this._nodes = 0;
        this._widgets = 0;
        this._shown = null;
        this._pinned = null;
        this._section(a.root, "", 0, column, null);
        container.appendChild(root);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        var self = this;
        this._onScroll = function () { self._track(); };
        scroll.addEventListener("scroll", this._onScroll);
        this._track();
    }

    show(path) {
        var at = this._byPath.get(path), self = this;
        if (!at) return false;
        TreeLayout._above(path).forEach(function (p) { if (self._folded.has(p)) self.fold(p, false); });
        this._pinned = path;
        this._scroll.scrollTop += at.mark.getBoundingClientRect().top - this._scroll.getBoundingClientRect().top;
        this._track();
        return true;
    }

    shown() { return this._shown; }

    fold(path, folded) {
        var at = this._byPath.get(path);
        if (!at) return false;
        if (folded) this._folded.add(path); else this._folded.delete(path);
        at.folds.forEach(function (el) { css.toggleClass(el, tl_hidden, !!folded); });
        this._track();
        return true;
    }

    folded(path) { return this._folded.has(path); }

    paths() { return this._sections.map(function (s) { return s.path; }); }

    widget(ref) { return this._placed.get(ref) || null; }

    dispose() {
        this._scroll.removeEventListener("scroll", this._onScroll);
        this._placed.forEach(function (w) { if (typeof w.dispose === "function") w.dispose(); });
        this._placed.clear();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    /** The paths above a path, the root's first: "a/b/c" → "", "a", "a/b". */
    static _above(path) {
        if (path === "") return [];
        var out = [""], parts = path.split("/");
        for (var i = 1; i < parts.length; i++) out.push(parts.slice(0, i).join("/"));
        return out;
    }

    /**
     * A node: its section - indented below the root - its own part (its heading, then its leaves), then its children;
     * on the party's root for the root, else on a branch of its parent's.
     */
    _section(node, path, depth, parent, above) {
        var branch = above ? above.createBranch("n" + (++this._nodes)) : this._dom, self = this;
        if (above) branch.activate(_treeLayoutOwner);
        var section = branch.createElement("section", "section");
        css.addClass(section, tl_section);
        if (depth > 0) css.addClass(section, tl_nested);
        var own = branch.createElement("own", "div");
        css.addClass(own, tl_own);
        section.appendChild(own);
        var mark = own, folds = [];
        if (node.label && node.label.text) {
            mark = branch.createElement("heading", "h" + Math.min(depth + 1, 6));
            css.addClass(mark, depth < 2 ? tl_heading : tl_subheading);
            this._label(branch, mark, node.label);
            own.appendChild(mark);
        }
        if (node.leaves.length) {
            var leaves = branch.createElement("leaves", "div");
            css.addClass(leaves, tl_body);
            node.leaves.forEach(function (ref, i) { leaves.appendChild(self._leaf(branch, ref, i)); });
            own.appendChild(leaves);
            folds.push(leaves);
        }
        this._sections.push({ path: path, mark: mark });
        this._byPath.set(path, { mark: mark, own: own, folds: folds });
        if (node.children.length) {
            var children = branch.createElement("children", "div");
            css.addClass(children, tl_body);
            section.appendChild(children);
            folds.push(children);
            node.children.forEach(function (child) { self._section(child, path === "" ? child.name : path + "/" + child.name, depth + 1, children, branch); });
        }
        parent.appendChild(section);
    }

    /** A label: its text, or its runs - code as code, strong as strong. */
    _label(branch, heading, label) {
        if (!label.runs || !label.runs.length) { heading.textContent = label.text; return; }
        label.runs.forEach(function (run, i) {
            var el = branch.createElement("run" + i, _RUN_TAGS[run.kind] || "span");
            if (run.kind === "code") css.addClass(el, tl_code);
            el.textContent = run.text;
            heading.appendChild(el);
        });
    }

    /** A nameless leaf: a box, and in it the widget made from its type and params. */
    _leaf(branch, ref, i) {
        var box = branch.createElement("leaf" + i, "div");
        css.addClass(box, tl_leaf);
        var spec = this._arrangement.widgets[ref], Kind = spec ? this._kinds[spec.kind] : null;
        if (!Kind) {
            var note = branch.createElement("missing" + i, "p");
            css.addClass(note, tl_missing);
            note.textContent = spec ? "No widget of the type " + spec.kind + " here." : "No widget named " + ref + " in the arrangement.";
            box.appendChild(note);
            console.error("[TreeLayout] " + note.textContent);
            return box;
        }
        if (Kind.SIZING !== "flow") css.addClass(box, tl_leaf_fill);
        var widget = new Kind(box, spec.params), slot = "w" + (this._widgets++);
        if (widget.roots && widget.roots.dom) branch.graft(slot, widget.roots.dom);
        if (widget.roots && widget.roots.focus) this._focusParty.root.graft(slot, widget.roots.focus);
        if (typeof widget.compose === "function") widget.compose(this._kinds);
        if (this._given && typeof widget.join === "function") widget.join(this._given);
        this._placed.set(ref, widget);
        return box;
    }

    /** Where the reader is: the section asked for while it is in view, else the last shown one whose start has reached the top - marked, and told. */
    _track() {
        var box = this._scroll.getBoundingClientRect(), at = this._sections.length ? this._sections[0].path : null;
        if (this._pinned !== null) {
            var pinned = this._byPath.get(this._pinned).mark, top = pinned.getBoundingClientRect().top;
            if (pinned.getClientRects().length && top >= box.top - 1 && top < box.bottom) at = this._pinned;
            else this._pinned = null;
        }
        if (this._pinned === null) {
            for (var i = 0; i < this._sections.length; i++) {
                var mark = this._sections[i].mark;
                if (!mark.getClientRects().length) continue;   // folded away: not where anyone is
                if (mark.getBoundingClientRect().top <= box.top + 8) at = this._sections[i].path; else break;
            }
        }
        if (at === this._shown) return;
        if (this._shown !== null) css.toggleClass(this._byPath.get(this._shown).own, tl_current, false);
        this._shown = at;
        if (at !== null) css.toggleClass(this._byPath.get(at).own, tl_current, true);
        this._onShown(at);
    }
}
