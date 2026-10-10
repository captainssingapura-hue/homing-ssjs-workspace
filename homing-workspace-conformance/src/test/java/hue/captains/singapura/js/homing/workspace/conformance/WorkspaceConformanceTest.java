package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The workspace's report, graded as the build exports it: strictly, but for the widget rule's
 * ledger - recorded here (under {@code -Dconformance.recordLedger=true}), kept to debt that is still
 * owed, and only shrinking.
 */
class WorkspaceConformanceTest {

    @Test
    void everyServedModuleKeepsItsLane_butTheWidgetsRecordedDebt() {
        List<Finding> raw = new ConformanceEngine(WorkspaceConformance.POLICY, new ServedModuleRenderer()).checkCrates(WorkspaceConformance.TOP_LEVEL);
        WorkspaceConformance.WIDGETS.recordIfAsked(raw, "src/main/resources");
        assertEquals(List.of(), WorkspaceConformance.WIDGETS.stale(WorkspaceConformance.class, raw),
                "the widget ledger names debt that is paid: strike these lines");
        List<GradedFinding> errors = WorkspaceConformance.grader().grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> g.finding().fingerprint()).toList(),
                "the workspace carries no debt but the widgets' - fix these");
    }
}
