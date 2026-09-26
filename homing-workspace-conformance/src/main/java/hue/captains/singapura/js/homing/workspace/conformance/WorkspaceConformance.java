package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.Collection;
import java.util.List;

/**
 * This repo's crates, as the conformance export grades them: the log's
 * generated types, the log, and the shell. Graded by the framework's policy,
 * unextended, and strictly: there is no baseline, the debt this repo was
 * copied with having left with the old stack.
 */
public final class WorkspaceConformance {

    private WorkspaceConformance() {}

    /** This repo's crates: what the export grades. */
    public static final List<Crate> TOP_LEVEL = List.of(
            WorkspaceCodecsCrate.INSTANCE,
            WorkspaceLogCrate.INSTANCE,
            WorkspaceShellCrate.INSTANCE);

    /** The framework's policy: this repo declares no module types of its own. */
    public static final JsRulePolicy POLICY = DefaultJsRulePolicy.INSTANCE;

    /** The top level and everything it requires, transitively. */
    public static Collection<Crate> closure() { return CrateClosure.of(TOP_LEVEL); }

    /** The framework-strict grader: no ledger to hold a finding as debt. */
    public static FindingGrader grader() {
        return FindingGrader.STRICT;
    }
}
