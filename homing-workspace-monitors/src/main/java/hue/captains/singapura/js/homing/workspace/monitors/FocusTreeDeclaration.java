package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The focus tree monitor, declared: {@code focus-tree}, with no params. */
public record FocusTreeDeclaration() implements WidgetDeclaration<NoParams> {

    public static final FocusTreeDeclaration INSTANCE = new FocusTreeDeclaration();

    @Override public String kind() { return "focus-tree"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new FocusTreeModule.FocusTree()), FocusTreeModule.INSTANCE);
    }
}
