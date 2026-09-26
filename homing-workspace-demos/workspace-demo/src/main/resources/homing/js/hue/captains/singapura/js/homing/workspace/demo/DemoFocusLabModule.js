// =============================================================================
// DemoFocusLabModule — the Focus lab: widgets that act on each other's focus,
// to stress the steward's marker (RFC 0066 E3, keyboard §17.5).
//
//   new FormWidget(branch, params, host)      native fields in a group of its own, and a list:
//                                             two logical members under the room
//   new SummonerWidget(branch, params, host)  by script, on any form open: its field focused,
//                                             or its list claimed — now, or after two seconds
//   new MonitorWidget(branch, params, host)   the steward's lamp and the focus tree
//
// The forms say themselves to the lab as they come and go, and the summoner
// lists them. NOTHING HERE SAYS WHERE THE FOCUS IS: the steward marks the room,
// the group and the list, and moves the marker however the focus arrived — a
// press, a Tab, a script. A focus into a tab not on show cannot land, as in any
// browser; a claim can, and the pane shows the tab it lands in.
// =============================================================================

const _labOwner = Object.freeze({ toString: () => "focusLab" });
var _forms = [], _listeners = [], _made = 0;
var _LATER_MS = 2000;

function _changed() {
    _listeners.slice().forEach(function (fn) { try { fn(); } catch (e) { console.error("[FocusLab] a listener threw", e); } });
}

/** A logical member of a form's: a box whose own element is its root; Escape gives its keys up, to the room. */
class LabGroup {
    constructor(root, focus, name) {
        this.root = root;
        this.focus = focus.join(name, this);
        this._off = Keys.claimOn(root, this.focus);
    }
    keyDown(ev) { if (ev.key === "Escape") { Keys.yield(this.focus); return true; } return false; }
    dispose() { this._off(); if (this.focus.in) this.focus.leave(); }
}

/** A list of three, a logical member: ↑ ↓ move its choice while it holds the keys. */
class LabList extends LabGroup {
    constructor(branch, focus, name) {
        branch.activate(_labOwner);
        var root = branch.createElement("list", "div");
        css.addClass(root, dw_lab_group);
        root.setAttribute("role", "listbox");
        root.setAttribute("aria-label", name);
        super(root, focus, name);
        this._items = ["Alpha", "Beta", "Gamma"].map(function (t, i) {
            var it = branch.createElement("item" + i, "div");
            css.addClass(it, dw_lab_item);
            it.setAttribute("role", "option");
            it.textContent = t;
            root.appendChild(it);
            return it;
        });
        this._at = 0;
        this._draw();
    }
    keyDown(ev) {
        if (ev.key !== "ArrowDown" && ev.key !== "ArrowUp") return super.keyDown(ev);
        this._at = (this._at + (ev.key === "ArrowDown" ? 1 : this._items.length - 1)) % this._items.length;
        this._draw();
        return true;
    }
    _draw() {
        var at = this._at;
        this._items.forEach(function (it, i) { css.toggleClass(it, dw_lab_item_on, i === at); it.setAttribute("aria-selected", String(i === at)); });
    }
}

class FormWidget {
    constructor(branch, params, host) {
        branch.activate(_labOwner);
        this.name = "Form " + (++_made);
        if (host) host.title(this.name);
        var root = branch.createElement("form", "div");
        css.addClass(root, dw_lab);
        var fields = branch.createElement("fields", "div");
        css.addClass(fields, dw_lab_group);
        fields.setAttribute("role", "group");
        fields.setAttribute("aria-label", this.name + " fields");
        var name = branch.createElement("name", "input");
        name.type = "text";
        name.placeholder = "A name";
        name.setAttribute("aria-label", "name");
        var pick = branch.createElement("pick", "select");
        pick.setAttribute("aria-label", "shelf");
        ["Fiction", "History", "Science"].forEach(function (t, i) {
            var o = branch.createElement("opt" + i, "option");
            o.textContent = t;
            pick.appendChild(o);
        });
        var note = branch.createElement("note", "textarea");
        note.setAttribute("aria-label", "note");
        note.rows = 2;
        [name, pick, note].forEach(function (e) { fields.appendChild(e); });
        root.appendChild(fields);
        this._name = name;
        this._group = host && host.focus ? new LabGroup(fields, host.focus, "fields") : null;
        this._list = host && host.focus ? new LabList(branch.createBranch("list"), host.focus, "list") : null;
        if (this._list) root.appendChild(this._list.root);
        var hint = branch.createElement("hint", "p");
        css.addClass(hint, dw_hint);
        hint.textContent = "Click a field, Tab through them, or have the summoner do it: the steward marks the group lent, "
            + "the room and its tab follow. Escape leaves a field, then the group, then the room. The list is logical: ↑ ↓ while it holds.";
        root.appendChild(hint);
        this.root = root;
        _forms.push(this);
        _changed();
    }
    /** By script, as a page would: the browser's focus straight onto the field. The steward does the rest, or nothing when the tab is not on show. */
    focusField() { this._name.focus(); }
    /** By script: the list claims the keys — a logical focus, which reaches a tab not on show. */
    claimList() { if (this._list) Keys.claim(this._list.focus); }
    dispose() {
        var i = _forms.indexOf(this);
        if (i >= 0) _forms.splice(i, 1);
        if (this._list) this._list.dispose();
        if (this._group) this._group.dispose();
        _changed();
    }
}

class SummonerWidget {
    constructor(branch, params, host) {
        branch.activate(_labOwner);
        var self = this;
        this.branch = branch;
        this._n = 0;
        this._rowsBranch = null;
        var root = branch.createElement("summoner", "div");
        css.addClass(root, dw_lab);
        var head = branch.createElement("head", "label");
        css.addClass(head, dw_lab_row);
        var later = branch.createElement("later", "input");
        later.type = "checkbox";
        head.appendChild(later);
        head.appendChild(branch.createElement("later-say", "span")).textContent = "after two seconds — time to go elsewhere first";
        root.appendChild(head);
        var rows = branch.createElement("rows", "div");
        css.addClass(rows, dw_lab);
        root.appendChild(rows);
        var hint = branch.createElement("hint", "p");
        css.addClass(hint, dw_hint);
        hint.textContent = "A form's field focused by script moves the marker there — its tab's pane lit, its float raised; "
            + "a list claimed also shows its tab. Watch the monitor's lamp: it turns red if the steward ever disagrees with the page.";
        root.appendChild(hint);
        this.root = root;
        this._later = later;
        this._rows = rows;
        this._redraw = function () { self._draw(); };
        _listeners.push(this._redraw);
        this._draw();
    }
    _draw() {
        if (this._rowsBranch) this.branch.dissolveBranch(this._rowsBranch.name);
        while (this._rows.firstChild) this._rows.removeChild(this._rows.firstChild);
        var b = this.branch.createBranch("rows-" + (++this._n)), self = this;
        b.activate(_labOwner);
        this._rowsBranch = b;
        if (!_forms.length) {
            var none = b.createElement("none", "p");
            css.addClass(none, dw_hint);
            none.textContent = "No form open: add one from the + of any pane.";
            this._rows.appendChild(none);
        }
        _forms.forEach(function (f, i) {
            var row = b.createElement("row" + i, "div");
            css.addClass(row, dw_lab_row);
            var who = b.createElement("who" + i, "span");
            who.textContent = f.name;
            row.appendChild(who);
            row.appendChild(new ButtonBuilder().label("Focus its field").onClick(function () { self._act(function () { f.focusField(); }); }).build(b.createElement("field" + i, "button")).el);
            row.appendChild(new ButtonBuilder().label("Claim its list").plain().onClick(function () { self._act(function () { f.claimList(); }); }).build(b.createElement("list" + i, "button")).el);
            self._rows.appendChild(row);
        });
    }
    _act(fn) { if (this._later.checked) setTimeout(fn, _LATER_MS); else fn(); }
    dispose() {
        var i = _listeners.indexOf(this._redraw);
        if (i >= 0) _listeners.splice(i, 1);
    }
}

class MonitorWidget {
    constructor(branch) {
        branch.activate(_labOwner);
        var root = branch.createElement("monitor", "div");
        css.addClass(root, dw_lab);
        this._lamp = new StewardMonitor(branch.createBranch("lamp"), { host: root });
        this._tree = new FocusMonitor(branch.createBranch("tree"), { host: root });
        this.root = root;
    }
    dispose() { this._lamp.dispose(); this._tree.dispose(); }
}
