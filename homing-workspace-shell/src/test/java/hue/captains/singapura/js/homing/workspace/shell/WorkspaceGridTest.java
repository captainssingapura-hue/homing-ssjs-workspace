package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The one layout in its two spellings: the workspace's leaf/slotId under
 * {@code pane}, the grid's cell/id under {@code node}, ratios and orientation
 * carried as they are, and a round trip either way the same tree.
 */
class WorkspaceGridTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceGridModule.js";

    private static final String WS = "{ kind: 'split', orientation: 'horizontal', children: ["
            + "{ pane: { kind: 'leaf', slotId: 'main' }, ratio: 0.3 },"
            + "{ pane: { kind: 'split', orientation: 'vertical', children: ["
            + "  { pane: { kind: 'leaf', slotId: 'cell-1' }, ratio: 0.5 }, { pane: { kind: 'leaf', slotId: 'cell-2' }, ratio: 0.5 } ] }, ratio: 0.7 } ] }";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void theWorkspacesTree_inTheGridsWords() {
        assertEquals("{\"kind\":\"split\",\"orientation\":\"horizontal\",\"children\":[{\"node\":{\"kind\":\"cell\",\"id\":\"main\"},\"ratio\":0.3},"
                   + "{\"node\":{\"kind\":\"split\",\"orientation\":\"vertical\",\"children\":[{\"node\":{\"kind\":\"cell\",\"id\":\"cell-1\"},\"ratio\":0.5},"
                   + "{\"node\":{\"kind\":\"cell\",\"id\":\"cell-2\"},\"ratio\":0.5}]},\"ratio\":0.7}]}",
                eval("JSON.stringify(WorkspaceGrid.toGrid(" + WS + "))").asString());
    }

    @Test
    void aRoundTripEitherWay_isTheSameTree() {
        assertTrue(eval("var ws = " + WS + "; JSON.stringify(WorkspaceGrid.fromGrid(WorkspaceGrid.toGrid(ws))) === JSON.stringify(ws)").asBoolean());
        assertTrue(eval("var g = WorkspaceGrid.toGrid(" + WS + "); JSON.stringify(WorkspaceGrid.toGrid(WorkspaceGrid.fromGrid(g))) === JSON.stringify(g)").asBoolean());
    }

    @Test
    void oneCell_andNothing() {
        assertEquals("{\"kind\":\"cell\",\"id\":\"main\"}", eval("JSON.stringify(WorkspaceGrid.toGrid({ kind: 'leaf', slotId: 'main' }))").asString());
        assertEquals("{\"kind\":\"leaf\",\"slotId\":\"main\"}", eval("JSON.stringify(WorkspaceGrid.fromGrid({ kind: 'cell', id: 'main' }))").asString());
        assertEquals("null,null", eval("String(WorkspaceGrid.toGrid(null)) + ',' + String(WorkspaceGrid.fromGrid(undefined))").asString(), "no tree, no tree");
    }
}
