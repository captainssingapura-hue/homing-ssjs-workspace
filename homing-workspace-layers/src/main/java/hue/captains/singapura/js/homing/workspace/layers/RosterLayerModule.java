package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.WidgetIdModule;
import hue.captains.singapura.js.homing.workspace.log.js.RosterEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.WidgetParamModule;

import java.util.List;

/** The roster's layer of the log, live, in JavaScript: the dual of {@link hue.captains.singapura.js.homing.workspace.layers.RosterLayer}. No DOM. */
public record RosterLayerModule() implements DomModule<RosterLayerModule> {

    public record RosterLayer() implements Exportable._Class<RosterLayerModule> {}

    public static final RosterLayerModule INSTANCE = new RosterLayerModule();

    @Override
    public ImportsFor<RosterLayerModule> imports() {
        return ImportsFor.<RosterLayerModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetIdModule.WidgetId()), WidgetIdModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.WidgetKind()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetParamModule.WidgetParam()), WidgetParamModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RosterEventModule.WidgetOpened(), new RosterEventModule.WidgetClosed()), RosterEventModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RosterLayerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new RosterLayer())); }
}
