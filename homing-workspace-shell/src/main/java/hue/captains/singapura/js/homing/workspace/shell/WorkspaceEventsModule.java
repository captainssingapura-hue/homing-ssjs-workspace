package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The workspace event a component event is, or nothing.
 *
 * <p>The panes, the grid and the desk each report their mutations on one sink,
 * as frozen data tagged by {@code kind}. The workspace keeps a log of its own,
 * as frozen data tagged by {@code name} — {@link
 * hue.captains.singapura.js.homing.workspace.events.contract.WorkspaceEventPayload}.
 * The two vocabularies were arrived at twice, independently, and agree about
 * almost everything. This is the almost, written once.</p>
 *
 * <p>Pure: no DOM, no state, and no opinion about what the holder does with the
 * answer. It is the shell's old fan-in — ten {@code on*} callbacks each
 * building a payload inline, beside the code that mutated — as one function
 * that can be read and tested apart from any of it.</p>
 *
 * <h2>What it declines to translate</h2>
 *
 * <p>Each is a decision, and the JS header says why: {@code AddRequested} is a
 * question rather than a mutation; {@code TabAdded} cannot say whether a tab
 * came from the picker or was pinned, which the workspace records, so the
 * holder that authored the spawn names it; {@code CursorMoved} is live state;
 * and a float is transient: the panes never pass one on, and a tab that comes
 * down from a float comes down as a move from where it left.</p>
 */
public record WorkspaceEventsModule() implements EsModule<WorkspaceEventsModule> {

    public static final WorkspaceEventsModule INSTANCE = new WorkspaceEventsModule();

    /** The translator. */
    public record WorkspaceEvents() implements Exportable._Class<WorkspaceEventsModule> {}

    @Override public ImportsFor<WorkspaceEventsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceEventsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WorkspaceEvents()));
    }
}
