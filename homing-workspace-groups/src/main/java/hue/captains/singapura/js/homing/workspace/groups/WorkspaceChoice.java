package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a workspace choice party carries: the choosing of a kind of workspace to
 * look into, and the asking for a workspace to open. A member does -
 * {@link Choose}, {@link CurrentRequested}, {@link Open}; the party says -
 * {@link Chosen}, {@link Opening}. Browsing and opening are different acts: a
 * kind chosen opens nothing, and only an {@link Open} asks for a workspace.
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
    /** The party says: this kind is chosen. */
    record Chosen(String workspaceKind) implements WorkspaceChoice {}
    /** The party says: this workspace is asked to open - for whoever opens workspaces to hear. */
    record Opening(String workspaceKind, String workspaceId) implements WorkspaceChoice {}
    /**
     * The type: {@code workspace-choice}, its identity on a page - its constant
     * served as {@code WORKSPACE_CHOICE}, and a root instance's secretary,
     * unless a workspace puts its own, {@code WorkspaceChoiceSecretary}.
     */
    PartyType<WorkspaceChoice> TYPE = new PartyType<>("workspace-choice", WorkspaceChoice.class)
            .servedFrom(new ModuleImports<>(List.of(new WorkspaceChoiceModule.WORKSPACE_CHOICE()), WorkspaceChoiceModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new WorkspaceChoiceSecretaryModule.WorkspaceChoiceSecretary()), WorkspaceChoiceSecretaryModule.INSTANCE));
}
