package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.FloatEvent;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.PaneEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent;
import hue.captains.singapura.js.homing.workspace.log.RosterEvent;
import hue.captains.singapura.js.homing.workspace.log.RosterState;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogFile;

import java.util.List;

/**
 * A workspace log's meaning: its events folded, from the workspace's opening,
 * into the {@link WorkspaceState} they leave - one state per layer. The log is
 * one sequence; the layers are made here, in the processing: each event is
 * handed, by its family, to its layer's fold - the roster's ({@link
 * RosterFold}), the one pane's ({@link PaneFold}), the split grid's ({@link
 * GridFold}) - which folds it into that layer's state and refuses it, naming
 * it, when it cannot be where it falls. A placement stands on the roster: the
 * pane's fold reads it, and follows it when a widget is closed. A family with
 * no layer yet would be decoded and kept, and passed by here unfolded. The
 * JavaScript fold is this, line for line, and the two agree to the byte.
 */
public final class WorkspaceFold {

    private WorkspaceFold() {}

    /** An event the state it falls on cannot take. */
    public static final class Refused extends IllegalArgumentException {
        public Refused(String why) { super(why); }
    }

    /** A file's events, from the opening: the state, through its last event — or refused at the line that cannot be. */
    public static FoldedState fold(WorkspaceLogFile file) {
        WorkspaceState s = WorkspaceState.opening();
        long through = 0;
        for (int i = 0; i < file.events().size(); i++) {
            LoggedEvent e = file.events().get(i);
            try { s = apply(s, e.event()); }
            catch (IllegalArgumentException x) {
                throw new WorkspaceLogFile.Refused(i + 2, e.event().getClass().getSimpleName() + " cannot be: " + x.getMessage());
            }
            through = e.seq().value();
        }
        return new FoldedState(file.header(), EventSeq.of(through), s);
    }

    /** Where every log's fold starts: its header, through no event, the opening state. */
    public static FoldedState start(LogHeader header) {
        return new FoldedState(header, EventSeq.ZERO, WorkspaceState.opening());
    }

    /**
     * Events folded on from a folded state - a checkpoint's, or the opening - each
     * after the last it went through: the state they leave, through the last.
     * Folding a log's events from its opening, or on from the fold of any prefix
     * of them, is the same fold: nothing passes from one event to the next but the
     * state.
     */
    public static FoldedState foldFrom(FoldedState from, List<LoggedEvent> events) {
        WorkspaceState s = from.state();
        long through = from.through().value();
        for (LoggedEvent e : events) {
            long seq = e.seq().value();
            if (seq <= through) throw new Refused("seq " + seq + " is not after " + through + ", the last folded");
            try { s = apply(s, e.event()); }
            catch (IllegalArgumentException x) {
                throw new Refused("seq " + seq + ": " + e.event().getClass().getSimpleName() + " cannot be: " + x.getMessage());
            }
            through = seq;
        }
        return new FoldedState(from.header(), EventSeq.of(through), s);
    }

    /** One event on a state, handed to its layer: the state after it. */
    public static WorkspaceState apply(WorkspaceState state, WorkspaceEvent event) {
        return switch (event) {
            case RosterEvent e -> {
                RosterState roster = RosterFold.apply(state.roster(), e);
                yield new WorkspaceState(roster, PaneFold.follow(state.pane(), e), state.grid());
            }
            case PaneEvent e   -> state.withPane(PaneFold.apply(state.pane(), e, state.roster()));
            case TabEvent e    -> state.withGrid(GridFold.apply(state.grid(), e));
            case RegionEvent e -> state.withGrid(GridFold.apply(state.grid(), e));
            case FloatEvent e  -> state.withGrid(GridFold.apply(state.grid(), e));
        };
    }
}
