package hue.captains.singapura.js.homing.workspace.log.js;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The gate over what the manifest generates, as served: the crate declares
 * every module and imports only within itself; every module keeps its lane,
 * strictly — the line cap among the rules, so a file that outgrows it stops
 * the build here, where the file is; and the scripts, evaluated in the order
 * the crate lists them, define everything their modules say they export.
 */
class WorkspaceLogCodecGateTest extends JsModuleTestBase {

    @Test
    void theCrateDeclaresEveryModuleAndImportsWithinItself() {
        assertEquals(List.of(), OrphanCheck.check(WorkspaceLogCodecCrate.INSTANCE),
                "every served JS module in homing-workspace-log-codec must be declared in its crate");
        assertEquals(List.of(), CrateDependencyRule.check(WorkspaceLogCodecCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }

    @Test
    void everyModuleKeepsItsLane_strictly() {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer())
                .checkCrates(List.of(WorkspaceLogCodecCrate.INSTANCE));
        List<GradedFinding> errors = FindingGrader.STRICT.grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> g.finding().moduleClass() + " [" + g.finding().rule().value() + "] "
                + g.finding().message()).toList(), "generated code carries no debt: fix the generator, or regroup the Java declarations");
    }

    @Test
    void theScriptsLoadInTheCratesOrder_andDefineWhatTheyExport() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        int n = 0;
        for (var e : WorkspaceLogCodecCrate.INSTANCE.entries()) {
            for (var x : e.module().exports().exports()) {
                String name = x.getClass().getSimpleName();
                assertTrue(global(name).canInstantiate() || global(name).hasMembers(), name);
                n++;
            }
        }
        assertTrue(n > 80, "the log's classes and codecs, and the wire's reader; found " + n);
    }
}
