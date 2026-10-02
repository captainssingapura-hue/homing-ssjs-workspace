// =============================================================================
// SinglePane — a placement of one pane, on a page (RFC 0066 E3, the workspace
// detour: requests). It mounts and unmounts widgets' panes, and never creates
// or closes one: a widget's pane is the register's (PaneSlots), lent and
// closed with the widget by the core. What it SHOWS is its headless model's
// (PanePlacement: the widgets mounted, in order, and the one shown — the next
// when the one shown is unmounted); this is the page's side of it. The pane
// shown has its slot in the host and its widget's focus root grafted under the
// pane's focus branch; the others are kept whole, unshown: their DOM, their
// state, their parties.
//
// It OWNS a transient picker pane (PanePicker): asked to pick, it shows the
// picker in place of what it shows. The pick is the asker's, to turn into a
// request; showing anything lets the picker go, and a cancel brings back what
// was shown.
//
//   new SinglePane(branch, { host, focus, panes, entry })
//     branch  its own, unactivated
//     host    the pane: the element the shown widget's slot goes in
//     focus   the focus branch the pane holds a branch of its own under
//     panes   the register of panes: panes.slot(id) → the widget's slot
//     entry   (id) → the workspace's entry of that id: the widget whose focus root is grafted
//   pane.model   its PanePlacement: what it shows, and says, headless
//   pane.mount(entry, location)   location "shown" or "behind"   (the core's placement port)
//   pane.unmount(entry)                                         (the core's placement port)
//   pane.show(id | null)   the pane's own request     pane.shown() → the id shown, or null
//   pane.pick(kinds, onPick)   the picker shown: kinds [{ id, title }]; onPick(kindId) asked
//   pane.activate()        the shown widget asked for the keys
// =============================================================================

const _singlePaneOwner = Object.freeze({ toString: () => "singlePane" });

class SinglePane {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[SinglePane] a branch of its own is required");
        if (!o.host || !o.focus || !o.panes || typeof o.entry !== "function") throw new Error("[SinglePane] opts.host, opts.focus, opts.panes and opts.entry are required");
        branch.activate(_singlePaneOwner);
        this.branch = branch;
        this._host = o.host;
        this._panes = o.panes;
        this._entry = o.entry;
        this._focus = o.focus.createBranch("pane", this);   // the pane holds a branch: the shown widget's focus root is grafted there
        this.focus = this._focus.owner;
        this._inView = null;       // the id whose slot is in the host
        this._picker = null;       // { branch, picker } while one is up
        this._pickers = 0;
        this.model = new PanePlacement();
        this.model.on(function (n) { self._follow(n.id); });
    }

    mount(entry, location) { this.model.mount(entry, location); }

    unmount(entry) { this.model.unmount(entry); }

    show(id) { this.model.show(id == null ? null : id); }

    shown() { return this.model.shown(); }

    pick(kinds, onPick) {
        var self = this;
        this._dropPicker();
        this._detach(this._inView);
        var b = this.branch.createBranch("picker-" + (++this._pickers));
        var picker = new PanePicker(b, {
            kinds: kinds,
            onPick: function (kindId) { try { onPick(kindId); } finally { if (self._picker) self._cancelPick(); } },
            onCancel: function () { self._cancelPick(); }
        });
        this._picker = { branch: b, picker: picker };
        this._host.appendChild(picker.root);
    }

    /** Asked for the keys: the shown widget is asked. A container never claims for what it holds. */
    activate() {
        var e = this._inView && this._entry(this._inView), w = e && e.widget;
        if (w && typeof w.activate === "function") w.activate();
    }

    /** The page follows the model: the picker let go, the one in view hidden, the one shown now put in view. */
    _follow(id) {
        this._dropPicker();
        if (this._inView) this._hide();
        if (id === null) return;
        var slot = this._panes.slot(id);
        if (!slot) throw new Error("[SinglePane] no pane for '" + id + "'");
        this._host.appendChild(slot);
        var e = this._entry(id), roots = e && e.widget && e.widget.roots;
        if (roots && roots.focus) this._focus.graft("shown", roots.focus);
        this._inView = id;
    }

    /** The one in view, hidden: its slot out of the host, its focus root off. */
    _hide() {
        this._detach(this._inView);
        var grafted = this._focus.members.some(function (m) { return m.kind === "proxy" && m.name === "shown"; });
        if (grafted) this._focus.detach("shown");
        this._inView = null;
    }

    _detach(id) {
        var slot = id && this._panes.slot(id);
        if (slot && slot.parentNode === this._host) this._host.removeChild(slot);
    }

    /** A cancel: the picker let go, and what was shown back in view. */
    _cancelPick() {
        this._dropPicker();
        var slot = this._inView && this._panes.slot(this._inView);
        if (slot) this._host.appendChild(slot);
    }

    _dropPicker() {
        if (!this._picker) return;
        var p = this._picker;
        this._picker = null;
        if (p.picker.root.parentNode === this._host) this._host.removeChild(p.picker.root);
        this.branch.dissolveBranch(p.branch.name);
    }
}
