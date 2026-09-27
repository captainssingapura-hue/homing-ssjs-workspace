// =============================================================================
// BenchMonitors — the bench's monitors, afloat: a bar of square toggles, one
// per monitor, and a desk whose floats lie over the page. A toggle turned on
// opens its monitor as a tab-pane in a float of its own — the monitor a
// self-contained widget hosted in the tab (HostedWidget), its parties grafted
// where the tab puts it — and turned off closes that tab. However a monitor's
// tab goes — its cross, its float's cross, dragged into another float — the
// bar says what is open: it reads the desk after every report. A float opened
// again comes back where it was last left.
//
// The desk takes no steward: the monitors are members of the focus tree, a
// press on one claims the keys for it, and its Escape gives them back.
//
//   new BenchMonitors(branch, { host, monitors })
//     branch     its own, unactivated: the bar minted on it, the desk under it
//     host       the page, positioned: the bar goes in it, the floats lie over it
//     monitors   [{ kind, title, mark, Widget }], in the bar's order: the mark is
//                the toggle's face, the title its name
//   bench.root              the bar
//   bench.toggle(kind, on?) → whether it is open after: flipped, or as said
//   bench.isOpen(kind)      → whether its tab is open
//   bench.desk
//   bench.dispose()
// =============================================================================

const _benchMonitorsOwner = Object.freeze({ toString: () => "benchMonitors" });

class BenchMonitors {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[BenchMonitors] a branch of its own is required");
        if (!o.host) throw new Error("[BenchMonitors] opts.host is required: the page the floats lie over");
        branch.activate(_benchMonitorsOwner);
        this.branch = branch;
        this._host = o.host;
        this._monitors = (o.monitors || []).slice();
        this._open = {};      // kind → its tab-pane, while open
        this._rects = {};     // kind → where its float was last left
        this._toggles = {};
        var bar = branch.createElement("bar", "div");
        css.addClass(bar, wb_bar);
        bar.setAttribute("role", "toolbar");
        bar.setAttribute("aria-label", "Monitors");
        this._monitors.forEach(function (m) {
            var b = branch.createElement("toggle-" + m.kind, "button");
            b.type = "button";
            css.addClass(b, wb_toggle);
            b.textContent = m.mark;
            b.title = m.title;
            b.setAttribute("aria-label", m.title);
            b.setAttribute("aria-pressed", "false");
            b.addEventListener("click", function () { self.toggle(m.kind); });
            bar.appendChild(b);
            self._toggles[m.kind] = b;
        });
        o.host.appendChild(bar);
        this.root = bar;
        this.desk = new Desk(branch.createBranch("desk"), { host: o.host, focusName: "monitors", onEvent: function (ev) { self._heard(ev); } });
    }

    isOpen(kind) { var tp = this._open[kind]; return !!tp && !tp.closed(); }

    toggle(kind, on) {
        var m = this._monitor(kind), was = this.isOpen(kind), want = on == null ? !was : !!on;
        if (want !== was) {
            if (want) this._float(m);
            else this._open[kind].close();
        }
        this._sync();
        return this.isOpen(kind);
    }

    /** Its monitor in a float of its own, where it was last left, or in its place down the page's right. */
    _float(m) {
        var r = this._rects[m.kind] || this._placed(m), spec = { x: r.x, y: r.y, w: r.w, h: r.h };
        if (!this.desk.layer.has(m.kind)) spec.id = m.kind;   // named as its monitor, unless a float of that name still holds other tabs
        var f = this.desk.float(spec);
        this._open[m.kind] = this.desk.open({ id: m.kind, title: m.title,
            make: function (branch, tab) { return new HostedWidget(branch, tab, m.Widget, {}); } }, f.host, null, "front");
    }

    /** A first place: the page's right edge, each monitor a step down and in from the one before. */
    _placed(m) {
        var i = this._monitors.indexOf(m), w = BenchMonitors.W, h = BenchMonitors.H;
        return { x: Math.max(8, this._host.clientWidth - w - 16 - i * 28), y: 56 + i * 40, w: w, h: h };
    }

    /** A report of the desk: where a monitor's float went, and then the bar read again. */
    _heard(ev) {
        var r = ev && this._monitorOf(ev.id) ? (this._rects[ev.id] = this._rects[ev.id] || {}) : null;
        if (r && (ev.kind === "Opened" || ev.kind === "Moved")) { r.x = ev.x; r.y = ev.y; }
        if (r && (ev.kind === "Opened" || ev.kind === "Resized")) { r.w = ev.w; r.h = ev.h; }
        this._sync();
    }

    /** Each toggle as its tab is: pressed while open. */
    _sync() {
        var self = this;
        this._monitors.forEach(function (m) {
            if (self._open[m.kind] && self._open[m.kind].closed()) delete self._open[m.kind];
            var on = self.isOpen(m.kind), b = self._toggles[m.kind];
            b.setAttribute("aria-pressed", on ? "true" : "false");
            css.toggleClass(b, wb_toggle_on, on);
        });
    }

    _monitorOf(kind) {
        for (var i = 0; i < this._monitors.length; i++) if (this._monitors[i].kind === kind) return this._monitors[i];
        return null;
    }

    _monitor(kind) {
        var m = this._monitorOf(kind);
        if (!m) throw new Error("[BenchMonitors] no monitor '" + kind + "'");
        return m;
    }

    dispose() {
        this.desk.dispose();
        this.branch.dissolve();
    }
}

/** A monitor's float, first opened. */
BenchMonitors.W = 380;
BenchMonitors.H = 280;
