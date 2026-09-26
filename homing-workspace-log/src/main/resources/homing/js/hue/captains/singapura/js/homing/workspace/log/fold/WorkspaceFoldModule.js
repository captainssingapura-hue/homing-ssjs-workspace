// =============================================================================
// WorkspaceFold — a workspace log's meaning: its events folded, from the
// workspace's opening, into the WorkspaceState they leave. Each event must be
// possible where it falls, and one that is not is refused, naming it. Java's
// WorkspaceFold, transcribed: the two agree to the byte.
//
//   WorkspaceFold.opening()              → the WorkspaceState every log starts from
//   WorkspaceFold.apply(state, event)    → the WorkspaceState after it
//   WorkspaceFold.fold(header, events)   → a FoldedState: through the last event
//
// A tab leaving a host takes the host's showing with it when it was the one
// shown — a reorder within the host keeps it — and whatever the host shows
// next, the log says. A region's room goes as the grid's did (LayoutAlgebra);
// a float raised goes to the top of the stack.
// =============================================================================

function _no(why) { throw new Error("[WorkspaceFold] " + why); }

function _hostName(host) { return (host instanceof InRegion ? "region " : "float ") + host.id.value; }

class WorkspaceFold {
    static opening() {
        var main = new RegionId(WorkspaceState.OPENING_REGION);
        return new WorkspaceState(new Cell(main), [new RegionState(main, [], null)], [], []);
    }

    static fold(header, events) {
        var s = WorkspaceFold.opening(), through = 0;
        for (var i = 0; i < events.length; i++) {
            try { s = WorkspaceFold.apply(s, events[i].event); }
            catch (e) { throw new Error("[WorkspaceFold] seq " + events[i].seq.value + ": " + events[i].event.constructor.name + " cannot be: " + e.message); }
            through = events[i].seq.value;
        }
        return new FoldedState(header, new EventSeq(through), s);
    }

    static apply(state, e) {
        var w = new _Working(state), h, t, f;
        if (e instanceof TabOpened) {
            if (w.tabs.has(e.id.value)) _no("the tab " + e.id.value + " is already open");
            w.insert(e.host, e.id.value, e.index);
            w.tabs.set(e.id.value, new TabState(e.id, e.kind, e.title));
        } else if (e instanceof TabBecame) {
            t = w.open(e.id.value);
            w.tabs.set(e.id.value, new TabState(t.id, e.kind, e.title));
        } else if (e instanceof TabRenamed) {
            t = w.open(e.id.value);
            w.tabs.set(e.id.value, new TabState(t.id, t.kind, e.title));
        } else if (e instanceof TabMoved) {
            w.open(e.id.value);
            var from = w.hostOf(e.id.value);
            var kept = _sameHost(from, e.host) && w.hosted(from).shown === e.id.value;
            w.takeOut(e.id.value);
            w.insert(e.host, e.id.value, e.index);
            if (kept) w.hosted(from).shown = e.id.value;
        } else if (e instanceof TabShown) {
            h = w.hosted(e.host);
            if (h.tabs.indexOf(e.id.value) < 0) _no("the " + _hostName(e.host) + " does not hold " + e.id.value);
            h.shown = e.id.value;
        } else if (e instanceof TabClosed) {
            w.open(e.id.value);
            w.takeOut(e.id.value);
            w.tabs.delete(e.id.value);
        } else if (e instanceof RegionParted) {
            w.layout = LayoutAlgebra.subdivide(w.layout, e.region, e.side, e.newRegion);
            w.regions.set(e.newRegion.value, _hosted());
        } else if (e instanceof RegionRemoved) {
            h = w.hosted(new InRegion(e.region));
            if (h.tabs.length) _no("the region " + e.region.value + " still holds " + h.tabs.join(", "));
            w.layout = LayoutAlgebra.remove(w.layout, e.region, e.toward);
            w.regions.delete(e.region.value);
        } else if (e instanceof TracksChanged) {
            w.layout = LayoutAlgebra.tracks(w.layout, e.path.value, e.shares);
        } else if (e instanceof FloatOpened) {
            if (w.floats.has(e.id.value)) _no("the float " + e.id.value + " is already open");
            f = _hosted();
            f.x = e.x; f.y = e.y; f.w = e.w; f.h = e.h;
            w.floats.set(e.id.value, f);
        } else if (e instanceof FloatMoved) {
            f = w.floatOf(e.id); f.x = e.x; f.y = e.y;
        } else if (e instanceof FloatResized) {
            f = w.floatOf(e.id); f.w = e.w; f.h = e.h;
        } else if (e instanceof FloatRaised) {
            f = w.floatOf(e.id);
            w.floats.delete(e.id.value);
            w.floats.set(e.id.value, f);
        } else if (e instanceof FloatClosed) {
            f = w.floatOf(e.id);
            if (f.tabs.length) _no("the float " + e.id.value + " still holds " + f.tabs.join(", "));
            w.floats.delete(e.id.value);
        } else {
            _no("not a WorkspaceEvent: " + JSON.stringify(e));
        }
        return w.state();
    }
}

function _hosted() { return { tabs: [], shown: null, x: 0, y: 0, w: 0, h: 0 }; }

function _sameHost(a, b) { return a.constructor === b.constructor && a.id.value === b.id.value; }

/** The state while the fold works on it: hosts by id, tabs by id, in the orders the state keeps. */
class _Working {
    constructor(s) {
        this.layout = s.layout;
        this.regions = new Map();
        this.floats = new Map();      // bottom first
        this.tabs = new Map();        // by id; the state lists them in the order the hosts hold them
        var self = this;
        s.regions.forEach(function (r) { self.regions.set(r.id.value, { tabs: r.tabs.map(function (t) { return t.value; }), shown: r.shown ? r.shown.value : null }); });
        s.floats.forEach(function (f) {
            self.floats.set(f.id.value, { tabs: f.tabs.map(function (t) { return t.value; }), shown: f.shown ? f.shown.value : null, x: f.x, y: f.y, w: f.w, h: f.h });
        });
        s.tabs.forEach(function (t) { self.tabs.set(t.id.value, t); });
    }

    open(id) {
        var t = this.tabs.get(id);
        if (!t) _no("the tab " + id + " is not open");
        return t;
    }

    hosted(host) {
        var h = host instanceof InRegion ? this.regions.get(host.id.value) : this.floats.get(host.id.value);
        if (!h) _no("there is no " + _hostName(host));
        return h;
    }

    floatOf(id) { return this.hosted(new InFloat(id)); }

    hostOf(id) {
        for (var [r, h] of this.regions) if (h.tabs.indexOf(id) >= 0) return new InRegion(new RegionId(r));
        for (var [f, g] of this.floats) if (g.tabs.indexOf(id) >= 0) return new InFloat(new FloatId(f));
        _no("the tab " + id + " is in no host");
    }

    insert(host, id, index) {
        var h = this.hosted(host);
        if (index > h.tabs.length) _no("the " + _hostName(host) + " holds " + h.tabs.length + " tabs: no index " + index);
        h.tabs.splice(index, 0, id);
    }

    takeOut(id) {
        var h = this.hosted(this.hostOf(id));
        h.tabs.splice(h.tabs.indexOf(id), 1);
        if (h.shown === id) h.shown = null;
    }

    state() {
        var self = this, tab = function (id) { return new TabId(id); };
        var cells = _cellsOf(this.layout);
        var regions = cells.map(function (r) {
            var h = self.regions.get(r);
            if (!h) _no("the layout has a region " + r + " the fold does not");
            return new RegionState(new RegionId(r), h.tabs.map(tab), h.shown === null ? null : tab(h.shown));
        });
        if (regions.length !== this.regions.size) _no("the fold has regions the layout does not");
        var floats = [];
        this.floats.forEach(function (h, f) { floats.push(new FloatState(new FloatId(f), h.x, h.y, h.w, h.h, h.tabs.map(tab), h.shown === null ? null : tab(h.shown))); });
        var listed = [];
        regions.concat(floats).forEach(function (h) { h.tabs.forEach(function (t) { listed.push(self.tabs.get(t.value)); }); });
        return new WorkspaceState(this.layout, regions, floats, listed);
    }
}

function _cellsOf(l) {
    if (l instanceof Cell) return [l.region.value];
    var out = [];
    l.tracks.forEach(function (t) { out = out.concat(_cellsOf(t.node)); });
    return out;
}
