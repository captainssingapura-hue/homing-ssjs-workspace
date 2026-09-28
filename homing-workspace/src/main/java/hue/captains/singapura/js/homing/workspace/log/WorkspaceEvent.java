package hue.captains.singapura.js.homing.workspace.log;

/**
 * What the workspace log records: every change of the workspace that outlives
 * the page, one sequence of events of every layer. The roster's - a widget
 * opened, closed ({@link RosterEvent}) - which every placement stands on; the
 * one pane's - what it shows ({@link PaneEvent}); and the split grid's - what
 * happened to a tab ({@link TabEvent}), to a region ({@link RegionEvent}), to a
 * float ({@link FloatEvent}). Declared here and only here: the JavaScript
 * classes and both languages' codecs are generated from these records.
 *
 * <p>A family of families. Each is a file of its own, and a JavaScript module
 * of its own; on the wire an event says only its own name, the families' names
 * never, so the family it falls in is Java's grouping and not the log's. The
 * layers are made in the processing: the fold hands each event, by its family,
 * to its layer's fold, and the log is stored as one sequence whatever the
 * layers. The vocabulary grows by addition only.</p>
 *
 * <p>In the split grid a tab is always somewhere — a {@link Host}: a region's
 * dock or a float. A float is a state the workspace comes back to, recorded
 * like a region: where it lies and how big, in whole pixels of the desk, and
 * which is on top. A merge is the moves the merge made, then the region it
 * emptied removed; a float that empties closes, after the move that emptied
 * it.</p>
 */
public sealed interface WorkspaceEvent permits RosterEvent, PaneEvent, TabEvent, RegionEvent, FloatEvent {}
