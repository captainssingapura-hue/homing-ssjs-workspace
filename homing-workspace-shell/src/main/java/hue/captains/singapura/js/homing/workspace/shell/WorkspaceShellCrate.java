package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.site.mpa.MpaCrate;
import hue.captains.singapura.js.homing.ui.docking.UiDockingCrate;
import hue.captains.singapura.js.homing.ui.menu.UiMenuCrate;
import hue.captains.singapura.js.homing.ui.panes.UiPanesCrate;
import hue.captains.singapura.js.homing.ui.splitgrid.UiSplitGridCrate;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreCrate;
import hue.captains.singapura.js.homing.workspace.layers.WorkspaceLayersCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-shell}: the split-grid
 * workspace on its headless core, as a page of any standard MPA that a widget
 * set's app hands its manifest to ({@code WorkspacePage}), built as the
 * gallery's docking page is — a desk and its dock grid on a floor — with its log
 * recorded, restored and exported. It knows no widget. Requires the log below it,
 * the core and its layers, the core-js and server substrate, the MPA and the
 * ui-components it is built of — and nothing of the studio.
 */
public final class WorkspaceShellCrate implements Crate {

    public static final WorkspaceShellCrate INSTANCE = new WorkspaceShellCrate();

    private WorkspaceShellCrate() {}

    @Override public String name() { return "homing-workspace-shell"; }

    @Override public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                // The framework page model the workspace is a page of, for the one
                // word it wears on the slot it is handed.
                MpaCrate.INSTANCE,
                // The ones the workspace's panes are made of, and its menus.
                UiSplitGridCrate.INSTANCE,
                UiPanesCrate.INSTANCE,
                UiDockingCrate.INSTANCE,
                UiMenuCrate.INSTANCE,
                WorkspaceLogCodecCrate.INSTANCE,
                // The workspace log: its store, where it keeps its rows, its export, its fold.
                hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate.INSTANCE,
                // The headless core a workspace's widgets live in, and the parties beside it; the roster's
                // layer of the log; what a widget is to a workspace, and the holder its tab lends it.
                WorkspaceCoreCrate.INSTANCE,
                WorkspaceLayersCrate.INSTANCE,
                WorkspaceWidgetsCrate.INSTANCE,
                // The design substrate its sheets wear words of. No legacy palette:
                // nothing here reads a --color-* or a legacy font token.
                hue.captains.singapura.js.homing.design.DesignCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // The addresses a workspace page reaches, stamped.
                CrateEntry.of(WorkspaceAddressesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // Its log: the recorder (headless), the restore and read-back, and the bar that exports it.
                CrateEntry.of(WorkspaceRecorderModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceProjectionModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceLogBarModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceStyles.INSTANCE),
                // What a right-click offers: the tab's, which is the pane's own,
                // and the room's, which only the workspace can know.
                CrateEntry.of(WorkspaceMenus.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // THE WORKSPACE ON ITS CORE, for any workspace declared in Java: a page a widget set's app
                // hands its manifest to; the split grid on the core; its register of panes; its placement.
                CrateEntry.of(WorkspacePageModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(GridWorkspaceModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WidgetTabsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(GridPlacementModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // a split grid's arrangement, laid out on a workspace that has nothing yet
                CrateEntry.of(GridArrangementModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
