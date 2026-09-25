// =============================================================================
// DemoWidgetsModule — two widgets for the demo workspace, written to the new
// contract and nothing more.
//
//   new NoteWidget(branch, params, host)      -> { root }
//   new CounterWidget(branch, params, host)   -> { root, keyDown }
//
// A WIDGET RUNS IN A ROOM and knows nothing else. It is handed a branch and
// gives back { root }. It does not touch the tab, the dock or the focus party:
// the room is the member of the party, and it wraps the element given back.
// Its one word to where it runs is its HOST: host.title(text) names its tab.
//
// The two show the two things a widget may be:
//
//   NoteWidget has no keys at all. It needs nothing - the room holds the tab's
//   place in the focus tree, and the note just sits in it.
//
//   CounterWidget has both worlds. Its buttons are native controls and take
//   the browser's focus the ordinary way; and it offers keyDown, so when the
//   tab holds the keys the room hands arrows on to it. It never asks the party
//   for anything. Its name is its count, so it tells its host on every change.
// =============================================================================

const _demoOwner = Object.freeze({ toString: () => "demoWidget" });

class NoteWidget {
    constructor(branch, params) {
        branch.activate(_demoOwner);
        var p = params || {};
        var root = branch.createElement("note", "div");
        css.addClass(root, dw_note);
        root.textContent = p.text || ("A note: a widget with no keys at all. It gave the room an element and "
            + "nothing else. Right-click the tab bar to part the room; right-click this tab to float it.");
        this.root = root;
    }
}

class CounterWidget {
    constructor(branch, params, host) {
        branch.activate(_demoOwner);
        var self = this;
        this._host = host || null;
        this._n = Number(params && params.start) || 0;

        var root = branch.createElement("counter", "div");
        css.addClass(root, dw_counter);

        var count = branch.createElement("count", "div");
        css.addClass(count, dw_count);
        root.appendChild(count);

        var row = branch.createElement("row", "div");
        css.addClass(row, dw_row);
        var dec = new ButtonBuilder().label("−").plain().onClick(function () { self._by(-1); })
            .build(branch.createElement("dec", "button"));
        var inc = new ButtonBuilder().label("+").onClick(function () { self._by(1); })
            .build(branch.createElement("inc", "button"));
        row.appendChild(dec.el);
        row.appendChild(inc.el);
        root.appendChild(row);

        var hint = branch.createElement("hint", "p");
        css.addClass(hint, dw_hint);
        hint.textContent = "The buttons take the browser's own focus. With the tab holding the keys, "
            + "↑ and ↓ count too — the room hands them on; the counter never asks for them.";
        root.appendChild(hint);

        this.root = root;
        this._count = count;
        this._draw();
    }

    _by(d) { this._n += d; this._draw(); }
    /** The number, and the name that is the number: the tab says it too. */
    _draw() {
        this._count.textContent = String(this._n);
        if (this._host) this._host.title("Counter · " + this._n);
    }

    /** Offered, not claimed: the room calls this with a key it holds. */
    keyDown(ev) {
        if (ev.altKey || ev.ctrlKey || ev.metaKey) return false;
        if (ev.key === "ArrowUp")   { this._by(1);  return true; }
        if (ev.key === "ArrowDown") { this._by(-1); return true; }
        return false;
    }
}
