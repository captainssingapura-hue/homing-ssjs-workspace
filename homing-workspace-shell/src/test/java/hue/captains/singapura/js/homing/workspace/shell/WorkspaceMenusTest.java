package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.ui.docking.SplitMenu;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuRegistry;
import hue.captains.singapura.js.homing.ui.panes.TabMenu;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The workspace's menus, derived as the gallery's are: the tab menu the
 * multi-tab pane needs, the region's ground menu the dock grid needs and
 * answers — nothing listed by the workspace — stamped into the module the
 * page imports.
 */
class WorkspaceMenusTest extends JsModuleTestBase {

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", String.join("\n", WorkspaceMenus.INSTANCE.selfContent(null)));
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void theKindsAreDerivedFromTheComponentsNeeds() {
        assertEquals(List.of(), ContextMenuRegistry.validate(List.of(WorkspaceShellCrate.INSTANCE)));
        assertTrue(WorkspaceMenus.REGISTRY.kinds().contains(TabMenu.INSTANCE), "the pane's tab menu");
        assertTrue(WorkspaceMenus.REGISTRY.kinds().contains(SplitMenu.INSTANCE), "the dock grid's ground menu");
    }

    @Test
    void theModuleCarriesThem_asFrozenData() {
        assertTrue(eval("Object.keys(MENUS).indexOf('split') >= 0 && Object.keys(MENUS).indexOf('tab') >= 0").asBoolean());
        assertTrue(eval("Object.isFrozen(MENUS)").asBoolean());
    }
}
