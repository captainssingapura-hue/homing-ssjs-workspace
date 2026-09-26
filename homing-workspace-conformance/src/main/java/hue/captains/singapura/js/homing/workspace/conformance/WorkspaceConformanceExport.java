package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.studio.CrateConformanceGetAction;
import hue.captains.singapura.js.homing.conformance.studio.CrateGraphGetAction;
import hue.captains.singapura.js.homing.conformance.studio.CrateTreeGetAction;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Build-time: this repo's crates graded, the report written in the studio's
 * layout under the directory given — where the served studio reads it back.
 *
 * <p>Then the studio's own crate views — the crate tree, the crate graph, the
 * crate conformance — are run here, over this repo's crates, and their answers
 * written under {@code live/} beside the report: {@link #LIVE}. The studio
 * cannot run them itself, since it cannot load these crates; it serves what
 * was written.</p>
 */
public final class WorkspaceConformanceExport {

    private WorkspaceConformanceExport() {}

    /** The studio's paths, and the files their answers are written to, under {@code live/}. */
    public static final String[][] LIVE = {
            { "/crate-tree", "crate-tree.json" },
            { "/crate-graph", "crate-graph.txt" },
            { "/crate-conformance", "crate-conformance.json" } };

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: WorkspaceConformanceExport <output-directory>");
        Path dir = Path.of(args[0]);
        ConformanceRun run = new ConformanceEngine(WorkspaceConformance.POLICY, new ServedModuleRenderer())
                .assemble(WorkspaceConformance.TOP_LEVEL, WorkspaceConformance.grader());
        new ConformanceReportWriter().write(dir, run);
        System.out.println("[WorkspaceConformanceExport] wrote report to " + dir
                + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, "
                + run.summary().warningCount() + " warnings)");

        // the crate views, over this repo's crates - the conformance view reads the report just written
        var none = new EmptyParam.NoHeaders();
        var top = WorkspaceConformance.TOP_LEVEL;
        Path live = dir.resolve("live");
        Files.createDirectories(live);
        write(live.resolve(LIVE[0][1]), new CrateTreeGetAction(top).execute(new CrateTreeGetAction.Query(null), none).join());
        write(live.resolve(LIVE[1][1]), new CrateGraphGetAction(top).execute(new CrateGraphGetAction.Query(null), none).join());
        write(live.resolve(LIVE[2][1]), new CrateConformanceGetAction(top).execute(new CrateConformanceGetAction.Query(null), none).join());
        System.out.println("[WorkspaceConformanceExport] wrote the crate views to " + live);
    }

    private static void write(Path file, DocContent content) throws IOException {
        Files.writeString(file, content.body(), StandardCharsets.UTF_8);
    }
}
