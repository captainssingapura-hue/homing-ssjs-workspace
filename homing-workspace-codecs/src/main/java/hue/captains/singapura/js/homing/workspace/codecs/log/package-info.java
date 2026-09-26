/**
 * The workspace log's generators: every type on the log's wire is declared in
 * Java ({@code homing-workspace}, package {@code log}), and its JavaScript
 * class and both languages' codecs are generated from the declaration.
 *
 * <p>{@link hue.captains.singapura.js.homing.workspace.codecs.log.WorkspaceLogManifest}
 * is the single source of truth: each type, paired with its own generators —
 * an identifier, a record, a sealed type, an enum. {@link
 * hue.captains.singapura.js.homing.workspace.codecs.log.LogModules} groups the
 * types into JavaScript modules, one per top-level Java declaration, importing
 * one way. Generated and hand-written live apart: nothing here is served;
 * {@code WorkspaceLogJsGen} and {@code WorkspaceLogJavaGen} write the modules,
 * the Java that serves them and the Java codecs into the output-only
 * {@code homing-workspace-log-codec}, at its build.</p>
 */
package hue.captains.singapura.js.homing.workspace.codecs.log;
