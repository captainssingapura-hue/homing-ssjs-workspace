package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.List;
import java.util.Objects;

/**
 * A heading in a group, and the workspaces filed under it, in order. Its slug
 * is its segment in their paths: derived from its title unless given.
 *
 * @param title      the heading: not blank
 * @param slug       its segment in its workspaces' paths
 * @param workspaces those filed under it, in order: at least one
 */
public record Section(String title, SectionSlug slug, List<GroupedWorkspace> workspaces) {

    public Section {
        Objects.requireNonNull(title, "Section.title");
        Objects.requireNonNull(slug, "Section.slug");
        Objects.requireNonNull(workspaces, "Section.workspaces");
        if (title.isBlank()) throw new IllegalArgumentException("Section.title - not blank");
        workspaces = List.copyOf(workspaces);
        if (workspaces.isEmpty()) throw new IllegalArgumentException("the section '" + title + "' files no workspace");
    }

    /** A section whose slug its title derives. */
    public static Section of(String title, GroupedWorkspace... workspaces) {
        return new Section(title, SectionSlug.from(title), List.of(workspaces));
    }

    /** A section with a slug of its own - for a title that derives none, or one that should read otherwise. */
    public static Section of(String title, SectionSlug slug, GroupedWorkspace... workspaces) {
        return new Section(title, slug, List.of(workspaces));
    }
}
