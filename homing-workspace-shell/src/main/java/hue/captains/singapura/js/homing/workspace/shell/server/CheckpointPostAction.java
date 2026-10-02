package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.codec.CheckpointCodec;
import hue.captains.singapura.js.homing.workspace.log.json.Json;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import hue.captains.singapura.js.homing.workspace.log.store.CheckpointKeeper;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import hue.captains.singapura.tao.http.action.PostAction;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A checkpoint a page sends, taken in: its body read by the generated codec
 * into a Java {@link Checkpoint} - every value checked as the declaration
 * checks it, the state's own rules among them - and handed to the server's
 * {@link CheckpointKeeper}. What is not a checkpoint is refused, 400, saying
 * why; what is, is answered with whose log it was, through which event, and
 * whether it was kept - not when the keeper has one through as far already.
 */
public final class CheckpointPostAction
        implements PostAction<RoutingContext, CheckpointPostAction.Posted, EmptyParam.NoHeaders, JsonReply> {

    /** The request's body, as it came. */
    public record Posted(String text) implements Param._Post {}

    private final CheckpointKeeper keeper;

    public CheckpointPostAction(CheckpointKeeper keeper) {
        this.keeper = Objects.requireNonNull(keeper, "CheckpointPostAction.keeper");
    }

    @Override
    public ParamMarshaller._Post<RoutingContext, Posted> postMarshaller() {
        return ctx -> new Posted(ctx.body() == null ? null : ctx.body().asString());
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<JsonReply> execute(Posted posted, EmptyParam.NoHeaders headers) {
        Checkpoint checkpoint;
        try { checkpoint = read(posted.text()); }
        catch (CheckpointRefused refused) { return CompletableFuture.failedFuture(refused); }
        boolean kept = keeper.keep(checkpoint);
        var header = checkpoint.folded().header();
        var reply = new LinkedHashMap<String, Json>();
        reply.put("kind", new Json.Str(header.kind().value()));
        reply.put("workspaceId", new Json.Str(header.workspaceId().toString()));
        reply.put("through", new Json.Int(checkpoint.folded().through().value()));
        reply.put("kept", Json.Bool.of(kept));
        return CompletableFuture.completedFuture(new JsonReply(JsonText.write(new Json.Obj(reply))));
    }

    /** A body read as a checkpoint, or refused saying why. */
    public static Checkpoint read(String text) {
        if (text == null || text.isBlank()) throw CheckpointRefused.because("no checkpoint in the request's body");
        try { return CheckpointCodec.INSTANCE.transformFrom(JsonText.parse(text)); }
        catch (RuntimeException e) { throw CheckpointRefused.because("not a checkpoint: " + e.getMessage()); }
    }
}
