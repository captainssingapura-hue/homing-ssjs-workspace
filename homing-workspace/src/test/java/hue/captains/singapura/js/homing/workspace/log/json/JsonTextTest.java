package hue.captains.singapura.js.homing.workspace.log.json;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The wire's text: written as JSON.stringify writes it, read strictly. */
class JsonTextTest {

    private static Json.Obj obj(Object... kv) {
        var m = new LinkedHashMap<String, Json>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], (Json) kv[i + 1]);
        return new Json.Obj(m);
    }

    @Test
    void writesMembersInOrderWithoutSpace() {
        var v = obj("b", new Json.Int(2), "a", new Json.Arr(List.of(Json.Bool.TRUE, Json.Null.INSTANCE, new Json.Int(-7))));
        assertEquals("{\"b\":2,\"a\":[true,null,-7]}", JsonText.write(v));
    }

    @Test
    void escapesAsJsonStringifyDoes() {
        String s = "q\" b\\ \b\f\n\r\t \u0001\u001f \u007f é 😀 \ud800 \udc00 /";
        assertEquals("\"q\\\" b\\\\ \\b\\f\\n\\r\\t \\u0001\\u001f \u007f é 😀 \\ud800 \\udc00 /\"",
                JsonText.write(new Json.Str(s)));
    }

    @Test
    void readsWhatItWrites() {
        var v = obj("s", new Json.Str("a\u0000\ud800z😀"), "n", new Json.Int(Json.MAX_SAFE), "m", new Json.Int(-Json.MAX_SAFE),
                    "e", obj(), "l", new Json.Arr(List.of()));
        String text = JsonText.write(v);
        assertEquals(v, JsonText.parse(text));
        assertEquals(text, JsonText.write(JsonText.parse(text)));
    }

    @Test
    void refusesWhatTheWireDoesNotCarry() {
        for (String bad : List.of("1.5", "1e3", "1E3", "-0", "01", "9007199254740992", "-9007199254740992",
                                  "12345678901234567", "{\"a\":1,\"a\":2}", "\"\u0001\"", "[1,]", "{} x", "tru", "\"\\x\"")) {
            assertThrows(JsonText.Malformed.class, () -> JsonText.parse(bad), bad);
        }
    }

    @Test
    void readsWhitespaceAndOtherEscapes() {
        var v = JsonText.parse(" { \"a\" : [ 1 , \"\\/\\u00E9\" ] } ");
        assertEquals(obj("a", new Json.Arr(List.of(new Json.Int(1), new Json.Str("/é")))), v);
    }

    @Test
    void theIntegerLimitIsJavaScriptsSafeOne() {
        assertEquals(new Json.Int(9007199254740991L), JsonText.parse("9007199254740991"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> new Json.Int(Json.MAX_SAFE + 1)).getMessage().contains("2^53"));
    }
}
