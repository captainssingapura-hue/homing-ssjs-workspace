package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * RFC 0044 — the {@link Crate} for {@code homing-workspace-codecs}: the
 * workspace log's types in JavaScript, generated from their Java declarations.
 * The generated module imports nothing, so the crate requires nothing.
 */
public final class WorkspaceCodecsCrate implements Crate {

    public static final WorkspaceCodecsCrate INSTANCE = new WorkspaceCodecsCrate();

    private WorkspaceCodecsCrate() {}

    @Override public String name() { return "homing-workspace-codecs"; }

    @Override public List<Crate> requires() {
        return List.of();
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // The workspace log's types: generated from their Java declarations by WorkspaceLogJsGen.
                CrateEntry.of(WorkspaceLogCodecsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
