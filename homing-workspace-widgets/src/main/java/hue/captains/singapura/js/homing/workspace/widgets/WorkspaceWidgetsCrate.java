package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksModule;

import java.util.List;

/** The workspace's widgets, each self-contained, and the sheet they fill their containers by. */
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
                RelGridCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetStyles.INSTANCE),
                // the books, in the relation grid: native focus inside a widget
                CrateEntry.of(BooksModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(BooksGridModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
