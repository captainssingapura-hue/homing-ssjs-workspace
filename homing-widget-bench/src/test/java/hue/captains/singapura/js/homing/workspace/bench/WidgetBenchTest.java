package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.ParamCodec.Decoded;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Query;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bench's page: a kind named on the address, the rest of it the kind's
 * params read by the kind's own query; a page per kind; and nothing in the
 * page the widget is given but its container and its params.
 */
class WidgetBenchTest {

    private static String pageAt(String path, Query query) {
        var found = WidgetBenchSite.INSTANCE.router().resolve(Path.parse(path));
        assertTrue(found.isPresent(), "no page at " + path);
        return found.get().html(query).body();
    }

    @Test
    void theKindIsRequired_andOneTheBenchKnows() {
        assertInstanceOf(Decoded.Missing.class, WidgetBenchApp.CODEC.from(Map.of()));
        var r = assertInstanceOf(Decoded.Malformed.class, WidgetBenchApp.CODEC.from(Map.of("widget", List.of("nope"))));
        assertTrue(r.describe().contains("books-grid"), r.describe());
    }

    @Test
    void theRestOfTheAddressIsTheKindsToRead() {
        var ok = WidgetBenchApp.CODEC.from(Map.of("widget", List.of("books-grid"), "columns", List.of("title,rating"), "numbers", List.of("on")));
        assertTrue(ok.isOk());
        assertEquals(new BooksGridDeclaration.Params(List.of("title", "rating"), true), ok.orNull().params());
        assertEquals(Map.of("widget", List.of("books-grid"), "columns", List.of("title,rating"), "numbers", List.of("on")),
                WidgetBenchApp.CODEC.to(ok.orNull()), "written back as it was read");
        var bad = assertInstanceOf(Decoded.Malformed.class,
                WidgetBenchApp.CODEC.from(Map.of("widget", List.of("books-grid"), "columns", List.of("price"))));
        assertTrue(bad.describe().contains("columns"), "the kind's refusal names its key: " + bad.describe());
    }

    @Test
    void aKindHasAPage_theRootIsTheFirst_andTheAddressIsReadOverItsDefaults() {
        String root = pageAt("/", Query.NONE);
        assertTrue(root.contains("\"widget\":\"books-grid\""), "the root stands the first kind up");
        assertTrue(root.contains(WidgetBenchApp.class.getCanonicalName()));
        assertFalse(pageAt("/books-grid", Query.NONE).contains("numbers"), "at its defaults, the page says nothing of them");
        assertTrue(pageAt("/books-grid", Query.of("numbers", "on")).contains("\"numbers\":\"on\""),
                "the address's query read over the page's defaults");
        assertTrue(WidgetBenchSite.INSTANCE.router().resolve(Path.parse("/nope")).isEmpty());
    }

    @Test
    void thePageGeneratesTheKindsList() {
        var js = String.join("\n", BenchWidgetsModule.INSTANCE.selfContent(null));
        assertTrue(js.contains("\"books-grid\": BooksGrid"), js);
    }
}
