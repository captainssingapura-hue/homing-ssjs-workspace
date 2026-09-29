package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import java.util.List;

/**
 * The workspace groups as a page has them, and nothing that draws: the page's
 * directory of its site's groups, and the workspace choice party a switcher
 * speaks in - its type, and its secretaries, the root's and a switcher's
 * scope's. Whatever shows them is a widget set of its own.
 */
public final class WorkspaceGroupsCrate implements Crate {

    public static final WorkspaceGroupsCrate INSTANCE = new WorkspaceGroupsCrate();

    private WorkspaceGroupsCrate() {}

    @Override public String name() { return "homing-workspace-groups"; }

    @Override public List<Crate> requires() { return List.of(); }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                // the page's directory, provided once at boot
                CrateEntry.of(WorkspaceDirectoryModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // the workspace choice party: its type, generated from Java, and its secretaries
                CrateEntry.of(WorkspaceChoiceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WorkspaceChoiceSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(WorkspaceSwitcherSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY));
    }
}
