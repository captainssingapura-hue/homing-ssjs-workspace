package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.demowidgets.WorkspaceDemoWidgetsCrate;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsCrate;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.site.WorkspaceSiteCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;

import java.util.List;

/**
 * The demo's workspaces, runnable: their manifests, generated from their
 * declarations; their groups; and their page. The widgets are the widget sets'
 * own crates; the page is the grouped site's, on the shell's.
 */
public final class WorkspaceDemoCrate implements Crate {

    public static final WorkspaceDemoCrate INSTANCE = new WorkspaceDemoCrate();

    private WorkspaceDemoCrate() {}

    @Override public String name() { return "homing-workspace-demo"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the page a grouped workspace is: the shell's, choosing the workspace by its route
                WorkspaceSiteCrate.INSTANCE,
                // the three widget sets it puts together
                WorkspaceDemoWidgetsCrate.INSTANCE,
                WorkspaceSwitcherCrate.INSTANCE,
                WorkspaceMonitorsCrate.INSTANCE,
                // the workspace choice party its manifests make at their roots: its type and secretary
                WorkspaceGroupsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DemoWorkspacesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DemoGroupsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DemoArrangementsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(DemoWorkspaceApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
