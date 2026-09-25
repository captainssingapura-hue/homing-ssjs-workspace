package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.events.contract.WorkspaceEventPayload;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two event vocabularies, held together.
 *
 * <p>The components' vocabularies each say a test does this, and the
 * workspace's did not have one — which is how {@code SplitRatioChanged} came to
 * be emitted by the JS and folded by the model for two RFCs with no variant in
 * the sealed sum to answer for it. A vocabulary that calls itself centralised
 * and typo-safe needs something that fails when it stops being either.</p>
 */
class WorkspaceEventsTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceEventsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value translate(String componentEventJs) {
        return global("WorkspaceEvents").invokeMember("of", js.eval("js", componentEventJs));
    }

    /** JS null crosses the boundary as a Value that IS null, never as a Java null. */
    private static void assertNothing(Value out, String why) {
        assertTrue(out == null || out.isNull(), why + " — got " + out);
    }

    private static String nameOf(Value out) { return out.getMember("name").asString(); }
    private static Value payloadOf(Value out) { return out.getMember("payload"); }

    // ── every name it mints is a name the sealed sum knows ──────────────────

    /**
     * The guard the workspace's vocabulary never had. Every name this can
     * produce must be a variant of {@link WorkspaceEventPayload}; a typo, or a
     * name invented in JS and never declared, fails here.
     */
    @Test
    void everyNameItMintsIsDeclaredInJava() {
        var declared = new TreeSet<String>();
        for (Class<?> c : WorkspaceEventPayload.class.getPermittedSubclasses()) {
            declared.add(c.getSimpleName());
        }

        var minted = new TreeSet<String>();
        for (String js : new String[]{
                "({kind:'TabRemoved',   slotId:'p', tab:{id:'t', widgetInstanceUuid:'u', widgetKind:'K'}, fromIndex:0})",
                "({kind:'TabMoved',     srcSlotId:'a', tab:{id:'t', widgetInstanceUuid:'u'}, srcIndex:0, destSlotId:'b', destIndex:1})",
                "({kind:'TabActivated', slotId:'p', tabId:'t', tab:{id:'t', widgetInstanceUuid:'u'}})",
                "({kind:'Subdivided',   cellId:'p', newCellId:'q', side:'right'})",
                "({kind:'Removed',      cellId:'p'})",
                "({kind:'TracksChanged', path:'', ratios:[0.5, 0.5]})"}) {
            minted.add(nameOf(translate(js)));
        }

        assertTrue(declared.containsAll(minted),
                "names minted in JS with no variant in the sealed sum: "
              + new TreeSet<>(minted) { { removeAll(declared); } });
        assertEquals(new TreeSet<>(Arrays.asList(
                        "SplitCreated", "SplitMerged", "TabActivated", "TabClosed",
                        "TabMoved", "TracksChanged")),
                minted, "the set it translates to — change this when the mapping changes");
    }

    /** KINDS says what it answers for, and it answers for exactly those. */
    @Test
    void itAnswersForTheKindsItClaims() {
        Value kinds = global("WorkspaceEvents").getMember("KINDS");
        Set<String> claimed = new LinkedHashSet<>();
        for (long i = 0; i < kinds.getArraySize(); i++) claimed.add(kinds.getArrayElement(i).asString());
        assertEquals(Set.of("TabRemoved", "TabMoved", "TabActivated",
                            "Subdivided", "Removed", "TracksChanged"), claimed);
    }

    // ── what it declines, and that declining is silent ──────────────────────

    @Test
    void theKindsTheWorkspaceKeepsNoRecordOfAreNull() {
        assertNothing(translate("({kind:'AddRequested', slotId:'p'})"),
                "a question, not a mutation");
        assertNothing(translate("({kind:'TabAdded', slotId:'p', tab:{id:'t', widgetInstanceUuid:'u'}, index:0})"),
                "the holder authored the spawn and names it picker or pinned");
        assertNothing(translate("({kind:'CursorMoved', cellId:'p', by:'pointer'})"),
                "live state");
        assertNothing(translate("({kind:'DetachRequested', slotId:'p', tabId:'t'})"), "no workspace record");
    }

    /** A component may grow a kind this does not know; a log that refuses to be written is worse. */
    @Test
    void anUnknownKindIsNullAndNotAThrow() {
        assertNothing(translate("({kind:'SomethingNewNextYear', slotId:'p'})"), "no workspace record");
        assertNothing(translate("({})"), "no workspace record");
        assertNothing(translate("null"), "no workspace record");
    }

    // ── the translations themselves ─────────────────────────────────────────

    @Test
    void aClosedTabCarriesItsWidgetAndWhereItWas() {
        Value out = translate("({kind:'TabRemoved', slotId:'left', tab:{id:'t1', widgetInstanceUuid:'u1', widgetKind:'Graph'}, fromIndex:2})");
        assertEquals("TabClosed", nameOf(out));
        assertEquals("u1",    payloadOf(out).getMember("widgetInstanceId").asString());
        assertEquals("Graph", payloadOf(out).getMember("widgetKind").asString());
        assertEquals("left",  payloadOf(out).getMember("from").getMember("paneId").asString());
        assertEquals(2,       payloadOf(out).getMember("from").getMember("tabIndex").asInt());
    }

    @Test
    void aMoveNamesBothEnds() {
        Value out = translate("({kind:'TabMoved', srcSlotId:'a', tab:{id:'t', widgetInstanceUuid:'u'}, srcIndex:0, destSlotId:'b', destIndex:3})");
        assertEquals("TabMoved", nameOf(out));
        assertEquals("a", payloadOf(out).getMember("from").getMember("paneId").asString());
        assertEquals("b", payloadOf(out).getMember("to").getMember("paneId").asString());
        assertEquals(3,   payloadOf(out).getMember("to").getMember("tabIndex").asInt());
    }

    @Test
    void aTabWithNoWidgetOnItIsNotAWorkspaceEvent() {
        assertNothing(translate("({kind:'TabRemoved', slotId:'p', tab:{id:'t'}, fromIndex:0})"),
                "a tab the workspace did not put there is not its business");
        assertNothing(translate("({kind:'TabMoved', srcSlotId:'a', tab:{id:'t'}, srcIndex:0, destSlotId:'b', destIndex:0})"), "no workspace record");
    }

    @Test
    void aSubdivisionNamesTheNewPaneAndTheSide() {
        Value out = translate("({kind:'Subdivided', cellId:'left', newCellId:'mid', side:'right'})");
        assertEquals("SplitCreated", nameOf(out));
        assertEquals("left",  payloadOf(out).getMember("paneId").asString());
        assertEquals("mid",   payloadOf(out).getMember("newPaneId").asString());
        assertEquals("right", payloadOf(out).getMember("side").asString());
    }

    @Test
    void aSideTheGridDoesNotHaveIsRefused() {
        assertNothing(translate("({kind:'Subdivided', cellId:'a', newCellId:'b', side:'sideways'})"), "no workspace record");
    }

    @Test
    void aRemovedCellIsTheMergedPaneAndTowardIsOptional() {
        Value bare = translate("({kind:'Removed', cellId:'gone'})");
        assertEquals("SplitMerged", nameOf(bare));
        assertEquals("gone", payloadOf(bare).getMember("paneId").asString());
        assertTrue(payloadOf(bare).getMember("toward").isNull());

        Value toward = translate("({kind:'Removed', cellId:'gone', toward:'heir'})");
        assertEquals("heir", payloadOf(toward).getMember("toward").asString());
    }

    @Test
    void tracksCrossUnchanged() {
        Value out = translate("({kind:'TracksChanged', path:'0/1', ratios:[0.25, 0.25, 0.5]})");
        assertEquals("TracksChanged", nameOf(out));
        assertEquals("0/1", payloadOf(out).getMember("path").asString());
        assertEquals(3, payloadOf(out).getMember("ratios").getArraySize());
        assertEquals(0.5, payloadOf(out).getMember("ratios").getArrayElement(2).asDouble(), 1e-9);
    }

    /**
     * An activation is recorded by the WIDGET the tab holds, never by the
     * dock's id for the tab: the two differ (a chooser's tab keeps its id when
     * it becomes a widget, and a tab id must be a branch name, which a widget's
     * id is not), and only the widget's means anything on the next visit.
     */
    @Test
    void anActivationIsRecordedByTheWidgetNotTheTabId() {
        Value out = translate("({kind:'TabActivated', slotId:'right', tabId:'picker-3', tab:{id:'picker-3', widgetInstanceUuid:'Note:7'}})");
        assertEquals("TabActivated", nameOf(out));
        assertEquals("right",  payloadOf(out).getMember("paneId").asString());
        assertEquals("Note:7", payloadOf(out).getMember("widgetInstanceId").asString());
    }

    /**
     * A merge is one fact. The tabs a pane carries across as it goes, and what
     * each dock shows after, are marked as part of it and not recorded: the
     * merge is, and its replay moves the tabs the same way.
     */
    @Test
    void whatAMergeCarriesAcrossIsPartOfTheMerge() {
        assertNothing(translate("({kind:'TabMoved', srcSlotId:'b', tab:{id:'t', widgetInstanceUuid:'u'}, srcIndex:0, destSlotId:'a', destIndex:2, merging:'b'})"), "part of the merge");
        assertNothing(translate("({kind:'TabActivated', slotId:'a', tabId:'t', tab:{id:'t', widgetInstanceUuid:'u'}, merging:'b'})"), "part of the merge");
        Value merged = translate("({kind:'Removed', cellId:'b', toward:'a'})");
        assertEquals("SplitMerged", nameOf(merged));
        assertEquals("a", payloadOf(merged).getMember("toward").asString());
    }

    /** A pane showing a chooser, or a tab the holder never handed along, is showing nothing to come back to. */
    @Test
    void anActivationOfATabWithNoWidgetIsNotRecorded() {
        assertNothing(translate("({kind:'TabActivated', slotId:'p', tabId:'picker-1', tab:{id:'picker-1'}})"), "a chooser");
        assertNothing(translate("({kind:'TabActivated', slotId:'p', tabId:'t', tab:null})"), "no tab handed along");
        assertNothing(translate("({kind:'TabActivated', slotId:'p', tabId:'t'})"), "no tab handed along");
    }
}
