package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;

import java.util.List;

/**
 * Content parties, a set of their own: the params as a party carries them, the one secretary
 * of every content party, and the first content type - the flow - with its widget. Nothing here
 * knows a doc, a plan, or a page.
 */
public final class WorkspaceContentCrate implements Crate {

    public static final WorkspaceContentCrate INSTANCE = new WorkspaceContentCrate();

    private WorkspaceContentCrate() {}

    @Override public String name() { return "homing-workspace-content"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOpsParty the flow mints its own from
                CoreJsCrate.INSTANCE,
                // the focus party, the css manager
                ServerCrate.INSTANCE,
                // the design words the sheet wears
                DesignCrate.INSTANCE,
                // the parties' runtime a content party is an instance of
                WorkspacePartiesCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(ContentParamsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(ContentSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(FlowContentModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(ContentStyles.INSTANCE),
                CrateEntry.of(FlowModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
