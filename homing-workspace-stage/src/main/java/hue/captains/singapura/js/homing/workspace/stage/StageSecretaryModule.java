package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The secretary of a page's stage party: {@code initial} and {@code behavior(state, envelope) →
 * { newState, actions }} - one widget on the stage at a time; a member's asks sent to the steward,
 * the steward's words told to every member; a second Present refused as the bug it is, and kept.
 */
public record StageSecretaryModule() implements EsModule<StageSecretaryModule> {

    public static final StageSecretaryModule INSTANCE = new StageSecretaryModule();

    public record StageSecretary() implements Exportable._Constant<StageSecretaryModule> {}

    @Override public ImportsFor<StageSecretaryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<StageSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new StageSecretary())); }
}
