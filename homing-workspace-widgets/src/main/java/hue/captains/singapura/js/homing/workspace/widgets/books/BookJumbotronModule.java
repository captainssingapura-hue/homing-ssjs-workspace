package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The chosen book, big: {@code new BookJumbotron(container, params)} - its
 * title and its author, sized by the box it is lent. It holds no books; what
 * it shows is what the book selection party says, joined after it is made.
 */
public record BookJumbotronModule() implements DomModule<BookJumbotronModule> {

    public record BookJumbotron() implements SelfContainedWidget<BookJumbotronModule>, NeedKeyboard {
        @Override public String summary() { return "The chosen book, big - its title and its author, sized by its box - as the book selection party says it."; }
        @Override public List<KeyBinding> keys() { return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back")); }
    }

    public static final BookJumbotronModule INSTANCE = new BookJumbotronModule();

    @Override
    public ImportsFor<BookJumbotronModule> imports() {
        return ImportsFor.<BookJumbotronModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                // the type it joins, by which it is given a party
                .add(new ModuleImports<>(List.of(new BookSelectionModule.BOOK_SELECTION()), BookSelectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill()), WidgetStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookStyles.bj_frame(), new BookStyles.bj_stage(), new BookStyles.bj_title(), new BookStyles.bj_author(),
                        new BookStyles.bj_none(), new BookStyles.bj_hidden()), BookStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BookJumbotronModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BookJumbotron())); }
}
