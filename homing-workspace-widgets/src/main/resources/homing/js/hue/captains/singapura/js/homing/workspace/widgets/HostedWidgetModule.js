// =============================================================================
// HostedWidget — a self-contained widget where a component would stand: the
// host's side of the graft (Parties Are Grafted Hierarchies). A tab-pane makes
// its widget make(branch, tab) — a sub-branch of the tab-pane's own, and the
// focus branch of the host that shows it — and moves the widget's membership
// from host to host as the tab travels. A self-contained widget has neither:
// it is made new Kind(container, params), and its parties are its own, which
// no host may adopt. So the host lends it what it needs and grafts the rest:
//
//   the container   a box minted on the branch the tab made, filling the pane
//   its DomOps      the widget's party grafted on that branch, as "widget"
//   its focus       a holder joined to the tab's focus branch, the widget's
//                   party grafted under it: a tab that travels moves the holder,
//                   and the graft goes with it, whole
//
// It holds nothing of the widget's but the graft: asked for the keys, it asks
// the widget. A widget that offers no roots of its own stands in the container
// ungrafted — its host has nothing to graft.
//
// Two ways to be made. Given the widget's class, it makes the widget itself,
// and disposed, disposes it — its parties dissolved take their proxies with
// them. Given none, it is a holder LENT EMPTY: its container is for another to
// make the widget in — a workspace's core, which owns the widget's life — and
// the widget is handed in after, hold(widget); disposed, it leaves the widget
// to its maker.
//
//   new HostedWidget(branch, tab, Kind?, params?)
//     branch   the sub-branch its host made for it, unactivated
//     tab      its host's handle: { name, focus } — the focus branch to join, under that name
//     Kind     the widget's class, made new Kind(container, params); none, and it is lent empty
//   hosted.root     the container, which the host puts in place
//   hosted.focus    the holder: the host's member, by the pane's law
//   hosted.widget   the widget, or null while none is held
//   hosted.hold(widget)   the widget made in hosted.root, grafted; one, once
//   hosted.dispose()
// =============================================================================

const _hostedOwner = Object.freeze({ toString: () => "hostedWidget" });

class HostedWidget {
    constructor(branch, tab, Kind, params) {
        if (!branch) throw new Error("[HostedWidget] a branch of its own is required");
        if (!tab || !tab.focus) throw new Error("[HostedWidget] the tab's handle is required: tab.focus, the branch to join");
        if (Kind != null && typeof Kind !== "function") throw new Error("[HostedWidget] the widget's class, when given, is a class");
        branch.activate(_hostedOwner);
        this.branch = branch;
        var slot = branch.createElement("slot", "div");
        css.addClass(slot, wg_slot);
        this.root = slot;
        this._place = tab.focus.createBranch(tab.name || branch.name, this);
        this.focus = this._place.owner;
        this.widget = null;
        this._own = Kind != null;
        if (this._own) this.hold(new Kind(slot, params || {}));
    }

    hold(widget) {
        if (this.widget) throw new Error("[HostedWidget] it holds a widget already");
        if (!widget || typeof widget !== "object") throw new Error("[HostedWidget] hold wants the widget made in its container");
        this.widget = widget;
        var roots = widget.roots || {};
        if (roots.dom instanceof MobileDomOpsParty) this.branch.graft("widget", roots.dom);
        if (roots.focus instanceof MobileFocusParty) this._place.graft("widget", roots.focus);
    }

    /** Asked for the keys: the widget is asked. A container never claims for what it holds. */
    activate() { if (this.widget && typeof this.widget.activate === "function") this.widget.activate(); }

    dispose() {
        if (this._disposed) return;
        this._disposed = true;
        if (this._own && typeof this.widget.dispose === "function") this.widget.dispose();
    }
}
