package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.demowidgets.WorkspaceDemoWidgetsCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;
import java.util.stream.Stream;

/**
 * The books workspace, declared: {@code books} - its log's kind too - where the
 * demo's books can be opened, and the switcher, to go elsewhere. Its root parties
 * are resolved from its kinds: the book selection and the workspace choice.
 */
public record BooksWorkspace() implements WorkspaceDeclaration {

    public static final BooksWorkspace INSTANCE = new BooksWorkspace();

    @Override public String name() { return "books"; }

    @Override
    public List<WidgetDeclaration<?>> kinds() {
        return Stream.of(WorkspaceDemoWidgetsCrate.KINDS, WorkspaceSwitcherCrate.KINDS).flatMap(List::stream).toList();
    }
}
