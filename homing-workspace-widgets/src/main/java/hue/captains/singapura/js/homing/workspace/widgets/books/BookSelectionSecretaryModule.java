package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The book selection party's secretary: {@code initial} and {@code
 * behavior(state, envelope) → { newState, actions }} - which book is chosen,
 * said to every member when it changes and to a member that asks; diligent,
 * its state answering an operator's questions, every kind tested.
 */
public record BookSelectionSecretaryModule() implements EsModule<BookSelectionSecretaryModule> {

    public record BookSelectionSecretary() implements Exportable._Constant<BookSelectionSecretaryModule> {}

    public static final BookSelectionSecretaryModule INSTANCE = new BookSelectionSecretaryModule();

    @Override public ImportsFor<BookSelectionSecretaryModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<BookSelectionSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BookSelectionSecretary())); }
}
