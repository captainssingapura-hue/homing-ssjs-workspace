package hue.captains.singapura.js.homing.workspace.events.contract;

import hue.captains.singapura.js.homing.workspace.state.ArrangementSource;
import hue.captains.singapura.js.homing.workspace.state.PaneDirection;
import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed sum of the recordable workspace events. Every event a chrome
 * emits is a typed instance of one of these variants; the sealed shape
 * gives consumers exhaustive {@code switch} and the compiler catches a
 * missed variant the moment it's added.
 *
 * <p>Each variant declares a {@link EventName} constant naming itself.
 * The {@link EventLog#append(EventName, Object) EventLog.append} call
 * passes both the name and the payload — and the typed binding is what
 * forbids the {@code _emit('WidgetSpawnedFormPicker', ...)} category of
 * typo at the contract boundary.</p>
 *
 * <p>Variants per the RFC 0035 spec table:</p>
 * <ul>
 *   <li>{@link SessionStarted} — boot bookmark</li>
 *   <li>{@link WidgetSpawnedFromPicker} — user spawned a widget via picker</li>
 *   <li>{@link WidgetSpawnedPinned} — PINNED auto-spawn</li>
 *   <li>{@link TabClosed} — widget tab closed</li>
 *   <li>{@link TabMoved} — tab relocated (strip-drag or modal redock)</li>
 *   <li>{@link SplitCreated} — a pane split</li>
 *   <li>{@link SplitMerged} — two panes merged back</li>
 *   <li>{@link WorkspaceActiveChanged} — workspace-active tab transition</li>
 * </ul>
 *
 * @since RFC 0035 P1
 */
public sealed interface WorkspaceEventPayload {

    /** The typed name of this payload — matches {@code event.name()}. */
    EventName name();

    // ─── Variants ────────────────────────────────────────────────────────

    /**
     * RFC 0060 — this workspace has been given its opening arrangement, and will
     * not be given one again.
     *
     * <p><b>The stamp is the gate.</b> Seeding used to be guarded by "is the event
     * log empty", which asks the wrong question: {@code SessionStarted} is written
     * during boot before replay runs, so the log is never empty and the seed never
     * happened. Emptiness was a proxy for "has this workspace been arranged yet",
     * and this event answers that directly.</p>
     *
     * <p>It is written after seeding <b>whether or not there was anything to
     * seed</b> — a workspace whose kind declares nothing still gets the mark, so
     * the gate has one condition rather than two and no special case for the
     * empty arrangement.</p>
     *
     * <p>Because a checkpoint can advance past it, the gate must look for this
     * stamp across the <b>whole log</b> rather than the post-checkpoint queue —
     * re-seeding a workspace that already has real state would be a worse failure
     * than the one this fixes.</p>
     *
     * @param source   where the arrangement came from
     * @param seededAt when
     */
    record WorkspaceSeeded(ArrangementSource source, Instant seededAt)
            implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WorkspaceSeeded");
        public WorkspaceSeeded {
            Objects.requireNonNull(source,   "WorkspaceSeeded.source");
            Objects.requireNonNull(seededAt, "WorkspaceSeeded.seededAt");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Boot bookmark — first event of every session. */
    record SessionStarted(URI href, Instant startedAt) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SessionStarted");
        public SessionStarted {
            Objects.requireNonNull(href,      "SessionStarted.href");
            Objects.requireNonNull(startedAt, "SessionStarted.startedAt");
        }
        @Override public EventName name() { return NAME; }
    }

    /** User picked a widget from the picker and it spawned. */
    record WidgetSpawnedFromPicker(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            WidgetTitle      title,
            Location         to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WidgetSpawnedFromPicker");
        public WidgetSpawnedFromPicker {
            Objects.requireNonNull(widgetInstanceId, "WidgetSpawnedFromPicker.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "WidgetSpawnedFromPicker.widgetKind");
            Objects.requireNonNull(title,            "WidgetSpawnedFromPicker.title");
            Objects.requireNonNull(to,               "WidgetSpawnedFromPicker.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Auto-spawn of a PINNED widget at chrome boot. */
    record WidgetSpawnedPinned(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            Location         to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WidgetSpawnedPinned");
        public WidgetSpawnedPinned {
            Objects.requireNonNull(widgetInstanceId, "WidgetSpawnedPinned.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "WidgetSpawnedPinned.widgetKind");
            Objects.requireNonNull(to,               "WidgetSpawnedPinned.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Widget tab closed by the user (× click). */
    record TabClosed(
            WidgetInstanceId widgetInstanceId,
            WidgetKind       widgetKind,
            Location         from
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TabClosed");
        public TabClosed {
            Objects.requireNonNull(widgetInstanceId, "TabClosed.widgetInstanceId");
            Objects.requireNonNull(widgetKind,       "TabClosed.widgetKind");
            Objects.requireNonNull(from,             "TabClosed.from");
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * Tab relocated between (or within) panes. {@code from} is empty when
     * the tab re-docked from "outside" a pane (modal redock, picker-modal
     * dock); the replay handler resolves the live source by widgetInstanceId.
     */
    record TabMoved(
            WidgetInstanceId   widgetInstanceId,
            Optional<Location> from,
            Location           to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TabMoved");
        public TabMoved {
            Objects.requireNonNull(widgetInstanceId, "TabMoved.widgetInstanceId");
            if (from == null) from = Optional.empty();
            Objects.requireNonNull(to,               "TabMoved.to");
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * A new, empty pane carved off an existing one, on the side named.
     *
     * <p>Schema 2. It used to say {@code (paneId, orientation, newRatio)} where
     * {@code paneId} was the parent split's PATH after the split — so the event
     * named neither the pane that was split nor the pane that appeared, and the
     * reader had to work both out. It names both now.</p>
     *
     * <p>A DIRECTION, never an orientation: the axis follows from the side, and
     * is derived in {@link PaneDirection} and nowhere else, which is what makes
     * it impossible to write down backwards. Where the new pane LANDS in the tree
     * is not this event's to say — a row already running this way takes it as a
     * sibling, anything else becomes a split of the two — because that is the
     * grid's rule and it must be applied by whoever holds the tree, not recorded
     * twice and allowed to disagree.</p>
     *
     * <p>No ratio: a fresh pane halves the one it came from, and any share other
     * than that is a drag, which is {@link TracksChanged}.</p>
     */
    record SplitCreated(
            PaneId        paneId,
            PaneId        newPaneId,
            PaneDirection side
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SplitCreated");
        public SplitCreated {
            Objects.requireNonNull(paneId,    "SplitCreated.paneId");
            Objects.requireNonNull(newPaneId, "SplitCreated.newPaneId");
            Objects.requireNonNull(side,      "SplitCreated.side");
            if (paneId.equals(newPaneId)) {
                throw new IllegalArgumentException("SplitCreated: a pane cannot be carved off itself (" + paneId + ")");
            }
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * A pane is gone and its room went to another.
     *
     * <p>Schema 2, and inverted. It used to name the SURVIVING leaf, which said
     * nothing about which pane left or where its space went — and with more than
     * two panes in a row those are different questions. It names the pane that
     * GOES, as the grid does, and where its room went.</p>
     *
     * <p>{@code toward} is the pane that gains the room when one was chosen —
     * they must share a whole divider, one pane alone on each side, which is the
     * only pair a merge is offered between. Empty means the room went to the
     * neighbour holding it, which is what happens when nothing was named.</p>
     */
    record SplitMerged(
            PaneId           paneId,
            Optional<PaneId> toward
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("SplitMerged");
        public SplitMerged {
            Objects.requireNonNull(paneId, "SplitMerged.paneId");
            if (toward == null) toward = Optional.empty();
            if (toward.isPresent() && toward.get().equals(paneId)) {
                throw new IllegalArgumentException("SplitMerged: a pane cannot inherit its own room (" + paneId + ")");
            }
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * A split re-shared: a divider was dragged, and these are its tracks now.
     *
     * <p>Schema 2, and NEW to this sum although not to the runtime — the JS has
     * emitted {@code SplitRatioChanged} and the model has folded it since RFC
     * 0029, with no variant here to answer for it. The vocabulary said it was
     * centralised and typo-safe; it was one name short of both.</p>
     *
     * <p>All the shares, not one of them, because a split has two or more tracks
     * and a drag moves the pair either side of the divider. The {@link SplitPath}
     * is the only path in this sum: a split has no id to name it by.</p>
     */
    record TracksChanged(
            SplitPath    path,
            List<Double> ratios
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TracksChanged");
        public TracksChanged {
            Objects.requireNonNull(path,   "TracksChanged.path");
            Objects.requireNonNull(ratios, "TracksChanged.ratios");
            if (ratios.size() < 2) {
                throw new IllegalArgumentException(
                        "TracksChanged.ratios: a split has two or more tracks (got " + ratios.size() + ")");
            }
            double sum = 0.0;
            for (Double r : ratios) {
                Objects.requireNonNull(r, "TracksChanged.ratios element");
                if (!(r > 0.0)) {
                    throw new IllegalArgumentException("TracksChanged.ratios: every share must be positive, got " + r);
                }
                sum += r;
            }
            if (Math.abs(sum - 1.0) > 1e-6) {
                throw new IllegalArgumentException("TracksChanged.ratios: the shares must sum to one, got " + sum);
            }
            ratios = List.copyOf(ratios);
        }
        @Override public EventName name() { return NAME; }
    }

    /**
     * Which tab a pane is showing.
     *
     * <p>Schema 2, and the gap it fills was a plain one: {@link WidgetLocation.InPane}
     * has carried {@code isActive} since RFC 0029, meaning "the active tab in THIS
     * pane", and {@link hue.captains.singapura.js.homing.workspace.state.WorkspaceState}
     * has enforced at most one per pane — but no event ever said so. The state
     * could describe it and nothing could record it, so a workspace came back with
     * every pane showing whichever tab it happened to pick. With four panes open
     * that is three wrong tabs on every reload.</p>
     *
     * <p>PER PANE, which is what the dock reports: a pane shows one of its tabs
     * whether or not the keyboard is anywhere near it. {@link WorkspaceActiveChanged}
     * is the other question — which tab the workspace as a whole is in — and both
     * are kept because neither derives from the other once the pane the user was
     * last IN is not itself recorded: the cursor over the panes is transient and
     * deliberately unrecorded.</p>
     */
    record TabActivated(
            PaneId           paneId,
            WidgetInstanceId widgetInstanceId
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("TabActivated");
        public TabActivated {
            Objects.requireNonNull(paneId,           "TabActivated.paneId");
            Objects.requireNonNull(widgetInstanceId, "TabActivated.widgetInstanceId");
        }
        @Override public EventName name() { return NAME; }
    }

    /** Workspace-active tab transition. Either side may be absent (no active tab). */
    record WorkspaceActiveChanged(
            Optional<TabRef> from,
            Optional<TabRef> to
    ) implements WorkspaceEventPayload {
        public static final EventName NAME = EventName.of("WorkspaceActiveChanged");
        public WorkspaceActiveChanged {
            if (from == null) from = Optional.empty();
            if (to   == null) to   = Optional.empty();
        }
        @Override public EventName name() { return NAME; }
    }
}
