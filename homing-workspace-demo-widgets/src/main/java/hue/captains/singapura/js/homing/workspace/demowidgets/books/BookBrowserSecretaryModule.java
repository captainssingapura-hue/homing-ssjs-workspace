package hue.captains.singapura.js.homing.workspace.demowidgets.books;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The secretary of a book browser's own book selection party: the book
 * selection secretary within the scope, and the scope's edge - which of a
 * member's words go up, declared kind by kind, and what comes down taken as
 * the scope's own choice, never sent back up.
 */
public record BookBrowserSecretaryModule() implements EsModule<BookBrowserSecretaryModule> {

    public record BookBrowserSecretary() implements Exportable._Constant<BookBrowserSecretaryModule> {}

    public static final BookBrowserSecretaryModule INSTANCE = new BookBrowserSecretaryModule();

    @Override
    public ImportsFor<BookBrowserSecretaryModule> imports() {
        return ImportsFor.<BookBrowserSecretaryModule>builder()
                // within the scope, it is the book selection secretary
                .add(new ModuleImports<>(List.of(new BookSelectionSecretaryModule.BookSelectionSecretary()), BookSelectionSecretaryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BookBrowserSecretaryModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BookBrowserSecretary())); }
}
