package hue.captains.singapura.js.homing.workspace.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which widget: the id the workspace's core gives it when it is opened - its
 * PREFIX, what it is in a word (its kind, and a concise form of its params
 * when it has any), and a SEQUENCE climbing for that prefix, never reused in
 * the workspace: {@code books-grid-1}, {@code books-grid_title-rating-2}. The
 * widget's, never a tab's: a placement that shows the widget names it by
 * this, and the log records it by this.
 *
 * <p>One of the core's models: the core makes it (its WidgetIds makes the
 * prefix), and the log writes it and holds its sequence to climbing, each
 * knowing only that an id is a prefix and a sequence. Written bare on the
 * log's wire, its codecs generated from here.</p>
 *
 * @param value a prefix of letters, digits, hyphens and underscores; a hyphen; a sequence from 1, at most nine digits
 */
public record WidgetId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+-[1-9]\\d*");

    /** The most digits a sequence has: nine, so it is an int in both languages. */
    public static final int SEQUENCE_DIGITS = 9;

    public WidgetId {
        Objects.requireNonNull(value, "WidgetId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WidgetId.value '" + value + "' — a prefix (letters, digits, hyphen, underscore), a hyphen, a sequence from 1");
        }
        if (value.length() - value.lastIndexOf('-') - 1 > SEQUENCE_DIGITS) {
            throw new IllegalArgumentException("WidgetId.value '" + value + "' — a sequence of at most " + SEQUENCE_DIGITS + " digits");
        }
    }

    public static WidgetId of(String value) { return new WidgetId(value); }

    /** The n-th id of a prefix. */
    public static WidgetId of(String prefix, int sequence) {
        if (sequence < 1) throw new IllegalArgumentException("WidgetId — a sequence starts at 1: " + sequence);
        return new WidgetId(prefix + "-" + sequence);
    }

    /** What the widget is, in a word: all before the last hyphen. */
    public String prefix() { return value.substring(0, value.lastIndexOf('-')); }

    /** Its place among its prefix's: the number after the last hyphen. */
    public int sequence() { return Integer.parseInt(value.substring(value.lastIndexOf('-') + 1)); }

    @Override public String toString() { return value; }
}
