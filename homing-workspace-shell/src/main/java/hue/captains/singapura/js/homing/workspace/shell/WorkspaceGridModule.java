package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The one layout in the two spellings it has: the workspace's leaf/slotId tree
 * and the grid's cell/id tree.
 *
 * <p>Two sides need the translation and need it to agree: the panes, which hand
 * the grid its layout and read it back, and the model, which replays a merge by
 * the grid's own rules so the room a pane leaves goes where it went. Pure.</p>
 */
public record WorkspaceGridModule() implements EsModule<WorkspaceGridModule> {

    public static final WorkspaceGridModule INSTANCE = new WorkspaceGridModule();

    /** The translation, both ways. */
    public record WorkspaceGrid() implements Exportable._Class<WorkspaceGridModule> {}

    @Override public ImportsFor<WorkspaceGridModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceGrid()));
    }
}
