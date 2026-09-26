package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;

import java.util.Objects;

/**
 * The first line of an exported workspace log: what the file is, in which
 * version of the format, and whose log it is — the kind of workspace and the
 * one workspace of that kind.
 *
 * @param format      always {@link #FORMAT}
 * @param version     always {@link #VERSION}
 * @param kind        the workspace's kind
 * @param workspaceId the workspace
 */
public record LogHeader(String format, int version, WorkspaceSpecKind kind, WorkspaceInstanceId workspaceId) {

    public static final String FORMAT = "homing.workspace.log";
    public static final int VERSION = 1;

    public LogHeader {
        Objects.requireNonNull(format, "LogHeader.format");
        Objects.requireNonNull(kind, "LogHeader.kind");
        Objects.requireNonNull(workspaceId, "LogHeader.workspaceId");
        if (!FORMAT.equals(format)) throw new IllegalArgumentException("LogHeader.format '" + format + "' — not " + FORMAT);
        if (version != VERSION) throw new IllegalArgumentException("LogHeader.version " + version + " — this reads " + VERSION);
    }

    public static LogHeader of(WorkspaceSpecKind kind, WorkspaceInstanceId workspaceId) {
        return new LogHeader(FORMAT, VERSION, kind, workspaceId);
    }
}
