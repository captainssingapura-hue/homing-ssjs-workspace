package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.component.keyboard.KeyboardRegistry;
import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The demo's gate: the framework's conformance over the demo's own crate — its
 * widgets, their declarations, their sheet and their domain — as the shell's
 * gate runs it over the shell's and the components' over theirs —
 * <ol>
 *   <li><b>crate integrity</b>: every served module declared, every cross-crate
 *       import required;</li>
 *   <li><b>the lanes</b>: the served artifact of every module, graded by the
 *       framework's policy under the role its crate declares — against a
 *       committed BASELINE of the demo's debt, a ratchet
 *       both ways: a finding not in it fails, and a line in it no longer found
 *       fails too, so the ledger is always exactly the debt;</li>
 *   <li><b>the CSS graph laws</b>, strict;</li>
 *   <li><b>the substrate</b>: every one of the framework's designs binds every
 *       pair the demo's sheet wears, strict;</li>
 *   <li><b>the keys</b>: none of the demo's own modules listens for keys
 *       undeclared or captures them on the document. (The closure's older
 *       modules are the shell's gate's to hold.)</li>
 * </ol>
 *
 * <p>The baseline is re-recorded deliberately, never to silence a fresh
 * finding: {@code mvn test -Dtest=DemoConformanceTest -Dconformance.record=true}
 * writes it (UTF-8 — a mangled em dash once emptied a baseline silently).</p>
 */
class DemoConformanceTest {

    private static final List<Crate> TOP = List.of(WorkspaceDemoCrate.INSTANCE);
    private static final Path BASELINE = Path.of("src/test/resources/demo-conformance-baseline.txt");


    private static Set<String> own() {
        return TOP.stream().flatMap(c -> c.entries().stream()).map(e -> e.moduleClass()).collect(Collectors.toSet());
    }

    @Test
    void theCrateIsStructurallyComplete() {
        var result = CrateConformance.evaluate(new ArrayList<>(CrateClosure.of(TOP))).crates().get(WorkspaceDemoCrate.INSTANCE.name());
        assertEquals(List.of(), result.orphans(), "every served module must be declared");
        assertEquals(List.of(), result.illegalImports(), "every cross-crate import must be declared in requires()");
    }

    @Test
    void everyServedModuleKeepsItsLane_andTheDebtOnlyShrinks() throws IOException {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer()).checkCrates(TOP);
        if (Boolean.getBoolean("conformance.record")) {
            var lines = new ArrayList<String>();
            lines.add("# workspace-demo - its conformance debt. A ratchet: it only shrinks.");
            lines.add("# Re-record deliberately (-Dconformance.record=true), never to silence a new finding.");
            lines.add("# By decision (2026-09-25): the 'construct' function WorkspaceWidget generates for every widget is recorded here as debt, pending the widget contract (exports-are-classes exempts only appMain).");
            lines.addAll(Baseline.record(raw));
            Files.write(BASELINE, lines, StandardCharsets.UTF_8);
            return;
        }
        Baseline baseline = Baseline.of(Files.readAllLines(BASELINE, StandardCharsets.UTF_8));
        assertTrue(baseline.size() > 0, "the baseline loaded (an unreadable file loads empty and hides nothing)");

        List<GradedFinding> errors = FindingGrader.STRICT.withBaseline(baseline).allowingPreExisting(true)
                .grade(raw).stream().filter(GradedFinding::isError).toList();
        assertEquals(List.of(), errors.stream().map(g -> describe(g.finding())).toList(), "NEW conformance findings - fix them, do not file them");

        var found = new TreeSet<String>(Baseline.record(raw));
        var stale = new TreeSet<String>(baseline.fingerprints());
        stale.removeAll(found);
        assertEquals(Set.of(), stale, "debt paid off: remove these lines from the baseline, so it stays exactly the debt");
    }

    @Test
    void theCssGraphKeepsItsLaws() {
        Set<String> own = own();
        List<Finding> css = CssConformance.check(new ArrayList<>(CrateClosure.of(TOP)), HomingDesigns.REGISTRY.palettes())
                .stream().filter(f -> own.contains(f.moduleClass())).toList();
        assertEquals(List.of(), css.stream().map(DemoConformanceTest::describe).toList());
    }

    @Test
    void everyDesignBindsEveryPairTheShellWears() {
        var groups = new ArrayList<CssGroup<?>>();
        for (var e : WorkspaceDemoCrate.INSTANCE.entries()) if (e.module() instanceof CssGroup<?> g) groups.add(g);
        assertTrue(groups.size() >= 1, "the demo's sheet; found " + groups.size());
        var worn = Deployment.wornBy(groups);
        for (var t : HomingDesigns.REGISTRY.themes()) {
            var r = Deployment.of(worn, Deployment.scaledBy(groups), Deployment.grownBy(groups), (Design) t).resolve();
            assertEquals(List.of(), r.findings(), () -> ((Design) t).slug() + ": " + r.findings());
        }
    }

    /** The demo's widgets take no keys of their own: the room hands them on. None of its modules listens undeclared or captures on the document. */
    @Test
    void theDemosModulesTakeNoKeysOfTheirOwn() {
        Set<String> ownNames = WorkspaceDemoCrate.INSTANCE.entries().stream()
                .map(e -> e.module().getClass().getSimpleName()).collect(Collectors.toSet());
        assertEquals(List.of(), KeyboardRegistry.undeclaredListeners(TOP).stream().filter(ownNames::contains).toList());
        assertEquals(List.of(), KeyboardRegistry.validate(TOP).stream()
                .filter(s -> ownNames.stream().anyMatch(s::contains)).toList());
    }

    private static String describe(Finding f) {
        return f.moduleClass() + " [" + f.rule().value() + "] @" + f.line() + " " + f.message();
    }
}
