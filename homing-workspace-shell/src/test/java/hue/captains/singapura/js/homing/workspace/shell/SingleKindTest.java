package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A kind a workspace holds one of: opened again, the one there is what the
 * workspace has - the first of its kind - and a kind of many, or one not held
 * yet, has none, so it is made.
 */
class SingleKindTest extends JsModuleTestBase {

    @BeforeEach
    void load() {
        loadModule("/homing/js/hue/captains/singapura/js/homing/workspace/shell/GridWorkspaceModule.js");
        js.eval("js", """
                var kinds = { play: { title: "Play", single: true }, watch: { title: "Watch" } };
                var entries = [{ id: "watch-1", kind: "watch" }, { id: "play-1", kind: "play" }, { id: "watch-2", kind: "watch" }];
                function held(kind, from) { var e = GridWorkspace.held(kinds, from || entries, kind); return e ? e.id : "none"; }
                """);
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void aSingleKindHeld_isTheOneThere() {
        assertEquals("play-1", eval("held('play')"));
    }

    @Test
    void aKindOfMany_orASingleOneNotHeldYet_orOneNotAKind_hasNone() {
        assertEquals("none", eval("held('watch')"));
        assertEquals("none", eval("held('play', [{ id: 'watch-1', kind: 'watch' }])"));
        assertEquals("none", eval("held('nothing')"));
    }
}
