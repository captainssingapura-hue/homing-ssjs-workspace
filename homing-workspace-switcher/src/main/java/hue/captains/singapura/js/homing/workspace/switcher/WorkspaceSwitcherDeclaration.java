package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;
import java.util.stream.Stream;

/**
 * The workspace switcher, declared: {@code workspace-switcher}, with no params.
 * The parties it joins are its subordinates', the union of theirs - derived
 * from their declarations, never written again here.
 */
public record WorkspaceSwitcherDeclaration() implements WidgetDeclaration<NoParams> {

    public static final WorkspaceSwitcherDeclaration INSTANCE = new WorkspaceSwitcherDeclaration();

    @Override public String kind() { return "workspace-switcher"; }
    @Override public String title() { return "Workspace switcher"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    /** The union of its subordinates': the kinds' and the workspaces'. */
    @Override
    public List<PartyType<?>> parties() {
        return Stream.of(WorkspaceKindsDeclaration.INSTANCE.parties(),
                         WorkspaceInstancesDeclaration.INSTANCE.parties())
                .flatMap(List::stream).distinct().toList();
    }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new WorkspaceSwitcherModule.WorkspaceSwitcher()), WorkspaceSwitcherModule.INSTANCE);
    }
}
