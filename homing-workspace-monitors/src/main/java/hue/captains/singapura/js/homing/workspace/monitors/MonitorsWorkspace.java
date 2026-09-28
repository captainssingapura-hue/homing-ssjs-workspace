package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The monitors' workspace, declared: {@code monitors} - its log's kind too -
 * where every monitor can be opened, each watching the page's own parties: the
 * workspace watching itself.
 */
public record MonitorsWorkspace() implements WorkspaceDeclaration {

    public static final MonitorsWorkspace INSTANCE = new MonitorsWorkspace();

    @Override public String name() { return "monitors"; }

    @Override public List<WidgetDeclaration<?>> kinds() { return WorkspaceMonitorsCrate.KINDS; }
}
