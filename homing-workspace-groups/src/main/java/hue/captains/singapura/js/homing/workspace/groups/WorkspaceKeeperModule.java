package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The page's member of its workspace choice party that keeps the workspaces:
 * {@code WorkspaceKeeper} - a workspace called otherwise, or deleted, done by the
 * page, and how it went said to every view of the kind (Report). Pure: the page
 * hands it the doing.
 */
public record WorkspaceKeeperModule() implements EsModule<WorkspaceKeeperModule> {

    public record WorkspaceKeeper() implements Exportable._Class<WorkspaceKeeperModule> {}

    public static final WorkspaceKeeperModule INSTANCE = new WorkspaceKeeperModule();

    @Override public ImportsFor<WorkspaceKeeperModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceKeeperModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceKeeper())); }
}
