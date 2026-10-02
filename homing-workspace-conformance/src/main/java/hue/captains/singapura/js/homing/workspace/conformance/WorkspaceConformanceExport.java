package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Build-time: this repo's crates graded, the report written in the studio's
 * layout under the directory given - this module's classes, so the report
 * travels in its jar. The studio that views it lives downstream, in the
 * studio repo, and browses these crates itself: it reads the report from here.
 */
public final class WorkspaceConformanceExport {

    private WorkspaceConformanceExport() {}

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
    }
}
