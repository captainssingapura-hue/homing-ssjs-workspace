// =============================================================================
// TreeLayout — the tree placement's engine (engine "tree"): a tree's
// arrangement laid out as a reading flow. Each node is a section under its
// heading, drawn from its label; then its nameless leaves, in order, each a box
// lent to a widget made from its type and its params and nothing else; then its
// named children, nested. A widget never learns where it sits.
//
// A widget made here is hosted as any host hosts one: its DomOps party grafted
// under its node's branch, its focus party under the layout's, and - when the
// host gave parties - joined to them. A widget whose class says
// SIZING = "flow" is lent a box as tall as its content; any other fills what it
// is lent, and is given a height the reader may drag. A type the host does not
// offer is said, in its box and on the console.
//
// It keeps where the reader is: the last section whose heading has reached the
// top of the view, reported when it changes; and it shows a section when
// asked, which is then where the reader is while its heading is in view - so a
// short section at the end, which can never reach the top, is still where a
// reader who asked for it is. The placement itself never changes.
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
//   layout.show(path) → true when the tree has the path   layout.shown() → the path in view
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
        this._byPath = new Map();     // path → its mark
        this._placed = new Map();     // ref → the widget made for it
        this._nodes = 0;
        this._widgets = 0;
        this._shown = null;
        this._pinned = null;
        this._section(a.root, "", 0, column);
        container.appendChild(root);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        var self = this;
        this._onScroll = function () { self._track(); };
        scroll.addEventListener("scroll", this._onScroll);
        this._track();
    }

    show(path) {
        var mark = this._byPath.get(path);
        if (!mark) return false;
        this._pinned = path;
        this._scroll.scrollTop += mark.getBoundingClientRect().top - this._scroll.getBoundingClientRect().top;
        this._track();
        return true;
    }

    shown() { return this._shown; }

    paths() { return this._sections.map(function (s) { return s.path; }); }

    widget(ref) { return this._placed.get(ref) || null; }

    dispose() {
        this._scroll.removeEventListener("scroll", this._onScroll);
        this._placed.forEach(function (w) { if (typeof w.dispose === "function") w.dispose(); });
        this._placed.clear();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }

    /** A node: its section, its heading, its leaves, then its children - each under its own branch. */
    _section(node, path, depth, parent) {
        var branch = this._dom.createBranch("n" + (this._nodes++)), self = this;
        branch.activate(_treeLayoutOwner);
        var section = branch.createElement("section", "section");
        css.addClass(section, tl_section);
        var mark = section;
        if (node.label && node.label.text) {
            mark = branch.createElement("heading", "h" + Math.min(depth + 1, 6));
            css.addClass(mark, depth < 2 ? tl_heading : tl_subheading);
            this._label(branch, mark, node.label);
            section.appendChild(mark);
        }
        this._sections.push({ path: path, mark: mark });
        this._byPath.set(path, mark);
        node.leaves.forEach(function (ref, i) { section.appendChild(self._leaf(branch, ref, i)); });
        node.children.forEach(function (child) { self._section(child, path === "" ? child.name : path + "/" + child.name, depth + 1, section); });
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
        if (this._given && typeof widget.join === "function") widget.join(this._given);
        this._placed.set(ref, widget);
        return box;
    }

    /** Where the reader is: the section asked for while it is in view, else the last whose start has reached the top. */
    _track() {
        var box = this._scroll.getBoundingClientRect(), at = this._sections.length ? this._sections[0].path : null;
        if (this._pinned !== null) {
            var top = this._byPath.get(this._pinned).getBoundingClientRect().top;
            if (top >= box.top - 1 && top < box.bottom) at = this._pinned;
            else this._pinned = null;
        }
        if (this._pinned === null) {
            for (var i = 0; i < this._sections.length; i++) {
                if (this._sections[i].mark.getBoundingClientRect().top <= box.top + 8) at = this._sections[i].path; else break;
            }
        }
        if (at !== this._shown) { this._shown = at; this._onShown(at); }
    }
}
