package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Where a kind of workspace is in its group, as the page's anchor names it:
 * {@code WorkspaceAnchor} - {@code ws/<section>/<kind>}, read against a group as
 * the directory has it, and written for a kind of it (RFC 0058). The group is
 * the page; the anchor is the group's own tree, and never reaches the server.
 */
public record WorkspaceAnchorModule() implements EsModule<WorkspaceAnchorModule> {

    public record WorkspaceAnchor() implements Exportable._Class<WorkspaceAnchorModule> {}

    public static final WorkspaceAnchorModule INSTANCE = new WorkspaceAnchorModule();

    @Override public ImportsFor<WorkspaceAnchorModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceAnchorModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceAnchor())); }
}
