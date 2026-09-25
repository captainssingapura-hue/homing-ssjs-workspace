package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.workspace.catalogue.WorkspaceCatalogueModule;
import hue.captains.singapura.js.homing.workspace.events.CheckpointStoreModule;
import hue.captains.singapura.js.homing.workspace.events.CheckpointWorkerModule;
import hue.captains.singapura.js.homing.workspace.events.WorkspaceEventLogModule;
import hue.captains.singapura.js.homing.workspace.party.LayoutSecretaryModule;
import hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace}: the workspace
 * primitives (picker, layout, catalogue, event log, party bus, checkpoint
 * stores, widget-params codec registry). Requires the core-js substrate, the
 * server's, and the design substrate. Nothing of the studio, and no legacy
 * palette: no module here reads one.
 */
public final class WorkspaceCrate implements Crate {

    public static final WorkspaceCrate INSTANCE = new WorkspaceCrate();

    private WorkspaceCrate() {}

    @Override public String name() { return "homing-workspace"; }

    @Override public List<Crate> requires() {
        // RFC 0066 - the design substrate its sheets wear words of. No legacy
        // palette: nothing here reads a --color-* token any more.
        return List.of(CoreJsCrate.INSTANCE,
                hue.captains.singapura.js.homing.design.DesignCrate.INSTANCE,
                hue.captains.singapura.js.homing.server.ServerCrate.INSTANCE);   // the Party primitive is the base's now
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetPickerModule.INSTANCE),
                CrateEntry.of(WidgetPickerStyles.INSTANCE),
                CrateEntry.of(WorkspaceLayoutModule.INSTANCE),
                CrateEntry.of(WorkspaceLayoutStyles.INSTANCE),
                CrateEntry.of(WorkspaceCatalogueModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(CheckpointStoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(CheckpointWorkerModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceEventLogModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(LayoutSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(WidgetParamsCodecRegistryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
