package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyFixedDeclaration;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridDeclaration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The widget kinds the bench stands up: declared once, here, for the codec and the page alike. */
public final class WidgetBench {

    private WidgetBench() {}

    /** Every kind, the first the one the bench's root shows: the widgets, the monitors, and last the nasty ones the bench keeps to catch. */
    public static final List<WidgetDeclaration<?>> KINDS = Stream.of(
            Stream.<WidgetDeclaration<?>>of(BooksGridDeclaration.INSTANCE),
            WorkspaceMonitorsCrate.KINDS.stream(),
            Stream.<WidgetDeclaration<?>>of(NastyFixedDeclaration.INSTANCE)).flatMap(s -> s).toList();

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
