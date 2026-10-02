package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The page's member of its workspace choice party: {@code WorkspaceOpener} -
 * what the party says is asked to open, opened by the address the page makes
 * of it, unless it is the workspace the page shows. Pure: the page hands it
 * the going.
 */
public record WorkspaceOpenerModule() implements EsModule<WorkspaceOpenerModule> {

    public record WorkspaceOpener() implements Exportable._Class<WorkspaceOpenerModule> {}

    public static final WorkspaceOpenerModule INSTANCE = new WorkspaceOpenerModule();

    @Override public ImportsFor<WorkspaceOpenerModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceOpenerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceOpener())); }
}
