package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.panes.PaneEventsModule;

import java.util.List;

/**
 * The split grid as a workspace's placement: a widget's tab-pane mounted in a
 * region's dock or a float - a move the desk reports - and unmounted, which the
 * placement says itself, since the host did not start it. It comes back from
 * the grid's layer of the log, the roster already back, and lets go what the
 * roster does not hold, in the grid's events.
 */
public record GridPlacementModule() implements DomModule<GridPlacementModule> {

    public record GridPlacement() implements Exportable._Class<GridPlacementModule> {}

    public static final GridPlacementModule INSTANCE = new GridPlacementModule();

    @Override
    public ImportsFor<GridPlacementModule> imports() {
        return ImportsFor.<GridPlacementModule>builder()
                .add(new ModuleImports<>(List.of(new PaneEventsModule.PaneEvents()), PaneEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GridPlacementModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GridPlacement())); }
}
