package hue.captains.singapura.js.homing.workspace.shell;

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
 * The shell's gate: the framework's conformance over {@code homing-workspace-shell},
 * as the components' own gate runs it over theirs —
 * <ol>
 *   <li><b>crate integrity</b>: every served module declared, every cross-crate
 *       import required;</li>
 *   <li><b>the lanes</b>: the served artifact of every module, graded strictly by
 *       the framework's policy under the role its crate declares. There is no
 *       baseline: the debt the shell was copied with left with the old shell,
 *       and there is no ledger to file a new finding in;</li>
 *   <li><b>the CSS graph laws</b>, strict;</li>
 *   <li><b>the substrate</b>: every one of the framework's designs binds every
 *       pair the shell's sheets wear, strict;</li>
 *   <li><b>the keys</b>: no module listens for keys undeclared, and none
 *       captures them on the document but the steward.</li>
 * </ol>
 */
class ShellConformanceTest {

    private static final List<Crate> TOP = List.of(WorkspaceShellCrate.INSTANCE);

    private static Set<String> own() {
        return TOP.stream().flatMap(c -> c.entries().stream()).map(e -> e.moduleClass()).collect(Collectors.toSet());
    }

    @Test
    void theCrateIsStructurallyComplete() {
        var result = CrateConformance.evaluate(new ArrayList<>(CrateClosure.of(TOP))).crates().get(WorkspaceShellCrate.INSTANCE.name());
        assertEquals(List.of(), result.orphans(), "every served module must be declared");
        assertEquals(List.of(), result.illegalImports(), "every cross-crate import must be declared in requires()");
    }

    @Test
    void everyServedModuleKeepsItsLane_strictly() {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer()).checkCrates(TOP);
        List<GradedFinding> errors = FindingGrader.STRICT.grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> describe(g.finding())).toList(),
                "the shell carries no debt - fix these, there is no ledger to file them in");
    }

    @Test
    void theCssGraphKeepsItsLaws() {
        Set<String> own = own();
        List<Finding> css = CssConformance.check(new ArrayList<>(CrateClosure.of(TOP)))
                .stream().filter(f -> own.contains(f.moduleClass())).toList();
        assertEquals(List.of(), css.stream().map(ShellConformanceTest::describe).toList());
    }

    @Test
    void everyDesignBindsEveryPairTheShellWears() {
        var groups = new ArrayList<CssGroup<?>>();
        for (var e : WorkspaceShellCrate.INSTANCE.entries()) if (e.module() instanceof CssGroup<?> g) groups.add(g);
        assertTrue(groups.size() >= 1, "the shell's sheet; found " + groups.size());
        var worn = Deployment.wornBy(groups);
        for (var t : HomingDesigns.REGISTRY.themes()) {
            var r = Deployment.of(worn, Deployment.scaledBy(groups), Deployment.grownBy(groups), (Design) t).resolve();
            assertEquals(List.of(), r.findings(), () -> ((Design) t).slug() + ": " + r.findings());
        }
    }

    /** The shell's modules take their keys through the party: none listens undeclared, none captures on the document. */
    @Test
    void keysComeThroughTheParty() {
        Set<String> ownNames = WorkspaceShellCrate.INSTANCE.entries().stream()
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
