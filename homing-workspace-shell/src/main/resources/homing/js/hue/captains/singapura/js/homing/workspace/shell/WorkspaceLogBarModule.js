// =============================================================================
// WorkspaceLogBar — the workspace's control strip, on an EdgeStrip at the foot
// of the floor: the floor keeps all its room but a thin lip, and the hand
// brought to the lip lays the strip over the foot of the grid; the hand gone,
// the grid has its foot back. On it, the workspace log's own line: how many
// events it has recorded; Export log, which saves the log as a workspace log
// file — the file the Java validator reads; and Export state, which saves what
// the browser folds that log to — the file the validator compares with Java's
// own fold. And a stored log the page could not read, set aside rather than
// cleared: how many there are, the latest exported as the log file it was -
// where the validator says which line fails - and discarded when asked. What
// the workspace is called, first. And, while another page writes this
// workspace, the way to one of this page's own: a new workspace of the same
// kind. The gallery keeps no log, so this is the workspace's alone.
//
// Every control on it is a designed Button, small, in the plain word but for
// Take over (warning) and Discard set-aside (danger). THE STRIP STAYS OUT
// WHILE IT SAYS SOMETHING A PERSON MUST KNOW — that another page writes this
// workspace, or that a log was set aside — and goes with the hand otherwise.
// What applies only sometimes is ON the strip only while it applies: every
// control is minted once, flat on the bar's branch, and attached in its place
// while it applies and detached when it does not - nothing is toggled hidden.
//
//   new WorkspaceLogBar(branch, { host, store, server?, fresh? })
//     host    where the strip goes: the workspace's floor, a positioned box, under its grid
//     store   the WorkspaceLogStore it exports
//     server  whether the server keeps this workspace's states: a new workspace
//             opened from the bar is kept there too
//     fresh   ({ ws_id, ws_server }) → the page's own link to a workspace of its kind -
//             the nav of the app that made it, which the bar cannot know; none, and
//             no new workspace is offered
//   bar.named(entry) says what the workspace is called: its WorkspaceEntry
//   bar.switching({ hint, open })   what the workspace is called, offered as the way to
//                    another: a button, open() on a press, hint its title - the page's, who
//                    alone knows how workspaces are switched; not offered, the name is only said
//   bar.resetting({ hint, reset })   the workspace offered back to how it starts: a button,
//                    reset() on a press - the page's, who alone can build it again; hidden while
//                    another page writes the workspace, and when not offered
//   bar.count(n)     says how many the log holds
//   bar.notice(text) says what the page would have a person know - how the address it
//                    came by was read, say; "" says nothing
//   bar.restored(same)  says whether the page came back as its log has it
//   bar.lock(writeLock, onTakeOver?)  says who writes the log - read-only when
//                    another page does, with a Take over that calls onTakeOver,
//                    and a link to a new workspace of this kind
//   bar.export()     → Promise<the log's text>, and the file saved
//   bar.exportState() → Promise<the state's text>, and the file saved
//   bar.asides()     → Promise<the set-aside logs>, and the bar says them
//   bar.exportAside() → Promise<the latest set-aside log's text>, and the file saved
//   bar.discardAsides() → Promise<how many>, and the bar says none
//   bar.root         the strip;  bar.strip  the EdgeStrip it is
//   bar.dispose()
// =============================================================================

const _logBarOwner = Object.freeze({ toString: () => "workspaceLogBar" });

class WorkspaceLogBar {
    constructor(branch, opts) {
        var o = opts || {};
        if (!branch) throw new Error("[WorkspaceLogBar] a branch of its own is required");
        if (!o.host || !o.store) throw new Error("[WorkspaceLogBar] opts.host and opts.store are required");
        branch.activate(_logBarOwner);
        this.branch = branch;
        this._store = o.store;
        this._server = !!o.server;
        this._freshTo = typeof o.fresh === "function" ? o.fresh : null;
        this._url = null;
        this._readOnly = false;
        this._latest = null;
        var self = this;
        this.strip = new EdgeStripBuilder().label("Workspace").host(o.host).build(branch.createBranch("strip"));
        this.root = this.strip.root;
        // what the workspace is called: said, or offered as the way to another - its name and the mark that it opens
        this._named = branch.createElement("named", "strong");
        css.addClass(this._named, ws_logbar_name);
        this._switchTo = null;
        this._switch = this._button("switch", "plain", "", function () { if (self._switchTo) self._switchTo(); });
        this._switchName = branch.createElement("switchName", "span");
        var mark = branch.createElement("switchMark", Icon.TAG);
        new Icon(mark, { name: "disclose" });
        this._switch.el.appendChild(this._switchName);
        this._switch.el.appendChild(mark);
        this._counted = branch.createElement("count", "span");
        css.addClass(this._counted, ws_logbar_count);
        this._noticed = this._line("notice");
        // who writes the log: said while it is not this page, with the way to take it over
        this._locked = this._line("locked");
        this._takeOver = this._button("takeOver", "warning", "Take over", function () { if (self._onTakeOver) self._onTakeOver(); });
        // this page's own workspace, while another page writes this one: a new one of the kind
        this._fresh = branch.createElement("fresh", "a");
        css.addClass(this._fresh, ws_logbar_fresh);
        this._fresh.textContent = "Open a new workspace of this kind";
        // the workspace back to how it starts: the page's, offered, and only to the page that writes it
        this._resetTo = null;
        this._reset = this._button("reset", "plain", "Reset…", function () { if (self._resetTo) self._resetTo(); });
        var exportLog = this._button("export", "plain", "Export log", function () { self.export(); });
        var exportState = this._button("exportState", "plain", "Export state", function () { self.exportState(); });
        // a stored log the page could not read: said, exported, discarded
        this._aside = this._line("aside");
        this._exportAside = this._button("exportAside", "plain", "Export set-aside log", function () { self.exportAside(); });
        this._discardAside = this._button("discardAside", "danger", "Discard set-aside", function () { self.discardAsides(); });
        // the file is handed over by a link the bar keeps, never shown
        this._link = branch.createElement("link", "a");
        css.addClass(this._link, ws_logbar_link);
        // THE STRIP'S ORDER: every control in its place, attached while it applies. The log line and the exports always
        // do; the rest are attached by what the bar is told, below.
        this._order = [this._switch.el, this._named, this._counted, this._noticed, this._locked, this._takeOver.el, this._fresh, this._reset.el,
                       exportLog.el, exportState.el, this._aside, this._exportAside.el, this._discardAside.el, this._link];
        [this._counted, exportLog.el, exportState.el, this._link].forEach(function (el) { self._mount(el, true); });
        this._note = "";
        this._onTakeOver = null;
        this.named(null);
        this.count(0);
        this.lock(null);
        this._said([]);
        this.asides();
    }

    /** A designed button on the strip: small, in a colour word, pressed to act. */
    _button(name, colour, label, onClick) {
        var b = new ButtonBuilder();
        return b.label(label).colour(colour).size(-1).onClick(onClick).build(this.branch.createElement(name, b.tag));
    }

    /** A line of text on the strip that says something, and says nothing while it is empty. */
    _line(name) {
        var el = this.branch.createElement(name, "span");
        css.addClass(el, ws_logbar_note);
        return el;
    }

    /** Attached to the strip in its place while it applies - before the next one in the order that is there - and detached when not. */
    _mount(el, on) {
        var strip = this.root;
        if (!on) { if (el.parentNode === strip) strip.removeChild(el); return; }
        if (el.parentNode === strip) return;
        var next = null;
        for (var i = this._order.indexOf(el) + 1; i < this._order.length && !next; i++) if (this._order[i].parentNode === strip) next = this._order[i];
        strip.insertBefore(el, next);
    }

    /** Kept out while it says something a person must know: that another page writes this workspace, or that a log was set aside. */
    _attend() { this.strip.hold(this._readOnly || !!this._latest); }

    /** What the workspace is called, offered as the way to another: a button, pressed to open(); hint its title. */
    switching(offer) {
        var o = offer || {};
        this._switchTo = typeof o.open === "function" ? o.open : null;
        this._mount(this._switch.el, !!this._switchTo);
        this._switch.el.title = o.hint ? String(o.hint) : "";
        this._switch.el.setAttribute("aria-label", o.hint ? String(o.hint) : "Switch workspace");
        this._mount(this._named, !this._switchTo && !!this._name);
    }

    /** The workspace offered back to how it starts: a button, pressed to reset(); hint its title. */
    resetting(offer) {
        var o = offer || {};
        this._resetTo = typeof o.reset === "function" ? o.reset : null;
        this._reset.el.title = o.hint ? String(o.hint) : "";
        this._paintReset();
    }

    _paintReset() { this._mount(this._reset.el, !!this._resetTo && !this._readOnly); }

    /** What the workspace is called; nothing said until it is known. */
    named(entry) {
        this._name = entry ? entry.name.value : "";
        this._named.textContent = this._name;
        this._switchName.textContent = this._name || "Switch workspace";
        this._mount(this._named, !!entry && !this._switchTo);
    }

    /**
     * Who writes the log, said while it is not simply this page; and, read-only, the way to take it over, and the
     * way to a workspace of this page's own - a new one of the kind, under an id no log has had.
     */
    lock(writeLock, onTakeOver) {
        var held = writeLock ? writeLock.held : null;
        var readOnly = held === Held.ELSEWHERE || held === Held.TAKEN;
        this._locked.textContent = held === Held.ELSEWHERE ? "Read-only: another page writes this workspace"
            : held === Held.TAKEN ? "Read-only: another page took this workspace over - what changes here is not kept"
            : held === Held.UNGUARDED ? "Unguarded: this browser keeps no locks" : "";
        this._mount(this._locked, !!this._locked.textContent);
        this._onTakeOver = onTakeOver || null;
        this._mount(this._takeOver.el, !!(this._onTakeOver && readOnly));
        this._mount(this._fresh, !!(readOnly && this._freshTo));
        this._readOnly = readOnly;
        this._paintReset();
        this._attend();
        if (readOnly && this._freshTo) {
            HrefManagerInstance.set(this._fresh, this._freshTo({ ws_id: WorkspaceLogIdentity.fresh().id, ws_server: this._server ? "on" : null }));
        }
    }

    count(n) {
        this._n = n;
        this._counted.textContent = "Workspace log: " + n + (n === 1 ? " event" : " events") + this._note;
    }

    notice(text) {
        var said = text ? String(text) : "";
        this._noticed.textContent = said;
        this._mount(this._noticed, !!said);
    }

    restored(same) {
        this._note = same ? " · restored as logged" : " · restored otherwise than logged";
        this.count(this._n || 0);
    }

    export() {
        var self = this;
        return WorkspaceLogExport.of(this._store).then(function (text) { return self._save(text, WorkspaceLogExport.fileName(self._store.header)); });
    }

    exportState() {
        var self = this;
        return WorkspaceLogExport.state(this._store).then(function (text) { return self._save(text, WorkspaceLogExport.stateFileName(self._store.header)); });
    }

    asides() {
        var self = this;
        return this._store.asides().then(function (all) { self._said(all); return all; },
                                         function (e) { console.error("[WorkspaceLogBar] the set-aside logs do not read: " + (e && e.message)); return []; });
    }

    exportAside() {
        if (!this._latest) return Promise.resolve(null);
        var a = this._latest;
        return Promise.resolve(this._save(WorkspaceLogExport.asideText(a), WorkspaceLogExport.asideFileName(a)));
    }

    discardAsides() {
        var self = this;
        return this._store.discardAsides().then(function (n) { self._said([]); return n; });
    }

    /** How many are set aside, and why the latest was; the controls only while there are some. */
    _said(all) {
        var n = all.length;
        this._latest = n ? all[n - 1] : null;
        this._aside.textContent = n ? n + (n === 1 ? " log" : " logs") + " set aside" : "";
        this._aside.title = n ? this._latest.why : "";
        this._mount(this._aside, !!n);
        this._mount(this._exportAside.el, !!n);
        this._mount(this._discardAside.el, !!n);
        this._attend();
    }

    /** The text handed over as a file, by the link the bar keeps. */
    _save(text, name) {
        if (this._url) URL.revokeObjectURL(this._url);
        this._url = URL.createObjectURL(new Blob([text], { type: "text/plain;charset=utf-8" }));
        HrefManagerInstance.set(this._link, this._url);
        this._link.download = name;
        this._link.click();
        return text;
    }

    dispose() {
        if (this._url) URL.revokeObjectURL(this._url);
        this.strip.dispose();
        try { this.branch.dissolve(); } catch (e) {}
    }
}
