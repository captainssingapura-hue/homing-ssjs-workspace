// =============================================================================
// StageButton — what a widget offers itself to the stage by: a button that,
// pressed, joins the page's stage party and asks for the widget, by its name,
// to be shown - and, while it is, to be brought back. It hears what the
// steward says of its widget and says so: "Focus", or "Back" while shown; when
// the widget is back, or was refused - the refusal on the console - it leaves.
// A control, pressed natively. It knows the party and its widget's name: not
// where the widget is, nor where it goes. A BRANCH component.
//
//   var b = new StageButton(branch, { party, widget })   b.root   b.shown()   b.dispose()
// =============================================================================

const _stageButtonOwner = Object.freeze({ toString: () => "stage-button" });

class StageButton {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[StageButton] a branch of its own is required");
        if (!o.party || typeof o.party.join !== "function") throw new Error("[StageButton] the stage party is required");
        if (!o.widget) throw new Error("[StageButton] its widget's name is required");
        branch.activate(_stageButtonOwner);
        this.branch = branch;
        this._party = o.party;
        this._widget = String(o.widget);
        this._member = null;
        this._shown = false;
        var b = new ButtonBuilder().label("Focus").plain().size(-1).onClick(function () { self._press(); });
        this._button = b.build(branch.createElement("stage", b.tag));
        this._button.el.setAttribute("aria-label", "Show it on the stage");
        this.root = this._button.el;
    }

    shown() { return this._shown; }

    dispose() {
        this._leave();
        this.branch.dissolve();
    }

    _press() {
        if (!this._member) this._join();
        this._member.tell(this._shown ? { kind: "Dismiss" } : { kind: "Present", widget: this._widget });
    }

    _join() {
        var self = this, mine = function (m) { return m.widget === self._widget; };
        this._member = this._party.join(this._widget, {
            Shown: function (m) { if (mine(m)) self._at(true); },
            Returned: function (m) { if (mine(m)) self._done(); },
            Refused: function (m) { if (mine(m)) { console.error("[StageButton] " + m.widget + " could not be shown: " + m.why); self._done(); } }
        });
    }

    _at(shown) {
        this._shown = shown;
        this._button.label(shown ? "Back" : "Focus");
        this._button.el.setAttribute("aria-label", shown ? "Bring it back from the stage" : "Show it on the stage");
    }

    /** Back, or refused: the button as it was, and out of the party. */
    _done() {
        this._at(false);
        this._leave();
    }

    _leave() {
        if (this._member) { this._member.leave(); this._member = null; }
    }
}
