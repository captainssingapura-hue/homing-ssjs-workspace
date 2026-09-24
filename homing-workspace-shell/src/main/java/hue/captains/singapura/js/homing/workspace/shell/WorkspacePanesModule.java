package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.docking.DockingModule;
import hue.captains.singapura.js.homing.ui.panes.MultiTabPaneModule;
import hue.captains.singapura.js.homing.ui.panes.PaneEventsModule;
import hue.captains.singapura.js.homing.ui.splitgrid.SplitGridModule;
import hue.captains.singapura.js.homing.ui.splitgrid.SplitGridTreeModule;

import java.util.List;

/**
 * The workspace's panes: a {@link SplitGridModule.SplitGrid} of cells, one
 * {@link MultiTabPaneModule.MultiTabPane} per cell, a
 * {@link DockingModule.Docking} desk over all of them.
 *
 * <h2>What it replaces</h2>
 *
 * <p>The studio's {@code MultiTabPane} was a pane that <b>split itself</b>: one
 * object owning a tree of leaves, a strip per leaf, the dividers, the drag
 * between strips, the selection paint, and ten {@code on*} callbacks. Three
 * components do that now, each minding one thing — the grid arranges cells and
 * never knows what a cell holds, a dock holds tabs and never knows where it
 * sits, the desk carries what floats — and each reports on ONE sink as frozen
 * data tagged by kind, which {@link WorkspaceEventsModule} turns into the
 * workspace's own.</p>
 *
 * <p>So this is an assembly rather than a port, and the surface the shell
 * already calls is kept on purpose: the shell keeps calling what it called,
 * and underneath every one of those calls is now somebody's single job.</p>
 *
 * <p><b>A pane is its id.</b> {@code paneIdOf} used to walk the tree and build
 * {@code "_1_2"} — a path to a leaf, handed on as though it were a name, which
 * is why the split events carried a path where they meant a pane. A cell's id
 * is the pane's id, so it answers itself.</p>
 */
public record WorkspacePanesModule() implements DomModule<WorkspacePanesModule> {

    public static final WorkspacePanesModule INSTANCE = new WorkspacePanesModule();

    /** The assembly. */
    public record WorkspacePanes() implements Exportable._Class<WorkspacePanesModule> {}

    @Override
    public ImportsFor<WorkspacePanesModule> imports() {
        return ImportsFor.<WorkspacePanesModule>builder()
                .add(new ModuleImports<>(List.of(new SplitGridModule.SplitGrid()),
                        SplitGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SplitGridTreeModule.SplitGridTree()),
                        SplitGridTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceGridModule.WorkspaceGrid()),
                        WorkspaceGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MultiTabPaneModule.MultiTabPane()),
                        MultiTabPaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneEventsModule.PaneEvents()),
                        PaneEventsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DockingModule.Docking()),
                        DockingModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetPaneModule.WidgetPane()), WidgetPaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspacePanesStyles.wp_host()),
                        WorkspacePanesStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspacePanesModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspacePanes()));
    }
}
