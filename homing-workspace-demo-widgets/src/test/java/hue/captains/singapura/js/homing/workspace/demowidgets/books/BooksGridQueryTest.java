package hue.captains.singapura.js.homing.workspace.demowidgets.books;

import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery.Read;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridDeclaration.Params;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The books grid's params on an address: read, refused by the key that is wrong, and written back saying only what differs. */
class BooksGridQueryTest {

    private static final BooksGridDeclaration.Query QUERY = new BooksGridDeclaration.Query();

    private static Read<Params> read(Map<String, List<String>> query) { return QUERY.from(query); }

    @Test
    void anAddressThatSaysNothingIsTheDefault() {
        assertEquals(new Read.Ok<>(Params.DEFAULT), read(Map.of()));
        assertEquals(Map.of(), QUERY.to(Params.DEFAULT), "the default is written as nothing");
    }

    @Test
    void theColumnsAndTheGutterAreRead_andWrittenBackAsTheyWereRead() {
        var q = Map.of("columns", List.of("rating,title"), "numbers", List.of("on"));
        var ok = assertInstanceOf(Read.Ok.class, read(q));
        assertEquals(new Params(List.of("rating", "title"), true), ok.params());
        assertEquals(q, QUERY.to((Params) ok.params()));
    }

    @Test
    void whatCannotBeReadIsRefused_namingItsKey() {
        for (String bad : List.of("", "title,", "price", "title,title")) {
            var r = assertInstanceOf(Read.Refused.class, read(Map.of("columns", List.of(bad))), bad);
            assertEquals("columns", r.key());
        }
        var r = assertInstanceOf(Read.Refused.class, read(Map.of("numbers", List.of("yes"))));
        assertEquals("numbers", r.key());
    }

    @Test
    void theKindIsAddressShaped_andConstructsOneClass() {
        WidgetDeclaration<Params> d = BooksGridDeclaration.INSTANCE;
        assertTrue(WidgetDeclaration.KIND.matcher(d.kind()).matches());
        assertEquals("BooksGrid", d.className());
    }
}
