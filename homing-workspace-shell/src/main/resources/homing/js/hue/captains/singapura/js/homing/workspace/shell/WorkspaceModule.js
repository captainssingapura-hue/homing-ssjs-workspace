// =============================================================================
// Workspace — the workspace, built the way the gallery's docking page is built,
// from the same components (RFC 0066 E3, the workspace detour): a DESK, the
// whole, and its docks laid out in a split grid by a DOCK GRID, on a floor
// that fills what holds it. Nothing between them and nothing of its own.
//
//   new Workspace(branch, { host, kinds?, keyboard?, menus?, budget?, log?, state?, logged? })
//     host      where the floor goes: the page's slot, which it fills
//     kinds     what a tab may hold - TabSource's kinds, { id, label, title,
//               make(branch, { focus, id, title, pane, tab }) } - offered by the
//               chooser a dock's plus opens
//     keyboard  the page's KeyboardSteward
//     menus     the page's ContextMenuSteward, else one of its own
//     budget    the most tabs the workspace holds at once: the desk's
//     log       a WorkspaceLogStore: what the components report is recorded
//               in it, as the WorkspaceEvents declared in Java - regions, floats,
//               every tab in one or the other - and a bar along
//               the foot of the floor says how many and exports the file
//     state     a WorkspaceState to come back to - what the log folds to: the grid
//               laid out as it has it, every region's tabs back under their ids,
//               kinds and titles, the tab each shows; then read back, and the two
//               compared (ws.restored). Nothing of the restoring is recorded: the
//               recorder is made after it
//     logged    how many events the log already holds, for the bar
//   ws.root .desk .docks .source .recorder .logBar .restored { same, state, read }
//   ws.dispose()
//
// THE LOG (the rebuild's step three): every report of the desk, the docks and the
// grid that changes the arrangement is recorded, typed; a page comes back to what
// its log folds to - the grid, its floats, every tab in one or the other. THE TABS work as the gallery's
// docking page's do, from the same parts: every tab a tab-pane of the desk's
// register, made by one TabSource; a dock's PLUS opens a tab holding the
// CHOOSER (TabOpener), with the keys, and a pick turns that tab into the kind
// picked, in place; the tab menu - Detach, Close - is the desk's; Shift+↓ on a
// dock floats its tab under the chip; a float of one dropped on a strip lands
// there. A region's tab bar, right-clicked on its own ground, parts, merges or
// closes the region: the dock grid's.
// =============================================================================

const _workspaceOwner = Object.freeze({ toString: () => "workspace" });

class Workspace {
    constructor(branch, opts) {
        var o = opts || {};
        if (!branch) throw new Error("[Workspace] a branch of its own is required");
        if (!o.host) throw new Error("[Workspace] opts.host is required");
        branch.activate(_workspaceOwner);
        this.branch = branch;
        var kb = o.keyboard || null;
        var floor = branch.createElement("floor", "div");
        css.addClass(floor, ws_floor);
        o.host.appendChild(floor);
        this.root = floor;
        // one menu steward for the page, the page's when it has one
        this._menus = o.menus || new ContextMenuSteward(branch.createBranch("menus"), { types: MENUS, keyboard: kb, keyboardId: "workspace/menus" });
        this._ownMenus = o.menus ? null : this._menus;
        var self = this;
        function report(ev) { self._answer(ev); if (self.recorder) self.recorder.hear(ev); }
        // THE DESK, the whole: every tab a tab-pane of its register, its floats over the floor, the tab menu answered
        this.desk = new Desk(branch.createBranch("desk"), { host: floor, budget: o.budget == null ? 16 : o.budget, onEvent: report,
                                                            keyboard: kb, keyboardId: "workspace/desk", menus: this._menus, focusName: "workspace" });
        // WHERE THE TABS COME FROM, all of them: the kinds the page offers, and the chooser a plus opens - listed:
        // false, never one of the things it offers, and turned into what is picked in it, the same tab
        this.source = new TabSource(branch.createBranch("source"), { desk: this.desk,
                                                                     onBecame: function (tp, kindId) { if (self.recorder) self.recorder.became(tp, kindId); },
                                                                     kinds: (o.kinds || []).concat([
            { id: "opener", label: "Open…", title: "Open", listed: false,
              make: function (b, p) { return new TabOpener(b, { focus: p.focus, tab: p.tab, source: self.source }); } } ]) });
        // THE REGIONS: a cell of the grid and a dock in it, parted, merged and closed from the dock's own tab bar
        this.docks = new DockGrid(branch.createBranch("docks"), { host: floor, desk: this.desk, menus: this._menus, onEvent: report, dock: { addable: true },
                                                                  layout: o.state ? WorkspaceProjection.gridLayout(o.state.layout) : undefined });
        // COMING BACK: the tabs where the state has them, then the workspace read back, to see that it is so
        this.restored = null;
        if (o.state) {
            WorkspaceProjection.restore(this, o.state);
            var read = WorkspaceProjection.read(this);
            this.restored = { same: WorkspaceProjection.same(o.state, read), state: o.state, read: read };
            if (!this.restored.same) console.error("[Workspace] the workspace came back otherwise than its log has it", this.restored);
        }
        // THE LOG: a tab is in a region, the grid's, or in a float, the desk's; its kind is the source's
        this.recorder = null;
        this.logBar = null;
        if (o.log) {
            var logged = o.logged || 0;
            this.logBar = new WorkspaceLogBar(branch.createBranch("log"), { host: floor, store: o.log });
            this.logBar.count(logged);
            if (this.restored) this.logBar.restored(this.restored.same);
            this.recorder = new WorkspaceRecorder({ store: o.log,
                isRegion: function (slotId) { return !!self.docks.region(slotId); },
                isFloat: function (slotId) { return !self.docks.region(slotId) && self.desk.docks().some(function (d) { return d.slotId === slotId; }); },
                kindOf: function (tabId) { return self.source.kindOf(tabId); },
                onCount: function (n) { self.logBar.count(logged + n); } });
        }
    }

    /**
     * What a dock or the desk asks for, answered as the gallery's docking page answers it: a dock's plus, a tab
     * holding the chooser, with the keys - the asking happened on the strip; Shift+↓, the dock's tab floated
     * under its chip.
     */
    _answer(ev) {
        if (ev.kind === "AddRequested") {
            var dock = this.desk.docks().filter(function (p) { return p.slotId === ev.slotId; })[0];
            if (dock) this.source.addTo(dock, "opener", "focus");
        } else if (ev.kind === "DetachRequested") {
            var tp = this.desk.register.get(ev.tabId);
            if (tp && !tp.pinned) this.desk.detach(tp);
        }
    }

    /** The recorder stopped first - taking the workspace down is not the user's closing its tabs; the desk, since a dock is disposed only empty; then the docks and their grid; then the floor. */
    dispose() {
        if (this.recorder) this.recorder.stop();
        this.desk.dispose();
        this.source.dispose();
        this.docks.dispose();
        if (this.logBar) this.logBar.dispose();
        if (this._ownMenus) this._ownMenus.dispose();
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
