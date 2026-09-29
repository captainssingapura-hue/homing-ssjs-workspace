package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a workspace choice party carries: the choosing of a kind of workspace to
 * look into, and the asking for a workspace to open, or a new one; and keeping
 * them - renamed, deleted. A member does - {@link Choose}, {@link CurrentRequested},
 * {@link Open}, {@link OpenNew}, {@link Rename}, {@link Delete}, and, keeping them,
 * {@link Report}; the party says - {@link Chosen}, {@link Opening}, {@link OpeningNew},
 * {@link Renaming}, {@link Deleting}, {@link Reported}. Browsing and opening are
 * different acts: a kind chosen opens nothing; only an {@link Open} or an
 * {@link OpenNew} asks for a workspace.
 *
 * <p>A kind travels as {@code workspaceKind}, never {@code kind}: that field is
 * a message's own, and names which of these it is.</p>
 */
public sealed interface WorkspaceChoice {
    /** A member chose this kind: the one whose workspaces are looked into. */
    record Choose(String workspaceKind) implements WorkspaceChoice {}
    /** A member asks which kind is chosen - one that joins late - and is answered alone, when one is. */
    record CurrentRequested() implements WorkspaceChoice {}
    /** A member asks for a workspace to open: of this kind, this one of it - its id, or "" for the kind's own. */
    record Open(String workspaceKind, String workspaceId) implements WorkspaceChoice {}
    /**
     * A member asks for a new workspace of this kind: under this name - "" for the
     * kind's next, as the catalogue names one - here, or in a new tab. What makes it -
     * its id, its listing - is whoever opens workspaces'; a widget has none to give.
     */
    record OpenNew(String workspaceKind, String workspaceName, boolean newTab) implements WorkspaceChoice {}
    /** A member asks for a workspace to be called this: its name, the one thing of it a person says. */
    record Rename(String workspaceKind, String workspaceId, String workspaceName) implements WorkspaceChoice {}
    /** A member asks for a workspace to be deleted - softly: out of the list, its log kept. */
    record Delete(String workspaceKind, String workspaceId) implements WorkspaceChoice {}
    /**
     * Whoever keeps the workspaces says how an asking went, for every view of the kind:
     * whether its list changed, and a note to show - what was done, or why not.
     */
    record Report(String workspaceKind, String note, boolean changed) implements WorkspaceChoice {}
    /** The party says: this kind is chosen. */
    record Chosen(String workspaceKind) implements WorkspaceChoice {}
    /** The party says: this workspace is asked to open - for whoever opens workspaces to hear. */
    record Opening(String workspaceKind, String workspaceId) implements WorkspaceChoice {}
    /** The party says: a new workspace is asked for, as it was asked - for whoever opens workspaces to make it. */
    record OpeningNew(String workspaceKind, String workspaceName, boolean newTab) implements WorkspaceChoice {}
    /** The party says: this workspace is asked to be called so - for whoever keeps the workspaces. */
    record Renaming(String workspaceKind, String workspaceId, String workspaceName) implements WorkspaceChoice {}
    /** The party says: this workspace is asked to be deleted - for whoever keeps the workspaces. */
    record Deleting(String workspaceKind, String workspaceId) implements WorkspaceChoice {}
    /** The party says how an asking went: the kind's list changed, or not, and the note to show. */
    record Reported(String workspaceKind, String note, boolean changed) implements WorkspaceChoice {}
    /**
     * The type: {@code workspace-choice}, its identity on a page - its constant
     * served as {@code WORKSPACE_CHOICE}, and a root instance's secretary,
     * unless a workspace puts its own, {@code WorkspaceChoiceSecretary}.
     */
    PartyType<WorkspaceChoice> TYPE = new PartyType<>("workspace-choice", WorkspaceChoice.class)
            .servedFrom(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new WorkspaceChoiceSecretaryModule.WorkspaceChoiceSecretary()), WorkspaceChoiceSecretaryModule.INSTANCE));
}
