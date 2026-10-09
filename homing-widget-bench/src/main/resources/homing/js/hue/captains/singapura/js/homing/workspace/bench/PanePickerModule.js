// =============================================================================
// PanePicker — a transient pane its host owns (RFC 0066 E3, the workspace
// detour: requests): the kinds the workspace can open, a button each, and
// Cancel. Picked, it ASKS — onPick(kindId) — and acts on nothing itself: whoever
// asked for the picker turns the pick into a request, the core executes it,
// and the host lets its picker go. Nothing of the register's: no widget, not
// logged.
//
//   new PanePicker(branch, { kinds, onPick, onCancel })
//     branch  its own, unactivated; the host dissolves it when the picker goes
//     kinds   [{ id, title }]: what can be opened, in the order offered
//   picker.root
// =============================================================================

const _panePickerOwner = Object.freeze({ toString: () => "panePicker" });

/** A kind's button pressed: the kind picked. */
class PanePickerPick {
    constructor(opts, kind) { this._opts = opts; this._kind = kind; }
    canHandle(type) { return type === EVENT.CLICK; }
    handle() { this._opts.onPick(this._kind); }
}

/** The cancel pressed: nothing picked. */
class PanePickerCancel {
    constructor(opts) { this._opts = opts; }
    canHandle(type) { return type === EVENT.CLICK; }
    handle() { this._opts.onCancel(); }
}

class PanePicker {
    constructor(branch, opts) {
        var o = opts || {};
        if (!branch) throw new Error("[PanePicker] a branch of its own is required");
        if (typeof o.onPick !== "function" || typeof o.onCancel !== "function") throw new Error("[PanePicker] opts.onPick and opts.onCancel are required");
        branch.activate(_panePickerOwner);
        var root = branch.createElement("picker", "div");
        css.addClass(root, wb_picker);
        root.setAttribute("role", "group");
        root.setAttribute("aria-label", "Open a widget");
        var heading = branch.createElement("heading", "span");
        css.addClass(heading, wb_ws_label);
        heading.textContent = "Open a widget";
        root.appendChild(heading);
        (o.kinds || []).forEach(function (k, i) {
            var pick = new PlainButton(PlainButtonFactory.create(branch, "kind-" + i), { label: k.title || k.id });
            pick.setOnClick(new PanePickerPick(o, k.id));
            root.appendChild(pick.el);
        });
        var cancel = new PlainButton(PlainButtonFactory.create(branch, "cancel"), { label: "Cancel", size: -1 });
        cancel.setOnClick(new PanePickerCancel(o));
        root.appendChild(cancel.el);
        this.root = root;
    }
}
