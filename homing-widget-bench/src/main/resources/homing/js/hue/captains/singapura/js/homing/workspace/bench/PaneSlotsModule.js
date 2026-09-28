// =============================================================================
// PaneSlots — the bench's register of panes: the core's port (RFC 0066 E3, the
// workspace detour: requests). A widget and its pane live and die together, so
// the core asks here for a widget's pane when it creates it — a slot of its
// own, on a branch of its own, placed nowhere — and closes it here with the
// widget. Where a slot is shown is the placement's; it never makes or closes one.
//
//   new PaneSlots(branch)   branch: its own, unactivated
//   slots.lend(entry) → the widget's slot, placed nowhere       (the core's port)
//   slots.release(entry)  the slot closed, its branch dissolved  (the core's port)
//   slots.slot(id) → the slot of that widget, or null
// =============================================================================

const _paneSlotsOwner = Object.freeze({ toString: () => "paneSlots" });

class PaneSlots {
    constructor(branch) {
        if (!branch) throw new Error("[PaneSlots] a branch of its own is required");
        branch.activate(_paneSlotsOwner);
        this.branch = branch;
        this._slots = new Map();   // a widget's id → { branch, slot }
        this._n = 0;
    }

    lend(entry) {
        if (this._slots.has(entry.id)) throw new Error("[PaneSlots] '" + entry.id + "' has a pane already");
        var b = this.branch.createBranch("slot-" + (++this._n));
        b.activate(_paneSlotsOwner);
        var slot = b.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        this._slots.set(entry.id, { branch: b, slot: slot });
        return slot;
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
