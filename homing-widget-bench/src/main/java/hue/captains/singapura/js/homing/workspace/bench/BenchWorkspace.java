package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The bench's workspace of one pane, declared: {@code bench-one-pane} - its
 * log's kind too - where every kind the bench knows can be opened but the
 * nasty ones, which are the bench's to catch. Its root parties are resolved
 * from these kinds, each with its type's default secretary.
 */
public record BenchWorkspace() implements WorkspaceDeclaration {

    public static final BenchWorkspace INSTANCE = new BenchWorkspace();

    @Override public String name() { return "bench-one-pane"; }

    @Override
    public List<WidgetDeclaration<?>> kinds() {
        return WidgetBench.KINDS.stream().filter(k -> !k.kind().startsWith("nasty-")).toList();
    }
}
