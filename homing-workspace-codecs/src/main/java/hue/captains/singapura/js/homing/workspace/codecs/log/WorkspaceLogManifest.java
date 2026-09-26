package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.workspace.log.FloatEvent;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LogIds;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.SetAsideLog;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Every type on the workspace log's wire, each with its generators: the one
 * list both languages' codecs are generated from. Listed file by file, since a
 * file is a JavaScript module ({@link LogModules}); within a file a sealed type
 * comes before its variants, since a JavaScript class extends only what is
 * already there.
 */
public final class WorkspaceLogManifest {

    private WorkspaceLogManifest() {}

    public static final List<LogCodecEntry<?>> ENTRIES = List.of(
            // LogIds: identifiers and names, one scalar each, bare on the wire
            LogCodecEntry.id(LogIds.TabId.class),
            LogCodecEntry.id(LogIds.RegionId.class),
            LogCodecEntry.id(LogIds.FloatId.class),
            LogCodecEntry.id(LogIds.WidgetKind.class),
            LogCodecEntry.id(LogIds.WidgetTitle.class),
            LogCodecEntry.id(LogIds.SplitPath.class),
            LogCodecEntry.id(LogIds.EventSeq.class),
            LogCodecEntry.id(LogIds.WorkspaceKind.class),
            LogCodecEntry.id(LogIds.WorkspaceInstanceId.class),
            // an exact decimal
            LogCodecEntry.record(Scaled.class),
            // where a tab is: a region's dock or a float
            LogCodecEntry.sealed(Host.class),
            LogCodecEntry.record(Host.InRegion.class),
            LogCodecEntry.record(Host.InFloat.class),
            // the regions as a tree: recursive, so one file
            LogCodecEntry.enumeration(Layout.Axis.class),
            LogCodecEntry.sealed(Layout.class),
            LogCodecEntry.record(Layout.Cell.class),
            LogCodecEntry.record(Layout.Split.class),
            LogCodecEntry.record(Layout.Track.class),
            // the events, a family of three families: what happens to a tab, a region, a float
            LogCodecEntry.sealed(TabEvent.class),
            LogCodecEntry.record(TabEvent.TabOpened.class),
            LogCodecEntry.record(TabEvent.TabBecame.class),
            LogCodecEntry.record(TabEvent.TabRenamed.class),
            LogCodecEntry.record(TabEvent.TabMoved.class),
            LogCodecEntry.record(TabEvent.TabShown.class),
            LogCodecEntry.record(TabEvent.TabClosed.class),
            LogCodecEntry.enumeration(RegionEvent.Side.class),
            LogCodecEntry.sealed(RegionEvent.class),
            LogCodecEntry.record(RegionEvent.RegionParted.class),
            LogCodecEntry.record(RegionEvent.RegionRemoved.class),
            LogCodecEntry.record(RegionEvent.TracksChanged.class),
            LogCodecEntry.sealed(FloatEvent.class),
            LogCodecEntry.record(FloatEvent.FloatOpened.class),
            LogCodecEntry.record(FloatEvent.FloatMoved.class),
            LogCodecEntry.record(FloatEvent.FloatResized.class),
            LogCodecEntry.record(FloatEvent.FloatRaised.class),
            LogCodecEntry.record(FloatEvent.FloatClosed.class),
            LogCodecEntry.sealed(WorkspaceEvent.class),
            // the lines of a log
            LogCodecEntry.record(LoggedEvent.class),
            LogCodecEntry.record(LogHeader.class),
            // a stored log the page could not read, kept as it was
            LogCodecEntry.record(SetAsideLog.class),
            // the state a log folds to, and the file that carries it
            LogCodecEntry.record(WorkspaceState.TabState.class),
            LogCodecEntry.record(WorkspaceState.RegionState.class),
            LogCodecEntry.record(WorkspaceState.FloatState.class),
            LogCodecEntry.record(WorkspaceState.class),
            LogCodecEntry.record(FoldedState.class));

    /**
     * The manifest holds together, or the build stops here: one name per type,
     * every type a component refers to listed, every variant of a listed sealed
     * type listed after it, and every listed variant's family listed.
     */
    public static void check(List<LogCodecEntry<?>> entries) {
        var names = new HashMap<String, Class<?>>();
        var seen = new HashSet<Class<?>>();
        var problems = new ArrayList<String>();
        for (var e : entries) {
            Class<?> t = e.type();
            Class<?> clash = names.put(t.getSimpleName(), t);
            if (clash != null) problems.add(t.getSimpleName() + " names both " + clash.getName() + " and " + t.getName());
            Class<?> parent = t.isRecord() ? LogShapes.sealedParent(t) : null;
            if (parent != null && !seen.contains(parent)) problems.add(t.getName() + " comes before its family " + parent.getName());
            seen.add(t);
        }
        for (var e : entries) {
            Class<?> t = e.type();
            var refs = new ArrayList<Class<?>>();
            if (t.isRecord()) for (var c : LogShapes.components(t)) LogSlot.typesIn(c.slot(), refs);
            if (t.isInterface() && t.isSealed()) refs.addAll(SumCodeGen.variants(t));
            for (Class<?> r : refs) if (!seen.contains(r)) problems.add(t.getName() + " refers to " + r.getName() + ", which is not listed");
        }
        if (!problems.isEmpty()) throw new IllegalStateException("the workspace log manifest does not hold together:\n  " + String.join("\n  ", problems));
    }
}
