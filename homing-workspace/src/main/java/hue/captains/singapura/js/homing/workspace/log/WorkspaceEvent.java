package hue.captains.singapura.js.homing.workspace.log;

/**
 * What the workspace log records: every change of the workspace's arrangement
 * that outlives the page — what happened to a tab ({@link TabEvent}), to a
 * region ({@link RegionEvent}), to a float ({@link FloatEvent}). Declared here
 * and only here: the JavaScript classes and both languages' codecs are
 * generated from these records.
 *
 * <p>A family of three families. Each is a file of its own, and a JavaScript
 * module of its own; on the wire an event says only its own name, the three
 * families' names never, so the family it falls in is Java's grouping and not
 * the log's.</p>
 *
 * <p>A tab is always somewhere — a {@link Host}: a region's dock or a float. A
 * float is a state the workspace comes back to, recorded like a region: where
 * it lies and how big, in whole pixels of the desk, and which is on top. A
 * merge is the moves the merge made, then the region it emptied removed; a
 * float that empties closes, after the move that emptied it.</p>
 */
public sealed interface WorkspaceEvent permits TabEvent, RegionEvent, FloatEvent {}
