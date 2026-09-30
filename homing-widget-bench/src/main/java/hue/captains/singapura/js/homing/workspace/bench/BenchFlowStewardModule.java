package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/** The steward of the content bench's flows: {@code new BenchFlowSteward(tell)} - a {@code BenchContentSteward} of the type flow. */
public record BenchFlowStewardModule() implements EsModule<BenchFlowStewardModule> {

    public static final BenchFlowStewardModule INSTANCE = new BenchFlowStewardModule();

    public record BenchFlowSteward() implements Exportable._Class<BenchFlowStewardModule> {}

    @Override
    public ImportsFor<BenchFlowStewardModule> imports() {
        return ImportsFor.<BenchFlowStewardModule>builder()
                .add(new ModuleImports<>(List.of(new BenchContentStewardModule.BenchContentSteward()), BenchContentStewardModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BenchFlowStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchFlowSteward())); }
}
