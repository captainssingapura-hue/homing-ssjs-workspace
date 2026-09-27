package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The bench's manual secretary: {@code initial} and {@code behavior(state,
 * envelope) → { newState, actions }} - it decides nothing, keeps what it
 * heard, and leaves what goes down to a hand at the party's simulator.
 */
public record BenchSecretaryModule() implements EsModule<BenchSecretaryModule> {

    public record BenchSecretary() implements Exportable._Constant<BenchSecretaryModule> {}

    public static final BenchSecretaryModule INSTANCE = new BenchSecretaryModule();

    @Override public ImportsFor<BenchSecretaryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<BenchSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BenchSecretary())); }
}
