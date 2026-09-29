package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The workspace choice party's secretary: {@code initial} and {@code
 * behavior(state, envelope) → { newState, actions }} - which kind is chosen,
 * said to every member when it changes and to a member that asks; and each
 * workspace asked to open, said to every member, for whoever opens them.
 * Diligent: its state answers an operator's questions, every kind tested.
 */
public record WorkspaceChoiceSecretaryModule() implements EsModule<WorkspaceChoiceSecretaryModule> {

    public record WorkspaceChoiceSecretary() implements Exportable._Constant<WorkspaceChoiceSecretaryModule> {}

    public static final WorkspaceChoiceSecretaryModule INSTANCE = new WorkspaceChoiceSecretaryModule();

    @Override public ImportsFor<WorkspaceChoiceSecretaryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceChoiceSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceChoiceSecretary())); }
}
