package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A workspace group: workspaces filed under sections, and the one a bare
 * address opens. The group owns its whole tree - the headings, their order,
 * which workspace is filed under which, and the default - and a workspace knows
 * none of it: the same workspace may be filed otherwise in a group of another
 * site, and re-filing it changes its path, never its name, so never its log.
 *
 * <p>It holds together when it is made, or it is not made:</p>
 * <ol>
 *   <li>at least one section, and every section files at least one workspace;</li>
 *   <li>no two sections share a slug - it is their segment in the paths;</li>
 *   <li>no workspace is filed twice - a name is one log, and two paths to it
 *       would be one workspace in two places;</li>
 *   <li>the default is filed in the group - its first workspace unless said.</li>
 * </ol>
 *
 * @param id               its identity on its site
 * @param title            how it is called: not blank
 * @param sections         its headings, in order, each with its workspaces in order
 * @param defaultWorkspace the one a bare address opens
 */
public record WorkspaceGroup(GroupId id, String title, List<Section> sections, WorkspaceName defaultWorkspace) {

    public WorkspaceGroup {
        Objects.requireNonNull(id, "WorkspaceGroup.id");
        Objects.requireNonNull(title, "WorkspaceGroup.title");
        Objects.requireNonNull(sections, "WorkspaceGroup.sections");
        if (title.isBlank()) throw new IllegalArgumentException("WorkspaceGroup '" + id + "': title - not blank");
        sections = List.copyOf(sections);
        if (sections.isEmpty()) throw new IllegalArgumentException("WorkspaceGroup '" + id + "' has no section");
        var slugs = new LinkedHashMap<SectionSlug, String>();
        var filed = new LinkedHashMap<WorkspaceName, String>();
        for (Section s : sections) {
            String before = slugs.putIfAbsent(s.slug(), s.title());
            if (before != null) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': the sections '" + before + "' and '" + s.title()
                        + "' share the slug '" + s.slug() + "' - give one another");
            }
            for (GroupedWorkspace w : s.workspaces()) {
                String under = filed.putIfAbsent(w.name(), s.title());
                if (under != null) {
                    throw new IllegalArgumentException("WorkspaceGroup '" + id + "': the workspace '" + w.name() + "' is filed twice - under '"
                            + under + "' and under '" + s.title() + "'");
                }
            }
        }
        if (defaultWorkspace == null) defaultWorkspace = sections.get(0).workspaces().get(0).name();
        if (!filed.containsKey(defaultWorkspace)) {
            throw new IllegalArgumentException("WorkspaceGroup '" + id + "': the default '" + defaultWorkspace + "' is not filed in it - "
                    + String.join(", ", filed.keySet().stream().map(WorkspaceName::value).toList()));
        }
    }

    /** A group to build: its sections, then its default. */
    public static Builder of(String id, String title) { return new Builder(GroupId.of(id), title); }

    /** Every workspace filed in it, section by section, in order. */
    public List<GroupedWorkspace> workspaces() {
        var out = new ArrayList<GroupedWorkspace>();
        for (Section s : sections) out.addAll(s.workspaces());
        return List.copyOf(out);
    }

    /** The workspace of that name, when it is filed here. */
    public Optional<GroupedWorkspace> workspace(WorkspaceName name) {
        for (Section s : sections) for (GroupedWorkspace w : s.workspaces()) if (w.name().equals(name)) return Optional.of(w);
        return Optional.empty();
    }

    /** The section a workspace is filed under, when it is filed here. */
    public Optional<Section> sectionOf(WorkspaceName name) {
        for (Section s : sections) for (GroupedWorkspace w : s.workspaces()) if (w.name().equals(name)) return Optional.of(s);
        return Optional.empty();
    }

    public boolean files(WorkspaceName name) { return workspace(name).isPresent(); }

    /** A filed workspace's path: its section's slug, then its name. Refused for one not filed here. */
    public GroupPath path(WorkspaceName name) {
        Section s = sectionOf(name).orElseThrow(() -> new IllegalArgumentException(
                "WorkspaceGroup '" + id + "': the workspace '" + name + "' is not filed in it"));
        return new GroupPath(s.slug(), name);
    }

    /** Every filed workspace's path, in order: what a router serves. */
    public Map<GroupPath, GroupedWorkspace> paths() {
        var out = new LinkedHashMap<GroupPath, GroupedWorkspace>();
        for (Section s : sections) for (GroupedWorkspace w : s.workspaces()) out.put(new GroupPath(s.slug(), w.name()), w);
        return java.util.Collections.unmodifiableMap(out);
    }

    /** The workspace at two segments of an address, when they are one of its paths - a section and the name filed under it. */
    public Optional<GroupedWorkspace> at(String section, String workspace) {
        for (Section s : sections) {
            if (!s.slug().value().equals(section)) continue;
            for (GroupedWorkspace w : s.workspaces()) if (w.name().value().equals(workspace)) return Optional.of(w);
        }
        return Optional.empty();
    }

    /** The default's path: where a bare address goes. */
    public GroupPath defaultPath() { return path(defaultWorkspace); }

    /** A group, section by section. Each call a new builder, so one in the making is never shared. */
    public static final class Builder {

        private final GroupId id;
        private final String title;
        private final List<Section> sections;
        private final WorkspaceName defaultWorkspace;

        private Builder(GroupId id, String title) { this(id, title, List.of(), null); }

        private Builder(GroupId id, String title, List<Section> sections, WorkspaceName defaultWorkspace) {
            this.id = id;
            this.title = title;
            this.sections = sections;
            this.defaultWorkspace = defaultWorkspace;
        }

        /** A heading and the workspaces filed under it, its slug derived from its title. */
        public Builder section(String title, GroupedWorkspace... workspaces) { return with(Section.of(title, workspaces)); }

        /** A heading with a slug of its own. */
        public Builder section(String title, SectionSlug slug, GroupedWorkspace... workspaces) { return with(Section.of(title, slug, workspaces)); }

        /** The one a bare address opens; the first filed unless said. */
        public Builder defaultTo(WorkspaceName name) { return new Builder(id, title, sections, name); }

        public WorkspaceGroup build() { return new WorkspaceGroup(id, title, sections, defaultWorkspace); }

        private Builder with(Section s) {
            var next = new ArrayList<>(sections);
            next.add(s);
            return new Builder(id, title, List.copyOf(next), defaultWorkspace);
        }
    }
}
