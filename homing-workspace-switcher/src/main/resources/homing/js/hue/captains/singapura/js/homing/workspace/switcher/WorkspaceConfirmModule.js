// =============================================================================
// WorkspaceConfirm — a question put to a person before an act on a workspace:
// the system dialog (Dialog), modal, over whatever is open — a switcher's
// dialog, too — its question in the body, and two verbs: Cancel, and the act.
// However it closes — Cancel, Escape, the scrim, the act — it is answered once:
// yes only when the act was pressed.
//
//   new WorkspaceConfirm(branch, { title, question, act, keyboard })
//     branch     the sub-branch its caller made for it, unactivated; dissolved on close
//     keyboard   the page's KeyboardSteward: the dialog's keys, and given back after
//   confirm.answered   Promise<boolean>: true when the act was pressed
// =============================================================================

class WorkspaceConfirm {
    constructor(branch, opts) {
        var o = opts || {}, yes = false, self = this;
        if (!branch) throw new Error("[WorkspaceConfirm] a branch of its own is required");
        this.answered = new Promise(function (resolve) { self._answer = resolve; });
        this._dialog = new Dialog(branch, {
            title: o.title || "Are you sure?",
            modal: true,
            size: { w: 460, h: 220 },
            keyboard: o.keyboard,
            content: function (b, bodyEl) {
                var q = b.createElement("question", "p");
                css.addClass(q, sw_question);
                q.textContent = String(o.question || "");
                bodyEl.appendChild(q);
                return {};
            },
            actions: [
                { id: "cancel", label: "Cancel", onClick: function (d) { d.close(); } },
                { id: "act", label: o.act || "OK", primary: true, onClick: function (d) { yes = true; d.close(); } }
            ],
            onClose: function () { self._answer(yes); }
        });
    }

    close() { this._dialog.close(); }
}
