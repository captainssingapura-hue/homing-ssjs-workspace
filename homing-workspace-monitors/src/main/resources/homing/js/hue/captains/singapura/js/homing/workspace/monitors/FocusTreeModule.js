// =============================================================================
// FocusTree — the page's logical-focus tree, a monitor widget (Monitor): the
// FocusMonitor component in a widget of its own. Every branch and member of
// the page's focus party, a grafted party under its proxy, the holder of the
// keys lit and the member a walk offers them to marked — redrawn as the party
// and the steward speak. Grafted, it is in the tree it shows.
//
//   new FocusTree(container, params)   params: none
// =============================================================================

var _focusTrees = 0;

class FocusTree extends Monitor {
    constructor(container, params) {
        super(container, "focusTree-" + (++_focusTrees), "Logical focus");
        this._it = new FocusMonitor(this._dom.createBranch("monitor"), { host: this.box });
    }

    dispose() {
        this._it.dispose();
        super.dispose();
    }
}
