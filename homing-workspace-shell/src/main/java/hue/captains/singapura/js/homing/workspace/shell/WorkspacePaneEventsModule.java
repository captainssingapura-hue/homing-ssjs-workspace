package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.panes.PaneEventsModule;

import java.util.List;

/**
 * Every event from the grid, the docks and the desk on to the panes' holder,
 * with what only the assembly knows filled in: the holder's own tab on every
 * event, a merge said as one fact, a tab closed afloat said as the tab closed.
 * Static helpers over {@link WorkspacePanesModule.WorkspacePanes}, split out of
 * it to keep that module within the effective-line ceiling. Pure: no DOM.
 */
public record WorkspacePaneEventsModule() implements EsModule<WorkspacePaneEventsModule> {

    public static final WorkspacePaneEventsModule INSTANCE = new WorkspacePaneEventsModule();

    public record WorkspacePaneEvents() implements Exportable._Class<WorkspacePaneEventsModule> {}

    @Override
    public ImportsFor<WorkspacePaneEventsModule> imports() {
        return ImportsFor.<WorkspacePaneEventsModule>builder()
                .add(new ModuleImports<>(List.of(new PaneEventsModule.PaneEvents()), PaneEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspacePaneEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspacePaneEvents()));
    }
}
