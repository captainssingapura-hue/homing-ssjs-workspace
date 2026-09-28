package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.demowidgets.WorkspaceDemoWidgetsCrate;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * The demo workspace, runnable: its manifest, generated from its declaration,
 * and its page. The widgets are the widget sets' own crates; the page is the
 * shell's.
 */
public final class WorkspaceDemoCrate implements Crate {

    public static final WorkspaceDemoCrate INSTANCE = new WorkspaceDemoCrate();

    private WorkspaceDemoCrate() {}

    @Override public String name() { return "homing-workspace-demo"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the page a workspace is: the shell's, handed the manifest
                WorkspaceShellCrate.INSTANCE,
                // the two widget sets it puts together
                WorkspaceDemoWidgetsCrate.INSTANCE,
                WorkspaceMonitorsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DemoWorkspaceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DemoWorkspaceApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
