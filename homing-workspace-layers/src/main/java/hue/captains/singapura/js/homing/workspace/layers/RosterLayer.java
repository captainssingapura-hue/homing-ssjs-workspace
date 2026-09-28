package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.workspace.core.WorkspaceCore;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.RosterEvent;
import hue.captains.singapura.js.homing.workspace.log.RosterState;
import hue.captains.singapura.js.homing.workspace.log.WidgetParam;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The roster's layer of the workspace log, live: the core's word recorded -
 * a widget opened, its id, its kind and its params in the order of their keys;
 * a widget closed - and a roster come back to. It sits beside the core, never
 * in it: the core says what happens, and knows no log.
 *
 * <p>A workspace comes back ROSTER FIRST - every id its prefixes gave spent,
 * closed or not, so the ids go on past them; then each widget held made again
 * under its id, in the order opened - and its placement after. Nothing of the
 * coming back is recorded: the recorder is attached after it.</p>
 *
 * <p>The JavaScript is this, step for step (RosterLayerModule.js), and the two
 * write the same lines (LayersParityTest).</p>
 */
public final class RosterLayer {

    private RosterLayer() {}

    /** The event a notice of the core's is recorded as: an opening, a closing - or none, for a widget about to close. */
    public static Optional<RosterEvent> event(WorkspaceCore.Notice notice) {
        return switch (notice) {
            case WorkspaceCore.Notice.WidgetOpened o -> Optional.of(new RosterEvent.WidgetOpened(
                    o.entry().id(), WidgetKind.of(o.entry().kind()), WidgetParam.of(o.entry().params())));
            case WorkspaceCore.Notice.WidgetClosing c -> Optional.empty();
            case WorkspaceCore.Notice.WidgetClosed c -> Optional.of(new RosterEvent.WidgetClosed(c.id()));
        };
    }

    /** The core's word recorded into a log, as it is said; the runnable returned stops it. */
    public static Runnable record(WorkspaceCore<?, ?> core, Consumer<? super WorkspaceEvent> log) {
        return core.on(n -> event(n).ifPresent(log));
    }

    /** What came back: the widgets made again, and those that could not be - a kind the workspace no longer has, or that failed. */
    public record Restored(List<WidgetId> opened, List<WidgetId> skipped) {
        public Restored { opened = List.copyOf(opened); skipped = List.copyOf(skipped); }
    }

    /** A roster come back to: its ids spent, then its widgets made again under them, in the order opened. */
    public static Restored restore(WorkspaceCore<?, ?> core, RosterState roster) {
        for (var s : roster.sequences()) core.spend(s.prefix(), s.last());
        var opened = new ArrayList<WidgetId>();
        var skipped = new ArrayList<WidgetId>();
        for (var w : roster.widgets()) {
            try {
                core.open(w.kind().value(), WidgetParam.asMap(w.params()), w.id());
                opened.add(w.id());
            } catch (RuntimeException e) {
                skipped.add(w.id());
            }
        }
        return new Restored(opened, skipped);
    }
}
