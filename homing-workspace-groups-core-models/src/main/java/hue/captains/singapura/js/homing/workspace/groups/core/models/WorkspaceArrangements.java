package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A workspace's arrangements, one for each placement engine it may be shown by -
 * the split grid's, one pane's, the tiles' - each its own, since each engine has
 * its own vocabulary of where. A workspace shown by an engine it has none for
 * starts with nothing open.
 *
 * @param workspace    whose they are
 * @param arrangements each of that workspace, at most one an engine
 * @param <W> the workspace
 */
public record WorkspaceArrangements<W extends WorkspaceSpec>(W workspace, List<Arrangement<W, ?>> arrangements) {

    public WorkspaceArrangements {
        Objects.requireNonNull(workspace, "WorkspaceArrangements.workspace");
        arrangements = List.copyOf(Objects.requireNonNull(arrangements, "WorkspaceArrangements.arrangements"));
        var engines = new HashSet<PlacementEngine>();
        for (Arrangement<W, ?> a : arrangements) {
            if (!a.workspace().equals(workspace)) {
                throw new IllegalArgumentException("the arrangements of " + workspace.workspaceKind() + " hold one of " + a.workspace().workspaceKind());
            }
            if (!engines.add(a.engine())) {
                throw new IllegalArgumentException("the arrangements of " + workspace.workspaceKind() + " hold two for " + a.engine());
            }
        }
    }

    @SafeVarargs
    public static <W extends WorkspaceSpec> WorkspaceArrangements<W> of(W workspace, Arrangement<W, ?>... arrangements) {
        return new WorkspaceArrangements<>(workspace, List.of(arrangements));
    }

    /** Its arrangement for that engine, if it has one. */
    public Optional<Arrangement<W, ?>> forEngine(PlacementEngine engine) {
        for (Arrangement<W, ?> a : arrangements) if (a.engine().equals(engine)) return Optional.of(a);
        return Optional.empty();
    }

    /**
     * Its arrangement for that engine, typed as the engine's placement - for the
     * engine that reads it; none when it has none, or one of another type.
     */
    @SuppressWarnings("unchecked")
    public <P extends Placement> Optional<Arrangement<W, P>> forEngine(PlacementEngine engine, Class<P> placement) {
        return forEngine(engine).filter(a -> placement.isInstance(a.placement())).map(a -> (Arrangement<W, P>) a);
    }

    /** The engines it has an arrangement for, in its order. */
    public List<PlacementEngine> engines() { return arrangements.stream().map(Arrangement::engine).toList(); }
}
