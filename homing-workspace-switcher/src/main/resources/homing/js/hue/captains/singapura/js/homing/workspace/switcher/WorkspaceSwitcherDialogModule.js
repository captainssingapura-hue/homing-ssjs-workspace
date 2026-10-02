// =============================================================================
// WorkspaceSwitcherDialog — the workspace switcher, summoned: the composed
// switcher (WorkspaceSwitcher) in the system dialog (Dialog) — modal, titled,
// its verbs underneath: Rename and Delete…, the table's F2 and Delete on the
// workspace at its cursor; then Cancel, Open in new tab, Open. Browsing and opening are
// different acts: moving through the kinds and their workspaces opens nothing;
// the table's new row, named, asks for a new one; Open on it, unnamed, too;
// Open, Enter on one, or a double press does.
//
// It is a page's, never a workspace's: made when summoned, gone when closed,
// kept by no log. The switcher is lent a box filling the dialog's body, and
// grafted: its DomOps party on the dialog's branch, its focus party under the
// page's, where the steward reaches it. It joins the workspace choice party it
// is handed — the page's — so what it asks to open, the page's opener opens;
// and whatever the party says is opening, the dialog closes for, whoever asked.
//
// The keys: the dialog claims them, then the switcher's tree is given them,
// at the kind the page shows and — when its table shows it — the workspace the
// page shows. Escape closes the dialog wherever the hand is in the switcher:
// its tree and its table leave Escape to the page, and the dialog takes it
// before the steward would only take the browser's focus away.
//
//   new WorkspaceSwitcherDialog(branch, opts)
//     branch     the sub-branch the page made for it, unactivated; dissolved on close
//     opts.party     the workspace choice party it joins: the page's
//     opts.keyboard  the page's KeyboardSteward
//     opts.here      { workspaceKind, workspaceId } the page shows, for the table's cursor
//     opts.addressOf (workspaceKind, workspaceId) → an address, for Open in new tab; none, no such verb
//     opts.title, opts.onClose
//   dialog.close()   dialog.isOpen()
// =============================================================================

const _switcherDialogOwner = Object.freeze({ toString: () => "workspaceSwitcherDialog" });
var _switcherDialogs = 0;

class WorkspaceSwitcherDialog {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[WorkspaceSwitcherDialog] a branch of its own is required");
        if (!o.party || typeof o.party.join !== "function") throw new Error("[WorkspaceSwitcherDialog] opts.party is required: the workspace choice party it joins");
        this._open = true;
        this._switcher = null;
        this._addressOf = typeof o.addressOf === "function" ? o.addressOf : null;
        this._name = "workspaceSwitcherDialog-" + (++_switcherDialogs);
        this._member = o.party.join(this._name, { Opening: function () { self.close(); }, OpeningNew: function () { self.close(); } });
        var actions = [
            { id: "rename", label: "Rename", onClick: function () { if (self._switcher) self._switcher.rename(); } },
            { id: "delete", label: "Delete…", onClick: function () { if (self._switcher) self._switcher.remove(); } },
            { id: "cancel", label: "Cancel", onClick: function (d) { d.close(); } }];
        if (this._addressOf) actions.push({ id: "newtab", label: "Open in new tab", onClick: function () { self._go(true); } });
        actions.push({ id: "open", label: "Open", primary: true, onClick: function () { self._go(false); } });
        this._dialog = new Dialog(branch, {
            title: o.title || "Switch workspace",
            modal: true,
            size: { w: 860, h: 520 },
            keyboard: o.keyboard,
            actions: actions,
            content: function (b, bodyEl) { return self._content(b, bodyEl, o); },
            onClose: function () {
                self._open = false;
                if (self._member) { self._member.leave(); self._member = null; }
                if (typeof o.onClose === "function") o.onClose();
            }
        });
        if (this._switcher) this._switcher.activate();
    }

    /** The switcher in a box of its own filling the body, grafted, joined, at where the page is. */
    _content(b, bodyEl, o) {
        var self = this, place = b.createBranch("switcher");
        place.activate(_switcherDialogOwner);
        var slot = place.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        bodyEl.appendChild(slot);
        // Escape closes the dialog wherever the hand is in the switcher: taken here, before the steward
        slot.addEventListener("keydown", function (ev) {
            if (ev.key !== "Escape" || ev.defaultPrevented) return;
            ev.preventDefault();
            ev.stopPropagation();
            self.close();
        });
        var sw = this._switcher = new WorkspaceSwitcher(slot, {});
        place.graft("widget", sw.roots.dom);
        focusParty.root.graft(this._name, sw.roots.focus);
        if (o.here && o.here.workspaceId) sw.prefer(o.here.workspaceId);
        var given = {};
        given[WORKSPACE_CHOICE.name] = o.party;
        sw.join(given);
        return { dispose: function () { sw.dispose(); self._switcher = null; } };
    }

    /** What is chosen, asked to open - here, through the party; or in a new tab, at its address. */
    _go(newTab) {
        var s = this._switcher ? this._switcher.selection() : null;
        if (!s) return;
        if (s.fresh) {   // the new row: a new one of the kind, under the catalogue's next name, here or in a new tab
            if (this._member) this._member.tell({ kind: "OpenNew", workspaceKind: s.workspaceKind, workspaceName: "", newTab: !!newTab });
            this.close();
            return;
        }
        if (newTab) {
            var address = this._addressOf(s.workspaceKind, s.workspaceId);
            if (address) HrefManagerInstance.openNew(address);
            this.close();
            return;
        }
        if (this._member) this._member.tell({ kind: "Open", workspaceKind: s.workspaceKind, workspaceId: s.workspaceId });
        this.close();
    }

    isOpen() { return this._open; }

    close() { if (this._open) this._dialog.close(); }
}
