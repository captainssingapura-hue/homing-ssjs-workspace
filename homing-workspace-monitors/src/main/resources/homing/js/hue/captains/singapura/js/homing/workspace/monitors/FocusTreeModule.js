// =============================================================================
// FocusTree — the page's logical-focus tree, a monitor widget (Monitor): the
// FocusMonitor widget, shown in a slot over its box. Every branch and member of
// the page's focus party, a grafted party under its proxy, the holder of the
// keys lit and the member a walk offers them to marked — redrawn as the party
// and the steward speak. Grafted, it is in the tree it shows.
//
//   new FocusTree(container, params)   params: none
// =============================================================================

const _focusTreeOwner = Object.freeze({ toString: () => "focusTree" });
var _focusTrees = 0;

class FocusTree extends Monitor {
    constructor(container, params) {
        super(container, "focusTree-" + (++_focusTrees), "Logical focus");
        var shown = this._dom.createBranch("monitor");
        shown.activate(_focusTreeOwner);
        this._slot = new WidgetSlot({ branch: shown, host: this.box });
        this._it = this._slot.show("focus", FocusMonitor, {});
    }

    dispose() {
        this._slot.disposeAll();
        super.dispose();
    }
}
