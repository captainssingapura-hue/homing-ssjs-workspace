package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The secretary of every content party, at every level: {@code initial} and {@code
 * behavior(state, envelope) → { newState, actions }} - what was loaded kept and answered from,
 * who waits for what kept, the steward asked once for many askers, and askers told when there
 * is none. Diligent: its state answers what is held, what is pending, what failed.
 */
public record ContentSecretaryModule() implements EsModule<ContentSecretaryModule> {

    public static final ContentSecretaryModule INSTANCE = new ContentSecretaryModule();

    public record ContentSecretary() implements Exportable._Constant<ContentSecretaryModule> {}

    @Override
    public ImportsFor<ContentSecretaryModule> imports() {
        return ImportsFor.<ContentSecretaryModule>builder()
                .add(new ModuleImports<>(List.of(new ContentParamsModule.ContentParams()), ContentParamsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ContentSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new ContentSecretary())); }
}
