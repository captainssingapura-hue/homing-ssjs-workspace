package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.DeclaredDebts;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The workspace's report, graded as the build exports it: strictly, but for the debts its modules
 * declare on their classes ({@code @ConformanceDebt}) - and a debt that is paid is refused, so the
 * debts only shrink.
 */
class WorkspaceConformanceTest {

    @Test
    void everyServedModuleKeepsItsLane_butTheDebtsItsModulesDeclare() {
        List<Finding> raw = new ConformanceEngine(WorkspaceConformance.POLICY, new ServedModuleRenderer()).checkCrates(WorkspaceConformance.TOP_LEVEL);
        assertEquals(List.of(), DeclaredDebts.INSTANCE.paid(WorkspaceConformance.TOP_LEVEL, raw), "a debt that is paid: remove its @ConformanceDebt");
        List<GradedFinding> errors = WorkspaceConformance.grader().grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> g.finding().fingerprint()).toList(),
                "the workspace carries no debt but what its modules declare - fix these");
    }
}
