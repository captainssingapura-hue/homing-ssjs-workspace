package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.PaneEventModule;

import java.util.List;

/** The one pane's layer of the log, live, in JavaScript: the dual of {@link hue.captains.singapura.js.homing.workspace.layers.PaneLayer}. No DOM. */
public record PaneLayerModule() implements DomModule<PaneLayerModule> {

    public record PaneLayer() implements Exportable._Class<PaneLayerModule> {}

    public static final PaneLayerModule INSTANCE = new PaneLayerModule();

    @Override
    public ImportsFor<PaneLayerModule> imports() {
        return ImportsFor.<PaneLayerModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.WidgetId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneEventModule.PaneShown()), PaneEventModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PaneLayerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PaneLayer())); }
}
