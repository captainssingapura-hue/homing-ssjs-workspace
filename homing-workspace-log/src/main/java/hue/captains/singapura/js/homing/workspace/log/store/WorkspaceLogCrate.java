package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code homing-workspace-log}: the workspace log's
 * hand-written JavaScript - the store, where it keeps its rows, the exporter,
 * whose log a page keeps. Requires the codecs crate, whose generated module
 * holds every type these read and write.
 */
public final class WorkspaceLogCrate implements Crate {

    public static final WorkspaceLogCrate INSTANCE = new WorkspaceLogCrate();

    private WorkspaceLogCrate() {}

    @Override public String name() { return "homing-workspace-log"; }

    @Override public List<Crate> requires() { return List.of(WorkspaceLogCodecCrate.INSTANCE); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                new CrateEntry(WorkspaceLogStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(IndexedDbLogModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(MemoryLogModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspaceLogExportModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspaceLogIdentityModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // a log read back to what it folds to, or set aside: the load every placement shares
                new CrateEntry(WorkspaceLoadModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // the fold: the log's meaning, as the Java fold makes it
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.ExactShareModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.LayoutAlgebraModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.RosterFoldModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.PaneFoldModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.GridFoldModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.WorkspaceFoldModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // checkpoints: the next one's fold, the worker it runs in, and what takes them as the log goes
                new CrateEntry(hue.captains.singapura.js.homing.workspace.log.fold.CheckpointFoldModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(CheckpointWorkerModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspaceCheckpointerModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // one writer per log
                new CrateEntry(WorkspaceWriteLockModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // the workspaces of each kind, listed
                new CrateEntry(WorkspaceCatalogueModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
