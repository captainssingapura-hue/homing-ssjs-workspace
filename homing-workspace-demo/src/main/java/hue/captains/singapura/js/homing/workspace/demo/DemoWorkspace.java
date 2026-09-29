package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.demowidgets.WorkspaceDemoWidgetsCrate;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;
import java.util.stream.Stream;

/**
 * The demo workspace, declared: {@code demo} - its log's kind too - where the
 * demo's widgets can be opened - the books, in the grid, big, and the two
 * composed - the workspace switcher's, and every monitor beside them, watching
 * the page's parties. Three widget sets, each a module of widgets only, put
 * together here: its root parties are resolved from their kinds - the book
 * selection and the workspace choice, each with its type's default secretary;
 * the monitors join none.
 */
public record DemoWorkspace() implements WorkspaceDeclaration {

    public static final DemoWorkspace INSTANCE = new DemoWorkspace();

    @Override public String name() { return "demo"; }

    @Override
    public List<WidgetDeclaration<?>> kinds() {
        return Stream.of(WorkspaceDemoWidgetsCrate.KINDS, WorkspaceSwitcherCrate.KINDS, WorkspaceMonitorsCrate.KINDS).flatMap(List::stream).toList();
    }
}
