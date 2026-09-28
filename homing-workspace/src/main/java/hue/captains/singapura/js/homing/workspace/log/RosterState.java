package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The roster's layer of what a workspace log folds to: the widgets the core
 * holds, in the order they were opened, each as it is made again - its id, its
 * kind, its params; and, for each prefix an id was ever given under, the last
 * sequence given, so the ids of a workspace that comes back go on past every
 * id it gave, closed or not. A widget held under an id its prefix has not
 * reached, or twice, is refused, whatever built the state.
 *
 * @param widgets   the widgets held, in the order they were opened
 * @param sequences the last sequence each prefix gave, in the order of the prefixes
 */
public record RosterState(List<RosterEntry> widgets, List<PrefixSequence> sequences) {

    /** A widget the roster holds: what it is made again from. */
    public record RosterEntry(WidgetId id, WidgetKind kind, List<WidgetParam> params) {
        public RosterEntry {
            Objects.requireNonNull(id, "RosterEntry.id");
            Objects.requireNonNull(kind, "RosterEntry.kind");
            params = WidgetParam.checked(params, "RosterEntry.params");
        }
    }

    /** The last sequence a prefix gave: the next of the prefix is past it. */
    public record PrefixSequence(String prefix, int last) {

        private static final Pattern PREFIX = Pattern.compile("[A-Za-z0-9_-]+");

        public PrefixSequence {
            Objects.requireNonNull(prefix, "PrefixSequence.prefix");
            if (!PREFIX.matcher(prefix).matches()) throw new IllegalArgumentException("PrefixSequence.prefix '" + prefix + "' — letters, digits, hyphen, underscore");
            if (last < 1) throw new IllegalArgumentException("PrefixSequence.last " + last + " — a sequence starts at 1");
        }
    }

    public RosterState {
        widgets = List.copyOf(Objects.requireNonNull(widgets, "RosterState.widgets"));
        sequences = List.copyOf(Objects.requireNonNull(sequences, "RosterState.sequences"));
        var last = new HashMap<String, Integer>();
        for (int i = 0; i < sequences.size(); i++) {
            var s = sequences.get(i);
            if (i > 0 && sequences.get(i - 1).prefix().compareTo(s.prefix()) >= 0) {
                throw new IllegalArgumentException("RosterState — the prefixes in order, each once: '" + sequences.get(i - 1).prefix() + "' then '" + s.prefix() + "'");
            }
            last.put(s.prefix(), s.last());
        }
        var held = new HashSet<WidgetId>();
        for (RosterEntry w : widgets) {
            if (!held.add(w.id())) throw new IllegalArgumentException("RosterState — the widget " + w.id() + " twice");
            Integer reached = last.get(w.id().prefix());
            if (reached == null || reached < w.id().sequence()) {
                throw new IllegalArgumentException("RosterState — the widget " + w.id() + " is past the last its prefix gave, " + (reached == null ? "none" : reached));
            }
        }
    }

    /** Where every log starts: no widget, no id given. */
    public static RosterState empty() { return new RosterState(List.of(), List.of()); }

    /** Whether the roster holds a widget of this id. */
    public boolean holds(WidgetId id) {
        for (RosterEntry w : widgets) if (w.id().equals(id)) return true;
        return false;
    }
}
