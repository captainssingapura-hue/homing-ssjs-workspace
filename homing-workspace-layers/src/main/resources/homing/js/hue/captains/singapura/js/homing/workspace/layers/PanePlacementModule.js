// =============================================================================
// PanePlacement — the one pane, headless (RFC 0066 E3, the workspace detour):
// a placement — it mounts and unmounts widgets' panes, and never creates or
// closes one. The widgets mounted, in the order mounted, and the one it shows,
// or none. What a page's pane does with them — a slot in view — is the page's;
// what it SHOWS is this, the same on any page. Mounted "shown", a widget is
// shown; "behind", it joins the pane, the one shown kept. Unmounted while
// shown, the pane shows the next — the one mounted after it, else the one
// before, else none — and says so: the core executing a close unmounts first,
// so the pane's word comes before the widget is gone.
//
// The core's placement as it is (WorkspaceCore's placement port). Java's
// PanePlacement, step for step; the two agree (LayersParityTest).
//
//   new PanePlacement()
//   pane.mount(entry, location)   location "shown" (unless said) or "behind"
//   pane.unmount(entry)           shown no more — the next shown, when it was this one
//   pane.show(id | null)          a widget mounted shown, or none: the pane's own request
//   pane.shown() → the id shown, or null      pane.mounted() → the ids, in the order mounted
//   pane.on(fn) → off   { kind: "PaneShown", id: id | null } whenever what it shows changes
// =============================================================================

class PanePlacement {
    constructor() {
        this._mounted = [];
        this._shown = null;
        this._sinks = [];
    }

    mount(entry, location) {
        var id = entry.id, at = location == null ? "shown" : location;
        if (at !== "shown" && at !== "behind") throw new Error("[PanePlacement] a place in the pane is \"shown\" or \"behind\", not " + JSON.stringify(location));
        if (this._mounted.indexOf(id) >= 0) throw new Error("[PanePlacement] '" + id + "' is mounted already");
        this._mounted.push(id);
        if (at === "shown") this._set(id);
    }

    unmount(entry) {
        var id = entry.id, i = this._mounted.indexOf(id);
        if (i < 0) throw new Error("[PanePlacement] '" + id + "' is not mounted");
        this._mounted.splice(i, 1);
        if (this._shown === id) this._set(i < this._mounted.length ? this._mounted[i] : i > 0 ? this._mounted[i - 1] : null);
    }

    show(id) {
        if (id != null && this._mounted.indexOf(id) < 0) throw new Error("[PanePlacement] '" + id + "' is not mounted");
        this._set(id == null ? null : id);
    }

    shown() { return this._shown; }

    mounted() { return this._mounted.slice(); }

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
