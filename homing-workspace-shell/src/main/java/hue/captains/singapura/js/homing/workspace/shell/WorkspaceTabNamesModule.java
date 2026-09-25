package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A tab's name and icon, for the {@link WorkspacePanesModule.WorkspacePanes}
 * holding the tab — the tab-pane's own: set wherever the tab is, on its dock's
 * chip or the floating pane's head, and on the tab so they come back down with
 * it. The icon elements are made here, one per tab. Static helpers over the
 * panes, split out of them to keep that module within the effective-line
 * ceiling.
 */
public record WorkspaceTabNamesModule() implements DomModule<WorkspaceTabNamesModule> {

    public static final WorkspaceTabNamesModule INSTANCE = new WorkspaceTabNamesModule();

    public record WorkspaceTabNames() implements Exportable._Class<WorkspaceTabNamesModule> {}

    @Override public ImportsFor<WorkspaceTabNamesModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceTabNamesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceTabNames()));
    }
}
