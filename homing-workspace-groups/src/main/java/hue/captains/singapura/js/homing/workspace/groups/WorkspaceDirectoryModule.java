package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The page's directory of its site's workspace groups: {@code WorkspaceDirectory},
 * provided once by the page - the shape {@link WorkspaceGroupsJs} generates -
 * and read by whatever shows the groups, never written by it.
 */
public record WorkspaceDirectoryModule() implements EsModule<WorkspaceDirectoryModule> {

    public record WorkspaceDirectory() implements Exportable._Class<WorkspaceDirectoryModule> {}

    public static final WorkspaceDirectoryModule INSTANCE = new WorkspaceDirectoryModule();

    @Override public ImportsFor<WorkspaceDirectoryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceDirectoryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceDirectory())); }
}
