package hue.captains.singapura.js.homing.workspace.demowidgets;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookBrowserModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookBrowserSecretaryModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookJumbotronModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookSelectionModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookSelectionSecretaryModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookStyles;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksModule;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/** The demo's widgets, a set of their own: the books - in the relation grid, big, and the two composed - and the party they meet in; runnable, a workspace of them on the shell's page. */
public final class WorkspaceDemoWidgetsCrate implements Crate {

    public static final WorkspaceDemoWidgetsCrate INSTANCE = new WorkspaceDemoWidgetsCrate();

    private WorkspaceDemoWidgetsCrate() {}

    @Override public String name() { return "homing-workspace-demo-widgets"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOpsParty a widget mints its root from
                CoreJsCrate.INSTANCE,
                // the focus party, the keys' convention, the css manager
                ServerCrate.INSTANCE,
                // the books widget's grid and its stock cells
                RelGridCrate.INSTANCE,
                // the design words the books' sheet wears
                DesignCrate.INSTANCE,
                // the messaging parties' runtime: a composed widget's own scope
                WorkspacePartiesCrate.INSTANCE,
                // what a widget is: the sheet it fills its container by
                WorkspaceWidgetsCrate.INSTANCE,
                // the page a workspace of them is: the shell's, handed their manifest
                WorkspaceShellCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // the books, in the relation grid: native focus inside a widget
                CrateEntry.of(BooksModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(BooksGridModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the book selection party: its type, generated from Java, and its secretary
                CrateEntry.of(BookSelectionModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(BookSelectionSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                // the chosen book, big
                CrateEntry.of(BookStyles.INSTANCE),
                CrateEntry.of(BookJumbotronModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the two composed: they meet in a scope of the browser's own, its secretary keeping the edge
                CrateEntry.of(BookBrowserSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(BookBrowserModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // runnable: the books workspace, its manifest from its declaration, and its page
                CrateEntry.of(BooksWorkspaceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(BooksWorkspaceApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
