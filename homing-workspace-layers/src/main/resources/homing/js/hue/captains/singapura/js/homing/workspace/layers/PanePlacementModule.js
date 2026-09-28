// =============================================================================
// PanePlacement — the one pane, headless (RFC 0066 E3, the workspace detour):
// the widgets lent to it, in the order lent, and the one it shows, or none.
// What a page's pane does with them — a slot for each, one in view — is the
// page's; what it SHOWS is this, the same on any page. When the widget shown
// is taken back, the pane shows the next — the one lent after it, else the one
// before, else none — and says so before the widget is gone from the roster,
// so its log says what it shows next before the widget closes.
//
// Java's PanePlacement, step for step; the two agree (LayersParityTest).
//
//   new PanePlacement()
//   pane.lend(id)       a widget lent a place: not shown
//   pane.release(id)    taken back: shown no more — the next shown, when it was this one
//   pane.show(id | null)   a widget lent shown, or none
//   pane.shown() → the id shown, or null      pane.lent() → the ids, in the order lent
//   pane.on(fn) → off   { kind: "PaneShown", id: id | null } whenever what it shows changes
// =============================================================================

class PanePlacement {
    constructor() {
        this._lent = [];
        this._shown = null;
        this._sinks = [];
    }

    lend(id) {
        if (this._lent.indexOf(id) >= 0) throw new Error("[PanePlacement] '" + id + "' is lent already");
        this._lent.push(id);
    }

    release(id) {
        var i = this._lent.indexOf(id);
        if (i < 0) throw new Error("[PanePlacement] '" + id + "' is not lent");
        this._lent.splice(i, 1);
        if (this._shown === id) this._set(i < this._lent.length ? this._lent[i] : i > 0 ? this._lent[i - 1] : null);
    }

    show(id) {
        if (id != null && this._lent.indexOf(id) < 0) throw new Error("[PanePlacement] '" + id + "' is not lent");
        this._set(id == null ? null : id);
    }

    shown() { return this._shown; }

    lent() { return this._lent.slice(); }

    on(fn) {
        if (typeof fn !== "function") throw new Error("[PanePlacement] on wants a function");
        var sinks = this._sinks;
        sinks.push(fn);
        return function () { var i = sinks.indexOf(fn); if (i >= 0) sinks.splice(i, 1); };
    }

    _set(id) {
        if (id === this._shown) return;
        this._shown = id;
        var n = Object.freeze({ kind: "PaneShown", id: id }), sinks = this._sinks.slice();
        for (var i = 0; i < sinks.length; i++) sinks[i](n);
    }
}
