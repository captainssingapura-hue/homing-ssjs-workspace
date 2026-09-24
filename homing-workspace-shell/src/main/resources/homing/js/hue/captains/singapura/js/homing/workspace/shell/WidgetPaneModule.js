// =============================================================================
// WidgetPaneModule — the PANE half of a tab-pane: the room a widget runs in.
//
//   var room = new WidgetPane(branch, { focus })     focus: the dock's focus branch
//   room.root                  the element a dock shows, and a desk floats
//   room.focus                 the room's membership of the focus party
//   room.branchFor(name)       a branch for the widget, under the room's own
//   room.setWidget(widget)     put a widget in the room; the one before goes
//   room.widget()              the widget in the room, or null
//   room.say(text)             a status line as the tenant, until a widget arrives
//   room.activate() .keyDown(ev) .dispose()
//
// A tab-pane is two parts. The TAB is the dock's: its chip, its title, its
// place in the strip. The PANE is this: a member of the focus party that wraps
// whatever element the widget gives it. The widget runs INSIDE and touches
// neither — it is handed a branch and gives back { root }, and that is the
// whole of its obligation.
//
// So a dock's law — a tab's widget must be a focus member with a root and
// activate() — is kept by the ROOM, never by the widget. A widget with no keys
// at all, or only native ones (a form, a grid of inputs), needs nothing: the
// room holds the tab's place in the focus tree, and the native world inside
// does what it always does. A widget that does want keys offers keyDown(ev)
// and activate(), and the room hands them on.
//
// The room owns a branch of its own, under the holder's and not the dock's, so
// it TRAVELS: a dock that lets a tab go takes the room's root out of its panel
// and dissolves only its own branch, and the room — root, membership, widget
// and all — goes wherever the tab goes. That is why a floated tab keeps its
// widget. The widget's branch is made under the room's, so the room's going
// takes the widget's with it: dock → room → widget, one nesting.
//
// A room can change tenant without the dock noticing — the tab keeps its chip,
// its title and its place, and only what is in the room changes. That is what
// a picker is: a tab whose first tenant is the chooser.
// =============================================================================

const _roomOwner = Object.freeze({ toString: () => "widgetPane" });

class WidgetPane {

    constructor(branch, opts) {
        if (!branch) throw new Error("[WidgetPane] a branch of its own is required");
        if (!opts || !opts.focus || typeof opts.focus.join !== "function") {
            throw new Error("[WidgetPane] opts.focus must be the dock's focus branch");
        }
        branch.activate(_roomOwner);
        this._branch = branch;
        this._widget = null;
        this._seq = 0;

        var root = branch.createElement("room", "div");
        css.addClass(root, wp_host);
        // -1: never in the Tab order - a tab is reached by its chip or the
        // dock's keys, never by tabbing into the middle of a workspace - but
        // able to take focus when the dock rests the keys here.
        root.setAttribute("tabindex", "-1");
        this.root = root;

        this.focus = opts.focus.join(branch.name, this);
        this._off = Keys.claimOn(root, this.focus);
    }

    /** A branch for a widget to be built on, under the room's own. */
    branchFor(name) {
        return this._branch.createBranch(String(name || "w" + (++this._seq)));
    }

    /**
     * A room with nothing in it yet says so - loading, failed, waiting - as
     * its tenant, on a branch of its own, so the next setWidget retires it
     * like any other.
     */
    say(text) {
        var b = this.branchFor("say" + (++this._seq));
        b.activate(_roomOwner);
        var el = b.createElement("text", "div");
        el.setAttribute("role", "status");
        el.textContent = String(text);
        return this.setWidget({ root: el, dispose: function () { try { b.dissolve(); } catch (e) {} } });
    }

    /** The widget in the room, or null. */
    widget() { return this._widget; }

    /**
     * Put a widget in the room. The one that was here is disposed: a room has
     * one tenant, and a tenant it no longer holds is nobody's to clean up.
     */
    setWidget(widget) {
        if (!widget || !widget.root) throw new Error("[WidgetPane] a widget gives back { root }");
        var was = this._widget;
        if (was) {
            if (was.root && was.root.parentNode === this.root) this.root.removeChild(was.root);
            WidgetPane._retire(was);
        }
        this._widget = widget;
        this.root.appendChild(widget.root);
        return this;
    }

    // ── The member: what the dock and the steward ask of the room ───────────

    /**
     * The keys arrive here. A widget that wants them says how - it may focus a
     * native control of its own - and a widget that does not leaves them with
     * the room, which holds the tab's place.
     */
    activate() {
        var w = this._widget;
        if (w && typeof w.activate === "function") { w.activate(); return; }
        Keys.claim(this.focus);
    }

    /** The steward routed a key here: the widget's if it takes it, Escape gives them back. */
    keyDown(ev) {
        var w = this._widget;
        if (w && typeof w.keyDown === "function" && w.keyDown(ev)) return true;
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    granted()   { this.root.setAttribute("data-keys", "held"); }
    taken()     { this.root.removeAttribute("data-keys"); }
    offered()   { if (this.root.getAttribute("data-keys") === null) this.root.setAttribute("data-keys", "candidate"); }
    withdrawn() { if (this.root.getAttribute("data-keys") === "candidate") this.root.removeAttribute("data-keys"); }

    /** A widget that keeps a lifecycle hears whether its tab is the one showing. */
    setActive(on) {
        var w = this._widget;
        if (w && typeof w.setActive === "function") { try { w.setActive(!!on); } catch (e) {} }
    }

    /** The room goes, and everything in it: the widget, the membership, the branch. */
    dispose() {
        if (this._widget) { WidgetPane._retire(this._widget); this._widget = null; }
        if (this._off) { this._off(); this._off = null; }
        if (this.focus && this.focus.in) this.focus.leave();
        try { this._branch.dissolve(); } catch (e) {}
    }

    /** Whatever a widget offers for going away; a widget that offers nothing has nothing to release. */
    static _retire(w) {
        try { if (typeof w.dispose === "function") w.dispose(); } catch (e) { console.error("[WidgetPane] widget dispose threw", e); }
        try { if (typeof w.partyDeregister === "function") w.partyDeregister(); } catch (e) {}
    }
}
