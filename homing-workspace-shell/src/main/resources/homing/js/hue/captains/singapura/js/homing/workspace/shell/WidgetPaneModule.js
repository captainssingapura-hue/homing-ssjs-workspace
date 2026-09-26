// =============================================================================
// WidgetPaneModule — the PANE half of a tab-pane: the room a widget runs in.
//
//   var room = new WidgetPane(branch, { focus, onTitle?, parties? })
//     focus     the dock's focus branch
//     onTitle   (text) the widget named itself: the holder puts it on the tab
//     parties   the parties the workspace exposes to its widgets, by name
//   room.root                  the element a dock shows, and a desk floats
//   room.focus                 the room's membership of the focus party: it holds a branch,
//                              where the widget's own logical members join
//   room.branchFor(name)       a branch for the widget, under the room's own
//   room.host()                the widget's HOST: what it may say to where it runs, handed to
//                              it at construction - { title(text), parties, focus }
//   room.setWidget(widget)     put a widget in the room; the one before goes
//   room.widget()              the widget in the room, or null
//   room.say(text)             a status line as the tenant, until a widget arrives
//   room.activate() .keyDown(ev) .dispose()
//
// What a widget is handed: its branch, its params, and its HOST - the one
// handle to where it runs, the same for every widget:
//   host.title(text)      its name, now: the tab says so. Its icon is its kind's.
//   host.parties.<name>   a party the workspace exposes to its widgets
//   host.focus            the room's branch of the focus party, for a widget whose inner parts
//                         are logical members of their own (RFC 0066 E3, keyboard §17.1): they
//                         join it, claim on their own roots, and leave when the widget goes. A
//                         yield from one of them is caught by the room
//
// What a widget may offer, all of it optional:
//   keyDown(ev)   a key the room holds, handed on; true when taken
//   activate()    the room has the keys now: a widget of native controls
//                 puts the browser's focus where its own keys work
//   setActive(on) its tab is, or is no longer, the one showing
//   dispose()     it is going
//
// A tab-pane is two parts. The TAB is the dock's: its chip, its title, its
// place in the strip. The PANE is this: a member of the focus party that wraps
// whatever element the widget gives it. The widget runs INSIDE and touches
// neither — it is handed a branch and gives back { root }, and that is the
// whole of its obligation.
//
// So a dock's law — a tab's widget must be a focus member with a root and
// activate() — is kept by the ROOM, never by the widget. A widget with no keys
// at all needs nothing: the room holds the tab's place in the focus tree. A
// widget that wants keys offers keyDown(ev), and the room hands them on.
//
// A widget of NATIVE controls - a grid, a tree, a form - has keys of its own,
// on elements that take the browser's focus. Where the focus is, is the
// steward's (RFC 0066 E3, keyboard §17.5): the browser's focus arriving on a
// control inside makes the room the holder, its keys LENT - the steward's mark
// on the room's root, never the room's - and an Escape the control did not
// want takes the focus out, so the room has the keys again and the next Escape
// is the room's, giving them back to the dock. The widget is told when the
// room comes to hold by a press or a call, by activate(), and puts the focus
// where its keys work; when the focus itself arrived, it is already there.
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
        if (!opts || !opts.focus || typeof opts.focus.createBranch !== "function") {
            throw new Error("[WidgetPane] opts.focus must be the dock's focus branch");
        }
        branch.activate(_roomOwner);
        this._branch = branch;
        this._widget = null;
        this._seq = 0;
        this._onTitle = typeof opts.onTitle === "function" ? opts.onTitle : null;
        this._parties = opts.parties || {};
        this._host = null;

        var root = branch.createElement("room", "div");
        css.addClass(root, wp_host);
        // NO TABINDEX: the room is only ever logically focused, never natively
        // (RFC 0066 E3, keyboard §17.1). A root with the browser's focus would
        // be a focused element, whose keys the steward leaves to the native
        // world - keyDown would never run, while the room still said held. A
        // press claims it; its keys come through the party; the browser's
        // focus is only ever on a control of the widget's inside it.
        this.root = root;

        this._inner = opts.focus.createBranch(branch.name, this);   // the room's own branch: its widget's logical members join it
        this.focus = this._inner.owner;
        this._off = Keys.claimOn(root, this.focus);
    }

    /**
     * The widget's host: what it may say to where it runs, and all of it.
     * Handed to the widget at construction; one per room, so a widget that
     * keeps it may use it for as long as it lives. A blank name is no name.
     */
    host() {
        if (!this._host) {
            var self = this;
            this._host = Object.freeze({
                title: function (text) {
                    if (text == null || !String(text).trim() || !self._onTitle) return;
                    self._onTitle(String(text));
                },
                parties: this._parties,
                focus: this._inner
            });
        }
        return this._host;
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

    /** Told to by the dock: the room claims the keys. What the widget does with them, it is told when they arrive. */
    activate() { Keys.claim(this.focus); }

    /** A yield from a logical member of the widget's: the room holds, and its own Escape gives the keys to the dock. */
    wouldHold() { return true; }

    /** The steward routed a key here: the widget's if it takes it, Escape gives them back. */
    keyDown(ev) {
        var w = this._widget;
        if (w && typeof w.keyDown === "function" && w.keyDown(ev)) return true;
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    /**
     * The keys are the room's: the widget hears so, and may put the focus where its own keys work - unless the
     * browser's focus arriving inside is what made the room the holder ("native"): it is where the user, or a
     * script, put it, and moving it would take it from them.
     */
    granted(by) {
        if (by === "native") return;
        var w = this._widget;
        if (w && typeof w.activate === "function") { try { w.activate(); } catch (e) { console.error("[WidgetPane] widget activate threw", e); } }
    }

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
