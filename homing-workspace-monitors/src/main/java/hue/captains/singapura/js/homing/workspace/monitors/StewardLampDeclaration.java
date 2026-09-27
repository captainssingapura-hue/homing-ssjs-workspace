package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The steward's lamp, declared: {@code steward-lamp}, with no params. */
public record StewardLampDeclaration() implements WidgetDeclaration<NoParams> {

    public static final StewardLampDeclaration INSTANCE = new StewardLampDeclaration();

    @Override public String kind() { return "steward-lamp"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new StewardLampModule.StewardLamp()), StewardLampModule.INSTANCE);
    }
}
