// =============================================================================
// WorkspaceLogBar — the workspace log's own line along the foot of the floor:
// how many events it has recorded; Export log, which saves the log as a
// workspace log file — the file the Java validator reads; and Export state,
// which saves what the browser folds that log to — the file the validator
// compares with Java's own fold. And a stored log the page could not read,
// set aside rather than cleared: how many there are, the latest exported as
// the log file it was - where the validator says which line fails - and
// discarded when asked. What the workspace is called, first. And, while
// another page writes this workspace, the way to one of this page's own: a new
// workspace of the same kind. The gallery keeps no log, so this is the
// workspace's alone.
//
//   new WorkspaceLogBar(branch, { host, store, server?, fresh? })
//     host    where the bar goes: the workspace's floor, under its grid
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
        var self = this;
        var bar = branch.createElement("bar", "div");
        css.addClass(bar, ws_logbar);
        this._named = branch.createElement("named", "strong");
        this._switch = branch.createElement("switch", "button");
        this._switch.type = "button";
        this._switch.hidden = true;
        this._switchTo = null;
        this._switch.addEventListener("click", function () { if (self._switchTo) self._switchTo(); });
        this._counted = branch.createElement("count", "span");
        css.addClass(this._counted, ws_logbar_count);
        this._noticed = branch.createElement("notice", "span");
        css.addClass(this._noticed, ws_logbar_count);
        this._noticed.hidden = true;
        var button = branch.createElement("export", "button");
        button.type = "button";
        button.textContent = "Export log";
        button.addEventListener("click", function () { self.export(); });
        var state = branch.createElement("exportState", "button");
        state.type = "button";
        state.textContent = "Export state";
        state.addEventListener("click", function () { self.exportState(); });
        // a stored log the page could not read: said, exported, discarded
        this._aside = branch.createElement("aside", "span");
        css.addClass(this._aside, ws_logbar_count);
        this._exportAside = branch.createElement("exportAside", "button");
        this._exportAside.type = "button";
        this._exportAside.textContent = "Export set-aside log";
        this._exportAside.addEventListener("click", function () { self.exportAside(); });
        this._discardAside = branch.createElement("discardAside", "button");
        this._discardAside.type = "button";
        this._discardAside.textContent = "Discard set-aside";
        this._discardAside.addEventListener("click", function () { self.discardAsides(); });
        // who writes the log: said while it is not this page, with the way to take it over
        this._locked = branch.createElement("locked", "span");
        css.addClass(this._locked, ws_logbar_count);
        this._takeOver = branch.createElement("takeOver", "button");
        this._takeOver.type = "button";
        this._takeOver.textContent = "Take over";
        this._takeOver.addEventListener("click", function () { if (self._onTakeOver) self._onTakeOver(); });
        // this page's own workspace, while another page writes this one: a new one of the kind
        this._fresh = branch.createElement("fresh", "a");
        this._fresh.textContent = "Open a new workspace of this kind";
        // the workspace back to how it starts: the page's, offered, and only to the page that writes it
        this._reset = branch.createElement("reset", "button");
        this._reset.type = "button";
        this._reset.textContent = "Reset…";
        this._reset.hidden = true;
        this._reset.addEventListener("click", function () { if (self._resetTo) self._resetTo(); });
        this._resetTo = null;
        this._readOnly = false;
        // the file is handed over by a link the bar keeps, never shown
        this._link = branch.createElement("link", "a");
        css.addClass(this._link, ws_logbar_link);
        bar.appendChild(this._switch);
        bar.appendChild(this._named);
        bar.appendChild(this._counted);
        bar.appendChild(this._noticed);
        bar.appendChild(this._locked);
        bar.appendChild(this._takeOver);
        bar.appendChild(this._fresh);
        bar.appendChild(this._reset);
        bar.appendChild(button);
        bar.appendChild(state);
        bar.appendChild(this._aside);
        bar.appendChild(this._exportAside);
        bar.appendChild(this._discardAside);
        bar.appendChild(this._link);
        o.host.appendChild(bar);
        this.root = bar;
        this._note = "";
        this._latest = null;
        this._onTakeOver = null;
        this.named(null);
        this.count(0);
        this.lock(null);
        this._said([]);
        this.asides();
    }

    /** What the workspace is called, offered as the way to another: a button, pressed to open(); hint its title. */
    switching(offer) {
        var o = offer || {};
        this._switchTo = typeof o.open === "function" ? o.open : null;
        this._switch.hidden = !this._switchTo;
        this._switch.title = o.hint ? String(o.hint) : "";
        this._switch.setAttribute("aria-label", o.hint ? String(o.hint) : "Switch workspace");
        this._named.hidden = !!this._switchTo || !this._name;
    }

    /** The workspace offered back to how it starts: a button, pressed to reset(); hint its title. */
    resetting(offer) {
        var o = offer || {};
        this._resetTo = typeof o.reset === "function" ? o.reset : null;
        this._reset.title = o.hint ? String(o.hint) : "";
        this._paintReset();
    }

    _paintReset() { this._reset.hidden = !this._resetTo || this._readOnly; }

    /** What the workspace is called; nothing said until it is known. */
    named(entry) {
        this._name = entry ? entry.name.value : "";
        this._named.textContent = this._name;
        this._switch.textContent = this._name ? this._name + " ▾" : "Switch workspace";
        this._named.hidden = !entry || !!this._switchTo;
    }

    /**
     * Who writes the log, said while it is not simply this page; and, read-only, the way to take it over, and the
     * way to a workspace of this page's own - a new one of the kind, under an id no log has had.
     */
    lock(writeLock, onTakeOver) {
        var held = writeLock ? writeLock.held : null;
        var readOnly = held === Held.ELSEWHERE || held === Held.TAKEN;
        this._locked.textContent = held === Held.ELSEWHERE ? " · read-only: another page writes this workspace"
            : held === Held.TAKEN ? " · read-only: another page took this workspace over - what changes here is not kept"
            : held === Held.UNGUARDED ? " · unguarded: this browser keeps no locks" : "";
        this._locked.hidden = !this._locked.textContent;
        this._onTakeOver = onTakeOver || null;
        this._takeOver.hidden = !(this._onTakeOver && readOnly);
        this._fresh.hidden = !(readOnly && this._freshTo);
        this._readOnly = readOnly;
        this._paintReset();
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
        this._noticed.textContent = said ? " · " + said : "";
        this._noticed.hidden = !said;
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
        this._aside.textContent = n ? " · " + n + (n === 1 ? " log" : " logs") + " set aside" : "";
        this._aside.title = n ? this._latest.why : "";
        this._aside.hidden = !n;
        this._exportAside.hidden = !n;
        this._discardAside.hidden = !n;
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
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
