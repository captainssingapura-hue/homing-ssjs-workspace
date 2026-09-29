package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The page's directory, provided with what Java generates from the groups' core
 * definitions: read back as they were defined - groups, sections, paths, the
 * default - frozen through; subscribers told; a malformed provide refused whole.
 */
class WorkspaceDirectoryTest extends JsModuleTestBase {

    static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("studio", "The \"studio\"")
                    .section("Everyday work", GroupedWorkspace.of("demo", "Demo"), GroupedWorkspace.of("books", "Books </script>"))
                    .section("Watching", GroupedWorkspace.of("monitors", "Monitors"))
                    .defaultTo(WorkspaceKind.of("books"))
                    .build(),
            WorkspaceGroup.of("bench", "Bench")
                    .section("On this bench", GroupedWorkspace.of("bench-one-pane", "One pane"))
                    .build());

    @BeforeEach
    void load() {
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceDirectoryModule.js");
        js.eval("js", "var console = { error: function () {} };");
        js.eval("js", WorkspaceGroupsJs.constant("GROUPS", GROUPS));
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void notProvided_ithasNone() {
        assertEquals("0 null null", eval("WorkspaceDirectory.groups().length + ' ' + WorkspaceDirectory.group() + ' ' + WorkspaceDirectory.find('demo')"));
    }

    @Test
    void providedWithWhatJavaGenerates_itReadsAsDefined() {
        eval("WorkspaceDirectory.provide(GROUPS)");
        assertEquals("studio,bench", eval("WorkspaceDirectory.groups().map(function (g) { return g.id; }).join()"));
        assertEquals("studio", eval("WorkspaceDirectory.group().id"), "no id: the first");
        assertEquals("The \"studio\"", eval("WorkspaceDirectory.group('studio').title"));
        assertEquals("books everyday-work/books", eval("var g = WorkspaceDirectory.group('studio'); g.defaultKind + ' ' + g.defaultPath"));
        assertEquals("everyday-work:demo,books|watching:monitors",
                eval("WorkspaceDirectory.group('studio').sections.map(function (s) { return s.slug + ':' + s.workspaces.map(function (w) { return w.kind; }).join(); }).join('|')"));
        assertEquals("Books </script>", eval("WorkspaceDirectory.find('books').workspace.title"));
        assertEquals("studio watching monitors watching/monitors",
                eval("var f = WorkspaceDirectory.find('monitors'); [f.group.id, f.section.slug, f.workspace.kind, f.workspace.path].join(' ')"));
        assertEquals("bench-one-pane", eval("WorkspaceDirectory.group('bench').defaultKind"), "no default said: the first");
        assertEquals("null null", eval("WorkspaceDirectory.find('nope') + ' ' + WorkspaceDirectory.group('nope')"));
    }

    @Test
    void theGeneratedTextEscapesWhatWouldBreakAPage() {
        String text = WorkspaceGroupsJs.constant("GROUPS", GROUPS);
        assertTrue(!text.contains("</script>"), text);
        assertTrue(text.startsWith("const GROUPS = Object.freeze(["), text);
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroupsJs.constant("groups", GROUPS));
    }

    @Test
    void whatIsProvidedIsFrozenThrough() {
        eval("WorkspaceDirectory.provide([{ id: 'g', title: 'G', defaultKind: 'a', sections: [{ title: 'S', slug: 's', workspaces: [{ kind: 'a', title: 'A', path: 's/a' }] }] }])");
        assertEquals("true true true", eval("var g = WorkspaceDirectory.group(); [Object.isFrozen(g), Object.isFrozen(g.sections[0]), Object.isFrozen(g.sections[0].workspaces[0])].join(' ')"));
    }

    @Test
    void subscribersAreToldAtEachProvide_untilTheyGo() {
        eval("var told = []; var off = WorkspaceDirectory.subscribe(function (gs) { told.push(gs.length); });"
                + "WorkspaceDirectory.subscribe(function () { throw new Error('a bad one'); });"
                + "WorkspaceDirectory.provide(GROUPS); off(); WorkspaceDirectory.provide([]);");
        assertEquals("2", eval("told.join()"), "told once, a bad subscriber beside it notwithstanding, and not after it went");
        assertEquals("0", eval("WorkspaceDirectory.groups().length"));
    }

    @Test
    void aMalformedProvideIsRefusedWhole() {
        eval("WorkspaceDirectory.provide(GROUPS)");
        for (String bad : new String[]{
                "'groups'",
                "[{ id: 'g', title: 'G', defaultKind: 'a', sections: [] }]",
                "[{ id: 'g', title: 'G', defaultKind: 'x', sections: [{ title: 'S', slug: 's', workspaces: [{ kind: 'a', title: 'A', path: 's/a' }] }] }]",
                "[GROUPS[0], GROUPS[0]]",
                "[GROUPS[0], { id: 'other', title: 'O', defaultKind: 'demo', sections: [{ title: 'S', slug: 's', workspaces: [{ kind: 'demo', title: 'D', path: 's/demo' }] }] }]"}) {
            assertThrows(PolyglotException.class, () -> eval("WorkspaceDirectory.provide(" + bad + ")"), bad);
        }
        assertEquals("studio,bench", eval("WorkspaceDirectory.groups().map(function (g) { return g.id; }).join()"), "what was provided stands");
    }
}
