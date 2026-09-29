package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The anchor a kind of workspace has in its group - {@code ws/<section>/<kind>} -
 * read against the group as the directory has it: its true anchor exact; a kind
 * of the group under another path moved to its own; no anchor, or another's, the
 * group's default; a workspace's anchor naming nothing of the group, the default,
 * and said.
 */
class WorkspaceAnchorTest extends JsModuleTestBase {

    @BeforeEach
    void load() {
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceDirectoryModule.js");
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceAnchorModule.js");
        js.eval("js", "var console = { error: function () {} };");
        js.eval("js", WorkspaceGroupsJs.constant("GROUPS", WorkspaceDirectoryTest.GROUPS));
        js.eval("js", "WorkspaceDirectory.provide(GROUPS); var studio = WorkspaceDirectory.group('studio'), bench = WorkspaceDirectory.group('bench');"
                + "function read(a) { var r = WorkspaceAnchor.read(studio, a); return [r.kind, r.anchor, r.said].join(' '); }");
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void aKindsAnchor_isItsPathInTheGroup() {
        assertEquals("ws/everyday-work/books", eval("WorkspaceAnchor.of(studio, 'books')"));
        assertEquals("ws/watching/monitors", eval("WorkspaceAnchor.of(studio, 'monitors')"));
        assertEquals("true", eval("WorkspaceAnchor.of(studio, 'bench-one-pane') === null"), "another group's kind has no anchor in this one");
    }

    @Test
    void itsTrueAnchor_isExact() {
        assertEquals("books ws/everyday-work/books exact", eval("read('ws/everyday-work/books')"));
        assertEquals("Everyday work|Books </script>", eval("var r = WorkspaceAnchor.read(studio, 'ws/everyday-work/books'); r.section.title + '|' + r.workspace.title"));
    }

    @Test
    void aKindUnderAnotherPath_isMovedToItsOwn_forItsSegmentIsItsIdentity() {
        assertEquals("monitors ws/watching/monitors moved", eval("read('ws/everyday-work/monitors')"));
        assertEquals("monitors ws/watching/monitors moved", eval("read('ws/monitors')"));
        assertEquals("monitors ws/watching/monitors moved", eval("read('ws//watching/monitors/')"));
        assertEquals("monitors ws/watching/monitors moved", eval("read('ws/watching/%6Donitors')"), "a segment as it was meant");
    }

    @Test
    void noAnchor_orAnothersThanTheWorkspaces_isTheGroupsDefault() {
        assertEquals("books ws/everyday-work/books none", eval("read('')"));
        assertEquals("books ws/everyday-work/books none", eval("read('a-heading')"));
        assertEquals("books ws/everyday-work/books none", eval("read(undefined)"));
        assertEquals("false false true true", eval("[WorkspaceAnchor.isWorkspace('a-heading'), WorkspaceAnchor.isWorkspace('wsx/books'),"
                + " WorkspaceAnchor.isWorkspace('ws'), WorkspaceAnchor.isWorkspace('ws/books')].join(' ')"));
    }

    @Test
    void aWorkspacesAnchorNamingNothingOfTheGroup_isTheDefault_andSaid() {
        assertEquals("books ws/everyday-work/books unknown", eval("read('ws/everyday-work/nothing')"));
        assertEquals("books ws/everyday-work/books unknown", eval("read('ws/on-this-bench/bench-one-pane')"), "another group's kind is that group's to show");
        assertEquals("books ws/everyday-work/books unknown", eval("read('ws')"));
        assertEquals("books ws/everyday-work/books unknown", eval("read('ws/%E0%A4%A')"), "a segment that does not decode is as it came");
        assertEquals("ws/everyday-work/nothing", eval("WorkspaceAnchor.read(studio, 'ws/everyday-work/nothing').asked"));
    }

    @Test
    void itReadsAGroup_andNothingElse() {
        assertThrows(PolyglotException.class, () -> eval("WorkspaceAnchor.read(null, 'ws/watching/monitors')"));
        assertEquals("bench-one-pane ws/on-this-bench/bench-one-pane none", eval("var r = WorkspaceAnchor.read(bench, ''); [r.kind, r.anchor, r.said].join(' ')"));
    }
}
