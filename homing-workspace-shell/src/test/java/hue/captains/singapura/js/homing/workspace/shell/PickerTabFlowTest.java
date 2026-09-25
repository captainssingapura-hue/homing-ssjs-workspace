package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for {@code PickerTabFlow}. Exercises:
 *
 * <ul>
 *   <li>every declared entry reaches the picker (RFC 0060 D20)</li>
 *   <li>{@code openInSlot} — synchronous side effects on stub MTP +
 *       stub WidgetPicker constructor (records args)</li>
 *   <li>{@code onCancel} → {@code mtp.removeTab}</li>
 * </ul>
 *
 * <p>onPick → mutate flow is largely integration territory (involves
 * dynamic import via WidgetMounter); covered by the live browser path.</p>
 *
 * <p>Style: full JS literal fixtures.</p>
 */
class PickerTabFlowTest extends JsModuleTestBase {

    private static final String STUBS = """
            class StubMtp {
                constructor() {
                    this.addedTabs       = [];
                    this.removedTabs     = [];
                    this.switchedTo      = [];
                    this.landed          = [];
                    this.retitled        = [];
                    this.iconed          = [];
                    this.workspaceActiveTabId = null;
                    this._tabsBySlot     = new Map();
                    this._named          = 0;
                }
                // A tab opened in a pane, as the desk opens one: named by the
                // register, and its widget a room - the pane half of a tab-pane,
                // which the flow puts a widget in; this one records what it was given.
                openTab(slotId, tab) {
                    const branch = new StubBranch('room');
                    tab.id = 'tab-' + (++this._named);
                    tab.widget = {
                        slotId, branch, widgets: [], said: [], active: null,
                        root: { children: [] }, focus: { join() {} },
                        _host: Object.freeze({ title() {}, parties: {} }),
                        host() { return this._host; },
                        branchFor(name) { return branch.createBranch(name); },
                        setWidget(w) { this.widgets.push(w); return this; },
                        say(t) { this.said.push(t); return this; },
                        widget() { return this.widgets[this.widgets.length - 1] || null; },
                        setActive(on) { this.active = on; }
                    };
                    if (!this._tabsBySlot.has(slotId)) this._tabsBySlot.set(slotId, { tabs: [] });
                    this._tabsBySlot.get(slotId).tabs.push(tab);
                    this.addedTabs.push({ slotId, tab });
                    return tab.widget;
                }
                tabOf(tabId) {
                    for (const s of this._tabsBySlot.values()) for (const t of s.tabs) if (t.id === tabId) return t;
                    return null;
                }
                removeTab(slotId, tabId) {
                    this.removedTabs.push({ slotId, tabId });
                    const s = this._tabsBySlot.get(slotId);
                    if (s) s.tabs = s.tabs.filter(t => t.id !== tabId);
                }
                switchTab(slotId, tabId) { this.switchedTo.push({ slotId, tabId }); }
                retitle(tabId, title) { this.retitled.push({ tabId, title }); }
                setIcon(tabId, icon) { this.iconed.push({ tabId, icon }); }
                land(slotId, tabId) { this.landed.push({ slotId, tabId }); }
                activeTabOf(slotId) {
                    const s = this._tabsBySlot.get(slotId);
                    return s && s.tabs.length ? s.tabs[s.tabs.length - 1].id : null;
                }
                getWorkspaceActiveTab() { return this.workspaceActiveTabId; }
                setWorkspaceActiveTab(id) { this.workspaceActiveTabId = id; }
                getState() {
                    const tabs = {};
                    this._tabsBySlot.forEach((v, k) => {
                        tabs[k] = { tabs: v.tabs.map(t => ({ id: t.id, title: t.title })) };
                    });
                    return { tabs };
                }
            }
            class StubBranch {
                constructor(name) {
                    this.name = name;
                    this.children = [];
                }
                createBranch(name) { const c = new StubBranch(name); this.children.push(c); return c; }
                activate() {}
                createElement(name, tag) { return { name, tag, textContent: '', children: [], appendChild(c) { this.children.push(c); } }; }
                dissolve() {}
            }
            globalThis.document = {
                createElement(tag) {
                    return { tag, style: { cssText: '' }, textContent: '',
                             children: [], appendChild(c) { this.children.push(c); } };
                }
            };
            // Stub WidgetPicker that records its constructor opts so tests
            // can verify entries / disabledIds + invoke onPick/onCancel.
            globalThis._lastPicker = null;
            // The picker is a branch component now: its own branch first, its
            // options second, as every component in this stack takes them.
            class StubPicker {
                constructor(branch, opts) {
                    this.branch = branch;
                    this.opts = opts;
                    this.mountedInto = null;
                    globalThis._lastPicker = this;
                }
                mountInto(host) { this.mountedInto = host; }
                dispose() {}
            }
            globalThis.StubPicker = StubPicker;
            """;

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PickerTabFlowModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /**
     * RFC 0060 D20 — the picker offers every declared widget. It used to filter
     * out {@code spec.pinnedSpawns}, which meant closing an auto-spawned tab made
     * that widget unreachable for good. Seeding is now the arrangement's job and
     * says nothing about pickability; whether a second copy may be opened is the
     * widget's own {@code lifecycleHint()} to declare.
     */
    @Test
    void pickerOffersEveryDeclaredEntryIncludingSeededOnes() {
        Value setup = newPickerFlow();
        setup.getMember("flow").invokeMember("openInSlot", "tl");

        Value offered = js.eval("js", "globalThis._lastPicker.opts.entries");
        assertEquals(2, offered.getArraySize(),
                     "both declared entries reach the picker");
        assertEquals("DocViewWidget",
                     offered.getArrayElement(0).getMember("simpleName").asString(),
                     "the seeded widget is still offered — closing its tab must not "
                   + "make it unreachable");
        assertEquals("Spinning",
                     offered.getArrayElement(1).getMember("simpleName").asString());
    }

    @Test
    void openInSlotAddsPickerTabAndMountsPicker() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        Value mtp  = setup.getMember("mtp");

        Value tabId = flow.invokeMember("openInSlot", "tr");

        assertEquals("tab-1", tabId.asString());
        // Tab was added to slot 'tr'.
        Value addedTabs = mtp.getMember("addedTabs");
        assertEquals(1, addedTabs.getArraySize());
        assertEquals("tr", addedTabs.getArrayElement(0).getMember("slotId").asString());
        assertEquals("tab-1",
                     addedTabs.getArrayElement(0).getMember("tab").getMember("id").asString());
        // The pane was told to rest the keys in the tab it is showing — what
        // the coordinator's deep-select used to mean, said by the party.
        Value landed = mtp.getMember("landed");
        assertEquals(1, landed.getArraySize());
        assertEquals("tr", landed.getArrayElement(0).getMember("slotId").asString());
        // switchTab fired too.
        Value switched = mtp.getMember("switchedTo");
        assertEquals(1, switched.getArraySize());
        assertEquals("tab-1", switched.getArrayElement(0).getMember("tabId").asString());
    }

    @Test
    void pickerReceivesEveryEntryAndNoDisabledIds() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "tl");

        Value picker = js.getBindings("js").getMember("_lastPicker");
        Value opts   = picker.getMember("opts");
        // Both declared entries — nothing is filtered any more (D20).
        assertEquals(2, opts.getMember("entries").getArraySize());
        // disabledIds is empty in this fresh setup (no singletons open). This,
        // not the removed filter, is how a kind gets held to one instance.
        assertEquals(0, opts.getMember("disabledIds").getMemberKeys().size());
        // Picker was mounted into the pickerHost (not the tab's contentEl
        // directly — into a host the tab.render attaches; observable via
        // picker.mountedInto being non-null).
        assertTrue(!picker.getMember("mountedInto").isNull());
    }

    /**
     * The chooser picked: the SAME tab takes the kind's name and icon at once,
     * by the tab-pane's own calls - the widget may name itself later, through
     * its host - and the widget is mounted with the room's host.
     */
    @Test
    void aPickNamesTheTabAfterTheKindWithItsIcon_andTheWidgetGetsTheRoomsHost() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        String tabId = flow.invokeMember("openInSlot", "tl").asString();
        js.eval("js", "_lastPicker.opts.onPick(_lastPicker.opts.entries[0], {})");
        Value mtp = setup.getMember("mtp");
        assertEquals(1, mtp.getMember("retitled").getArraySize());
        assertEquals(tabId, mtp.getMember("retitled").getArrayElement(0).getMember("tabId").asString());
        assertEquals("Doc", mtp.getMember("retitled").getArrayElement(0).getMember("title").asString());
        assertEquals("D", mtp.getMember("iconed").getArrayElement(0).getMember("icon").getMember("value").asString(),
                "the kind's icon, as a site's favicon");
        // The mount is past the flow's one async boundary; the jobs run when the eval above returns.
        Value hosts = setup.getMember("flow").getMember("_mounter").getMember("hosts");
        assertEquals(1, hosts.getArraySize(), "the widget was mounted");
        Value room = mtp.getMember("addedTabs").getArrayElement(0).getMember("tab").getMember("widget");
        assertTrue(hosts.getArrayElement(0).equals(room.invokeMember("host")), "with its room's host, and no other");
    }

    @Test
    void onCancelRemovesTheTab() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "br");

        // Trigger cancel through the picker's recorded opts.
        Value picker = js.getBindings("js").getMember("_lastPicker");
        picker.getMember("opts").getMember("onCancel").execute();

        Value removed = setup.getMember("mtp").getMember("removedTabs");
        assertEquals(1, removed.getArraySize());
        assertEquals("br",      removed.getArrayElement(0).getMember("slotId").asString());
        assertEquals("tab-1", removed.getArrayElement(0).getMember("tabId").asString());
    }

    @Test
    void inspectReportsState() {
        Value setup = newPickerFlow();
        Value flow = setup.getMember("flow");
        flow.invokeMember("openInSlot", "tl");
        flow.invokeMember("openInSlot", "tr");

        Value snap = flow.invokeMember("inspect");
        // What is LIVE, asked of the registry, rather than a tally kept on the side.
        assertEquals(0, snap.getMember("singletons").getMemberKeys().size());
        assertEquals(2, snap.getMember("tabsIssued").asInt());
    }

    /** Build a fresh PickerTabFlow + stub MTP. Spec: 2 declared entries. */
    private Value newPickerFlow() {
        return js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('widgets');
                    const spec = {
                        entries: [
                            { simpleName: 'DocViewWidget', moduleUrl: '/dvw', label: 'Doc', icon: { kind: 'emoji', value: 'D' } },
                            { simpleName: 'Spinning',      moduleUrl: '/s',   label: 'Spin' }
                        ]
                    };
                    const stubMounter = {
                        resolveCalls: [],
                        resolve: function (e) { this.resolveCalls.push(e);
                                                return Promise.resolve({ construct: () => ({ root:{}, setActive:()=>{} }) }); },
                        hosts:   [],
                        mount:   function (mod, b, e, p, host) { this.hosts.push(host); return mod.construct(b, {}, host); },
                        attach:  function (c, tab) { tab.controller = c; }
                    };
                    // The pane rests the keys itself now; nothing sits above it.
                    // a spy stands in for it.
                    const flow = new PickerTabFlow({
                        mtp, widgetsBranch: wB, spec,
                        WidgetPickerCtor: StubPicker, mounter: stubMounter
                    });
                    return { mtp, wB, spec, flow };
                })()""");
    }
}
