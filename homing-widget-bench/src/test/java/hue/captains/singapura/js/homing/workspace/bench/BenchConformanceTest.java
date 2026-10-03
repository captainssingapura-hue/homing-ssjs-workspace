package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.KeyboardRegistry;
import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.designs.HomingDesigns;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bench's gate: the framework's conformance over {@code homing-widget-bench},
 * as the shell's gate runs it over the shell - crate integrity; every served
 * module graded strictly, with no baseline; the CSS graph laws; every design
 * binding what the bench's sheet wears; and the keys, through the party.
 */
class BenchConformanceTest {

    private static final List<Crate> TOP = List.of(WidgetBenchCrate.INSTANCE);

    private static Set<String> own() {
        return TOP.stream().flatMap(c -> c.entries().stream()).map(e -> e.moduleClass()).collect(Collectors.toSet());
    }

    @Test
    void theCrateIsStructurallyComplete() {
        var result = CrateConformance.evaluate(new ArrayList<>(CrateClosure.of(TOP))).crates().get(WidgetBenchCrate.INSTANCE.name());
        assertEquals(List.of(), result.orphans(), "every served module must be declared");
        assertEquals(List.of(), result.illegalImports(), "every cross-crate import must be declared in requires()");
    }

    @Test
    void everyServedModuleKeepsItsLane_strictly() {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer()).checkCrates(TOP);
        List<GradedFinding> errors = FindingGrader.STRICT.grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> describe(g.finding())).toList(),
                "the bench carries no debt - fix these, there is no ledger to file them in");
    }

    @Test
    void theCssGraphKeepsItsLaws() {
        Set<String> own = own();
        List<Finding> css = CssConformance.check(new ArrayList<>(CrateClosure.of(TOP)))
                .stream().filter(f -> own.contains(f.moduleClass())).toList();
        assertEquals(List.of(), css.stream().map(BenchConformanceTest::describe).toList());
    }

    @Test
    void everyDesignBindsEveryPairTheBenchWears() {
        var groups = new ArrayList<CssGroup<?>>();
        for (var e : WidgetBenchCrate.INSTANCE.entries()) if (e.module() instanceof CssGroup<?> g) groups.add(g);
        assertTrue(groups.size() >= 1, "the bench's sheet; found " + groups.size());
        var worn = Deployment.wornBy(groups);
        for (var t : HomingDesigns.REGISTRY.themes()) {
            var r = Deployment.of(worn, Deployment.scaledBy(groups), Deployment.grownBy(groups), (Design) t).resolve();
            assertEquals(List.of(), r.findings(), () -> ((Design) t).slug() + ": " + r.findings());
        }
    }

    /** The bench takes its keys through the party: none listens undeclared, none captures on the document. */
    @Test
    void keysComeThroughTheParty() {
        Set<String> ownNames = WidgetBenchCrate.INSTANCE.entries().stream()
                .map(e -> e.module().getClass().getSimpleName()).collect(Collectors.toSet());
        assertEquals(List.of(), KeyboardRegistry.undeclaredListeners(TOP).stream().filter(ownNames::contains).toList(),
                "a module that listens for keys declares them (NeedKeyboard)");
        assertEquals(List.of(), KeyboardRegistry.validate(TOP).stream()
                .filter(s -> ownNames.stream().anyMatch(s::contains)).toList(),
                "only the steward captures keys on the document");
    }

    private static String describe(Finding f) {
        return f.moduleClass() + " [" + f.rule().value() + "] @" + f.line() + " " + f.message();
    }
}
