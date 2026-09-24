package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * What the demo site serves beyond the workspace itself: its two widgets, the
 * kinds that declare them, and their sheet.
 */
public final class WorkspaceDemoCrate implements Crate {

    public static final WorkspaceDemoCrate INSTANCE = new WorkspaceDemoCrate();

    private WorkspaceDemoCrate() {}

    @Override public String name() { return "workspace-demo"; }

    @Override public List<Crate> requires() {
        return List.of(WorkspaceShellCrate.INSTANCE, UiElementsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DemoWidgetStyles.INSTANCE),
                CrateEntry.of(DemoWidgetsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(DemoNoteWidget.INSTANCE),
                CrateEntry.of(DemoCounterWidget.INSTANCE));
    }
}
