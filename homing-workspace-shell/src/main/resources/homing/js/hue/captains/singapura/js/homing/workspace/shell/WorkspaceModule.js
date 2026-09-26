// =============================================================================
// Workspace — the workspace, built the way the gallery's docking page is built,
// from the same components (RFC 0066 E3, the workspace detour): a DESK, the
// whole, and its docks laid out in a split grid by a DOCK GRID, on a floor
// that fills what holds it. Nothing between them and nothing of its own.
//
//   new Workspace(branch, { host, keyboard?, menus?, budget? })
//     host      where the floor goes: the page's slot, which it fills
//     keyboard  the page's KeyboardSteward
//     menus     the page's ContextMenuSteward, else one of its own
//     budget    the most tabs the workspace holds at once: the desk's
//   ws.root .desk .docks
//   ws.dispose()
//
// STEP ONE of the rebuild: no event store, and no way to add a widget - a dock
// is made without its plus. A region's tab bar, right-clicked on its own ground,
// parts the region beside or below, merges it into one across a splitter of its
// own, or closes it: the dock grid's menu, answered by the dock grid.
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
        // THE DESK, the whole: every tab a tab-pane of its register, its floats over the floor
        this.desk = new Desk(branch.createBranch("desk"), { host: floor, budget: o.budget == null ? 16 : o.budget,
                                                            keyboard: kb, keyboardId: "workspace/desk", menus: this._menus, focusName: "workspace" });
        // THE REGIONS: a cell of the grid and a dock in it, parted, merged and closed from the dock's own tab bar
        this.docks = new DockGrid(branch.createBranch("docks"), { host: floor, desk: this.desk, menus: this._menus, dock: { addable: false } });
    }

    /** The desk first, since a dock is disposed only empty; then the docks and their grid; then the floor. */
    dispose() {
        this.desk.dispose();
        this.docks.dispose();
        if (this._ownMenus) this._ownMenus.dispose();
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
