package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;

import java.util.Objects;

/**
 * Which log: its kind and its workspace - what a server keeps one checkpoint
 * of, and what a storage keys it by.
 *
 * @param kind      the kind of workspace
 * @param workspace the one workspace of that kind
 */
public record LogKey(WorkspaceKind kind, WorkspaceInstanceId workspace) {

    public LogKey {
        Objects.requireNonNull(kind, "LogKey.kind");
        Objects.requireNonNull(workspace, "LogKey.workspace");
    }

    /** The log a header names. */
    public static LogKey of(LogHeader header) { return new LogKey(header.kind(), header.workspaceId()); }

    @Override public String toString() { return kind + "/" + workspace; }
}
