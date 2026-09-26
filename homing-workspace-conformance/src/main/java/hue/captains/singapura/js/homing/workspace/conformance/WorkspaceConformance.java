package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.demo.WorkspaceDemoCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * This repo's crates, as the conformance export grades them: the
 * workspace, its codecs, its log, the old persistence, the shell and the demo.
 * Graded by the framework's policy, unextended; the shell's debt is the shell's
 * own baseline, the one its gate keeps — read from where that gate keeps it.
 */
public final class WorkspaceConformance {

    private WorkspaceConformance() {}

    /** This repo's crates: what the export grades. */
    public static final List<Crate> TOP_LEVEL = List.of(
            WorkspaceCrate.INSTANCE,
            WorkspaceCodecsCrate.INSTANCE,
            WorkspaceLogCrate.INSTANCE,
            WorkspacePersistenceCrate.INSTANCE,
            WorkspaceShellCrate.INSTANCE,
            WorkspaceDemoCrate.INSTANCE);

    /** The framework's policy: this repo declares no module types of its own. */
    public static final JsRulePolicy POLICY = DefaultJsRulePolicy.INSTANCE;

    /** The top level and everything it requires, transitively. */
    public static Collection<Crate> closure() { return CrateClosure.of(TOP_LEVEL); }

    /** The shell's baseline, from the file its gate keeps; none named, or none there, is no debt. */
    public static Baseline baseline(Path file) {
        if (file == null || !Files.exists(file)) return Baseline.EMPTY;
        try { return Baseline.of(Files.readAllLines(file, StandardCharsets.UTF_8)); }
        catch (IOException e) { throw new UncheckedIOException("failed to read the baseline " + file, e); }
    }

    /** The framework-strict grader under that baseline, what it holds shown as debt rather than failure. */
    public static FindingGrader grader(Path baseline) {
        return FindingGrader.STRICT.withBaseline(baseline(baseline)).allowingPreExisting(true);
    }
}
