// =============================================================================
// RosterFold — the roster's layer of the fold, the core's: a widget opened is
// held, in the order opened, under no name, and its prefix's last sequence is
// its own; a widget renamed keeps the name given, or none when it is taken back; a
// widget closed is held no more. A widget opened must not be held already, and
// its sequence must be past the last its prefix gave, closed or not — an id is
// never given again; a widget closed must be held. Java's RosterFold,
// transcribed: the two agree to the byte.
//
//   RosterFold.empty()           → the RosterState every log starts from
//   RosterFold.apply(state, e)   → the RosterState after it
//   RosterFold.holds(state, id)  → whether it holds the widget of that WidgetId
//   RosterFold.split(id)         → { prefix, sequence } of a WidgetId
// =============================================================================

function _rosterNo(why) { throw new Error("[RosterFold] " + why); }

class RosterFold {
    static empty() { return new RosterState([], []); }

    static holds(state, id) {
        return state.widgets.some(function (w) { return w.id.value === id.value; });
    }

    static split(id) {
        var at = id.value.lastIndexOf("-"), digits = id.value.slice(at + 1);
        if (at < 1 || digits.length > WidgetId.SEQUENCE_DIGITS) _rosterNo("'" + id.value + "' is not a prefix and a sequence of at most " + WidgetId.SEQUENCE_DIGITS + " digits");
        return { prefix: id.value.slice(0, at), sequence: parseInt(digits, 10) };
    }

    static apply(state, e) {
        var widgets = state.widgets.slice(), last = new Map();
        state.sequences.forEach(function (s) { last.set(s.prefix, s.last); });
        if (e instanceof WidgetOpened) {
            if (RosterFold.holds(state, e.id)) _rosterNo("the widget " + e.id.value + " is already open");
            var s = RosterFold.split(e.id), reached = last.has(s.prefix) ? last.get(s.prefix) : 0;
            if (s.sequence <= reached) {
                _rosterNo("the widget " + e.id.value + " is not past " + s.prefix + "-" + reached + ", the last its prefix gave: an id is never given again");
            }
            widgets.push(new RosterEntry(e.id, e.kind, e.params, null));
            last.set(s.prefix, s.sequence);
        } else if (e instanceof WidgetRenamed) {
            if (!RosterFold.holds(state, e.id)) _rosterNo("the widget " + e.id.value + " is not open");
            widgets = widgets.map(function (w) { return w.id.value === e.id.value ? new RosterEntry(w.id, w.kind, w.params, e.name) : w; });
        } else if (e instanceof WidgetClosed) {
            if (!RosterFold.holds(state, e.id)) _rosterNo("the widget " + e.id.value + " is not open");
            widgets = widgets.filter(function (w) { return w.id.value !== e.id.value; });
        } else {
            _rosterNo("not the roster's: " + (e && e.constructor ? e.constructor.name : JSON.stringify(e)));
        }
        var prefixes = Array.from(last.keys()).sort(function (a, b) { return a < b ? -1 : a > b ? 1 : 0; });
        return new RosterState(widgets, prefixes.map(function (p) { return new PrefixSequence(p, last.get(p)); }));
    }
}
