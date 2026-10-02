package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.codec.CheckpointCodec;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import hue.captains.singapura.js.homing.workspace.log.store.CheckpointKeeper;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A log's latest checkpoint, as the server keeps it: {@code ?kind=<kind>&workspace=<uuid>},
 * answered as the checkpoint's wire - the same JSON the page posted - or 404
 * when the server keeps none of that log.
 */
public final class CheckpointGetAction
        implements GetAction<RoutingContext, CheckpointGetAction.Query, EmptyParam.NoHeaders, JsonReply> {

    /** Which log: its kind and its workspace, as the address gives them. */
    public record Query(String kind, String workspace) implements Param._QueryString {}

    private final CheckpointKeeper keeper;

    public CheckpointGetAction(CheckpointKeeper keeper) {
        this.keeper = Objects.requireNonNull(keeper, "CheckpointGetAction.keeper");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("kind"), ctx.request().getParam("workspace"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<JsonReply> execute(Query query, EmptyParam.NoHeaders headers) {
        WorkspaceKind kind;
        WorkspaceInstanceId workspace;
        try {
            kind = WorkspaceKind.of(Objects.requireNonNull(query.kind(), "kind is required"));
            workspace = WorkspaceInstanceId.parse(Objects.requireNonNull(query.workspace(), "workspace is required"));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(CheckpointRefused.because("which log: " + e.getMessage()));
        }
        return keeper.latest(kind, workspace)
                .map(c -> CompletableFuture.completedFuture(new JsonReply(JsonText.write(CheckpointCodec.INSTANCE.transformTo(c)))))
                .orElseGet(() -> CompletableFuture.failedFuture(new ResourceNotFound(
                        new ResourceNotFound._InternalError(null, "no checkpoint of " + kind + " " + workspace),
                        new ResourceNotFound._ExternalError(kind + "/" + workspace, "The server keeps no checkpoint of this log"))));
    }
}
