package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The workspace's headless core, in JavaScript: the roster and the widgets'
 * life - the dual of {@link hue.captains.singapura.js.homing.workspace.core.WorkspaceCore}.
 * It touches no DOM and imports no DOM module: the widgets' classes are handed to it.
 */
public record WorkspaceCoreModule() implements EsModule<WorkspaceCoreModule> {

    public record WorkspaceCore() implements Exportable._Class<WorkspaceCoreModule> {}

    public static final WorkspaceCoreModule INSTANCE = new WorkspaceCoreModule();

    @Override
    public ImportsFor<WorkspaceCoreModule> imports() {
        return ImportsFor.<WorkspaceCoreModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetIdsModule.WidgetIds()), WidgetIdsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceCoreModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceCore())); }
}
