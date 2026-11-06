package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;

import java.util.List;

/** The workspace's headless core, served: its ids, the rule its widgets are titled by, its roster and the widgets' life, and the parties beside it. No DOM. */
public final class WorkspaceCoreCrate implements Crate {

    public static final WorkspaceCoreCrate INSTANCE = new WorkspaceCoreCrate();

    private WorkspaceCoreCrate() {}

    @Override public String name() { return "homing-workspace-core"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the root instances the core's parties are
                WorkspacePartiesCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                new CrateEntry(WidgetIdsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspaceRequestModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspaceCoreModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(KindAndParamsTitleModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WorkspacePartiesModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
