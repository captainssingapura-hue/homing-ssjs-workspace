package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.server.HtmlPageContent;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.site.Navigable;
import hue.captains.singapura.js.homing.site.catalogue.L0_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.L1_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0058 law 4, the tree half — every group placed exactly once, no unknown
 * group placed, and no kind both held by a group and placed as a flat kind leaf.
 * A static walk from a site's L0 root over core's generic catalogue tree
 * ({@code homing-site-catalogue}), reading each leaf's page for the app params
 * it binds, so the site fails to boot with the names rather than a placement
 * going missing.
 */
class WorkspaceGroupsTest {

    /** The pages are a standard MPA's, as a site makes them. */
    private static final StandardMpa MPA = StandardMpa.of(Brand.of("Test"), ThemeRegistry.EMPTY);

    private WorkspaceGroup desk, ops;

    @BeforeEach
    void groups() {
        var trader = WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("trader", "Trader", "Trading"));
        var audit  = WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("audit",  "Audit",  "Governance"));
        WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("loose", "Loose", "Workspaces"));
        desk = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("desk", "Desk", "", List.of(trader)));
        ops  = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("ops",  "Ops",  "", List.of(audit)));
    }

    @AfterEach
    void reset() {
        WorkspaceGroupRegistry.INSTANCE.resetForTesting();
        WorkspaceSpecRegistry.INSTANCE.resetForTesting();
        Tree.LEAVES = List.of();
        Sub.LEAVES  = List.of();
    }

    // Fixtures: a root and one child whose leaves a test sets.
    record Tree() implements L0_Catalogue<Tree> {
        static final Tree INSTANCE = new Tree();
        static List<Leaf<Tree>> LEAVES = List.of();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<Tree, ?>> subCatalogues() { return List.of(Sub.INSTANCE); }
        @Override public List<Leaf<Tree>> leaves() { return LEAVES; }
    }
    record Sub() implements L1_Catalogue<Tree, Sub> {
        static final Sub INSTANCE = new Sub();
        static List<Leaf<Sub>> LEAVES = List.of();
        @Override public Tree parent() { return Tree.INSTANCE; }
        @Override public String name() { return "Sub"; }
        @Override public List<Leaf<Sub>> leaves() { return LEAVES; }
    }

    /** A group placed: the group app's page, bound to the group. */
    private static Navigable group(WorkspaceGroupApp.Params p) { return MPA.page(WorkspaceGroupApp.INSTANCE, p); }

    /** One kind placed flat, the standard MPA's way. */
    private static Navigable kind(String kind) { return MPA.page(WorkspaceApp.INSTANCE, new WorkspaceApp.Params(kind)); }

    /** One kind placed flat, the studio mounting's way - the address every pre-0058 link carries. */
    private static Navigable legacy(String kind) { return MPA.page(GenericWorkspace.INSTANCE, new GenericWorkspace.Params(kind)); }

    @Test
    void everyGroupPlacedOnceAcrossTheTreePasses() {
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops",  "", group(WorkspaceGroupApp.of(ops))));
        assertDoesNotThrow(() -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
    }

    @Test
    void anUnplacedGroupIsNamed() {
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("group 'ops'") && e.getMessage().contains("placed nowhere"), e.getMessage());
        assertTrue(e.getMessage().contains("Leaf.of(host"), "and says how to place it, in the catalogue's own words: " + e.getMessage());
    }

    @Test
    void aGroupPlacedTwiceNamesBothPositions() {
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Desk again", "", group(WorkspaceGroupApp.of(desk))),
                              Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("group 'desk' is placed 2 times"), e.getMessage());
        assertTrue(e.getMessage().contains(Tree.class.getName()) && e.getMessage().contains(Sub.class.getName()), e.getMessage());
    }

    @Test
    void aFlatKindLeafIsFineUntilAGroupHoldsTheKind() {
        // 'loose' is held by no group: its flat leaf is the only address it has.
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))),
                              Leaf.of(Tree.INSTANCE, "Loose", "", legacy("loose")));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))));
        assertDoesNotThrow(() -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));

        // 'trader' is held by 'desk' AND placed flat: one kind, two positions.
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))),
                              Leaf.of(Sub.INSTANCE,  "Trader (old)", "", legacy("trader")));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("kind 'trader' is positioned twice") && e.getMessage().contains("group 'desk'")
                && e.getMessage().contains(Sub.class.getName()), e.getMessage());
    }

    /** The standard MPA's own workspace page is a flat kind leaf too, and the law holds it the same. */
    @Test
    void aStandardWorkspacePageIsAFlatKindLeaf() {
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))),
                              Leaf.of(Tree.INSTANCE, "Audit", "", kind("audit")));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("kind 'audit' is positioned twice") && e.getMessage().contains("group 'ops'"), e.getMessage());
    }

    /** A page that is not an app bound to its params is not a workspace's, and the walk passes over it. */
    @Test
    void aPageThatIsNoAppsIsPassedOver() {
        Navigable plain = q -> new HtmlPageContent("plain");
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))),
                              Leaf.of(Tree.INSTANCE, "About", "", plain));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))));
        assertDoesNotThrow(() -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
    }

    @Test
    void anUnregisteredGroupIsRefused() {
        Tree.LEAVES = List.of(Leaf.of(Tree.INSTANCE, "Desk", "", group(WorkspaceGroupApp.of(desk))),
                              Leaf.of(Tree.INSTANCE, "Ghost", "", group(new WorkspaceGroupApp.Params("ghost"))));
        Sub.LEAVES  = List.of(Leaf.of(Sub.INSTANCE,  "Ops", "", group(WorkspaceGroupApp.of(ops))));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("unregistered group: 'ghost'"), e.getMessage());
    }
}
