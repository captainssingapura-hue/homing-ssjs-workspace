package hue.captains.singapura.js.homing.workspace.shell.server;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.workspace.log.Checkpoint;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.codec.CheckpointCodec;
import hue.captains.singapura.js.homing.workspace.log.fold.CheckpointFold;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import hue.captains.singapura.js.homing.workspace.log.store.StoredCheckpointKeeper;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The server's side of the workspace's states: a checkpoint posted is read into
 * Java, checked and kept - the latest of each log, never an older one over it -
 * and read back as the same JSON; what is not one is refused, 400; a log it
 * keeps none of is 404. And the routes sit before the site's catch-all.
 */
class WorkspaceServerTest {

    private static final LogHeader HEADER = LogHeader.of(WorkspaceKind.of("demo"), WorkspaceInstanceId.parse("7f1b6c2e-5000-9000-7f1b-6c2e00000001"));
    private static final EmptyParam.NoHeaders NONE = new EmptyParam.NoHeaders();

    /** A checkpoint through the first n of a log of tabs opened in the main region. */
    private static Checkpoint through(int n) {
        var events = new ArrayList<LoggedEvent>();
        for (int i = 1; i <= n; i++) {
            events.add(new LoggedEvent(EventSeq.of(i), Instant.ofEpochMilli(1_790_000_000_000L + i),
                    new TabEvent.TabOpened(TabId.of("tab-" + i), WidgetKind.of("note"), WidgetTitle.of("Note " + i), Host.region("main"), i - 1)));
        }
        return CheckpointFold.next(null, HEADER, events);
    }

    private static String wire(Checkpoint c) { return JsonText.write(CheckpointCodec.INSTANCE.transformTo(c)); }

    @Test
    void aCheckpointPostedIsKept_andReadBackAsTheSameJson() {
        var keeper = StoredCheckpointKeeper.inMemory();
        var post = new CheckpointPostAction(keeper);
        String reply = post.execute(new CheckpointPostAction.Posted(wire(through(3))), NONE).join().body();
        assertEquals("{\"kind\":\"demo\",\"workspaceId\":\"7f1b6c2e-5000-9000-7f1b-6c2e00000001\",\"through\":3,\"kept\":true}", reply);
        assertTrue(post.execute(new CheckpointPostAction.Posted(wire(through(2))), NONE).join().body().endsWith("\"through\":2,\"kept\":false}"),
                "an older one arriving late is not kept");
        String back = new CheckpointGetAction(keeper).execute(new CheckpointGetAction.Query("demo", HEADER.workspaceId().toString()), NONE).join().body();
        assertEquals(wire(through(3)), back);
        assertEquals(3, keeper.latest(HEADER.kind(), HEADER.workspaceId()).orElseThrow().events());
    }

    @Test
    void whatIsNotACheckpointIsRefused_400_sayingWhy() {
        var post = new CheckpointPostAction(StoredCheckpointKeeper.inMemory());
        for (String body : new String[]{ "", "not json", "{\"folded\":1}", wire(through(2)).replace("\"events\":2", "\"events\":2.5") }) {
            var e = assertThrows(CompletionException.class, () -> post.execute(new CheckpointPostAction.Posted(body), NONE).join(), body);
            var refused = assertInstanceOf(CheckpointRefused.class, e.getCause());
            assertEquals(400, refused.statusCode());
            assertTrue(!refused.externalError().why().isBlank());
        }
        // a state its own rules refuse: a region shown a tab it does not hold
        String good = wire(through(1)), bad = good.replace("\"tabs\":[\"tab-1\"],\"shown\":null", "\"tabs\":[\"tab-1\"],\"shown\":\"tab-9\"");
        assertTrue(!bad.equals(good), "the region's showing was changed: " + good);
        var e = assertThrows(CompletionException.class, () -> post.execute(new CheckpointPostAction.Posted(bad), NONE).join());
        assertTrue(assertInstanceOf(CheckpointRefused.class, e.getCause()).externalError().why().contains("tab-9"));
    }

    @Test
    void aLogItKeepsNoneOfIs404_andAnAddressThatNamesNoLogIs400() {
        var get = new CheckpointGetAction(StoredCheckpointKeeper.inMemory());
        var none = assertThrows(CompletionException.class, () -> get.execute(new CheckpointGetAction.Query("demo", HEADER.workspaceId().toString()), NONE).join());
        assertEquals(404, assertInstanceOf(ResourceNotFound.class, none.getCause()).statusCode());
        var bad = assertThrows(CompletionException.class, () -> get.execute(new CheckpointGetAction.Query("no spaces", "x"), NONE).join());
        assertEquals(400, assertInstanceOf(CheckpointRefused.class, bad.getCause()).statusCode());
    }

    @Test
    void theRoutesSitBeforeTheSitesCatchAll() {
        var site = new LinkedHashMap<String, GetAction<RoutingContext, ?, ?, ?>>();
        site.put("/module", new CheckpointGetAction(StoredCheckpointKeeper.inMemory()));
        site.put("/*", new CheckpointGetAction(StoredCheckpointKeeper.inMemory()));
        ActionRegistry<RoutingContext> routes = WorkspaceServer.with(new ActionRegistry<>() {
            @Override public Map<String, GetAction<RoutingContext, ?, ?, ?>> getActions() { return site; }
            @Override public Map<String, hue.captains.singapura.tao.http.action.PostAction<RoutingContext, ?, ?, ?>> postActions() { return Map.of(); }
        }, StoredCheckpointKeeper.inMemory());
        assertEquals(List.of("/module", WorkspaceServer.CHECKPOINTS, "/*"), List.copyOf(routes.getActions().keySet()));
        assertEquals(List.of(WorkspaceServer.CHECKPOINTS), List.copyOf(routes.postActions().keySet()));
    }
}
