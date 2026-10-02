package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A widget's params as a content party carries them: {@code ContentParams.of(params)} - the
 * list of {@link Param}s in the order of their names - and back; the one text an item is kept
 * under. Pure.
 */
public record ContentParamsModule() implements EsModule<ContentParamsModule> {

    public static final ContentParamsModule INSTANCE = new ContentParamsModule();

    public record ContentParams() implements Exportable._Class<ContentParamsModule> {}

    @Override public ImportsFor<ContentParamsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<ContentParamsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ContentParams())); }
}
