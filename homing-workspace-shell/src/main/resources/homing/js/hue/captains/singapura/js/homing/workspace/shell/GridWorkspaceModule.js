// =============================================================================
// GridWorkspace — the split-grid workspace on its headless core (RFC 0066 E3,
// the workspace detour: requests): the desk and its dock grid on a floor, as
// the gallery's docking page builds them, and every tab a WIDGET's — the
// core's, made new Kind(container, params) in a container its tab lends. What
// it is comes from its manifest, generated from its declaration in Java
// (WorkspaceDeclaration): the kinds that can be opened, and its ROOT PARTIES,
// made with it before any widget opens (WorkspaceParties). It knows no widget.
//
// Three roles. Its CORE (WorkspaceCore) keeps the widgets and their panes'
// life — its register of panes is WidgetTabs, a tab-pane a widget — and
// executes what a user asks: an open is create, then mount; a close is
// unmount, then close. Its PLACEMENT, the split grid (GridPlacement), mounts
// and unmounts. Its CONTROLS only ask: a dock's plus shows that dock's picker,
// and a pick asks to open the kind there, with the keys; a tab's cross, the
// tab menu's Close and a float's cross ask to close. What the grid does with a
// tab — moved, floated, a region parted, merged or closed — is the desk's and
// the grid's own.
//
// Its log keeps every layer: the roster's, recorded beside the core
// (RosterLayer); the grid's — the desk's and the docks' reports — keyed by the
// widget's id (WorkspaceRecorder). A title is the widget's, which the grid does
// not keep: a rename is the roster's alone. It comes back ROSTER FIRST — every
// widget made again under its id — then each mounted where the grid has it,
// read back and compared, its placement alone. A tab the roster does not hold
// is let go, said in the grid's events; a widget no host holds is put in the
// first region.
//
//   new GridWorkspace(branch, { host, manifest, keyboard?, menus?, budget?, log?, state?, logged?, checkpointer?, readOnly?, server? })
//     manifest  { name, kinds: { [kind]: { Widget, title, parties } }, parties: [{ type, secretary }] }
//     state     a WorkspaceState to come back to: every layer, as its log folds
//     the rest as the workspace of the tab source's (Workspace)
//   ws.core .tabs .placement .parties .desk .docks .recorder .logBar .checkpointer
//   ws.restored { same, state, read, strays, skipped }, or null
//   ws.request(request) → what the core gives, or null when it could not be done (said)
//   ws.open(kind, params?, location?)   ws.close(id)   ws.rename(id, name | null)   asked as requests
//   ws.titleOf(entry)   ws.kindOf(tabId)   ws.stopRecording()   ws.dispose()
// =============================================================================

const _gridWorkspaceOwner = Object.freeze({ toString: () => "gridWorkspace" });

class GridWorkspace {
    constructor(branch, opts) {
        var o = opts || {}, self = this;
        if (!branch) throw new Error("[GridWorkspace] a branch of its own is required");
        if (!o.host) throw new Error("[GridWorkspace] opts.host is required");
        if (!o.manifest || !o.manifest.kinds) throw new Error("[GridWorkspace] opts.manifest is required: the workspace's, generated from its declaration");
        branch.activate(_gridWorkspaceOwner);
        this.branch = branch;
        this._kinds = o.manifest.kinds;
        var kb = o.keyboard || null;
        var floor = branch.createElement("floor", "div");
        css.addClass(floor, ws_floor);
        o.host.appendChild(floor);
        this.root = floor;
        this._menus = o.menus || new ContextMenuSteward(branch.createBranch("menus"), { types: MENUS, keyboard: kb, keyboardId: "workspace/menus" });
        this._ownMenus = o.menus ? null : this._menus;
        // every report recorded - but a title, which is the widget's - and then answered
        function report(ev) { if (self.recorder && ev.kind !== "TabRenamed") self.recorder.hear(ev); self._answer(ev); }
        this.desk = new Desk(branch.createBranch("desk"), { host: floor, budget: o.budget == null ? 16 : o.budget, onEvent: report,
                                                            keyboard: kb, keyboardId: "workspace/desk", menus: this._menus, focusName: "workspace" });
        var grid = o.state ? o.state.grid : null;
        this.docks = new DockGrid(branch.createBranch("docks"), { host: floor, desk: this.desk, menus: this._menus, onEvent: report, dock: { addable: true },
                                                                  layout: grid ? WorkspaceProjection.gridLayout(grid.layout) : undefined });
        // THE REGISTER OF PANES, THE PLACEMENT, THE CORE; THE PARTIES, before any widget opens
        this.tabs = new WidgetTabs(this.desk, { title: function (e) { return self.titleOf(e); }, onClose: function (id) { self.close(id); } });
        this.placement = new GridPlacement({ desk: this.desk, docks: this.docks, tabs: this.tabs, onEvent: report });
        this.core = new WorkspaceCore({ kinds: this._kinds, panes: this.tabs, placement: this.placement });
        this.core.on(function (n) { if (n.kind === "WidgetOpened") self.tabs.hold(n.entry); });
        this.parties = new WorkspaceParties(this.core, { parties: o.manifest.parties || [], kinds: this._kinds });
        this.recorder = null;
        this.logBar = null;
        this._offRoster = null;
        this.checkpointer = o.checkpointer || null;
        this.restored = o.state ? this._restore(o.state) : null;
        if (o.log) {
            var logged = o.logged || 0;
            this.logBar = new WorkspaceLogBar(branch.createBranch("log"), { host: floor, store: o.log, server: !!o.server });
            this.logBar.count(logged);
            if (this.restored) this.logBar.restored(this.restored.same);
            if (!o.readOnly) this._record(o.log, logged);
        }
        // WHAT DID NOT COME BACK AS IT WAS, now that it can be said: the strays let go; a widget in no host put in the first region
        if (this.restored) this.placement.letGo(this.restored.strays);
        this.placement.unplaced().forEach(function (id) { self.placement.mount(self.core.entry(id), { how: "quiet" }); });
    }

    /** Come back to what the log folds to: the roster first, then the grid; read back, its placement alone. Not recorded. */
    _restore(state) {
        var back = RosterLayer.restore(this.core, state.roster);
        var placed = this.placement.restore(state.grid);
        var expected = WorkspaceProjection.without(state.grid, placed.strays.map(function (s) { return s.id; }));
        var read = WorkspaceProjection.read(this);
        var same = WorkspaceProjection.same(WorkspaceProjection.untitled(expected), WorkspaceProjection.untitled(read));
        if (!same) console.error("[GridWorkspace] the workspace came back otherwise than its log has it", { expected: expected, read: read });
        return Object.freeze({ same: same, state: state, read: read, strays: placed.strays, skipped: back.skipped });
    }

    /** Recorded from now on: the roster's word beside the core, the grid's reports by the recorder - one count. */
    _record(log, logged) {
        var self = this;
        this.recorder = new WorkspaceRecorder({ store: log,
            isRegion: function (slotId) { return !!self.docks.region(slotId); },
            isFloat: function (slotId) { return !self.docks.region(slotId) && !!self.placement.host(slotId); },
            kindOf: function (tabId) { return self.kindOf(tabId); },
            onCount: function (n) { self.logBar.count(logged + n); if (self.checkpointer) self.checkpointer.recorded(n); } });
        this._offRoster = RosterLayer.record(this.core, { append: function (e) { if (self.recorder) self.recorder.record(e); } });
    }

    /** What a dock or the desk asks for: a dock's plus, its picker, a pick asked as an open there with the keys; Shift+↓, the dock's tab floated. */
    _answer(ev) {
        var self = this;
        if (ev.kind === "AddRequested") {
            var dock = this.placement.host(ev.slotId), k = this._kinds;
            var kinds = Object.keys(k).map(function (id) { return { id: id, label: k[id].title || id }; });
            if (dock) dock.pick(kinds, function (kind) { self.open(kind, {}, { slotId: ev.slotId, how: "focus" }); });
        } else if (ev.kind === "DetachRequested") {
            var tp = this.desk.register.get(ev.tabId);
            if (tp && !tp.pinned) this.desk.detach(tp);
        }
    }

    request(r) {
        try { return this.core.execute(r); }
        catch (e) { console.error("[GridWorkspace] " + r.kind + " was not done: " + e.message); return null; }
    }

    open(kind, params, location) { return this.request(WorkspaceRequest.open(kind, params || {}, location || null)); }

    close(id) { return this.request(WorkspaceRequest.close(id)); }

    rename(id, name) { return this.request(WorkspaceRequest.rename(id, name && name.trim() ? name.trim() : null)); }

    /** A widget's title now, by the rule: the name a user gave it, else as it opened, by its identity. */
    titleOf(entry) {
        var kind = this._kinds[entry.kind];
        return KindAndParamsTitle.INSTANCE.titleOf(kind && kind.title ? kind.title : entry.kind, entry.id, entry.name);
    }

    /** The kind a tab holds: its widget's. */
    kindOf(tabId) { var e = this.core.entry(tabId); return e ? e.kind : null; }

    /** Nothing more recorded, and no more checkpoints: another page writes the log now. The workspace goes on, its changes kept by no one. */
    stopRecording() {
        if (this.recorder) this.recorder.stop();
        this.recorder = null;
        if (this._offRoster) { this._offRoster(); this._offRoster = null; }
        if (this.checkpointer) this.checkpointer.dispose();
        this.checkpointer = null;
    }

    /** The recording stopped first - taking the workspace down is not the user's closing its widgets; the widgets, each with its tab; then the desk, the grid, the floor. */
    dispose() {
        this.stopRecording();
        this.core.dispose();
        this.parties.dispose();
        this.desk.dispose();
        this.docks.dispose();
        if (this.logBar) this.logBar.dispose();
        if (this._ownMenus) this._ownMenus.dispose();
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
