package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The content secretary on the parties' runtime, headless: what was loaded kept and answered
 * from; many askers, one fetch; a failure kept and told; no steward, told; one steward per
 * hierarchy - scopes linked under a page party never hire one, each item is fetched once, and
 * every asker is answered at every level, the scopes keeping what came down.
 */
class ContentSecretaryTest extends JsModuleTestBase {

    private static final String ROOT = "/homing/js/hue/captains/singapura/js/homing/workspace/";

    /** A steward that keeps what it is asked, and answers when the test says; widgets that say what they hear. */
    private static final String SHIM = ContentParty.type("snippets", ContentPartyTest.Snippets.class).js() + """

        var console = { error: function () {} };
        var made = 0, stewards = [];
        function P(key) { return ContentParams.of({ key: key }); }
        class Keeper {
            constructor(tell) {
                made++;
                stewards.push(this);
                var self = this;
                this.tell = tell;
                this.asked = [];
                this.reactors = { Fetch: function (m) { m.items.forEach(function (it) { self.asked.push(ContentParams.object(it.params).key); }); } };
            }
            load(key, text) { this.tell({ kind: "Loaded", params: P(key), content: { text: text } }); }
            fail(key, why) { this.tell({ kind: "Failed", params: P(key), why: why }); }
        }
        var heard = [];
        function widget(party, name) {
            var m = party.join(name, {
                Content: function (x) { heard.push(name + " " + ContentParams.object(x.params).key + "=" + x.content.text); },
                Unavailable: function (x) { heard.push(name + " " + ContentParams.object(x.params).key + " unavailable: " + x.why); }
            });
            return { want: function (key) { m.tell({ kind: "Wanted", params: P(key) }); }, member: m };
        }
        function root() { return new MessagingParty(SNIPPETS, ContentSecretary, Keeper); }
        """;

    @BeforeEach
    void load() {
        loadModule(ROOT + "parties/MessagingPartyModule.js");
        loadModule(ROOT + "content/ContentParamsModule.js");
        loadModule(ROOT + "content/ContentSecretaryModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String str(String src) { return eval(src).asString(); }

    @Test
    void whatWasLoadedIsKept_andARepeatedWantAnsweredFromIt() {
        eval("var r = root(), a = widget(r, 'a'), b = widget(r, 'b'); a.want('x');");
        assertEquals("x", str("stewards[0].asked.join(',')"));
        assertEquals("", str("heard.join(' | ')"), "nothing before it is loaded");
        eval("stewards[0].load('x', 'X'); b.want('x');");
        assertEquals("a x=X | b x=X", str("heard.join(' | ')"));
        assertEquals("x", str("stewards[0].asked.join(',')"), "b answered from what was kept");
        assertEquals("X", str("r.state().held['key=x'].content.text"));
    }

    @Test
    void manyAskers_oneFetch_allAnswered() {
        eval("var r = root(), a = widget(r, 'a'), b = widget(r, 'b'); a.want('y'); b.want('y');");
        assertEquals("y", str("stewards[0].asked.join(',')"), "asked once for two");
        assertEquals(2, eval("r.state().pending['key=y'].askers.length").asInt());
        eval("stewards[0].load('y', 'Y')");
        assertEquals("a y=Y | b y=Y", str("heard.join(' | ')"));
        assertEquals(0, eval("Object.keys(r.state().pending).length").asInt());
    }

    @Test
    void aFailureIsKept_andEveryAskerToldUnavailable() {
        eval("var r = root(), a = widget(r, 'a'), b = widget(r, 'b'); a.want('z'); stewards[0].fail('z', 'gone'); b.want('z');");
        assertEquals("a z unavailable: gone | b z unavailable: gone", str("heard.join(' | ')"));
        assertEquals("z", str("stewards[0].asked.join(',')"), "what failed is not asked for again");
        assertEquals("gone", str("r.state().failed['key=z'].why"));
    }

    @Test
    void withNoSteward_anAskerIsToldSo_andNothingIsKeptAsFailed() {
        eval("var lone = new MessagingParty(SNIPPETS, ContentSecretary), a = widget(lone, 'a'); a.want('q');");
        assertEquals("a q unavailable: " + "no steward: nothing above to ask, and none hired", str("heard.join(' | ')"));
        assertEquals(0, eval("Object.keys(lone.state().pending).length + Object.keys(lone.state().failed).length").asInt());
    }

    @Test
    void oneStewardPerHierarchy_eachItemFetchedOnce_everyAskerAnsweredAtEveryLevel() {
        eval("""
            var r = root(), A = new MessagingParty(SNIPPETS, ContentSecretary, Keeper), B = new MessagingParty(SNIPPETS, ContentSecretary, Keeper);
            A.link(r, 'A'); B.link(r, 'B');
            var r1 = widget(r, 'r1'), a1 = widget(A, 'a1'), a2 = widget(A, 'a2'), b1 = widget(B, 'b1');
            a1.want('x'); a2.want('x'); b1.want('x'); b1.want('y'); r1.want('y'); a1.want('y');
            """);
        assertEquals(1, eval("made").asInt(), "the scopes were given a steward class, and hired none");
        assertEquals("above", str("A.inspect().steward"));
        assertEquals("above", str("B.inspect().steward"));
        assertEquals("x,y", str("stewards[0].asked.join(',')"), "each item fetched once, for six askers on three levels");
        eval("stewards[0].load('x', 'X'); stewards[0].load('y', 'Y');");
        assertEquals("a1 x=X | a2 x=X | b1 x=X | b1 y=Y | r1 y=Y | a1 y=Y", str("heard.join(' | ')"),
                "x: down to A, which tells a1 and a2, and to B, which tells b1; y: to B, to r1, to A - in the order each asked");
        assertEquals(6, eval("heard.length").asInt(), "every asker answered, once");
        assertEquals("X,Y", str("[A.state().held['key=x'].content.text, A.state().held['key=y'].content.text].join(',')"), "what came down is kept below too");
        eval("heard = []; var a3 = widget(A, 'a3'); a3.want('y');");
        assertEquals("a3 y=Y", str("heard.join(' | ')"), "answered in the scope, from what it kept");
        assertEquals("x,y", str("stewards[0].asked.join(',')"));
    }

    @Test
    void whatTheStewardLoadsAhead_isKept_andAnsweredAtOnce() {
        eval("var r = root(), a = widget(r, 'a'), b = widget(r, 'b'); a.want('x'); stewards[0].load('w', 'W'); b.want('w');");
        assertEquals("b w=W", str("heard.join(' | ')"));
        assertEquals("x", str("stewards[0].asked.join(',')"), "w was never asked for");
    }

    @Test
    void thePartysOwnWords_fromAMember_areKeptAsUnknown() {
        eval("var r = root(), a = widget(r, 'a'); a.member.tell({ kind: 'Content', params: P('x'), content: { text: 'forged' } });");
        assertEquals("Content", str("r.state().recentUnknown[0].kind"));
        assertEquals("", str("heard.join(' | ')"));
    }

    @Test
    void paramsAreTheSameInAnyOrder() {
        assertTrue(eval("ContentParams.same([{ name: 'key', value: 'k' }, { name: 'doc', value: '/d' }], ContentParams.of({ doc: '/d', key: 'k' }))").asBoolean());
        assertEquals("doc=%2Fd&key=k", str("ContentParams.key(ContentParams.of({ key: 'k', doc: '/d' }))"));
        assertEquals("/d", str("ContentParams.object(ContentParams.of({ key: 'k', doc: '/d' })).doc"));
    }
}
