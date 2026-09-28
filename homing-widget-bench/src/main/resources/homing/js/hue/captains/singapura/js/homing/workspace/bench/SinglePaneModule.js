// =============================================================================
// SinglePane — a placement of one pane (RFC 0066 E3, the workspace detour: the
// widget apart from its tab), on a page. What it SHOWS is its headless model's
// (PanePlacement: the widgets lent, in order, and the one shown — the next
// when the one shown is taken back); this is the page's side of it. Every
// widget of the workspace is lent a slot of the pane's own when it is opened;
// the one shown has its slot in the pane and its focus root grafted under the
// pane's focus branch, and the others are kept whole, unshown: their DOM, their
// state, their parties. Hiding one takes its slot out of the pane and its focus
// root off, and nothing else.
//
// It is the placement the headless core (WorkspaceCore) knows only as a port:
// it lends a container and takes it back, and never makes or disposes a widget.
//
//   new SinglePane(branch, { host, focus, entry })
//     branch  its own, unactivated: each slot minted on a sub-branch of it
//     host    the pane: the element the shown widget's slot goes in
//     focus   the focus branch the pane holds a branch of its own under: the shown
//             widget's focus root is grafted there
//     entry   (id) → the workspace's entry of that id: the widget whose focus root is grafted
//   pane.model           its PanePlacement: what it shows, and says, headless
//   pane.lend(entry) → the widget's slot, made and not shown     (the core's port)
//   pane.release(entry)  taken back: the next shown when it was the one shown   (the core's port)
//   pane.show(entry | null)  that widget shown, the one shown before hidden; null, none
//   pane.shown() → the id of the widget shown, or null
//   pane.activate()      the shown widget asked for the keys
// =============================================================================

const _singlePaneOwner = Object.freeze({ toString: () => "singlePane" });

class SinglePane {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[SinglePane] a branch of its own is required");
        if (!o.host || !o.focus || typeof o.entry !== "function") throw new Error("[SinglePane] opts.host, opts.focus and opts.entry are required");
        branch.activate(_singlePaneOwner);
        this.branch = branch;
        this._host = o.host;
        this._entry = o.entry;
        this._focus = o.focus.createBranch("pane", this);   // the pane holds a branch: the shown widget's focus root is grafted there
        this.focus = this._focus.owner;
        this._slots = new Map();   // a widget's id → { branch, slot }
        this._inView = null;       // the id whose slot is in the pane
        this._n = 0;
        this.model = new PanePlacement();
        this.model.on(function (n) { self._follow(n.id); });
    }

    lend(entry) {
        var b = this.branch.createBranch("slot-" + (++this._n));
        b.activate(_singlePaneOwner);
        var slot = b.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        this._slots.set(entry.id, { branch: b, slot: slot });
        this.model.lend(entry.id);
        return slot;
    }

    release(entry) {
        var s = this._slots.get(entry.id);
        if (!s) return;
        this.model.release(entry.id);   // the next shown first, when it was this one
        this._slots.delete(entry.id);
        this.branch.dissolveBranch(s.branch.name);
    }

    show(entry) { this.model.show(entry ? entry.id : null); }

    shown() { return this.model.shown(); }

    /** Asked for the keys: the shown widget is asked. A container never claims for what it holds. */
    activate() {
        var e = this._inView && this._entry(this._inView), w = e && e.widget;
        if (w && typeof w.activate === "function") w.activate();
    }

    /** The page follows the model: the one in view hidden, the one shown now put in view. */
    _follow(id) {
        if (this._inView) this._hide();
        if (id === null) return;
        var s = this._slots.get(id);
        if (!s) throw new Error("[SinglePane] no slot lent to '" + id + "'");
        this._host.appendChild(s.slot);
        var e = this._entry(id), roots = e && e.widget && e.widget.roots;
        if (roots && roots.focus) this._focus.graft("shown", roots.focus);
        this._inView = id;
    }

    /** The one in view, hidden: its slot out of the pane, its focus root off - a widget closed has taken it off already. */
    _hide() {
        var s = this._slots.get(this._inView);
        if (s && s.slot.parentNode === this._host) this._host.removeChild(s.slot);
        var grafted = this._focus.members.some(function (m) { return m.kind === "proxy" && m.name === "shown"; });
        if (grafted) this._focus.detach("shown");
        this._inView = null;
    }
}
