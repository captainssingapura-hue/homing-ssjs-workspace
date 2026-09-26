package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.workspace.events.contract.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.Axis;
import hue.captains.singapura.js.homing.workspace.log.FloatId;
import hue.captains.singapura.js.homing.workspace.log.FloatState;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.RegionState;
import hue.captains.singapura.js.homing.workspace.log.TabState;
import hue.captains.singapura.js.homing.workspace.log.Track;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionId;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.Side;
import hue.captains.singapura.js.homing.workspace.log.TabId;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceSpecKind;
import hue.captains.singapura.js.homing.workspace.state.SplitPath;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Every type on the workspace log's wire, each with its generators: the one
 * list both languages' codecs are generated from. A sealed type comes before
 * its variants, since a JavaScript class extends only what is already there.
 */
public final class WorkspaceLogManifest {

    private WorkspaceLogManifest() {}

    public static final List<LogCodecEntry<?>> ENTRIES = List.of(
            // identifiers: one scalar each, bare on the wire
            LogCodecEntry.id(TabId.class),
            LogCodecEntry.id(RegionId.class),
            LogCodecEntry.id(FloatId.class),
            LogCodecEntry.id(WidgetKind.class),
            LogCodecEntry.id(WidgetTitle.class),
            LogCodecEntry.id(SplitPath.class),
            LogCodecEntry.id(EventSeq.class),
            LogCodecEntry.id(WorkspaceSpecKind.class),
            LogCodecEntry.id(WorkspaceInstanceId.class),
            // values
            LogCodecEntry.enumeration(Side.class),
            LogCodecEntry.record(Scaled.class),
            // where a tab is: a region's dock or a float
            LogCodecEntry.sealed(Host.class),
            LogCodecEntry.record(Host.InRegion.class),
            LogCodecEntry.record(Host.InFloat.class),
            // the events: the family, then each of its variants
            LogCodecEntry.sealed(WorkspaceEvent.class),
            LogCodecEntry.record(WorkspaceEvent.TabOpened.class),
            LogCodecEntry.record(WorkspaceEvent.TabBecame.class),
            LogCodecEntry.record(WorkspaceEvent.TabRenamed.class),
            LogCodecEntry.record(WorkspaceEvent.TabMoved.class),
            LogCodecEntry.record(WorkspaceEvent.TabShown.class),
            LogCodecEntry.record(WorkspaceEvent.TabClosed.class),
            LogCodecEntry.record(WorkspaceEvent.RegionParted.class),
            LogCodecEntry.record(WorkspaceEvent.RegionRemoved.class),
            LogCodecEntry.record(WorkspaceEvent.TracksChanged.class),
            LogCodecEntry.record(WorkspaceEvent.FloatOpened.class),
            LogCodecEntry.record(WorkspaceEvent.FloatMoved.class),
            LogCodecEntry.record(WorkspaceEvent.FloatResized.class),
            LogCodecEntry.record(WorkspaceEvent.FloatRaised.class),
            LogCodecEntry.record(WorkspaceEvent.FloatClosed.class),
            // the lines of a log
            LogCodecEntry.record(LoggedEvent.class),
            LogCodecEntry.record(LogHeader.class),
            // the state a log folds to: the layout, the hosts, the tabs; and the file that carries it
            LogCodecEntry.enumeration(Axis.class),
            LogCodecEntry.sealed(Layout.class),
            LogCodecEntry.record(Layout.Cell.class),
            LogCodecEntry.record(Layout.Split.class),
            LogCodecEntry.record(Track.class),
            LogCodecEntry.record(TabState.class),
            LogCodecEntry.record(RegionState.class),
            LogCodecEntry.record(FloatState.class),
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
