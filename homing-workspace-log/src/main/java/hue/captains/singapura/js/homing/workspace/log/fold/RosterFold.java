package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.RosterEvent;
import hue.captains.singapura.js.homing.workspace.log.RosterState;
import hue.captains.singapura.js.homing.workspace.log.RosterState.PrefixSequence;
import hue.captains.singapura.js.homing.workspace.log.RosterState.RosterEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The roster's layer of the fold - the core's: a widget opened is held, in
 * the order opened, under no name, and its prefix's last sequence is its own; a
 * widget renamed keeps the name given, or none when it is taken back; a widget
 * closed is held no more. A widget opened must not be held already, and its
 * sequence must be past the last its prefix gave, closed or not - an id is
 * never given again; a widget closed must be held. The JavaScript is this, line
 * for line (RosterFoldModule.js), and the two agree to the byte.
 */
public final class RosterFold {

    private RosterFold() {}

    /** One of the roster's events on its state: the state after it. */
    public static RosterState apply(RosterState state, RosterEvent event) {
        var widgets = new ArrayList<>(state.widgets());
        var last = new TreeMap<String, Integer>();
        for (PrefixSequence s : state.sequences()) last.put(s.prefix(), s.last());
        switch (event) {
            case RosterEvent.WidgetOpened e -> {
                if (state.holds(e.id())) throw new WorkspaceFold.Refused("the widget " + e.id() + " is already open");
                String prefix = e.id().prefix();
                int reached = last.getOrDefault(prefix, 0);
                if (e.id().sequence() <= reached) {
                    throw new WorkspaceFold.Refused("the widget " + e.id() + " is not past " + prefix + "-" + reached + ", the last its prefix gave: an id is never given again");
                }
                widgets.add(new RosterEntry(e.id(), e.kind(), e.params(), Optional.empty()));
                last.put(prefix, e.id().sequence());
            }
            case RosterEvent.WidgetRenamed e -> {
                if (!state.holds(e.id())) throw new WorkspaceFold.Refused("the widget " + e.id() + " is not open");
                widgets.replaceAll(w -> w.id().equals(e.id()) ? new RosterEntry(w.id(), w.kind(), w.params(), e.name()) : w);
            }
            case RosterEvent.WidgetClosed e -> {
                if (!state.holds(e.id())) throw new WorkspaceFold.Refused("the widget " + e.id() + " is not open");
                widgets.removeIf(w -> w.id().equals(e.id()));
            }
        }
        List<PrefixSequence> sequences = new ArrayList<>();
        for (var s : last.entrySet()) sequences.add(new PrefixSequence(s.getKey(), s.getValue()));
        return new RosterState(widgets, sequences);
    }
}
