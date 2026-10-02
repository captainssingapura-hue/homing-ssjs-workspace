package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;

import java.util.Objects;

/**
 * A log's meaning, written down: whose log, through which event, and the state
 * it folds to. What the browser exports beside a log, and what Java writes for
 * the same log, byte for byte when the two folds agree.
 *
 * @param header  whose log
 * @param through the last event folded; 0 when there was none
 * @param state   what the log folds to
 */
public record FoldedState(LogHeader header, EventSeq through, WorkspaceState state) {
    public FoldedState {
        Objects.requireNonNull(header, "FoldedState.header");
        Objects.requireNonNull(through, "FoldedState.through");
        Objects.requireNonNull(state, "FoldedState.state");
        if (through.value() > Scaled.MAX_SAFE) throw new IllegalArgumentException("FoldedState.through " + through.value() + " — beyond 2^53 − 1");
    }
}
