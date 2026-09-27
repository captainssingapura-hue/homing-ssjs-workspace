// =============================================================================
// NastyFixed — a widget that misbehaves on purpose, for the bench to catch.
// Self-contained in every other way — its own DomOps party, offered as
// roots.dom for its host to graft, its focus member its own, its keys
// through the party, nothing left when disposed — it
// breaks the one rule of the container: its root is fixed at 640 x 360,
// whatever container it is lent, and it never follows a size the host changes.
// In a bigger container it does not fill it; in a smaller one it overflows it.
//
//   new NastyFixed(container, params)   params: none
// =============================================================================

const _nastyOwner = Object.freeze({ toString: () => "nastyFixed" });
var _nasties = 0;

class NastyFixed {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[NastyFixed] a container is required: the one its page lends it");
        var name = "nastyFixed-" + (++_nasties);
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_nastyOwner);
        this.roots = Object.freeze({ dom: this._dom });   // its own DomOps party, for its host to graft
        var root = this._dom.createElement("root", "div");
        css.addClass(root, nasty_fixed);
        root.textContent = "Fixed at 640 x 360: I take no notice of the container I am lent, whatever its size.";
        container.appendChild(root);
        this.root = root;
        this.focus = focusParty.root.join(name, this);
        this._off = Keys.claimOn(root, this.focus);
    }

    activate() { Keys.claim(this.focus); }

    keyDown(ev) {
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    dispose() {
        if (this._off) { this._off(); this._off = null; }
        if (this.focus && this.focus.in) this.focus.leave();
        this._dom.dissolve();
    }
}
