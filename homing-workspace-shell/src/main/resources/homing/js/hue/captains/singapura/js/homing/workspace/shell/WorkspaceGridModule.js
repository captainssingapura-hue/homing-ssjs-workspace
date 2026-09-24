// =============================================================================
// WorkspaceGridModule — the one layout, in the two spellings it has.
//
//   WorkspaceGrid.toGrid(layout)   the workspace's tree → the grid's
//   WorkspaceGrid.fromGrid(tree)   the grid's tree → the workspace's
//
// The workspace says leaf/slotId and holds a sub-tree under `pane`; the grid
// says cell/id and holds one under `node`. Neither is better and there is no
// case where both are right, so they meet here rather than either side
// learning the other's words.
//
// Two sides need it, and need it to agree: the panes, which hand the grid its
// layout and read it back; and the model, which replays a merge by the grid's
// own rules - so the room a pane leaves goes where it went, on the day and on
// every replay of it.
// =============================================================================

class WorkspaceGrid {

    static toGrid(n) {
        if (!n) return null;
        if (n.kind === "leaf") return { kind: "cell", id: n.slotId };
        return { kind: "split", orientation: n.orientation,
                 children: n.children.map(function (c) { return { node: WorkspaceGrid.toGrid(c.pane), ratio: c.ratio }; }) };
    }

    static fromGrid(n) {
        if (!n) return null;
        if (n.kind === "cell") return { kind: "leaf", slotId: n.id };
        return { kind: "split", orientation: n.orientation,
                 children: n.children.map(function (c) { return { pane: WorkspaceGrid.fromGrid(c.node), ratio: c.ratio }; }) };
    }
}
