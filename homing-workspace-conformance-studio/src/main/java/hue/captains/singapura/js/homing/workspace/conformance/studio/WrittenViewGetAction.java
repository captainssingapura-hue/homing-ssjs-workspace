package hue.captains.singapura.js.homing.workspace.conformance.studio;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * One of the studio's crate views, answered with what the export wrote for it:
 * the view run at build time over this repo's crates, which this studio cannot
 * load. Read from the classpath - {@code /conformance-report/live/<file>} - and
 * served as it is.
 *
 * @param file        the file under {@code live/}
 * @param contentType what it is served as
 */
public record WrittenViewGetAction(String file, String contentType)
        implements GetAction<RoutingContext, WrittenViewGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    public WrittenViewGetAction {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(contentType, "contentType");
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("id"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        try (InputStream in = WrittenViewGetAction.class.getResourceAsStream("/conformance-report/live/" + file)) {
            if (in == null) {
                return CompletableFuture.failedFuture(notFound(file, "not written: build homing-workspace-conformance"));
            }
            return CompletableFuture.completedFuture(new DocContent(new String(in.readAllBytes(), StandardCharsets.UTF_8), contentType));
        } catch (IOException e) {
            return CompletableFuture.failedFuture(notFound(file, e.getMessage()));
        }
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(new ResourceNotFound._InternalError(null, reason + ": " + resource),
                                    new ResourceNotFound._ExternalError(resource, reason));
    }
}
