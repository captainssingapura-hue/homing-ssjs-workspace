package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A widget's id says what it is - its kind, a concise form of its params, a sequence - and the two languages make it alike. */
class WidgetIdsTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/core/WidgetIdsModule.js";

    private static Map<String, String> params(String... kv) {
        var m = new LinkedHashMap<String, String>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }

    @Test
    void theKindAlone_whenThereAreNoParams() {
        assertEquals("books-grid", WidgetIds.prefix("books-grid", Map.of()));
        assertEquals("books-grid-1", WidgetIds.of("books-grid", 1).value());
    }

    @Test
    void theParamsConcisely_inTheOrderOfTheirKeys() {
        assertEquals("books-grid_title-rating_on", WidgetIds.prefix("books-grid", params("numbers", "on", "columns", "title,rating")));
        assertEquals("note_Hello-world", WidgetIds.prefix("note", params("text", "  Hello, world!  ")), "letters kept as they are");
    }

    @Test
    void aValueWithNothingLeft_andAFormTooLong_areHashes() {
        String blank = WidgetIds.prefix("note", params("text", ",,,"));
        assertTrue(blank.matches("note_[0-9a-f]{8}"), blank);
        String longer = WidgetIds.prefix("note", params("text", "a rather long sentence, more than the form keeps"));
        assertTrue(longer.matches("note_[0-9a-f]{8}"), longer);
        WidgetId.of(longer, 1);   // an id the log takes
    }

    @Test
    void anIdTakenApart_isItsPrefixAndItsSequence() {
        var s = WidgetId.of("books-grid_title-rating-12");
        assertEquals("books-grid_title-rating", s.prefix());
        assertEquals(12, s.sequence());
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid"), "no sequence");
        assertThrows(IllegalArgumentException.class, () -> WidgetIds.prefix("Books", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books grid-1"));
    }

    /** The JavaScript makes the same prefixes, the same hashes, and takes ids apart alike. */
    @Test
    void theJavaScriptIsTheDual() {
        loadModule(MODULE);
        List<Map<String, String>> cases = List.of(
                Map.of(), params("columns", "title,rating"), params("numbers", "on", "columns", "title,rating"),
                params("text", ",,,"), params("text", "a rather long sentence, more than the form keeps"),
                params("b", "2", "a", "1"), params("title", "Ünïcødé ☃ 日本"), params("x", ""));
        for (var p : cases) {
            var obj = js.eval("js", "(" + json(p) + ")");
            String inJs = js.getBindings("js").getMember("WidgetIds").invokeMember("prefix", "note", obj).asString();
            assertEquals(WidgetIds.prefix("note", p), inJs, "params " + p);
        }
        assertEquals("books-grid_title-rating", js.eval("js", "WidgetIds.split('books-grid_title-rating-12').prefix").asString());
        assertEquals(12, js.eval("js", "WidgetIds.split('books-grid_title-rating-12').n").asInt());
        assertTrue(js.eval("js", "WidgetIds.split('books-grid') === null").asBoolean());
        assertTrue(js.eval("js", "WidgetIds.split('books-grid-1234567890') === null").asBoolean(), "more than nine digits, as the log's WidgetId");
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid-1234567890"));
    }

    private static String json(Map<String, String> m) {
        var out = new StringBuilder("{");
        m.forEach((k, v) -> out.append(out.length() > 1 ? "," : "").append('"').append(k).append("\":\"").append(v.replace("\\", "\\\\").replace("\"", "\\\"")).append('"'));
        return out.append('}').toString();
    }
}
