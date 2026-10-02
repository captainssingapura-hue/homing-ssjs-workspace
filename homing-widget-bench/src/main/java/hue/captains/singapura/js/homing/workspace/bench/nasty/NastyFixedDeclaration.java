package hue.captains.singapura.js.homing.workspace.bench.nasty;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The fixed-size nasty widget, declared: {@code nasty-fixed}, with no params. */
public record NastyFixedDeclaration() implements WidgetDeclaration<NoParams> {

    public static final NastyFixedDeclaration INSTANCE = new NastyFixedDeclaration();

    @Override public String kind() { return "nasty-fixed"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new NastyFixedModule.NastyFixed()), NastyFixedModule.INSTANCE);
    }
}
