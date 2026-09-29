package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.List;
import java.util.Objects;

/**
 * Where a kind of workspace is in its group: the section it is filed under,
 * then its kind - {@code media/video-room}. Relative to wherever the group is
 * placed on its site; the placement is the site's. The path is the group's to
 * give: a kind re-filed has another path, and the same kind.
 *
 * @param section the section's slug
 * @param kind    the kind
 */
public record GroupPath(SectionSlug section, WorkspaceKind kind) {

    public GroupPath {
        Objects.requireNonNull(section, "GroupPath.section");
        Objects.requireNonNull(kind, "GroupPath.kind");
    }

    /** The two segments, in order. */
    public List<String> segments() { return List.of(section.value(), kind.value()); }

    @Override public String toString() { return section.value() + "/" + kind.value(); }
}
