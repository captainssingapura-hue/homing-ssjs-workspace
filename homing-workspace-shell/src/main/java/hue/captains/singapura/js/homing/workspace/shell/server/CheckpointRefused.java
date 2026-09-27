package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.tao.http.action.ExternalError;
import hue.captains.singapura.tao.http.action.HttpReturnableException;
import hue.captains.singapura.tao.http.action.InternalError;

/**
 * What the workspace's server answers a request it will not take - a body that
 * is not a checkpoint, a kind or a workspace that is not one: 400, and why, in
 * the words the log's readers refused it with.
 */
public final class CheckpointRefused extends RuntimeException
        implements HttpReturnableException<CheckpointRefused.Why, CheckpointRefused.Cause> {

    /** What the page is told. */
    public record Why(String why) implements ExternalError {}

    /** What the server knows. */
    public record Cause(String why) implements InternalError {}

    private final String why;

    private CheckpointRefused(String why) {
        super(why);
        this.why = why;
    }

    public static CheckpointRefused because(String why) { return new CheckpointRefused(why == null ? "refused" : why); }

    @Override public int statusCode() { return 400; }

    @Override public Why externalError() { return new Why(why); }

    @Override public Cause internalError() { return new Cause(why); }
}
