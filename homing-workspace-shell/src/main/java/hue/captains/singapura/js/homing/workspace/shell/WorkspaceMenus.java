package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry;
import hue.captains.singapura.js.homing.ui.menu.tree.M1_Node;
import hue.captains.singapura.js.homing.ui.panes.TabMenu;

import java.util.List;

/**
 * The workspace's context menus, as typed trees: what a right-click offers on
 * a tab, and what it offers on the tab bar's own ground.
 *
 * <p>Two kinds, and the split is not arbitrary. {@link TabMenu} is the PANE'S
 * OWN — a tab can be detached or closed wherever a dock is put, so the dock
 * declares it and every holder gets it. {@link PaneMenu} is the WORKSPACE'S:
 * parting the room a dock sits in, merging it back, closing a region — none of
 * which a dock can know, because a dock does not know where it sits. The pane
 * asks its holder for a ground menu by name and never learns what the name
 * says.</p>
 *
 * <p>The four merge directions are all declared and the runtime hides the ones
 * with no pane across a splitter of their own, so the menu is the same tree
 * everywhere and only its offer changes.</p>
 */
public record WorkspaceMenus() implements EsModule<WorkspaceMenus>, SelfContent {

    public static final WorkspaceMenus INSTANCE = new WorkspaceMenus();

    /** The kinds, as one frozen object the steward is built from. */
    public record MENUS() implements Exportable._Constant<WorkspaceMenus> {}

    /**
     * The name the pane asks its holder for when its ground is right-clicked.
     * Derived from the kind's class by MenuTrees.kindOf - PaneMenu is "pane" -
     * and named here so the two sides cannot drift apart silently.
     */
    public static final String GROUND = PaneMenu.INSTANCE.kind();

    /** What a right-click on the tab bar's ground offers: the room, not the tab. */
    public record PaneMenu() implements ContextMenuKind<PaneMenu> {
        public static final PaneMenu INSTANCE = new PaneMenu();

        @Override public List<? extends M1_Node<PaneMenu, ?>> children() {
            return List.of(Beside.INSTANCE, Below.INSTANCE,
                           MergeLeft.INSTANCE, MergeRight.INSTANCE, MergeUp.INSTANCE, MergeDown.INSTANCE,
                           Close.INSTANCE);
        }

        public record Beside() implements M1_Node<PaneMenu, Beside> {
            public static final Beside INSTANCE = new Beside();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Split beside"; }
            @Override public Class<? extends Icon> icon() { return Icon.Column.class; }
            @Override public String hint() { return "a pane of its own, to the right"; }
        }
        public record Below() implements M1_Node<PaneMenu, Below> {
            public static final Below INSTANCE = new Below();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Split below"; }
            @Override public Class<? extends Icon> icon() { return Icon.Row.class; }
            @Override public String hint() { return "a pane of its own, underneath"; }
        }

        /** The four ways a pane's room can go, offered only where there is a pane to take it. */
        public record MergeLeft() implements M1_Node<PaneMenu, MergeLeft> {
            public static final MergeLeft INSTANCE = new MergeLeft();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Merge left"; }
            @Override public Class<? extends Icon> icon() { return Icon.Merge.class; }
            @Override public int section() { return 1; }
            @Override public String hint() { return "its tabs and its room to the pane beside it"; }
        }
        public record MergeRight() implements M1_Node<PaneMenu, MergeRight> {
            public static final MergeRight INSTANCE = new MergeRight();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Merge right"; }
            @Override public Class<? extends Icon> icon() { return Icon.Merge.class; }
            @Override public int section() { return 1; }
            @Override public String hint() { return "its tabs and its room to the pane beside it"; }
        }
        public record MergeUp() implements M1_Node<PaneMenu, MergeUp> {
            public static final MergeUp INSTANCE = new MergeUp();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Merge up"; }
            @Override public Class<? extends Icon> icon() { return Icon.Merge.class; }
            @Override public int section() { return 1; }
            @Override public String hint() { return "its tabs and its room to the pane above it"; }
        }
        public record MergeDown() implements M1_Node<PaneMenu, MergeDown> {
            public static final MergeDown INSTANCE = new MergeDown();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Merge down"; }
            @Override public Class<? extends Icon> icon() { return Icon.Merge.class; }
            @Override public int section() { return 1; }
            @Override public String hint() { return "its tabs and its room to the pane below it"; }
        }

        public record Close() implements M1_Node<PaneMenu, Close> {
            public static final Close INSTANCE = new Close();
            @Override public PaneMenu parent() { return PaneMenu.INSTANCE; }
            @Override public String label() { return "Close this pane"; }
            @Override public Class<? extends Icon> icon() { return Icon.Close.class; }
            @Override public int section() { return 2; }
            @Override public String hint() { return "its room to the pane beside it; the last pane cannot go"; }
        }
    }

    /**
     * Declared rather than derived. The gallery's are collected from its crates
     * by {@code requiredBy}, which reads {@code NeedContextMenu} off components;
     * the workspace's panes are an assembly rather than a declared component, so
     * its two kinds are named here until they are.
     */
    public static final ContextMenuRegistry REGISTRY =
            ContextMenuRegistry.of(PaneMenu.INSTANCE, TabMenu.INSTANCE);

    @Override public ImportsFor<WorkspaceMenus> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceMenus> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new MENUS()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) { return REGISTRY.js(); }
}
