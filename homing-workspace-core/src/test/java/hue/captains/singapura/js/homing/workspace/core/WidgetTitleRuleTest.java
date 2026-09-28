package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A widget's title is a display rule, apart from its id: the kind's title, the
 * params' concise form, the sequence past the first - and the JavaScript words
 * it the same.
 */
class WidgetTitleRuleTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/core/";

    private static final WidgetTitleRule RULE = KindAndParamsTitle.INSTANCE;

    @Test
    void theKind_theParams_theSequencePastTheFirst() {
        assertEquals("Books grid", RULE.title("Books grid", WidgetId.of("books-grid-1")));
        assertEquals("Books grid 2", RULE.title("Books grid", WidgetId.of("books-grid-2")));
        assertEquals("Books grid · title-rating", RULE.title("Books grid", WidgetId.of("books-grid_title-rating-1")));
        assertEquals("Books grid · title-rating_on 12", RULE.title("Books grid", WidgetId.of("books-grid_title-rating_on-12")));
        assertEquals("", WidgetIds.conciseOf(WidgetId.of("book-jumbotron-3")), "a kind with hyphens and no params");
        assertThrows(IllegalArgumentException.class, () -> RULE.title("", WidgetId.of("books-grid-1")));
        assertEquals("Books grid 2", RULE.titleOf("Books grid", WidgetId.of("books-grid-2"), Optional.empty()), "no name given: as it opened");
        assertEquals("My books", RULE.titleOf("Books grid", WidgetId.of("books-grid-2"), Optional.of(WidgetName.of("My books"))), "a name given wins");
        assertThrows(IllegalArgumentException.class, () -> WidgetName.of("  "), "a name is never blank: taking it back is none");
    }

    /** Made by the core's own rules, the ids the rule reads: params hashed, too long, or plain - titled alike in both languages. */
    @Test
    void theJavaScriptWordsItTheSame() {
        loadModule(DIR + "WidgetIdsModule.js");
        loadModule(DIR + "KindAndParamsTitleModule.js");
        Map<String, Map<String, String>> made = new LinkedHashMap<>();
        made.put("books-grid", Map.of());
        made.put("book-jumbotron", Map.of("mode", "fail-not"));
        made.put("note", Map.of("text", ",,,"));
        made.put("party-log", Map.of("keep", "a rather long sentence, more than the form keeps"));
        int checked = 0;
        for (var e : made.entrySet()) {
            for (int n : List.of(1, 2, 37)) {
                WidgetId id = WidgetIds.of(WidgetIds.prefix(e.getKey(), e.getValue()), n);
                String java = RULE.title("A kind", id);
                String inJs = js.eval("js", "KindAndParamsTitle.INSTANCE.title('A kind', '" + id.value() + "')").asString();
                assertEquals(java, inJs, id.value());
                assertEquals(RULE.titleOf("A kind", id, Optional.of(WidgetName.of("Named"))), js.eval("js", "KindAndParamsTitle.INSTANCE.titleOf('A kind', '" + id.value() + "', 'Named')").asString());
                assertEquals(java, js.eval("js", "KindAndParamsTitle.INSTANCE.titleOf('A kind', '" + id.value() + "', null)").asString());
                checked++;
            }
        }
        assertEquals(12, checked);
        assertTrue(js.eval("js", "(function () { try { KindAndParamsTitle.INSTANCE.title('A kind', 'books-grid'); return false; } catch (e) { return true; } })()").asBoolean(),
                "not a widget's id: refused");
        assertTrue(js.eval("js", "Object.isFrozen(KindAndParamsTitle.INSTANCE)").asBoolean(), "it holds nothing");
    }
}
