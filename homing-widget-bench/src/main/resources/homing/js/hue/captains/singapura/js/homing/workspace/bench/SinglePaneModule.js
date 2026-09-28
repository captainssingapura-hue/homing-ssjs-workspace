// =============================================================================
// SinglePane — a placement of one pane (RFC 0066 E3, the workspace detour: the
// widget apart from its tab). Every widget of the workspace is lent a slot of
// the pane's own when it is opened; ONE is shown at a time — its slot in the
// pane, its focus root grafted under the pane's focus branch — and the others
// are kept whole, unshown: their DOM, their state, their parties. Hiding one
// takes its slot out of the pane and its focus root off, and nothing else.
//
// It is the placement the headless core (WorkspaceCore) knows only as a port:
// it lends a container and takes it back, and never makes or disposes a widget.
//
//   new SinglePane(branch, { host, focus })
//     branch  its own, unactivated: each slot minted on a sub-branch of it
//     host    the pane: the element the shown widget's slot goes in
//     focus   the focus branch the pane holds a branch of its own under: the shown
//             widget's focus root is grafted there
//   pane.lend(entry) → the widget's slot, made and not shown     (the core's port)
//   pane.release(entry)  hidden if shown, and its slot gone       (the core's port)
//   pane.show(entry | null)  that widget shown, the one shown before hidden; null, none
//   pane.shown() → the id of the widget shown, or null
//   pane.activate()      the shown widget asked for the keys
// =============================================================================

const _singlePaneOwner = Object.freeze({ toString: () => "singlePane" });

class SinglePane {
    constructor(branch, opts) {
        var o = opts || {};
        if (!branch) throw new Error("[SinglePane] a branch of its own is required");
        if (!o.host || !o.focus) throw new Error("[SinglePane] opts.host and opts.focus are required");
        branch.activate(_singlePaneOwner);
        this.branch = branch;
        this._host = o.host;
        this._focus = o.focus.createBranch("pane", this);   // the pane holds a branch: the shown widget's focus root is grafted there
        this.focus = this._focus.owner;
        this._slots = new Map();   // a widget's id → { branch, slot }
        this._shown = null;        // the entry shown
        this._n = 0;
    }

    lend(entry) {
        var b = this.branch.createBranch("slot-" + (++this._n));
        b.activate(_singlePaneOwner);
        var slot = b.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        this._slots.set(entry.id, { branch: b, slot: slot });
        return slot;
    }

    release(entry) {
        var s = this._slots.get(entry.id);
        if (!s) return;
        if (this._shown && this._shown.id === entry.id) this._hide();
        this._slots.delete(entry.id);
        this.branch.dissolveBranch(s.branch.name);
    }

    show(entry) {
        if (entry && this._shown && this._shown.id === entry.id) return;
        if (this._shown) this._hide();
        if (!entry) return;
        var s = this._slots.get(entry.id);
        if (!s) throw new Error("[SinglePane] no slot lent to '" + entry.id + "'");
        this._host.appendChild(s.slot);
        var roots = entry.widget && entry.widget.roots;
        if (roots && roots.focus) this._focus.graft("shown", roots.focus);
        this._shown = entry;
    }

    shown() { return this._shown ? this._shown.id : null; }

    /** Asked for the keys: the shown widget is asked. A container never claims for what it holds. */
    activate() {
        var w = this._shown && this._shown.widget;
        if (w && typeof w.activate === "function") w.activate();
    }

    /** The one shown, hidden: its slot out of the pane, its focus root off - a widget closed has taken it off already. */
    _hide() {
        var s = this._slots.get(this._shown.id);
        if (s && s.slot.parentNode === this._host) this._host.removeChild(s.slot);
        var grafted = this._focus.members.some(function (m) { return m.kind === "proxy" && m.name === "shown"; });
        if (grafted) this._focus.detach("shown");
        this._shown = null;
    }
}
