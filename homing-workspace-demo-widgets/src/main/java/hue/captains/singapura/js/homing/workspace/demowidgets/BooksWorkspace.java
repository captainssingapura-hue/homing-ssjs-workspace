package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookBrowserDeclaration;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookJumbotronDeclaration;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The demo's workspace, declared: {@code books} - its log's kind too - where
 * the books can be opened: in the grid, big, and the two composed. Its root
 * parties are resolved from these kinds: the book selection, with its type's
 * default secretary.
 */
public record BooksWorkspace() implements WorkspaceDeclaration {

    public static final BooksWorkspace INSTANCE = new BooksWorkspace();

    @Override public String name() { return "books"; }

    @Override
    public List<WidgetDeclaration<?>> kinds() {
        return List.of(BooksGridDeclaration.INSTANCE, BookJumbotronDeclaration.INSTANCE, BookBrowserDeclaration.INSTANCE);
    }
}
