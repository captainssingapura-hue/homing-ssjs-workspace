package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The stage party on the parties' runtime, headless - its steward stood in for: a member's
 * Present goes to the steward, and what the steward says every member hears; a second Present
 * while the stage is taken is refused to its asker alone, never reaches the steward, and is kept
 * as the bug it is; a Dismiss goes to the steward only when something is shown; a refusal frees
 * the stage; and with no steward, a Present is refused.
 */
class StageSecretaryTest extends JsModuleTestBase {

    private static final String ROOT = "/homing/js/hue/captains/singapura/js/homing/workspace/";

    /** A steward that keeps what it is asked and says what the test says; widgets that say what they hear. */
    private static final String SHIM = StageParty.TYPE.js() + """

        var console = { error: function () {} };
        var stewards = [];
        class Mover {
            constructor(tell) {
                stewards.push(this);
                var self = this;
                this.tell = tell;
                this.asked = [];
                this.reactors = { Present: function (m) { self.asked.push("Present " + m.widget); }, Dismiss: function () { self.asked.push("Dismiss"); } };
            }
            show(widget, title) { this.tell({ kind: "Shown", widget: widget, title: title }); }
            back(widget) { this.tell({ kind: "Returned", widget: widget }); }
            refuse(widget, why) { this.tell({ kind: "Refused", widget: widget, why: why }); }
        }
        var heard = [];
        function widget(party, name) {
            var m = party.join(name, {
                Shown: function (x) { heard.push(name + " Shown " + x.widget + " " + x.title); },
                Returned: function (x) { heard.push(name + " Returned " + x.widget); },
                Refused: function (x) { heard.push(name + " Refused " + x.widget + ": " + x.why); }
            });
            return { present: function () { m.tell({ kind: "Present", widget: name }); }, dismiss: function () { m.tell({ kind: "Dismiss" }); } };
        }
        """;

    @BeforeEach
    void load() {
        loadModule(ROOT + "parties/MessagingPartyModule.js");
        loadModule(ROOT + "stage/StageSecretaryModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String str(String src) { return eval(src).asString(); }

    @Test
    void aPresent_goesToTheSteward_andWhatItSaysEveryMemberHears() {
        eval("var stage = new MessagingParty(STAGE, StageSecretary, Mover), a = widget(stage, 'code-1'), b = widget(stage, 'image-2'); a.present();");
        assertEquals("Present code-1", str("stewards[0].asked.join(' | ')"), "hired when first wanted, and asked");
        assertEquals("code-1", str("stage.state().asked"), "on its way");
        eval("stewards[0].show('code-1', 'A diagram');");
        assertEquals("code-1 Shown code-1 A diagram | image-2 Shown code-1 A diagram", str("heard.join(' | ')"));
        assertEquals("code-1 A diagram", str("stage.state().shown.widget + ' ' + stage.state().shown.title"));
    }

    @Test
    void aSecondPresent_whileTheStageIsTaken_isRefusedToItsAsker_andKeptAsABug() {
        eval("var stage = new MessagingParty(STAGE, StageSecretary, Mover), a = widget(stage, 'code-1'), b = widget(stage, 'image-2');"
                + "a.present(); stewards[0].show('code-1', 'A diagram'); heard = []; b.present();");
        assertEquals("image-2 Refused image-2: the stage is taken by code-1: one widget at a time, and nothing behind the stage can ask",
                str("heard.join(' | ')"), "to its asker alone");
        assertEquals("Present code-1", str("stewards[0].asked.join(' | ')"), "the steward never asked");
        assertEquals(1, eval("stage.state().bugs.length").asInt());
        eval("var s = new MessagingParty(STAGE, StageSecretary, Mover), d = widget(s, 'x'), e = widget(s, 'y'); d.present(); heard = []; e.present();");
        assertTrue(str("heard.join(' | ')").startsWith("y Refused y: the stage is taken by x"), "taken while on its way, too");
    }

    @Test
    void aDismiss_goesToTheSteward_onlyWhenSomethingIsShown_andTheReturnIsHeardByAll() {
        eval("var stage = new MessagingParty(STAGE, StageSecretary, Mover), a = widget(stage, 'code-1'), b = widget(stage, 'image-2'); a.dismiss();");
        assertEquals(0, eval("stewards.length").asInt(), "nothing shown: no steward asked, none hired");
        eval("a.present(); stewards[0].show('code-1', 'A diagram'); heard = []; a.dismiss(); stewards[0].back('code-1');");
        assertEquals("Present code-1 | Dismiss", str("stewards[0].asked.join(' | ')"));
        assertEquals("code-1 Returned code-1 | image-2 Returned code-1", str("heard.join(' | ')"));
        assertTrue(eval("stage.state().shown === null").asBoolean());
        eval("heard = []; b.present();");
        assertEquals("Present code-1 | Dismiss | Present image-2", str("stewards[0].asked.join(' | ')"), "the stage free again");
    }

    @Test
    void aRefusal_freesTheStage_andNoSteward_isARefusal() {
        eval("var stage = new MessagingParty(STAGE, StageSecretary, Mover), a = widget(stage, 'code-1'); a.present(); stewards[0].refuse('code-1', 'not kept');");
        assertEquals("code-1 Refused code-1: not kept", str("heard.join(' | ')"));
        assertTrue(eval("stage.state().asked === null").asBoolean(), "nothing on its way");
        eval("heard = []; var bare = new MessagingParty(STAGE, StageSecretary), w = widget(bare, 'code-9'); w.present();");
        assertEquals("code-9 Refused code-9: no steward: the host hired none for its stage", str("heard.join(' | ')"));
        assertTrue(eval("bare.state().asked === null").asBoolean());
    }
}
