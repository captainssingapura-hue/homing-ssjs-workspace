// =============================================================================
// SinglePaneWorkspace — a workspace of one pane (RFC 0066 E3, the workspace
// detour: the next experiment), self-contained as a widget is. Its DomOps and
// focus parties are its own, everything built under them while they are
// strays, offered as roots for the page to graft when it mounts it. Its
// widgets are its headless core's (WorkspaceCore) — each opened under an id
// that says what it is, its DomOps root grafted under the workspace's
// `widgets` branch — and their messaging parties the core's too, beside it
// (WorkspaceParties). What is shown is its placement's, a single pane
// (SinglePane): a dropdown of the widgets the workspace holds, one of them
// shown at a time, the others still there, still joined, unshown.
//
// Its bar: a kind to open, and Open — the widget opened is shown; what is
// shown, picked from the widgets it holds; and Close, the one shown closed,
// the next shown in its place.
//
//   new SinglePaneWorkspace(container, { kinds, secretaries, name? })
//     kinds        { [kind]: { Widget, title? } } — what can be opened
//     secretaries  { [party type name]: secretary } — each root party's
//   ws.roots    { dom, focus }: its own, for the page to graft
//   ws.core  ws.parties  ws.pane
//   ws.open(kind, params?) → the entry, shown     ws.show(id | null)     ws.close(id)
// =============================================================================

const _spwOwner = Object.freeze({ toString: () => "singlePaneWorkspace" });
var _spWorkspaces = 0;

class SinglePaneWorkspace {
    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[SinglePaneWorkspace] a container is required");
        var o = opts || {}, self = this, name = o.name || "workspace-" + (++_spWorkspaces);
        this._kinds = o.kinds || {};
        // ITS OWN ROOTS: built under while strays, grafted when the page mounts it
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_spwOwner);
        this._focusParty = focusParties.mobile(name);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._widgets = this._dom.createBranch("widgets");
        this._widgets.activate(_spwOwner);
        this._grafts = new Map();   // a widget's id → its graft's name under `widgets`
        this._n = 0;
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wb_ws);
        root.appendChild(this._bar());
        var paneBox = this._dom.createElement("pane", "div");
        css.addClass(paneBox, wb_bench);
        root.appendChild(paneBox);
        container.appendChild(root);
        this.root = root;
        // THE PLACEMENT, THE CORE, THE PARTIES: the core knows the pane only as its port
        this.pane = new SinglePane(this._dom.createBranch("placement"), { host: paneBox, focus: this._focusParty.root });
        this.core = new WorkspaceCore({ kinds: this._kinds, placement: this.pane });
        this.parties = new WorkspaceParties(this.core, { secretaries: o.secretaries || {} });
        this.core.on(function (n) { self._heard(n); });
        this._refill();
    }

    open(kind, params) {
        var entry = this.core.open(kind, params || {});
        this.show(entry.id);
        return entry;
    }

    show(id) {
        this.pane.show(id == null ? null : this.core.entry(id));
        this._showing.value = this.pane.shown() || "";
    }

    close(id) { this.core.close(id); }

    /** The core's word: a widget's DomOps root grafted when it opens, and the bar kept as the roster is. */
    _heard(n) {
        if (n.kind === "WidgetOpened") {
            var dom = n.entry.widget && n.entry.widget.roots && n.entry.widget.roots.dom;
            if (dom instanceof MobileDomOpsParty) {
                var g = "widget-" + (++this._n);
                this._widgets.graft(g, dom);
                this._grafts.set(n.entry.id, g);
            }
            this._refill();
        } else if (n.kind === "WidgetClosed") {
            var name = this._grafts.get(n.id);
            this._grafts.delete(n.id);
            // a widget disposed has dissolved its party, and the graft with it; one that did not is taken off
            if (name && this._widgets.listBranches().indexOf(name) >= 0) this._widgets.detach(name);
            this._refill();
            if (!this.pane.shown()) { var next = this.core.entries()[0]; this.show(next ? next.id : null); }
        }
    }

    /** What can be shown, as the roster has it: its options made afresh on a branch of their own, the one shown kept. */
    _refill() {
        if (this._options) this._dom.dissolveBranch(this._options.name);
        var b = this._dom.createBranch("options-" + (++this._refills)), sel = this._showing, shown = this.pane ? this.pane.shown() : null;
        b.activate(_spwOwner);
        this._options = b;
        sel.appendChild(SinglePaneWorkspace._option(b, "none", "", "(none shown)"));
        this.core.entries().forEach(function (e, i) { sel.appendChild(SinglePaneWorkspace._option(b, "o" + i, e.id, e.id)); });
        sel.value = shown || "";
    }

    /** The bar: a kind to open and Open; what is shown; and Close. */
    _bar() {
        var self = this, d = this._dom, bar = d.createElement("bar", "div");
        css.addClass(bar, wb_ws_bar);
        var kinds = d.createElement("kinds", "select");
        css.addClass(kinds, wb_sim_pick);
        kinds.setAttribute("aria-label", "A kind of widget to open");
        Object.keys(this._kinds).forEach(function (k, i) { kinds.appendChild(SinglePaneWorkspace._option(d, "kind-" + i, k, self._kinds[k].title || k)); });
        this._showing = d.createElement("showing", "select");
        css.addClass(this._showing, wb_sim_pick);
        this._showing.setAttribute("aria-label", "The widget shown");
        this._showing.addEventListener("change", function () { self.show(self._showing.value || null); });
        this._options = null;
        this._refills = 0;
        var b = new ButtonBuilder(), c = new ButtonBuilder();
        var open = b.label("Open").plain().size(-1).onClick(function () { if (kinds.value) self.open(kinds.value); }).build(d.createElement("open", b.tag));
        var close = c.label("Close").plain().size(-1).onClick(function () { var id = self.pane.shown(); if (id) self.close(id); }).build(d.createElement("close", c.tag));
        [this._label("open-label", "Open a"), kinds, open.el, this._label("showing-label", "Showing"), this._showing, close.el].forEach(function (el) { bar.appendChild(el); });
        return bar;
    }

    static _option(branch, name, value, text) {
        var op = branch.createElement(name, "option");
        op.value = value;
        op.textContent = text;
        return op;
    }

    _label(name, text) {
        var l = this._dom.createElement(name, "span");
        css.addClass(l, wb_ws_label);
        l.textContent = text;
        return l;
    }

    dispose() {
        this.core.entries().slice().reverse().forEach(function (e) { this.core.close(e.id); }, this);
        this.parties.dispose();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
