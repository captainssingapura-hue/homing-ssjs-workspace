// =============================================================================
// StewardLamp — where the keys are, as one lamp, a monitor widget (Monitor):
// the StewardMonitor widget, shown in a slot over its box. Held by a member, lent
// by it to a native control of its own, or away; the member a walk offers the
// keys to; and the first of the steward's invariants broken, in the danger
// colour while one is. Redrawn on every event of the steward and every move
// of the native focus.
//
//   new StewardLamp(container, params)   params: none
// =============================================================================

const _stewardLampOwner = Object.freeze({ toString: () => "stewardLamp" });
var _stewardLamps = 0;

class StewardLamp extends Monitor {
    constructor(container, params) {
        super(container, "stewardLamp-" + (++_stewardLamps), "Keyboard steward");
        var shown = this._dom.createBranch("monitor");
        shown.activate(_stewardLampOwner);
        this._slot = new WidgetSlot({ branch: shown, host: this.box });
        this._it = this._slot.show("steward", StewardMonitor, {});
    }

    dispose() {
        this._slot.disposeAll();
        super.dispose();
    }
}
