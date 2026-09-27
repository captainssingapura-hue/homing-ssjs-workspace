// =============================================================================
// StewardLamp — where the keys are, as one lamp, a monitor widget (Monitor):
// the StewardMonitor component in a widget of its own. Held by a member, lent
// by it to a native control of its own, or away; the member a walk offers the
// keys to; and the first of the steward's invariants broken, in the danger
// colour while one is. Redrawn on every event of the steward and every move
// of the native focus.
//
//   new StewardLamp(container, params)   params: none
// =============================================================================

var _stewardLamps = 0;

class StewardLamp extends Monitor {
    constructor(container, params) {
        super(container, "stewardLamp-" + (++_stewardLamps), "Keyboard steward");
        this._it = new StewardMonitor(this._dom.createBranch("monitor"), { host: this.box });
    }

    dispose() {
        this._it.dispose();
        super.dispose();
    }
}
