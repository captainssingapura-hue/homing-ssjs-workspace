// =============================================================================
// WidgetPaneModule — the PANE half of a tab-pane: the room a widget runs in.
//
//   var room = new WidgetPane(branch, { focus, onTitle?, parties? })
//     focus     the dock's focus branch
//     onTitle   (text) the widget named itself: the holder puts it on the tab
//     parties   the parties the workspace exposes to its widgets, by name
//   room.root                  the element a dock shows, and a desk floats
//   room.focus                 the room's membership of the focus party
//   room.branchFor(name)       a branch for the widget, under the room's own
//   room.host()                the widget's HOST: what it may say to where it runs, handed to
//                              it at construction - { title(text), parties }
//   room.setWidget(widget)     put a widget in the room; the one before goes
//   room.widget()              the widget in the room, or null
//   room.say(text)             a status line as the tenant, until a widget arrives
//   room.activate() .keyDown(ev) .dispose()
//
// What a widget is handed: its branch, its params, and its HOST - the one
// handle to where it runs, the same for every widget:
//   host.title(text)      its name, now: the tab says so. Its icon is its kind's.
//   host.parties.<name>   a party the workspace exposes to its widgets
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
// on elements that take the browser's focus. The room wires the seam, as a
// panel wires the controls it holds (§14): it says its keys are LENT while
// something inside it has the focus, and an Escape the control did not want
// takes the focus out - the room, the holder, has the keys again - so the next
// Escape is the room's and gives them back to the dock. The widget is told
// when the room comes to hold, by activate(), and puts the focus where its
// keys work; it never claims anything, having nothing to claim with.
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
        this._onTitle = typeof opts.onTitle === "function" ? opts.onTitle : null;
        this._parties = opts.parties || {};
        this._host = null;

        var root = branch.createElement("room", "div");
        css.addClass(root, wp_host);
        // -1: never in the Tab order - a tab is reached by its chip or the
        // dock's keys, never by tabbing into the middle of a workspace - but
        // able to take focus when the dock rests the keys here.
        root.setAttribute("tabindex", "-1");
        this.root = root;

        this.focus = opts.focus.join(branch.name, this);
        this._off = Keys.claimOn(root, this.focus);

        // The seam with the native world inside. An Escape out of a focused
        // control that the control did not take: the focus leaves it, and the
        // keys are the room's again.
        var self = this;
        root.addEventListener("keydown", function (ev) {
            if (ev.key !== "Escape" || ev.defaultPrevented || ev.target === root || !root.contains(ev.target)) return;
            if (typeof ev.target.blur === "function") ev.target.blur();
            ev.preventDefault();
            ev.stopPropagation();
            self._mark();
        });
        // Held, or lent to a control inside: said again whenever the focus moves.
        root.addEventListener("focusin", function () { self._mark(); });
        root.addEventListener("focusout", function () { setTimeout(function () { self._mark(); }, 0); });
    }

    /** Held, or lent while something inside has the browser's focus - only while the room has the keys at all. */
    _mark() {
        var was = this.root.getAttribute("data-keys");
        if (was !== "held" && was !== "lent") return;
        var a = typeof document === "undefined" ? null : document.activeElement;
        this.root.setAttribute("data-keys", a && a !== this.root && a !== document.body && this.root.contains(a) ? "lent" : "held");
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
                parties: this._parties
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

    /** The steward routed a key here: the widget's if it takes it, Escape gives them back. */
    keyDown(ev) {
        var w = this._widget;
        if (w && typeof w.keyDown === "function" && w.keyDown(ev)) return true;
        if (ev.key === "Escape") { Keys.yield(this.focus); return true; }
        return false;
    }

    /** The keys are the room's: the widget hears so, and may put the focus where its own keys work. */
    granted() {
        this.root.setAttribute("data-keys", "held");
        var w = this._widget;
        if (w && typeof w.activate === "function") { try { w.activate(); } catch (e) { console.error("[WidgetPane] widget activate threw", e); } }
        this._mark();
    }
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
