package hue.captains.singapura.js.homing.workspace.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Build-time: this repo's crates graded, the report written in the studio's
 * layout under the directory given — where the served studio reads it back.
 * The second argument is the shell's baseline file.
 */
public final class WorkspaceConformanceExport {

    private WorkspaceConformanceExport() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) throw new IllegalArgumentException("Usage: WorkspaceConformanceExport <output-directory> [<baseline>]");
        Path dir = Path.of(args[0]);
        Path baseline = args.length == 2 ? Path.of(args[1]) : null;
        ConformanceRun run = new ConformanceEngine(WorkspaceConformance.POLICY, new ServedModuleRenderer())
                .assemble(WorkspaceConformance.TOP_LEVEL, WorkspaceConformance.grader(baseline));
        new ConformanceReportWriter().write(dir, run);
        System.out.println("[WorkspaceConformanceExport] wrote report to " + dir
                + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, "
                + run.summary().warningCount() + " warnings)");
    }
}
