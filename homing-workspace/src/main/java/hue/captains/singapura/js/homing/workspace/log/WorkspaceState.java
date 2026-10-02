package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;

/**
 * What a workspace log folds to: one state per layer. The log is one sequence
 * of events of every layer, and each family of events is folded by its own
 * layer, into its own state - the roster, the core's, which every placement
 * stands on; the one pane's; the split grid's. A workspace places with one
 * engine at a time, by the display it is on, and the log keeps each; a
 * workspace that comes back makes its roster again, then lays it out by the
 * placement it shows. Each layer's state is its own fold's alone: a placement
 * never reads the roster (RFC 0066 E3, the workspace detour: placement
 * engines, each folding only its own).
 *
 * @param roster the widgets the core holds
 * @param pane   what the one pane shows
 * @param grid   the split grid's arrangement
 */
public record WorkspaceState(RosterState roster, PaneState pane, GridState grid) {

    public WorkspaceState {
        Objects.requireNonNull(roster, "WorkspaceState.roster");
        Objects.requireNonNull(pane, "WorkspaceState.pane");
        Objects.requireNonNull(grid, "WorkspaceState.grid");
    }

    /** Where every log starts: each layer's opening. */
    public static WorkspaceState opening() {
        return new WorkspaceState(RosterState.empty(), PaneState.empty(), GridState.opening());
    }

    public WorkspaceState withRoster(RosterState r) { return new WorkspaceState(r, pane, grid); }
    public WorkspaceState withPane(PaneState p)     { return new WorkspaceState(roster, p, grid); }
    public WorkspaceState withGrid(GridState g)     { return new WorkspaceState(roster, pane, g); }
}
