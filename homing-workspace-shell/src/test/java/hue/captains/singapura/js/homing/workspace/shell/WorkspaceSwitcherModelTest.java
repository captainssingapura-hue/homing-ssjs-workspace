package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The switcher's pure half: the kinds as a tree of sections, the instances as
 * a list, what a selection names and where a name sits, which rows may go,
 * and the address a choice navigates to — a kind change to the app's flat
 * address with ws_kind stamped, another instance by editing the current one,
 * the scoped parameters always cleared first.
 */
class WorkspaceSwitcherModelTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceSwitcherModel.js";

    // GraalVM JS has no WHATWG URL API: enough of URLSearchParams to back targetUrl, keeping the order it was given
    private static final String SHIM = """
        globalThis.URLSearchParams = class {
            constructor(s) { this._p = []; if (s) for (const kv of s.split('&')) { if (!kv) continue; const i = kv.indexOf('=');
                this._p.push(i < 0 ? [decodeURIComponent(kv), ''] : [decodeURIComponent(kv.slice(0, i)), decodeURIComponent(kv.slice(i + 1))]); } }
            delete(k) { this._p = this._p.filter(p => p[0] !== k); }
            set(k, v) { const i = this._p.findIndex(p => p[0] === k); if (i < 0) this._p.push([k, String(v)]); else { this._p[i][1] = String(v); this._p = this._p.filter((p, j) => j <= i || p[0] !== k); } }
            toString() { return this._p.map(p => encodeURIComponent(p[0]) + '=' + encodeURIComponent(p[1])).join('&'); }
        };
        var kinds = [
            { kind: 'demo',   title: 'Demo',   section: 'Examples', sectionSlug: 'ex' },
            { kind: 'notes',  title: 'Notes',  section: 'Writing' },
            { kind: 'charts' },
            { kind: 'plain',  title: 'Plain',  section: 'Examples', sectionSlug: 'ex' } ];
        var rows = [ { id: 'w1', name: 'Default', isDefault: true }, { id: 'w2', name: 'Mine' }, { id: 'w3' } ];
        function labels(t) { return t.children.map(function (g) { return g.segment + ':' + g.display.label + '[' + g.children.map(function (k) { return k.segment + (k.display.badge ? '*' : ''); }).join(',') + ']'; }).join(' '); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval("js", SHIM);
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void theKinds_asSectionsInTheOrderServed_theCurrentOneBadged() {
        assertEquals("ex:Examples[demo,plain*] writing:Writing[notes] workspaces:Workspaces[charts]", eval("labels(kindTreeData(kinds, 'plain'))").asString(),
                "a served slug is kept, else the name is slugged; a kind with no section goes under the default one");
        assertEquals("L0/ws/L1/L2/charts", eval("var t = kindTreeData(kinds, null); [t.level, t.segment, t.children[2].level, t.children[2].children[0].level, t.children[2].children[0].display.label].join('/')").asString(),
                "a kind with no title shows its id");
    }

    @Test
    void aSelectionNamesAKind_andAKindHasAPlace() {
        assertEquals("plain", eval("kindOfSelection({ kind: 'workspaceKind', namePath: 'ex/plain' })").asString());
        assertTrue(eval("kindOfSelection({ kind: 'section', namePath: 'ex' }) === null && kindOfSelection(null) === null").asBoolean(), "a section row names no kind");
        assertEquals("0,1|1,0|null", eval("[pathOfKind(kinds, 'plain'), pathOfKind(kinds, 'notes'), pathOfKind(kinds, 'nope')].map(String).join('|')").asString());
    }

    @Test
    void theInstances_asAList_theOpenOneAndTheDefaultBadged() {
        assertEquals("w1:Default:default w2:Mine:open w3:(unnamed):", eval("instanceListData(rows, 'w2').children.map(function (c) { return [c.segment, c.display.label, c.display.badge].join(':'); }).join(' ')").asString());
        assertEquals("0", eval("String(instanceListData(null, 'w2').children.length)").asString());
        assertEquals("w2", eval("instanceOfSelection({ kind: 'workspaceInstance', namePath: 'w2' })").asString());
        assertTrue(eval("instanceOfSelection({ kind: 'root' }) === null").asBoolean());
        assertEquals("1|null|null", eval("[pathOfInstance(rows, 'w2'), pathOfInstance(rows, 'nope'), pathOfInstance(null, 'w2')].map(String).join('|')").asString());
    }

    @Test
    void aRowMayGo_whenItIsNeitherTheDefaultNorTheOneOpen() {
        assertTrue(eval("canDelete(rows[2], 'w2')").asBoolean());
        assertFalse(eval("canDelete(rows[0], 'w2')").asBoolean(), "the default stays");
        assertFalse(eval("canDelete(rows[1], 'w2')").asBoolean(), "the one open stays");
        assertFalse(eval("canDelete(null, 'w2')").asBoolean());
    }

    @Test
    void aKindChange_goesToTheFlatAddress_withTheKindStamped_andTheScopedParamsCleared() {
        assertEquals("/ws?theme=dark&ws_kind=notes", eval("targetUrl('/ws/ex/demo?workspace=w1&slowmo=2', { kind: 'notes', currentKind: 'demo', base: '/ws?theme=dark' })").asString());
        assertEquals("/ws?ws_kind=notes&name=Scratch", eval("targetUrl('/x', { kind: 'notes', currentKind: 'demo', base: '/ws', name: 'Scratch' })").asString(), "a new instance, by name");
    }

    @Test
    void anotherInstance_editsTheCurrentAddress_andEveryOtherParamSurvives() {
        assertEquals("/ws/ex/demo?theme=dark&workspace=w3", eval("targetUrl('/ws/ex/demo?theme=dark&workspace=w1&slowmo=2', { kind: 'demo', currentKind: 'demo', base: '/ws', instanceId: 'w3' })").asString());
        assertEquals("/ws/ex/demo?name=New", eval("targetUrl('/ws/ex/demo?workspace=w1', { currentKind: 'demo', instanceId: 'w3', name: 'New' })").asString(), "never both: a name wins");
        assertEquals("/ws/ex/demo", eval("targetUrl('/ws/ex/demo?slowmo=1', { currentKind: 'demo' })").asString(), "nothing chosen: the scoped params cleared, and no stray ?");
    }
}
