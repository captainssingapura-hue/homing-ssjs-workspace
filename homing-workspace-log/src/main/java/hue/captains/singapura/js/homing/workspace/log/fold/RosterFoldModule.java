package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.RosterEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.RosterStateModule;

import java.util.List;

/** The roster's layer of the fold: a roster event on the RosterState, as Java's RosterFold folds it. */
public record RosterFoldModule() implements DomModule<RosterFoldModule> {

    public record RosterFold() implements Exportable._Class<RosterFoldModule> {}

    public static final RosterFoldModule INSTANCE = new RosterFoldModule();

    @Override
    public ImportsFor<RosterFoldModule> imports() {
        return ImportsFor.<RosterFoldModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.WidgetId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RosterEventModule.WidgetOpened(), new RosterEventModule.WidgetClosed()), RosterEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RosterStateModule.RosterState(), new RosterStateModule.RosterEntry(), new RosterStateModule.PrefixSequence()), RosterStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RosterFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new RosterFold())); }
}
