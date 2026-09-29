package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;
import java.util.stream.Stream;

/**
 * The monitors workspace, declared: {@code monitors} - its log's kind too - where
 * every monitor can be opened, watching the page's parties, and the switcher, to
 * go elsewhere. Its one root party is the workspace choice; the monitors join none.
 */
public record MonitorsWorkspace() implements WorkspaceDeclaration {

    public static final MonitorsWorkspace INSTANCE = new MonitorsWorkspace();

    @Override public String name() { return "monitors"; }

    @Override
    public List<WidgetDeclaration<?>> kinds() {
        return Stream.of(WorkspaceMonitorsCrate.KINDS, WorkspaceSwitcherCrate.KINDS).flatMap(List::stream).toList();
    }
}
