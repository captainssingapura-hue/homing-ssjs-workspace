package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.FloatId;

import java.util.Objects;

/**
 * What happens to a float on the desk: it is opened, moved, resized, raised,
 * closed — where it lies and how big in whole pixels of the desk, as the float
 * layer reports it.
 */
public sealed interface FloatEvent extends WorkspaceEvent {

    /** A float was opened on the desk: where it lies and how big, in whole pixels of the desk; on top. Its tabs come to it after. */
    record FloatOpened(FloatId id, int x, int y, int w, int h) implements FloatEvent {
        public FloatOpened {
            Objects.requireNonNull(id, "FloatOpened.id");
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatOpened " + w + "×" + h + " — a measure above zero");
        }
    }

    /** A float came to rest somewhere else. */
    record FloatMoved(FloatId id, int x, int y) implements FloatEvent {
        public FloatMoved {
            Objects.requireNonNull(id, "FloatMoved.id");
        }
    }

    /** A float has another measure. */
    record FloatResized(FloatId id, int w, int h) implements FloatEvent {
        public FloatResized {
            Objects.requireNonNull(id, "FloatResized.id");
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("FloatResized " + w + "×" + h + " — a measure above zero");
        }
    }

    /** A float came on top of the others. */
    record FloatRaised(FloatId id) implements FloatEvent {
        public FloatRaised {
            Objects.requireNonNull(id, "FloatRaised.id");
        }
    }

    /** A float was closed: emptied by a move, or closed with what it held, those closes said first. */
    record FloatClosed(FloatId id) implements FloatEvent {
        public FloatClosed {
            Objects.requireNonNull(id, "FloatClosed.id");
        }
    }
}
