// =============================================================================
// WorkspaceOpener — the page's member of its workspace choice party: whoever
// opens workspaces. What the party says is asked to open, it opens, by the
// address the page makes of it — unless it is the workspace the page shows,
// or one the page knows no address for. A new workspace asked for, it has the
// page make — its id, its listing, under the name asked for — and goes to it,
// here or in a new tab. On joining it chooses where the page
// is: the kind of workspace it shows, so a switcher starts there.
//
// Pure: it goes nowhere itself. The page hands it the address of a workspace
// and the going, and keeps the navigation its own.
//
//   new WorkspaceOpener({ party, here, addressOf, go, create?, goNew? })
//     party      the workspace choice party at the root of the page's workspace
//     here       { workspaceKind, workspaceId, own } — the workspace the page shows: its
//                kind, its id, and whether it is the kind's own
//     addressOf  (workspaceKind, workspaceId) → the address of that workspace, or null when
//                the page knows none; workspaceId "" for the kind's own
//     go         (address) → the page goes there
//     create     (workspaceKind, workspaceName) → Promise<the new workspace's address, or null>;
//                refused - a name taken - and it says why, Report, for every view of the kind:
//                the page makes one; none, and a new one asked for is not made
//     goNew      (address) → the page opens it in a new tab; go, unless said
//   opener.asked()   what it was asked to open, and what it did - the last few, the newest
//                    last: [{ workspaceKind, workspaceId, did: "went" | "here" | "unknown" | "making" | "failed" }]
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
        this._create = typeof o.create === "function" ? o.create : null;
        this._goNew = typeof o.goNew === "function" ? o.goNew : o.go;
        this._asked = [];
        this._member = o.party.join("workspaceOpener", {
            Opening: function (m) { self._opening(m.workspaceKind, m.workspaceId); },
            OpeningNew: function (m) { self._openingNew(m.workspaceKind, m.workspaceName, m.newTab); }
        });
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
        this._said({ workspaceKind: kind, workspaceId: id, did: did });
        if (did === "went") this._go(address);
    }

    /** A new one asked for: made by the page, and gone to - here, or in a new tab. Not made, said so. */
    _openingNew(kind, name, newTab) {
        var self = this;
        this._said({ workspaceKind: kind, workspaceId: "", did: this._create ? "making" : "unknown" });
        if (!this._create) return;
        Promise.resolve(this._create(kind, name)).then(function (address) {
            if (typeof address === "string" && address) (newTab ? self._goNew : self._go)(address);
            else self._said({ workspaceKind: kind, workspaceId: "", did: "unknown" });
        }, function (e) {
            // not made - a name another of the kind has, say - and said so, for every view of the kind
            self._said({ workspaceKind: kind, workspaceId: "", did: "failed" });
            if (self._member) self._member.tell({ kind: "Report", workspaceKind: kind, note: (e && e.message) || String(e), changed: false });
        });
    }

    _said(entry) {
        this._asked.push(Object.freeze(entry));
        if (this._asked.length > WorkspaceOpener.KEPT) this._asked.shift();
    }

    _isHere(kind, id) {
        var h = this._here;
        return kind === h.workspaceKind && (id === h.workspaceId || (id === "" && h.own));
    }
}
