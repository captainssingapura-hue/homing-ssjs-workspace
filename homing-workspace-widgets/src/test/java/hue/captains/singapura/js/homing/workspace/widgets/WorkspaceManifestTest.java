package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.ModuleImports;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A manifest says of a kind what the page needs before any widget is made: a
 * kind a workspace holds one of says so - {@code single: true} - and a kind of
 * many says nothing, as it always did.
 */
class WorkspaceManifestTest {

    record Kind(String kind, boolean single) implements WidgetDeclaration<NoParams> {
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() { return new ModuleImports<>(List.of(new HostedWidgetModule.HostedWidget()), HostedWidgetModule.INSTANCE); }
    }

    record Game() implements WorkspaceDeclaration {
        @Override public String name() { return "game"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(new Kind("play", true), new Kind("watch", false)); }
    }

    @Test
    void aKindHeldOnce_saysSo_andAKindOfMany_saysNothing() {
        String js = WorkspaceManifest.js("GAME", new Game());
        assertTrue(js.contains("\"play\": Object.freeze({ Widget: HostedWidget, title: \"Play\", parties: Object.freeze([]), single: true })"), js);
        assertTrue(js.contains("\"watch\": Object.freeze({ Widget: HostedWidget, title: \"Watch\", parties: Object.freeze([]) })"), js);
    }
}
