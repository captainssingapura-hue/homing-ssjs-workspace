// =============================================================================
// PaneSlots — the bench's register of panes: the core's port (RFC 0066 E3, the
// workspace detour: requests). A widget and its pane live and die together, so
// the core asks here for a widget's pane when it creates it — a slot of its
// own, on a branch of its own, placed nowhere, titled after its widget — titles
// it again when the widget is renamed, and closes it here with the
// widget. Where a slot is shown is the placement's; it never makes or closes one.
//
//   new PaneSlots(branch, { title })   branch: its own, unactivated
//     title   (entry) → the widget's title: the name a user gave it, else its identity's
//   slots.lend(entry) → the widget's slot, placed nowhere       (the core's port)
//   slots.rename(entry)   the slot titled again                  (the core's port)
//   slots.release(entry)  the slot closed, its branch dissolved  (the core's port)
//   slots.slot(id) → the slot of that widget, or null
// =============================================================================

const _paneSlotsOwner = Object.freeze({ toString: () => "paneSlots" });

class PaneSlots {
    constructor(branch, opts) {
        if (!branch) throw new Error("[PaneSlots] a branch of its own is required");
        branch.activate(_paneSlotsOwner);
        this.branch = branch;
        this._slots = new Map();   // a widget's id → { branch, slot }
        this._n = 0;
        this._title = opts && typeof opts.title === "function" ? opts.title : function (e) { return e.id; };
    }

    lend(entry) {
        if (this._slots.has(entry.id)) throw new Error("[PaneSlots] '" + entry.id + "' has a pane already");
        var b = this.branch.createBranch("slot-" + (++this._n));
        b.activate(_paneSlotsOwner);
        var slot = b.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        slot.setAttribute("role", "region");
        slot.setAttribute("aria-label", this._title(entry));
        this._slots.set(entry.id, { branch: b, slot: slot });
        return slot;
    }

    rename(entry) {
        var s = this._slots.get(entry.id);
        if (s) s.slot.setAttribute("aria-label", this._title(entry));
    }

    release(entry) {
        var s = this._slots.get(entry.id);
        if (!s) return;
        this._slots.delete(entry.id);
        if (s.slot.parentNode) s.slot.parentNode.removeChild(s.slot);
        this.branch.dissolveBranch(s.branch.name);
    }

    slot(id) {
        var s = this._slots.get(id);
        return s ? s.slot : null;
    }
}
