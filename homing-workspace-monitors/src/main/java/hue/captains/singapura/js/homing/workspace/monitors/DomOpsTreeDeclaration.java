package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The DomOps tree monitor, declared: {@code domops-tree}, with no params. */
public record DomOpsTreeDeclaration() implements WidgetDeclaration<NoParams> {

    public static final DomOpsTreeDeclaration INSTANCE = new DomOpsTreeDeclaration();

    @Override public String kind() { return "domops-tree"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new DomOpsTreeModule.DomOpsTree()), DomOpsTreeModule.INSTANCE);
    }
}
