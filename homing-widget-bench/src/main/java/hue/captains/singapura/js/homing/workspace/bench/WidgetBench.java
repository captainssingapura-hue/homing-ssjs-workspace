package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridDeclaration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** The widget kinds the bench stands up: declared once, here, for the codec and the page alike. */
public final class WidgetBench {

    private WidgetBench() {}

    /** Every kind, the first the one the bench's root shows. */
    public static final List<WidgetDeclaration<?>> KINDS = List.of(BooksGridDeclaration.INSTANCE);

    static {
        for (var k : KINDS) {
            if (!WidgetDeclaration.KIND.matcher(k.kind()).matches()) throw new IllegalStateException("not a kind's name: " + k.kind());
        }
        if (KINDS.stream().map(WidgetDeclaration::kind).distinct().count() != KINDS.size()) throw new IllegalStateException("two kinds of one name");
    }

    /** The kind of that name, if the bench knows it. */
    public static Optional<WidgetDeclaration<?>> kind(String name) {
        return KINDS.stream().filter(k -> k.kind().equals(name)).findFirst();
    }

    /** The kinds' names, for what an address is told it may say. */
    public static String names() { return KINDS.stream().map(WidgetDeclaration::kind).collect(Collectors.joining(", ")); }
}
