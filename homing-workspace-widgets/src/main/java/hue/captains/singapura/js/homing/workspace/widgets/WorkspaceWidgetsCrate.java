package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookBrowserModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookBrowserSecretaryModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookJumbotronModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookSelectionModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookSelectionSecretaryModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookStyles;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksModule;

import java.util.List;

/** The workspace's widgets, each self-contained, the sheet they fill their containers by, and the parties' types they join. */
public final class WorkspaceWidgetsCrate implements Crate {

    public static final WorkspaceWidgetsCrate INSTANCE = new WorkspaceWidgetsCrate();

    private WorkspaceWidgetsCrate() {}

    @Override public String name() { return "homing-workspace-widgets"; }

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
                WorkspacePartiesCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetStyles.INSTANCE),
                // a widget where a component would stand: the host's side of the graft
                CrateEntry.of(HostedWidgetModule.INSTANCE, StandardJsModuleType.CONSUMER),
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
                CrateEntry.of(BookBrowserModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
