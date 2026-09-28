package hue.captains.singapura.js.homing.workspace.demowidgets.books;

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
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetStyles;

import java.util.List;

/**
 * The books, in the relation grid: {@code new BooksGrid(container, params)}.
 * A widget that stands on its own - its DomOpsParty and FocusParty roots its
 * own, its data its own - with the browser's native focus inside it.
 */
public record BooksGridModule() implements DomModule<BooksGridModule> {

    public record BooksGrid() implements SelfContainedWidget<BooksGridModule>, NeedKeyboard {
        @Override public String summary() { return "The books in the relation grid, standing on its own: its roots its own, the grid's cells natively focused inside it."; }
        @Override public List<KeyBinding> keys() {
            return List.of(KeyBinding.of(Key.ESCAPE, "the keys given back, when the grid did not take it"));
        }
    }

    public static final BooksGridModule INSTANCE = new BooksGridModule();

    @Override
    public ImportsFor<BooksGridModule> imports() {
        return ImportsFor.<BooksGridModule>builder()
                // its own DomOps party and focus party, each from its party of parties
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BooksModule.BooksStore(), new BooksModule.BooksRelation()), BooksModule.INSTANCE))
                // the type it joins, by which it is given a party: its cursor is the choice
                .add(new ModuleImports<>(List.of(new BookSelectionModule.BOOK_SELECTION()), BookSelectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetStyles.wg_fill(), new WidgetStyles.wg_scroll()), WidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BooksGridModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BooksGrid())); }
}
