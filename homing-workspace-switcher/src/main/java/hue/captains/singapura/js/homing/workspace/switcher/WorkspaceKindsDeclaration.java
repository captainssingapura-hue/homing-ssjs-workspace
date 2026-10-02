package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceChoice;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The kinds of workspace, declared: {@code workspace-kinds}, with no params, joining the workspace choice party. */
public record WorkspaceKindsDeclaration() implements WidgetDeclaration<NoParams> {

    public static final WorkspaceKindsDeclaration INSTANCE = new WorkspaceKindsDeclaration();

    @Override public String kind() { return "workspace-kinds"; }
    @Override public String title() { return "Workspace kinds"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
    @Override public List<PartyType<?>> parties() { return List.of(WorkspaceChoice.TYPE); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new WorkspaceKindsModule.WorkspaceKinds()), WorkspaceKindsModule.INSTANCE);
    }
}
