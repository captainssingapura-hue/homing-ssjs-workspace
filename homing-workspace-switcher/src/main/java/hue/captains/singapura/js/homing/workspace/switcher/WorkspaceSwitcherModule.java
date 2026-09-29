package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoiceModule;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceSwitcherSecretaryModule;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The kinds and the chosen kind's workspaces: {@code new WorkspaceSwitcher(container, params)} -
 * a composed widget over the kinds' tree and the workspaces' table, their host,
 * where they meet in a workspace choice party of its own: a scope linked to the
 * party it is given, its secretary keeping the edge.
 */
public record WorkspaceSwitcherModule() implements DomModule<WorkspaceSwitcherModule> {

    public record WorkspaceSwitcher() implements SelfContainedWidget<WorkspaceSwitcherModule> {
        @Override public String summary() { return "The kinds of workspace and the chosen kind's workspaces, composed: they meet in a workspace choice scope of its own, linked to the party it joins."; }
    }

    public static final WorkspaceSwitcherModule INSTANCE = new WorkspaceSwitcherModule();

    @Override
    public ImportsFor<WorkspaceSwitcherModule> imports() {
        return ImportsFor.<WorkspaceSwitcherModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                // its subordinates
                .add(new ModuleImports<>(List.of(new WorkspaceKindsModule.WorkspaceKinds()), WorkspaceKindsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceInstancesModule.WorkspaceInstances()), WorkspaceInstancesModule.INSTANCE))
                // its scope: a party of the type they meet in, and the secretary that keeps its edge
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceSwitcherSecretaryModule.WorkspaceSwitcherSecretary()), WorkspaceSwitcherSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SwitcherStyles.sw_split(), new SwitcherStyles.sw_kinds(), new SwitcherStyles.sw_instances()), SwitcherStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceSwitcherModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceSwitcher())); }
}
