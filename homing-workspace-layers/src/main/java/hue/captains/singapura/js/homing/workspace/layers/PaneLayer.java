package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;
import hue.captains.singapura.js.homing.workspace.log.PaneEvent;
import hue.captains.singapura.js.homing.workspace.log.PaneState;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * The one pane's layer of the workspace log, live: every change of what the
 * pane shows recorded; and what it showed come back to, after the roster has
 * come back - a widget that did not come back is not shown. The JavaScript is
 * this, step for step (PaneLayerModule.js), and the two write the same lines
 * (LayersParityTest).
 */
public final class PaneLayer {

    private PaneLayer() {}

    /** The event a notice of the pane's is recorded as. */
    public static PaneEvent event(PanePlacement.Notice notice) {
        return switch (notice) {
            case PanePlacement.Notice.PaneShown s -> new PaneEvent.PaneShown(s.widget());
        };
    }

    /** The pane's word recorded into a log, as it is said; the runnable returned stops it. */
    public static Runnable record(PanePlacement pane, Consumer<? super WorkspaceEvent> log) {
        return pane.on(n -> log.accept(event(n)));
    }

    /** What the pane showed come back to, the roster having come back first: the widget shown, if it came back. */
    public static Optional<WidgetId> restore(PanePlacement pane, PaneState state) {
        Optional<WidgetId> shown = state.shown().filter(pane.lent()::contains);
        pane.show(shown);
        return shown;
    }
}
