package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The workspaces of a kind, listed over the memory backend: each listed the first
 * time it is opened, under a name of its own among its kind's; noted each time
 * after; renamed; a kind's list apart from another's.
 */
class WorkspaceCatalogueTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/log/store/";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        for (String m : new String[]{"MemoryLog", "WorkspaceLogIdentity", "WorkspaceCatalogue"}) loadModule(DIR + m + "Module.js");
        js.eval("js", """
                var clock = 1790000000000, backend = new MemoryLog();
                var catalogue = new WorkspaceCatalogue({ backend: backend, now: () => clock });
                var notes = new WorkspaceKind("notes");
                function key(kind, id) { return new LogKey(new WorkspaceKind(kind), new WorkspaceInstanceId(id)); }
                var own = key("notes", WorkspaceLogIdentity.placeholder("notes"));
                var second = key("notes", "7f1b6c2e-5000-9000-7f1b-6c2e00000002"), third = key("notes", "7f1b6c2e-5000-9000-7f1b-6c2e00000003");
                var got = {};
                function names(list) { return JSON.stringify(list.map((e) => e.name.value)); }
                """);
    }

    @Test
    void eachIsListedTheFirstTimeUnderANameOfItsOwn_andNotedAfter() {
        js.eval("js", """
                catalogue.opened(own).then((e) => { got.own = e; clock += 10; return catalogue.opened(second); })
                    .then((e) => { got.second = e; clock += 10; return catalogue.opened(third); })
                    .then(() => { clock += 10; return catalogue.opened(own); }).then((e) => { got.again = e; return catalogue.list(notes); })
                    .then((list) => { got.list = list; });
                """);
        assertTrue(js.eval("js", "got.own instanceof WorkspaceEntry && got.own.log instanceof LogKey && got.own.log.workspace.id === own.workspace.id").asBoolean(), "said as the WorkspaceEntry declared in Java");
        assertEquals("notes", js.eval("js", "got.own.name.value").asString());
        assertEquals("notes 2", js.eval("js", "got.second.name.value").asString());
        assertEquals("[\"notes\",\"notes 3\",\"notes 2\"]", js.eval("js", "names(got.list)").asString(), "the latest opened first");
        assertEquals(1790000000000L, js.eval("js", "got.again.created").asLong(), "opened again: created as it was");
        assertEquals(1790000000030L, js.eval("js", "got.again.opened").asLong(), "and its opening noted");
        assertEquals("notes", js.eval("js", "got.again.name.value").asString());
    }

    @Test
    void aRenameIsKept_andFreesTheNameItHad() {
        js.eval("js", """
                catalogue.opened(own).then(() => catalogue.rename(own, "  Groceries  ")).then((e) => { got.renamed = e; return catalogue.opened(second); })
                    .then((e) => { got.second = e; });
                """);
        assertEquals("Groceries", js.eval("js", "got.renamed.name.value").asString());
        assertEquals("notes", js.eval("js", "got.second.name.value").asString(), "the kind's own name is free again");
    }

    @Test
    void aBlankName_orAWorkspaceNotListed_isRefused() {
        js.eval("js", """
                catalogue.opened(own).then(() => catalogue.rename(own, "   ")).then(null, (e) => { got.blank = e.message; })
                    .then(() => catalogue.rename(second, "Two")).then(null, (e) => { got.unlisted = e.message; })
                    .then(() => catalogue.list(notes)).then((list) => { got.list = list; });
                """);
        assertTrue(js.eval("js", "got.blank").asString().contains("renamed to nothing"));
        assertTrue(js.eval("js", "got.unlisted").asString().contains("not listed"));
        assertEquals("[\"notes\"]", js.eval("js", "names(got.list)").asString(), "nothing kept for either");
    }

    @Test
    void aKindsListIsItsOwn_andAnEntryThatDoesNotReadIsPassedOver() {
        js.eval("js", """
                var demo = key("demo", WorkspaceLogIdentity.placeholder("demo"));
                backend.writeEntry("notes", "7f1b6c2e-5000-9000-7f1b-6c2e00000009", () => ({ log: "not a key" }))
                    .then(() => catalogue.opened(own)).then(() => catalogue.opened(demo)).then((e) => { got.demo = e; })
                    .then(() => catalogue.list(notes)).then((list) => { got.list = list; });
                """);
        assertEquals("demo", js.eval("js", "got.demo.name.value").asString(), "named among its own kind's");
        assertEquals("[\"notes\"]", js.eval("js", "names(got.list)").asString());
    }
}
