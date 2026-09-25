package hue.captains.singapura.js.homing.workspace.shell;

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
 * The shell's gate: the framework's conformance over {@code homing-workspace-shell},
 * as the components' own gate runs it over theirs —
 * <ol>
 *   <li><b>crate integrity</b>: every served module declared, every cross-crate
 *       import required;</li>
 *   <li><b>the lanes</b>: the served artifact of every module, graded by the
 *       framework's policy under the role its crate declares — against a
 *       committed BASELINE of the debt the shell was copied with, a ratchet
 *       both ways: a finding not in it fails, and a line in it no longer found
 *       fails too, so the ledger is always exactly the debt;</li>
 *   <li><b>the CSS graph laws</b>, strict;</li>
 *   <li><b>the substrate</b>: every one of the framework's designs binds every
 *       pair the shell's sheets wear, strict;</li>
 *   <li><b>the keys</b>: every module that listens for keys declares them, and
 *       none captures them on the document but the steward — the modules not
 *       yet there are held here by name, so the list only shrinks.</li>
 * </ol>
 *
 * <p>The baseline is re-recorded deliberately, never to silence a fresh
 * finding: {@code mvn test -Dtest=ShellConformanceTest -Dconformance.record=true}
 * writes it (UTF-8 — a mangled em dash once emptied a baseline silently).</p>
 */
class ShellConformanceTest {

    private static final List<Crate> TOP = List.of(WorkspaceShellCrate.INSTANCE);
    private static final Path BASELINE = Path.of("src/test/resources/shell-conformance-baseline.txt");

    /** Listens for keys without declaring them: the switcher's own native regions, the lock banner, the layout. To be migrated. */
    private static final List<String> UNDECLARED_LISTENERS = List.of("WorkspaceSwitcherModule", "WriteLockGuardModule", "WorkspaceLayoutModule");
    /** Captures keys on the document: the layout's fullscreen Escape. To be migrated. */
    private static final List<String> DOCUMENT_CAPTURES = List.of("WorkspaceLayoutModule");

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
    void everyServedModuleKeepsItsLane_andTheDebtOnlyShrinks() throws IOException {
        List<Finding> raw = new ConformanceEngine(DefaultJsRulePolicy.INSTANCE, new ServedModuleRenderer()).checkCrates(TOP);
        if (Boolean.getBoolean("conformance.record")) {
            var lines = new ArrayList<String>();
            lines.add("# homing-workspace-shell - the conformance debt it was copied with. A ratchet: it only shrinks.");
            lines.add("# Re-record deliberately (-Dconformance.record=true), never to silence a new finding.");
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
        assertEquals(List.of(), css.stream().map(ShellConformanceTest::describe).toList());
    }

    @Test
    void everyDesignBindsEveryPairTheShellWears() {
        var groups = new ArrayList<CssGroup<?>>();
        for (var e : WorkspaceShellCrate.INSTANCE.entries()) if (e.module() instanceof CssGroup<?> g) groups.add(g);
        assertTrue(groups.size() >= 4, "the shell's sheets: switcher, panes, party monitor, css graph; found " + groups.size());
        var worn = Deployment.wornBy(groups);
        for (var t : HomingDesigns.REGISTRY.themes()) {
            var r = Deployment.of(worn, Deployment.scaledBy(groups), Deployment.grownBy(groups), (Design) t).resolve();
            assertEquals(List.of(), r.findings(), () -> ((Design) t).slug() + ": " + r.findings());
        }
    }

    /** The room and the panes take their keys through the party; the list of those that do not yet only shrinks. */
    @Test
    void keysComeThroughTheParty_andTheMigrationListOnlyShrinks() {
        assertEquals(UNDECLARED_LISTENERS, KeyboardRegistry.undeclaredListeners(TOP),
                "a module that listens for keys declares them (NeedKeyboard); the ones not yet migrated are named here, and the list only shrinks");
        var captures = KeyboardRegistry.validate(TOP);
        assertEquals(DOCUMENT_CAPTURES.size(), captures.size(), "only the steward captures keys on the document: " + captures);
        for (String m : DOCUMENT_CAPTURES) assertTrue(captures.stream().anyMatch(s -> s.contains(m)), m + " still captures on the document");
    }

    private static String describe(Finding f) {
        return f.moduleClass() + " [" + f.rule().value() + "] @" + f.line() + " " + f.message();
    }
}
