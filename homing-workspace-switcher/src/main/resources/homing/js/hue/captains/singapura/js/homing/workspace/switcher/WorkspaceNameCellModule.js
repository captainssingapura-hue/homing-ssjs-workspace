// =============================================================================
// WorkspaceNameCell — a workspace's name in the switcher's table: the relation
// grid's stock text cell (RelGridTextCell), editable only when ARMED. Enter on
// a workspace opens it, so its name must not take Enter for itself: the cell
// declines the grid's offer of control until it is armed — by F2, or Rename —
// and is disarmed as soon as that edit is over, however it ended. What is
// committed is the owner's to have done; the cell shows what it is then set to.
//
// The cell contract, as the grid asks it of any cell, handed on to the stock
// cell - but mayTakeControl, which is the stock cell's AND armed:
//
//   new WorkspaceNameCell({ branch, value, onCommit })   as RelGridTextCell's
//   cell.arm()      the next offer of control taken
//   cell.cellElement()  cell.onSelect(mode)  cell.mayTakeControl()
//   cell.editorElement()  cell.takeControl()  cell.set(v)  cell.value()  cell.dispose()
// =============================================================================

class WorkspaceNameCell {
    constructor(opts) {
        var o = opts || {};
        if (typeof o.onCommit !== "function") throw new Error("[WorkspaceNameCell] opts.onCommit is required: a name is edited to be had");
        this._cell = new RelGridTextCell({ branch: o.branch, value: o.value, onCommit: o.onCommit });
        this._armed = false;
    }

    arm() { this._armed = true; return this; }

    cellElement() { return this._cell.cellElement(); }
    onSelect(mode) { this._cell.onSelect(mode); }
    mayTakeControl() { return this._armed && this._cell.mayTakeControl(); }
    editorElement() { return this._cell.editorElement(); }

    /** The edit, disarmed when it ends - committed, cancelled or lost. */
    takeControl() {
        var self = this;
        return this._cell.takeControl().then(function () { self._armed = false; });
    }

    set(v) { this._cell.set(v); return this; }
    value() { return this._cell.value(); }
    dispose() { this._armed = false; this._cell.dispose(); }
}
