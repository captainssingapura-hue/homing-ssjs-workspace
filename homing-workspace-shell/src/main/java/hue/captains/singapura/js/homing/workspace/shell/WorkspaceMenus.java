package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry;

import java.util.List;

/**
 * The workspace's context menus, stamped into one JS module, {@code MENUS},
 * for the page's steward — and, as the gallery's are, NOT LISTED BUT DERIVED:
 * the components in the crate closure say what they need
 * ({@code NeedContextMenu}), and the registry is their union. The tab menu is
 * the multi-tab pane's; a region's ground menu - part it, merge it, close it -
 * is the dock grid's, which answers it. The workspace declares no kind of its
 * own (RFC 0066 E3, the workspace detour).
 */
public record WorkspaceMenus() implements EsModule<WorkspaceMenus>, SelfContent {

    public static final WorkspaceMenus INSTANCE = new WorkspaceMenus();

    /** The kinds, as one frozen object the steward is built from. */
    public record MENUS() implements Exportable._Constant<WorkspaceMenus> {}

    /** Derived: the kinds the closure's catalogued components need. */
    public static final ContextMenuRegistry REGISTRY = ContextMenuRegistry.requiredBy(List.of(WorkspaceShellCrate.INSTANCE));

    @Override public ImportsFor<WorkspaceMenus> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceMenus> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MENUS()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) { return REGISTRY.js(); }
}
