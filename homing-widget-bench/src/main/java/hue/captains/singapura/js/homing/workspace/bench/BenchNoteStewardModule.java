package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/** The steward of the content bench's notes: {@code new BenchNoteSteward(tell)} - a {@code BenchContentSteward} of the type note. */
public record BenchNoteStewardModule() implements EsModule<BenchNoteStewardModule> {

    public static final BenchNoteStewardModule INSTANCE = new BenchNoteStewardModule();

    public record BenchNoteSteward() implements Exportable._Class<BenchNoteStewardModule> {}

    @Override
    public ImportsFor<BenchNoteStewardModule> imports() {
        return ImportsFor.<BenchNoteStewardModule>builder()
                .add(new ModuleImports<>(List.of(new BenchContentStewardModule.BenchContentSteward()), BenchContentStewardModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BenchNoteStewardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchNoteSteward())); }
}
