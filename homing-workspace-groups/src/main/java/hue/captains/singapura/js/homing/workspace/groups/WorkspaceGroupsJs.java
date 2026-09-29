package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Section;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The groups as a page has them: a constant of the shape {@code WorkspaceDirectory}
 * is provided with, generated from their core definitions - never written by
 * hand. A site's module serves it ({@code SelfContent}), and its page provides
 * it at boot.
 *
 * <pre>{@code
 * const NAME = Object.freeze([ { id, title, defaultKind, defaultPath,
 *     sections: [ { title, slug, workspaces: [ { kind, title, path } ] } ] } ]);
 * }</pre>
 */
public final class WorkspaceGroupsJs {

    /** A constant's name: upper case, words by underscores - as a generated constant is named. */
    public static final Pattern CONST_NAME = Pattern.compile("[A-Z][A-Z0-9_]*");

    private WorkspaceGroupsJs() {}

    /** {@code const <name> = Object.freeze([...]);} - every group, frozen through. */
    public static String constant(String name, WorkspaceGroups groups) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(groups, "groups");
        if (!CONST_NAME.matcher(name).matches()) throw new IllegalArgumentException("not a constant's name: " + name);
        return "const " + name + " = " + array(groups.groups().stream().map(WorkspaceGroupsJs::group).collect(Collectors.joining(", "))) + ";";
    }

    static String group(WorkspaceGroup g) {
        return object("id: " + quote(g.id().value())
                + ", title: " + quote(g.title())
                + ", defaultKind: " + quote(g.defaultKind().value())
                + ", defaultPath: " + quote(g.defaultPath().toString())
                + ", sections: " + array(g.sections().stream().map(s -> section(g, s)).collect(Collectors.joining(", "))));
    }

    static String section(WorkspaceGroup g, Section s) {
        return object("title: " + quote(s.title())
                + ", slug: " + quote(s.slug().value())
                + ", workspaces: " + array(s.workspaces().stream().map(w -> workspace(g, w)).collect(Collectors.joining(", "))));
    }

    static String workspace(WorkspaceGroup g, GroupedWorkspace w) {
        return object("kind: " + quote(w.kind().value()) + ", title: " + quote(w.title()) + ", path: " + quote(g.path(w.kind()).toString()));
    }

    private static String object(String fields) { return "Object.freeze({ " + fields + " })"; }

    private static String array(String items) { return "Object.freeze([" + items + "])"; }

    /** A JavaScript string literal: quotes, backslashes, the controls and the line separators escaped. */
    static String quote(String s) {
        var out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == ' ' || c == ' ' || c == '<') out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
