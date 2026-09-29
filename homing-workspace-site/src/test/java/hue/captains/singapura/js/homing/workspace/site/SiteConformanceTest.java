package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.core.Crate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The site's gate: the framework's conformance over {@code homing-workspace-site} -
 * crate integrity, and every served module graded strictly, with no baseline. The page
 * draws nothing of its own - the shell does - so there is no sheet to hold and no key to take.
 */
class SiteConformanceTest {

    private static final List<Crate> TOP = List.of(WorkspaceSiteCrate.INSTANCE);

    @Test
    void theCrateIsStructurallyComplete() {
        var result = CrateConformance.evaluate(new ArrayList<>(CrateClosure.of(TOP))).crates().get(WorkspaceSiteCrate.INSTANCE.name());
        assertEquals(List.of(), result.orphans(), "every served module must be declared");
        assertEquals(List.of(), result.illegalImports(), "every cross-crate import must be declared in requires()");
    }

    @Test
    void everyServedModuleKeepsItsLane_strictly() {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer()).checkCrates(TOP);
        List<GradedFinding> errors = FindingGrader.STRICT.grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> describe(g.finding())).toList(),
                "the site carries no debt - fix these, there is no ledger to file them in");
    }

    private static String describe(Finding f) {
        return f.moduleClass() + " [" + f.rule().value() + "] @" + f.line() + " " + f.message();
    }
}
