package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;

/**
 * A log's meaning, written down as the log goes: the state its events up to
 * one of them fold to, how many events that is, and by which rules of the fold
 * they were folded. A page takes one every so many events and comes back from
 * the latest - folding only what was logged after it - and a server may keep
 * one too, where the page sends it.
 *
 * <p>A checkpoint is a fold of a prefix, so folding on from it is folding the
 * whole log: the fold carries nothing from one event to the next but the
 * state. What it cannot know is a change to the fold itself - a later build
 * folding the same events to another state - so each says the rules it was
 * folded by, and one of other rules than {@link #FOLD} is not used.</p>
 *
 * @param folded whose log, through which event, and the state
 * @param events how many of the log's events are folded in it
 * @param fold   the rules of the fold it was made by
 */
public record Checkpoint(FoldedState folded, long events, int fold) {

    /**
     * The rules of the fold this build folds by. Raised whenever a change to the
     * fold would fold some log to another state; a checkpoint of other rules is
     * dropped, and the log folded whole. 2: the state one per layer - the
     * roster, the one pane, the split grid. 3: each layer's fold its own
     * events alone - the pane no longer follows a close. 4: the roster keeps
     * the name a user gave a widget.
     */
    public static final int FOLD = 4;

    public Checkpoint {
        Objects.requireNonNull(folded, "Checkpoint.folded");
        if (events < 0 || events > Scaled.MAX_SAFE) throw new IllegalArgumentException("Checkpoint.events " + events + " — from 0 to 2^53 − 1");
        if (fold < 1) throw new IllegalArgumentException("Checkpoint.fold " + fold + " — the rules are numbered from 1");
    }

    /** Whether it was folded by this build's rules, and can be folded on from. */
    public boolean current() { return fold == FOLD; }
}
