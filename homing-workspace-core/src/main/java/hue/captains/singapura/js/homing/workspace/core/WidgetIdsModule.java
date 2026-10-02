package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** How a widget's id is made, in JavaScript: the dual of {@link hue.captains.singapura.js.homing.workspace.core.WidgetIds}. Headless. */
public record WidgetIdsModule() implements EsModule<WidgetIdsModule> {

    public record WidgetIds() implements Exportable._Class<WidgetIdsModule> {}

    public static final WidgetIdsModule INSTANCE = new WidgetIdsModule();

    @Override public ImportsFor<WidgetIdsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WidgetIdsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WidgetIds())); }
}
