package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.List;
import java.util.Objects;

/**
 * Where a workspace is in its group: the section it is filed under, then its
 * name - {@code media/video-room}. Relative to wherever the group is placed on
 * its site; the placement is the site's. The path is the group's to give: a
 * workspace re-filed has another path, and the same name.
 *
 * @param section   the section's slug
 * @param workspace the workspace's name
 */
public record GroupPath(SectionSlug section, WorkspaceName workspace) {

    public GroupPath {
        Objects.requireNonNull(section, "GroupPath.section");
        Objects.requireNonNull(workspace, "GroupPath.workspace");
    }

    /** The two segments, in order. */
    public List<String> segments() { return List.of(section.value(), workspace.value()); }

    @Override public String toString() { return section.value() + "/" + workspace.value(); }
}
