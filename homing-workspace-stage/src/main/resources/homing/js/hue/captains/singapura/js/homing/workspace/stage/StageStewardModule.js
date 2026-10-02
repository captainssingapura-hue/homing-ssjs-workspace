// =============================================================================
// StageSteward — the steward of a page's stage party: the one member that
// moves anything. Asked to show a widget, it asks the placement that keeps it
// to lend it - its root, and a title for where it sits - seats it on the
// stage, and says Shown; asked to bring it back, or the stage closed by the
// reader, it gives it back to the placement, takes the stage down, and says
// Returned. A widget the placement does not keep for the stage is Refused.
// It knows the placement, and never where in it a widget sits; the widget
// never learns it moved - the very one mounted in place is the one on the
// stage, filling the box it is in.
//
//   StageSteward.over({ placement, branch }) → the class a stage party hires:
//     placement   function () → the placement engine - made after the party is, so asked for when wanted:
//                 lend(name) → { root, title } | null    restore(name) → true when it had lent it
//     branch      a DomOps branch for the stage's layer: the host's to lend
//   steward.reactors { Present, Dismiss }    steward.showing() → the name of the widget on the stage, or null
// =============================================================================

class StageSteward {

    static over(at) {
        if (!at || typeof at.placement !== "function" || !at.branch) throw new Error("[StageSteward] over a placement: StageSteward.over({ placement: () => engine, branch })");
        return class extends StageSteward { constructor(tell) { super(tell, at); } };
    }

    constructor(tell, at) {
        if (typeof tell !== "function") throw new Error("[StageSteward] hired with the means to tell its party");
        if (!at) throw new Error("[StageSteward] hired over a placement: a party hires StageSteward.over({ placement, branch })");
        var self = this;
        this._tell = tell;
        this._at = at;
        this._layer = null;
        this._shown = null;
        this.reactors = Object.freeze({
            Present: function (m) { self._present(m.widget); },
            Dismiss: function () { self._dismiss(); }
        });
    }

    showing() { return this._shown; }

    _present(name) {
        if (this._shown) { this._tell({ kind: "Refused", widget: name, why: "the stage is showing " + this._shown + " already" }); return; }
        var had = document.activeElement;   // read before the loan: a focused element taken from its box is blurred
        var placement = this._at.placement(), lent = placement ? placement.lend(name) : null;
        if (!lent) { this._tell({ kind: "Refused", widget: name, why: "the placement keeps no widget named " + name + " for the stage" }); return; }
        var self = this;
        if (!this._layer) this._layer = new StageLayer(this._at.branch, { onClose: function () { self._dismiss(); } });
        this._shown = name;
        this._layer.show(lent.root, lent.title, had);
        this._tell({ kind: "Shown", widget: name, title: lent.title || "" });
    }

    /** Brought back: the widget given back to its placement before the stage goes, so the focus comes back to a page that has it. */
    _dismiss() {
        var name = this._shown;
        if (!name) return;
        this._shown = null;
        this._at.placement().restore(name);
        this._layer.hide();
        this._tell({ kind: "Returned", widget: name });
    }
}
