package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.UUID;

/**
 * Which one workspace of its kind a log belongs to — a UUID under the wrapper,
 * lowercase on the wire. With the {@link WorkspaceKind}, it names a log.
 *
 * <p>A page that names no workspace keeps its kind's own: {@link
 * #placeholderFor(WorkspaceKind)}, the same UUID every visit, derived as the
 * browser's {@code WorkspaceLogIdentity.placeholder} derives it.</p>
 *
 * @param id the underlying UUID
 */
public record WorkspaceInstanceId(UUID id) {

    public WorkspaceInstanceId { Objects.requireNonNull(id, "WorkspaceInstanceId.id"); }

    public static WorkspaceInstanceId fresh() {
        return new WorkspaceInstanceId(UUID.randomUUID());
    }

    public static WorkspaceInstanceId parse(String s) {
        return new WorkspaceInstanceId(UUID.fromString(s));
    }

    /**
     * The kind's own workspace: a UUID derived from {@code "workspace:" + kind}
     * by a stable hash, so the same kind always yields the same id.
     */
    public static WorkspaceInstanceId placeholderFor(WorkspaceKind kind) {
        Objects.requireNonNull(kind, "kind");
        String seed = "workspace:" + kind.value();
        int hash = 0;
        for (int i = 0; i < seed.length(); i++) {
            hash = (hash << 5) - hash + seed.charAt(i);
        }
        long high = (((long) hash) << 32) | 0x7000_5000_9000L;
        long low  = (((long) hash) << 32) | 0x0001L;
        return new WorkspaceInstanceId(new UUID(high, low));
    }

    @Override public String toString() { return id.toString(); }
}
