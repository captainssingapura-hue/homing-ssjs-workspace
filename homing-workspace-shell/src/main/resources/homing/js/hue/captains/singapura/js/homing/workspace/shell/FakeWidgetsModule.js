// =============================================================================
// FakeWidgets — stand-ins for the workspace's widgets while the tabs are built
// (RFC 0066 E3, the workspace detour, step two). Each is a tab's widget by the
// dock's law, as the gallery's are: it joins the dock's focus branch itself,
// handed in params.focus, a press on its root claims it, activate() claims it,
// and Escape gives the keys back to the dock. Nothing of the workspace's.
//
//   new FakeNote(branch, params)      a paragraph: holds a place, and nothing else
//   new FakeCounter(branch, params)   native − and + buttons; ↑ ↓ count while it holds
//   new FakeField(branch, params)     a native input it puts the focus in when it holds
//     params: TabSource's - { focus, id, title, pane, tab }
// =============================================================================

const _fakeOwner = Object.freeze({ toString: () => "fakeWidget" });

/** The law's three things, done once: a member of the dock's branch, a press claiming it, and the leaving. */
function _join(w, branch, params) { w.focus = params.focus.join(branch.name, w); w._off = Keys.claimOn(w.root, w.focus); }
function _leave(w) { if (w._off) w._off(); if (w.focus && w.focus.in) w.focus.leave(); }

class FakeNote {
    constructor(branch, params) {
        branch.activate(_fakeOwner);
        var root = branch.createElement("note", "div");
        css.addClass(root, ws_fake);
        root.textContent = (params.title || "A note") + " — a fake widget. It holds a place, joins the dock's branch itself, and gives the keys back on Escape.";
        this.root = root;
        _join(this, branch, params);
    }
    activate() { Keys.claim(this.focus); }
    keyDown(ev) { if (ev.key === "Escape") { Keys.yield(this.focus); return true; } return false; }
    dispose() { _leave(this); }
}

class FakeCounter {
    constructor(branch, params) {
        branch.activate(_fakeOwner);
        var self = this;
        this._n = 0;
        var root = branch.createElement("counter", "div");
        css.addClass(root, ws_fake);
        var count = branch.createElement("count", "div");
        css.addClass(count, ws_fake_count);
        root.appendChild(count);
        var row = branch.createElement("row", "div");
        css.addClass(row, ws_fake_row);
        [["dec", "−", -1], ["inc", "+", 1]].forEach(function (b) {
            var btn = branch.createElement(b[0], "button");
            btn.type = "button";
            btn.textContent = b[1];
            btn.addEventListener("click", function () { self._by(b[2]); });
            row.appendChild(btn);
        });
        root.appendChild(row);
        var hint = branch.createElement("hint", "p");
        hint.textContent = "The buttons take the browser's own focus. While the tab holds the keys, ↑ and ↓ count too; Escape gives them back.";
        root.appendChild(hint);
        this.root = root;
        this._count = count;
        this._by(0);
        _join(this, branch, params);
    }
    _by(d) { this._n += d; this._count.textContent = String(this._n); }
    activate() { Keys.claim(this.focus); }
    keyDown(ev) {
        if (ev.altKey || ev.ctrlKey || ev.metaKey) return false;
        if (ev.key === "ArrowUp") { this._by(1); return true; }
        if (ev.key === "ArrowDown") { this._by(-1); return true; }
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }
    dispose() { _leave(this); }
}

class FakeField {
    constructor(branch, params) {
        branch.activate(_fakeOwner);
        var root = branch.createElement("field", "div");
        css.addClass(root, ws_fake);
        var label = branch.createElement("label", "label");
        label.textContent = "A fake field";
        var input = branch.createElement("input", "input");
        input.type = "text";
        input.setAttribute("aria-label", "a fake field");
        label.appendChild(input);
        root.appendChild(label);
        this.root = root;
        this._input = input;
        _join(this, branch, params);
    }
    activate() { Keys.claim(this.focus); }
    /** Holding the keys, it hands them to its one control: the input takes the browser's focus. */
    granted() { try { this._input.focus(); } catch (e) {} }
    keyDown(ev) { if (ev.key === "Escape") { Keys.yield(this.focus); return true; } return false; }
    dispose() { _leave(this); }
}
