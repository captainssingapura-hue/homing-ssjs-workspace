package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The secretary of a switcher's own workspace choice party: the workspace
 * choice secretary within the scope, and the scope's edge - which of a
 * member's words go up, declared kind by kind, and what comes down taken as
 * the scope's own choice, never sent back up.
 */
public record WorkspaceSwitcherSecretaryModule() implements EsModule<WorkspaceSwitcherSecretaryModule> {

    public record WorkspaceSwitcherSecretary() implements Exportable._Constant<WorkspaceSwitcherSecretaryModule> {}

    public static final WorkspaceSwitcherSecretaryModule INSTANCE = new WorkspaceSwitcherSecretaryModule();

    @Override
    public ImportsFor<WorkspaceSwitcherSecretaryModule> imports() {
        return ImportsFor.<WorkspaceSwitcherSecretaryModule>builder()
                // within the scope, it is the workspace choice secretary
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceSecretaryModule.WorkspaceChoiceSecretary()), WorkspaceChoiceSecretaryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceSwitcherSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceSwitcherSecretary())); }
}
