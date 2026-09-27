package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/** The messaging parties' runtime: the flat instance a widget joins. The types are declared where their domains are. */
public final class WorkspacePartiesCrate implements Crate {

    public static final WorkspacePartiesCrate INSTANCE = new WorkspacePartiesCrate();

    private WorkspacePartiesCrate() {}

    @Override public String name() { return "homing-workspace-parties"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(MessagingPartyModule.INSTANCE, StandardJsModuleType.PURE_LOGIC));
    }
}
