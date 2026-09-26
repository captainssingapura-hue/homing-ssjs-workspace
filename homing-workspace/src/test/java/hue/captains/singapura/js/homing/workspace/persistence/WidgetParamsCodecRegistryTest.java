package hue.captains.singapura.js.homing.workspace.persistence;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The widget kinds' params codecs, by kind: registered explicitly, a codec
 * being a transformTo/transformFrom pair; looked up loudly — an unknown kind
 * throws rather than dropping a widget's params at save time; a singleton of
 * statics that refuses to be constructed.
 */
class WidgetParamsCodecRegistryTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/persistence/WidgetParamsCodecRegistryModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        eval("var codec = { transformTo: function (v) { return { w: v }; }, transformFrom: function (w) { return w.w; } };");
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void aCodecRegisteredUnderAKind_isTheOneLookedUp_andALaterOneReplacesIt() {
        eval("WidgetParamsCodecRegistry.register('Note', codec)");
        assertTrue(eval("WidgetParamsCodecRegistry.has('Note') && WidgetParamsCodecRegistry.get('Note') === codec").asBoolean());
        eval("var other = { transformTo: function (v) { return v; }, transformFrom: function (w) { return w; } }; WidgetParamsCodecRegistry.register('Note', other)");
        assertTrue(eval("WidgetParamsCodecRegistry.get('Note') === other").asBoolean(), "a later registration under the same kind overwrites");
    }

    @Test
    void anUnknownKind_isLoud() {
        assertFalse(eval("WidgetParamsCodecRegistry.has('Ghost')").asBoolean());
        var ex = assertThrows(PolyglotException.class, () -> eval("WidgetParamsCodecRegistry.get('Ghost')"));
        assertTrue(ex.getMessage().contains("no codec registered for widget kind 'Ghost'"), ex.getMessage());
    }

    @Test
    void registrationRefusesANamelessKind_andACodecThatIsNotAPair() {
        assertTrue(refusal("WidgetParamsCodecRegistry.register('', codec)").contains("non-empty string"));
        assertTrue(refusal("WidgetParamsCodecRegistry.register(7, codec)").contains("non-empty string"));
        assertTrue(refusal("WidgetParamsCodecRegistry.register('Note', { transformTo: function () {} })").contains("transformTo + transformFrom"));
        assertTrue(refusal("WidgetParamsCodecRegistry.register('Note', null)").contains("transformTo + transformFrom"));
        assertFalse(eval("WidgetParamsCodecRegistry.has('Note')").asBoolean(), "nothing half-registered");
    }

    @Test
    void itIsStaticsOnly_andClearsForATest() {
        assertTrue(refusal("new WidgetParamsCodecRegistry()").contains("singleton"));
        eval("WidgetParamsCodecRegistry.register('Note', codec); WidgetParamsCodecRegistry._clear()");
        assertFalse(eval("WidgetParamsCodecRegistry.has('Note')").asBoolean());
    }

    private String refusal(String src) {
        return assertThrows(PolyglotException.class, () -> eval(src)).getMessage();
    }
}
