package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.ui.menu.tree.MenuTrees;
import hue.captains.singapura.js.homing.ui.panes.TabMenu;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The workspace's two menus, as typed trees and as the module the page
 * imports — and held to the chrome that answers them: every row the pane menu
 * offers is one the chrome's handler picks by its id, and the kinds are the
 * names the chrome handles, so neither side can drift from the other.
 */
class WorkspaceMenusTest extends JsModuleTestBase {

    private static final List<String> PANE_ROWS = List.of("beside", "below", "merge-left", "merge-right", "merge-up", "merge-down", "close");

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", String.join("\n", WorkspaceMenus.INSTANCE.selfContent(null)));
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void thePaneMenuIsAValidTree_itsRowsInOrder_andItsGroundIsPane() {
        assertEquals(List.of(), MenuTrees.validate(WorkspaceMenus.PaneMenu.INSTANCE));
        assertEquals(PANE_ROWS, MenuTrees.rows(WorkspaceMenus.PaneMenu.INSTANCE).stream().map(r -> r.id()).toList());
        assertEquals("pane", WorkspaceMenus.GROUND);
    }

    @Test
    void theModuleCarriesBothKinds_asFrozenData() {
        assertEquals("pane,tab", eval("Object.keys(MENUS).join(',')").asString());
        assertTrue(eval("Object.isFrozen(MENUS)").asBoolean());
        assertEquals(List.of(WorkspaceMenus.PaneMenu.INSTANCE, TabMenu.INSTANCE), WorkspaceMenus.REGISTRY.kinds());
    }

    @Test
    void theChromeHandlesEveryKindByItsName_andEveryPaneRowByItsId() {
        String chrome = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource(
                "homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceShellChromeModule.js"));
        assertTrue(chrome.contains("handle('" + WorkspaceMenus.GROUND + "'"), "the ground menu's handler, by the kind's name");
        assertTrue(chrome.contains("handle('" + TabMenu.INSTANCE.kind() + "'"), "the tab menu's handler, by the kind's name");
        for (String id : PANE_ROWS) assertTrue(chrome.contains("'" + id + "'"), "the chrome picks '" + id + "'");
        for (String id : List.of("detach", "close")) assertTrue(chrome.contains("id === '" + id + "'"), "the tab menu's '" + id + "'");
    }
}
