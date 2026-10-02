// =============================================================================
// Monitor — what every monitor widget is, before what it watches. A widget
// that stands on its own (the Workspace & Widgets doctrines): made with the
// container its page lends it and its params, and nothing else. Its elements
// are minted in a DomOps party of its own, and its keys come through a focus
// party of its own — each a mobile party, offered as roots for its host to
// graft. What it watches is the page's: the parties it is grafted into, read
// and never written. A monitor makes nothing in them, so a host that grafts
// one sees it in what it shows: the monitor among what it monitors.
//
// Its root fills the container; what it shows scrolls in its box, by the
// pointer, or by the keys while it holds them — the arrows a line, the page
// keys a page, Home and End the ends. Escape gives the keys back.
//
//   class X extends Monitor {
//       constructor(container, params) { super(container, name, label); ... into this.box ... }
//   }
//     monitor.root     its root, in the container
//     monitor.box      where what it shows goes: the scroller
//     monitor.roots    what its host grafts: { dom, focus }, each a stray until grafted
//     monitor.focus    its membership of its own focus party
//     monitor.dispose() both parties dissolved, and nothing left in the container.
//                      A monitor disposes what it made, then this
// =============================================================================

const _monitorOwner = Object.freeze({ toString: () => "monitor" });

class Monitor {
    constructor(container, name, label) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[" + name + "] a container is required: the one its page lends it");
        // ITS OWN DOMOPS PARTY: everything it mints is in it, and goes with it; its host grafts it
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_monitorOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, wg_fill);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", label);
        var box = this._dom.createElement("box", "div");
        css.addClass(box, wg_scroll);
        css.addClass(box, mn_pane);
        root.appendChild(box);
        this.root = root;
        this.box = box;
        container.appendChild(root);
        // ITS OWN FOCUS PARTY: it is the member of it; its host grafts it where the keys should reach it
        this._focusParty = focusParties.mobile(name);
        this.focus = this._focusParty.root.join("monitor", this);
        this._off = Keys.claimOn(root, this.focus);
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
    }

    /** Asked for the keys: they are claimed. */
    activate() { Keys.claim(this.focus); }

    /** Its box scrolled while it holds the keys; Escape gives them back. */
    keyDown(ev) {
        var box = this.box, page = Math.max(box.clientHeight - Monitor.LINE, Monitor.LINE);
        switch (ev.key) {
            case "Escape":   Keys.yield(this.focus); return true;
            case "ArrowDown": box.scrollTop += Monitor.LINE; return true;
            case "ArrowUp":   box.scrollTop -= Monitor.LINE; return true;
            case "PageDown":  box.scrollTop += page; return true;
            case "PageUp":    box.scrollTop -= page; return true;
            case "Home":      box.scrollTop = 0; return true;
            case "End":       box.scrollTop = box.scrollHeight; return true;
            default:          return false;
        }
    }

    dispose() {
        if (this._off) { this._off(); this._off = null; }
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}

/** A line, as the arrows scroll it. */
Monitor.LINE = 24;
