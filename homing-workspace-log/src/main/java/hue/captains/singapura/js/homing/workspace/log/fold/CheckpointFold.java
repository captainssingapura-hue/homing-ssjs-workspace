package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;

import java.util.List;
import java.util.Objects;

/**
 * The next checkpoint of a log: the events logged since the last one folded on
 * from it - or, there being none, from the log's opening. A periodic fold, and
 * no more: what it makes is what the whole log folds to, through the last of
 * the events given. The browser's {@code CheckpointFold} is this, transcribed.
 */
public final class CheckpointFold {

    private CheckpointFold() {}

    /**
     * @param previous the last checkpoint, of this build's rules, or null
     * @param header   whose log
     * @param events   what was logged after the previous checkpoint, in order
     */
    public static Checkpoint next(Checkpoint previous, LogHeader header, List<LoggedEvent> events) {
        Objects.requireNonNull(header, "CheckpointFold.header");
        FoldedState from = WorkspaceFold.start(header);
        long before = 0;
        if (previous != null) {
            if (!previous.current()) throw new WorkspaceFold.Refused("a checkpoint of rules " + previous.fold() + " cannot be folded on by rules " + Checkpoint.FOLD);
            if (!previous.folded().header().equals(header)) throw new WorkspaceFold.Refused("the checkpoint is of another log: " + previous.folded().header());
            from = previous.folded();
            before = previous.events();
        }
        return new Checkpoint(WorkspaceFold.foldFrom(from, events), before + events.size(), Checkpoint.FOLD);
    }
}
