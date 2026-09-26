package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * Stand-ins for the workspace's widgets while its tabs are built (RFC 0066 E3,
 * the workspace detour, step two): a note, a counter with native buttons and
 * arrows, a field with a native input. Each a tab's widget by the dock's law,
 * as the gallery's are — a member of the dock's branch itself.
 */
public record FakeWidgetsModule() implements DomModule<FakeWidgetsModule> {

    public record FakeNote() implements Exportable._Class<FakeWidgetsModule> {}
    public record FakeCounter() implements Exportable._Class<FakeWidgetsModule> {}
    public record FakeField() implements Exportable._Class<FakeWidgetsModule> {}

    public static final FakeWidgetsModule INSTANCE = new FakeWidgetsModule();

    @Override
    public ImportsFor<FakeWidgetsModule> imports() {
        return ImportsFor.<FakeWidgetsModule>builder()
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_fake(), new WorkspaceStyles.ws_fake_count(),
                                                 new WorkspaceStyles.ws_fake_row()), WorkspaceStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FakeWidgetsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new FakeNote(), new FakeCounter(), new FakeField()));
    }
}
