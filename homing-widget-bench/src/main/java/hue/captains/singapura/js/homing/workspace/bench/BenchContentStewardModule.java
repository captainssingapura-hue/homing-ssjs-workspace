package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.content.ContentParamsModule;

import java.util.List;

/**
 * What the content bench's stewards have in common: {@code BenchContentSteward} - a steward
 * that fetches each item it is sent for from the bench's server, by its type and its params,
 * tells what came, and keeps what it fetched and how often the server says it was asked.
 */
public record BenchContentStewardModule() implements EsModule<BenchContentStewardModule> {

    public static final BenchContentStewardModule INSTANCE = new BenchContentStewardModule();

    public record BenchContentSteward() implements Exportable._Class<BenchContentStewardModule> {}

    @Override
    public ImportsFor<BenchContentStewardModule> imports() {
        return ImportsFor.<BenchContentStewardModule>builder()
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BenchContentStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchContentSteward())); }
}
