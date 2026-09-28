package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyFixedDeclaration;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookBrowserDeclaration;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookJumbotronDeclaration;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridDeclaration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The widget kinds the bench stands up: declared once, here, for the codec and the page alike. */
public final class WidgetBench {

    private WidgetBench() {}

    /** The monitors: kinds the bench stands up like any other, and floats beside any other at a toggle. */
    public static final List<WidgetDeclaration<?>> MONITORS = WorkspaceMonitorsCrate.KINDS;

    /** Every kind, the first the one the bench's root shows: the widgets, the monitors, and last the nasty ones the bench keeps to catch. */
    public static final List<WidgetDeclaration<?>> KINDS = Stream.of(
            Stream.<WidgetDeclaration<?>>of(BooksGridDeclaration.INSTANCE, BookJumbotronDeclaration.INSTANCE, BookBrowserDeclaration.INSTANCE),
            MONITORS.stream(),
            Stream.<WidgetDeclaration<?>>of(NastyFixedDeclaration.INSTANCE)).flatMap(s -> s).toList();

    static {
        for (var k : KINDS) {
            if (!WidgetDeclaration.KIND.matcher(k.kind()).matches()) throw new IllegalStateException("not a kind's name: " + k.kind());
        }
        if (KINDS.stream().map(WidgetDeclaration::kind).distinct().count() != KINDS.size()) throw new IllegalStateException("two kinds of one name");
        // a toggle's mark is its monitor's initial: two alike could not be told apart on the bar
        if (MONITORS.stream().map(k -> k.title().substring(0, 1)).distinct().count() != MONITORS.size()) throw new IllegalStateException("two monitors of one mark");
    }

    /** The kind of that name, if the bench knows it. */
    public static Optional<WidgetDeclaration<?>> kind(String name) {
        return KINDS.stream().filter(k -> k.kind().equals(name)).findFirst();
    }

    /** The kinds' names, for what an address is told it may say. */
    public static String names() { return KINDS.stream().map(WidgetDeclaration::kind).collect(Collectors.joining(", ")); }
}
