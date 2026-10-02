package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import hue.captains.singapura.tao.http.action.TypedContent;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The content bench's items, as its stewards fetch them: {@code /bench/content?type=note&key=alpha}
 * - a note, or a flow of notes - answered after a moment as {@code { content, served }}, served
 * counting how often the server has been asked for that item: what shows each is fetched once.
 * An item the bench has none of is 404, for the party to say it is unavailable.
 */
public final class BenchContentGetAction implements GetAction<RoutingContext, BenchContentGetAction.Query, EmptyParam.NoHeaders, BenchContentGetAction.Json> {

    /** Where a bench steward fetches. */
    public static final String ROUTE = "/bench/content";

    /** How long an answer takes: long enough for a page to show what waits. */
    static final long DELAY_MS = 400;

    /** Which item: its type, note or flow, and its key. */
    public record Query(String type, String key) implements Param._QueryString {}

    /** The reply: JSON text. */
    public record Json(String body) implements TypedContent {
        @Override public String contentType() { return "application/json; charset=utf-8"; }
    }

    /** The notes: a title and a text each. */
    static final Map<String, List<String>> NOTES = Map.of(
            "alpha", List.of("Alpha", "Asked for by both scopes, and by a flow in each - fetched once."),
            "beta", List.of("Beta", "Asked for by scope A, and by the trio in scope B."),
            "gamma", List.of("Gamma", "Asked for by scope B, and by the pair in both."),
            "delta", List.of("Delta", "Asked for only by the trio."));

    /** The flows: the keys of the notes they are made of, in order. */
    static final Map<String, List<String>> FLOWS = Map.of(
            "pair", List.of("alpha", "gamma"),
            "trio", List.of("beta", "delta", "alpha"));

    private final Map<String, AtomicInteger> served = new ConcurrentHashMap<>();

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("type"), ctx.request().getParam("key"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() { return ctx -> new EmptyParam.NoHeaders(); }

    @Override
    public CompletableFuture<Json> execute(Query query, EmptyParam.NoHeaders headers) {
        String content = content(String.valueOf(query.type()), String.valueOf(query.key()));
        if (content == null) {
            String what = query.type() + " " + query.key();
            return CompletableFuture.failedFuture(new ResourceNotFound(
                    new ResourceNotFound._InternalError(null, "the bench has no " + what),
                    new ResourceNotFound._ExternalError(what, "The bench has no such item")));
        }
        int n = served.computeIfAbsent(query.type() + "/" + query.key(), k -> new AtomicInteger()).incrementAndGet();
        var reply = new Json("{\"content\":" + content + ",\"served\":" + n + "}");
        return CompletableFuture.supplyAsync(() -> reply, CompletableFuture.delayedExecutor(DELAY_MS, TimeUnit.MILLISECONDS));
    }

    /** An item's content as JSON, of its type's shape; null when the bench has none. */
    static String content(String type, String key) {
        if (type.equals("note") && NOTES.containsKey(key)) {
            var n = NOTES.get(key);
            return "{\"title\":" + quoted(n.get(0)) + ",\"text\":" + quoted(n.get(1)) + "}";
        }
        if (type.equals("flow") && FLOWS.containsKey(key)) {
            return "{\"parts\":[" + FLOWS.get(key).stream()
                    .map(k -> "{\"type\":\"note-card\",\"params\":[{\"name\":\"key\",\"value\":" + quoted(k) + "}]}")
                    .collect(Collectors.joining(",")) + "]}";
        }
        return null;
    }

    private static String quoted(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
}
