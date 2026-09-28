package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * How a widget is named for the eye unless a page says otherwise, in
 * JavaScript - the dual of {@link hue.captains.singapura.js.homing.workspace.core.KindAndParamsTitle}.
 * A display rule, apart from the id's model; it touches no DOM.
 */
public record KindAndParamsTitleModule() implements EsModule<KindAndParamsTitleModule> {

    public record KindAndParamsTitle() implements Exportable._Class<KindAndParamsTitleModule> {}

    public static final KindAndParamsTitleModule INSTANCE = new KindAndParamsTitleModule();

    @Override
    public ImportsFor<KindAndParamsTitleModule> imports() {
        return ImportsFor.<KindAndParamsTitleModule>builder()
                .add(new ModuleImports<>(List.of(new WidgetIdsModule.WidgetIds()), WidgetIdsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KindAndParamsTitleModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new KindAndParamsTitle())); }
}
