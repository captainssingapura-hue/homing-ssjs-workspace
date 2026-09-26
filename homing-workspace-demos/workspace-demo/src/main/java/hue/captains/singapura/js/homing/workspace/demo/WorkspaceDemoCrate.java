package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.focus.UiFocusCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * What the demo site serves beyond the workspace itself: its widgets, the kinds
 * that declare them, their sheet, and the small book domain two of them view.
 */
public final class WorkspaceDemoCrate implements Crate {

    public static final WorkspaceDemoCrate INSTANCE = new WorkspaceDemoCrate();

    private WorkspaceDemoCrate() {}

    @Override public String name() { return "workspace-demo"; }

    @Override public List<Crate> requires() {
        return List.of(WorkspaceShellCrate.INSTANCE, UiElementsCrate.INSTANCE, UiFocusCrate.INSTANCE,
                       hue.captains.singapura.js.homing.server.ServerCrate.INSTANCE,   // Keys, which the Focus lab's inner members claim through
                       RelGridCrate.INSTANCE, RelTreeCrate.INSTANCE, RelGridProtocolCrate.INSTANCE,
                       // the design substrate the demo's sheet wears words of: a crate edge, not a reach through the shell
                       hue.captains.singapura.js.homing.design.DesignCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DemoWidgetStyles.INSTANCE),
                CrateEntry.of(DemoWidgetsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(DemoNoteWidget.INSTANCE),
                CrateEntry.of(DemoCounterWidget.INSTANCE),
                // The views: the books as a grid and as shelves over one store, and a picture.
                CrateEntry.of(DemoRelationsModule.INSTANCE),
                CrateEntry.of(DemoViewsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(DemoBooksWidget.INSTANCE),
                CrateEntry.of(DemoShelvesWidget.INSTANCE),
                CrateEntry.of(DemoPictureWidget.INSTANCE),
                // The Focus lab: widgets that act on each other's focus, and the monitors (RFC 0066 E3, keyboard §17.5).
                CrateEntry.of(DemoFocusLabModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(DemoFormWidget.INSTANCE),
                CrateEntry.of(DemoSummonerWidget.INSTANCE),
                CrateEntry.of(DemoMonitorWidget.INSTANCE));
    }
}
