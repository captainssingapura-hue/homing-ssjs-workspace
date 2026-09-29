// =============================================================================
// WorkspaceOpener — the page's member of its workspace choice party: whoever
// opens workspaces. What the party says is asked to open, it opens, by the
// address the page makes of it — unless it is the workspace the page shows,
// or one the page knows no address for. On joining it chooses where the page
// is: the kind of workspace it shows, so a switcher starts there.
//
// Pure: it goes nowhere itself. The page hands it the address of a workspace
// and the going, and keeps the navigation its own.
//
//   new WorkspaceOpener({ party, here, addressOf, go })
//     party      the workspace choice party at the root of the page's workspace
//     here       { workspaceKind, workspaceId, own } — the workspace the page shows: its
//                kind, its id, and whether it is the kind's own
//     addressOf  (workspaceKind, workspaceId) → the address of that workspace, or null when
//                the page knows none; workspaceId "" for the kind's own
//     go         (address) → the page goes there
//   opener.asked()   what it was asked to open, and what it did - the last few, the newest
//                    last: [{ workspaceKind, workspaceId, did: "went" | "here" | "unknown" }]
//   opener.leave()
// =============================================================================

class WorkspaceOpener {

    /** How many openings are kept. */
    static KEPT = 5;

    constructor(opts) {
        var o = opts || {}, self = this;
        if (!o.party || typeof o.party.join !== "function") throw new TypeError("[WorkspaceOpener] opts.party is required: the workspace choice party it joins");
        if (!o.here || typeof o.here.workspaceKind !== "string") throw new TypeError("[WorkspaceOpener] opts.here is required: the workspace the page shows");
        if (typeof o.addressOf !== "function" || typeof o.go !== "function") throw new TypeError("[WorkspaceOpener] opts.addressOf and opts.go are required");
        this._here = Object.freeze({ workspaceKind: o.here.workspaceKind, workspaceId: String(o.here.workspaceId || ""), own: o.here.own === true });
        this._addressOf = o.addressOf;
        this._go = o.go;
        this._asked = [];
        this._member = o.party.join("workspaceOpener", { Opening: function (m) { self._opening(m.workspaceKind, m.workspaceId); } });
        this._member.tell({ kind: "Choose", workspaceKind: this._here.workspaceKind });
    }

    asked() { return this._asked.slice(); }

    leave() {
        if (this._member) { this._member.leave(); this._member = null; }
    }

    /** Asked to open: here, nothing; a workspace the page knows the address of, gone to; else, nothing, and said so. */
    _opening(kind, id) {
        var did = this._isHere(kind, id) ? "here" : null, address = null;
        if (!did) {
            address = this._addressOf(kind, id);
            did = typeof address === "string" && address ? "went" : "unknown";
        }
        this._asked.push(Object.freeze({ workspaceKind: kind, workspaceId: id, did: did }));
        if (this._asked.length > WorkspaceOpener.KEPT) this._asked.shift();
        if (did === "went") this._go(address);
    }

    _isHere(kind, id) {
        var h = this._here;
        return kind === h.workspaceKind && (id === h.workspaceId || (id === "" && h.own));
    }
}
