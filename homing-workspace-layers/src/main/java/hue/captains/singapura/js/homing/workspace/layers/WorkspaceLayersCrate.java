package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;

import java.util.List;

/** The log's layers, live and headless, served: the roster's recorded and come back to; the one pane, and its layer. No DOM. */
public final class WorkspaceLayersCrate implements Crate {

    public static final WorkspaceLayersCrate INSTANCE = new WorkspaceLayersCrate();

    private WorkspaceLayersCrate() {}

    @Override public String name() { return "homing-workspace-layers"; }

    @Override public List<Crate> requires() {
        return List.of(
                // what is recorded: the log's classes
                WorkspaceLogCodecCrate.INSTANCE,
                // whose word the roster's layer records
                WorkspaceCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(RosterLayerModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PanePlacementModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(PaneLayerModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
