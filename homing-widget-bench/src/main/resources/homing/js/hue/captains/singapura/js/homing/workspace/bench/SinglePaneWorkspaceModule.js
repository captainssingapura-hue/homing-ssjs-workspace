// =============================================================================
// SinglePaneWorkspace — a workspace of one pane (RFC 0066 E3, the workspace
// detour), self-contained as a widget is. Its DomOps and focus parties are its
// own, everything built under them while they are strays, offered as roots for
// the page to graft when it mounts it. What it is comes from its manifest,
// generated from its declaration in Java (WorkspaceDeclaration): the kinds that
// can be opened, each with the types it is given; and its ROOT PARTIES, made
// here, with it, before any widget opens (WorkspaceParties).
//
// Three roles (requests): its headless CORE (WorkspaceCore) keeps the widgets
// and their panes' life — its register of panes, PaneSlots — and EXECUTES what
// a user asks: an open is create, then mount; a close is unmount, then close.
// Its PLACEMENT, a single pane (SinglePane, over the headless PanePlacement),
// mounts and unmounts, one widget shown at a time. Its CONTROLS only ask: the
// pane's picker, Close, the list of what can be shown.
//
// It comes back as its log folds — ROSTER FIRST, every widget created again
// under its id, then mounted in the pane and the one it showed shown — and is
// recorded after, each layer beside what it records (RosterLayer, PaneLayer):
// nothing of the coming back is logged.
//
//   new SinglePaneWorkspace(container, { manifest, name? })
//     manifest  { name, kinds: { [kind]: { Widget, title, parties } }, parties: [{ type, secretary }] }
//   ws.roots    { dom, focus }: its own, for the page to graft
//   ws.core  ws.parties  ws.pane  ws.slots
//   ws.request(request) → what the core gives, or null when it could not be done (said)
//   ws.open(kind, params?, location?) → the entry — asked as a request; location "shown" unless said
//   ws.close(id)   ws.rename(id, name | null)   asked as requests; a blank name takes the name back
//   ws.show(id | null)   the pane's own        ws.titleOf(entry) → its title now: its name, else its identity's
//   ws.restore(state) → { opened, skipped, shown }   state: a WorkspaceState, as its log folds
//   ws.record(log) → off   its roster and its pane recorded: log.append(event)
// =============================================================================

const _spwOwner = Object.freeze({ toString: () => "singlePaneWorkspace" });
var _spWorkspaces = 0;

class SinglePaneWorkspace {
    constructor(container, opts) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[SinglePaneWorkspace] a container is required");
        var o = opts || {}, self = this, name = o.name || "workspace-" + (++_spWorkspaces);
        if (!o.manifest || !o.manifest.kinds) throw new Error("[SinglePaneWorkspace] opts.manifest is required: the workspace's, generated from its declaration");
        this._kinds = o.manifest.kinds;
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
        // THE REGISTER OF PANES, THE PLACEMENT, THE CORE, THE PARTIES
        this.slots = new PaneSlots(this._dom.createBranch("panes"), { title: function (entry) { return self.titleOf(entry); } });
        this.pane = new SinglePane(this._dom.createBranch("placement"), { host: paneBox, focus: this._focusParty.root, panes: this.slots,
                                                                         entry: function (id) { return self.core.entry(id); } });
        this.core = new WorkspaceCore({ kinds: this._kinds, panes: this.slots, placement: this.pane });
        this.parties = new WorkspaceParties(this.core, { parties: o.manifest.parties || [], kinds: this._kinds });
        this.core.on(function (n) { self._heard(n); });
        this.pane.model.on(function () { self._showing.value = self.pane.shown() || ""; });
        this._refill();
    }

    /** Come back to what its log folds to: the roster first, then the pane. Not recorded. */
    restore(state) {
        var back = RosterLayer.restore(this.core, state.roster);
        var shown = PaneLayer.restore(this.pane.model, this.core, state.pane);
        return Object.freeze({ opened: back.opened, skipped: back.skipped, shown: shown });
    }

    /** Recorded from now on: the roster's word and the pane's, each by its own layer. */
    record(log) {
        var offs = [RosterLayer.record(this.core, log), PaneLayer.record(this.pane.model, log)];
        return function () { offs.forEach(function (off) { off(); }); };
    }

    request(r) {
        try { return this.core.execute(r); }
        catch (e) { console.error("[SinglePaneWorkspace] " + r.kind + " was not done: " + e.message); return null; }
    }

    open(kind, params, location) { return this.request(WorkspaceRequest.open(kind, params || {}, location || "shown")); }

    close(id) { return this.request(WorkspaceRequest.close(id)); }

    show(id) { this.pane.show(id == null ? null : id); }

    rename(id, name) { return this.request(WorkspaceRequest.rename(id, name && name.trim() ? name.trim() : null)); }

    /** A widget's title now, by the rule: the name a user gave it, else as it opened, by its identity. */
    titleOf(entry) {
        var kind = this._kinds[entry.kind];
        return KindAndParamsTitle.INSTANCE.titleOf(kind && kind.title ? kind.title : entry.kind, entry.id, entry.name);
    }

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
        } else if (n.kind === "WidgetRenamed") {
            this._refill();
        } else if (n.kind === "WidgetClosed") {
            var name = this._grafts.get(n.id);
            this._grafts.delete(n.id);
            // a widget disposed has dissolved its party, and the graft with it; one that did not is taken off
            if (name && this._widgets.listBranches().indexOf(name) >= 0) this._widgets.detach(name);
            this._refill();
        }
    }

    /** What can be shown, as the roster has it: its options made afresh on a branch of their own, the one shown kept. */
    _refill() {
        if (this._options) this._dom.dissolveBranch(this._options.name);
        var b = this._dom.createBranch("options-" + (++this._refills)), sel = this._showing, shown = this.pane ? this.pane.shown() : null;
        b.activate(_spwOwner);
        this._options = b;
        sel.appendChild(SinglePaneWorkspace._option(b, "none", "", "(none shown)"));
        var self = this;
        this.core.entries().forEach(function (e, i) { sel.appendChild(SinglePaneWorkspace._option(b, "o" + i, e.id, self.titleOf(e))); });
        sel.value = shown || "";
    }

    /** The bar: Open…, the pane's picker; what is shown; a name for it and Rename; and Close - each only asks. */
    _bar() {
        var self = this, d = this._dom, bar = d.createElement("bar", "div");
        css.addClass(bar, wb_ws_bar);
        var kinds = Object.keys(this._kinds).map(function (k) { return { id: k, title: self._kinds[k].title || k }; });
        this._showing = d.createElement("showing", "select");
        css.addClass(this._showing, wb_sim_pick);
        this._showing.setAttribute("aria-label", "The widget shown");
        this._showing.addEventListener("change", function () { self.show(self._showing.value || null); });
        this._options = null;
        this._refills = 0;
        var b = new ButtonBuilder(), c = new ButtonBuilder(), r = new ButtonBuilder();
        this._name = d.createElement("name", "input");
        this._name.type = "text";
        this._name.placeholder = "A name";
        this._name.setAttribute("aria-label", "A name for the widget shown - none takes its name back");
        css.addClass(this._name, wb_ws_name);
        var rename = r.label("Rename").plain().size(-1).onClick(function () { var id = self.pane.shown(); if (id) self.rename(id, self._name.value); self._name.value = ""; })
                      .build(d.createElement("rename", r.tag));
        var open = b.label("Open…").plain().size(-1).onClick(function () { self.pane.pick(kinds, function (kind) { self.open(kind, {}, "shown"); }); })
                    .build(d.createElement("open", b.tag));
        var close = c.label("Close").plain().size(-1).onClick(function () { var id = self.pane.shown(); if (id) self.close(id); }).build(d.createElement("close", c.tag));
        [open.el, this._label("showing-label", "Showing"), this._showing, this._name, rename.el, close.el].forEach(function (el) { bar.appendChild(el); });
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
        this.core.dispose();
        this.parties.dispose();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
