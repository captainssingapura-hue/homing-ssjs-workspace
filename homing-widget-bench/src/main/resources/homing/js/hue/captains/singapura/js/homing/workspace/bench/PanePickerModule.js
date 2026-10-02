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
            var b = new ButtonBuilder();
            root.appendChild(b.label(k.title || k.id).plain().onClick(function () { o.onPick(k.id); }).build(branch.createElement("kind-" + i, b.tag)).el);
        });
        var c = new ButtonBuilder();
        root.appendChild(c.label("Cancel").plain().size(-1).onClick(function () { o.onCancel(); }).build(branch.createElement("cancel", c.tag)).el);
        this.root = root;
    }
}
