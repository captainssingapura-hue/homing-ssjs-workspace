package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;

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

    @Override public List<Crate> requires() { return List.of(WorkspaceCodecsCrate.INSTANCE); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WorkspaceLogStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(IndexedDbLogModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(MemoryLogModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceLogExportModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceLogIdentityModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
