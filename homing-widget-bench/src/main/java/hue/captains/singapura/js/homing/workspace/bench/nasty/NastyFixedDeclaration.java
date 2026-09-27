package hue.captains.singapura.js.homing.workspace.bench.nasty;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;
import java.util.Map;

/** The fixed-size nasty widget, declared: {@code nasty-fixed}, with no params. */
public record NastyFixedDeclaration() implements WidgetDeclaration<NastyFixedDeclaration.Params> {

    public static final NastyFixedDeclaration INSTANCE = new NastyFixedDeclaration();

    /** Nothing: the widget is the same whatever the address says. */
    public record Params() implements WidgetParams {
        public static final Params DEFAULT = new Params();
    }

    /** Reads nothing, and writes nothing. */
    public record Query() implements WidgetQuery<Params> {
        @Override public Read<Params> from(Map<String, List<String>> query) { return Read.ok(Params.DEFAULT); }
        @Override public Map<String, List<String>> to(Params params) { return Map.of(); }
    }

    @Override public String kind() { return "nasty-fixed"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public WidgetQuery<Params> query() { return new Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new NastyFixedModule.NastyFixed()), NastyFixedModule.INSTANCE);
    }
}
