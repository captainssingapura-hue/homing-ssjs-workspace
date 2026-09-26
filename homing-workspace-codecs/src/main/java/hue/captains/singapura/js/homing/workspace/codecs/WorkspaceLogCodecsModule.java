package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The served module of the workspace log's generated JavaScript: every type on
 * the log's wire, as a class and a codec, generated from the Java declarations
 * by {@code WorkspaceLogJsGen} into {@code target/classes} at build. The
 * exports below name what the generator emits — a class per manifest type and
 * {@code <Type>Codec} beside it; {@code WorkspaceLogCodecsModuleTest} holds the
 * two lists together.
 */
public record WorkspaceLogCodecsModule() implements DomModule<WorkspaceLogCodecsModule> {

    public static final WorkspaceLogCodecsModule INSTANCE = new WorkspaceLogCodecsModule();

    // identifiers
    public record TabId()               implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabIdCodec()          implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionId()            implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionIdCodec()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatId() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatIdCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WidgetKind()          implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WidgetKindCodec()     implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WidgetTitle()         implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WidgetTitleCodec()    implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record SplitPath()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record SplitPathCodec()      implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record EventSeq()            implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record EventSeqCodec()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WorkspaceSpecKind()   implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WorkspaceSpecKindCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WorkspaceInstanceId() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WorkspaceInstanceIdCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    // values
    public record Side()                implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record SideCodec()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record Scaled()              implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record ScaledCodec()         implements Exportable._Class<WorkspaceLogCodecsModule> {}
    // where a tab is
    public record Host() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record HostCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record InRegion() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record InRegionCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record InFloat() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record InFloatCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    // the events
    public record WorkspaceEvent()      implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record WorkspaceEventCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabOpened()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabOpenedCodec()      implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabBecame()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabBecameCodec()      implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabRenamed() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabRenamedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabMoved()            implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabMovedCodec()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabShown()            implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabShownCodec()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabClosed()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TabClosedCodec()      implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionParted()        implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionPartedCodec()   implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionRemoved()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record RegionRemovedCodec()  implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TracksChanged()       implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record TracksChangedCodec()  implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatOpened() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatOpenedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatMoved() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatMovedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatResized() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatResizedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatRaised() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatRaisedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatClosed() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record FloatClosedCodec() implements Exportable._Class<WorkspaceLogCodecsModule> {}
    // the lines of a log
    public record LoggedEvent()         implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record LoggedEventCodec()    implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record LogHeader()           implements Exportable._Class<WorkspaceLogCodecsModule> {}
    public record LogHeaderCodec()      implements Exportable._Class<WorkspaceLogCodecsModule> {}

    @Override
    public ImportsFor<WorkspaceLogCodecsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceLogCodecsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new TabId(),               new TabIdCodec(),
                new RegionId(),            new RegionIdCodec(),
                new FloatId(),             new FloatIdCodec(),
                new WidgetKind(),          new WidgetKindCodec(),
                new WidgetTitle(),         new WidgetTitleCodec(),
                new SplitPath(),           new SplitPathCodec(),
                new EventSeq(),            new EventSeqCodec(),
                new WorkspaceSpecKind(),   new WorkspaceSpecKindCodec(),
                new WorkspaceInstanceId(), new WorkspaceInstanceIdCodec(),
                new Side(),                new SideCodec(),
                new Scaled(),              new ScaledCodec(),
                new Host(),                new HostCodec(),
                new InRegion(),            new InRegionCodec(),
                new InFloat(),             new InFloatCodec(),
                new WorkspaceEvent(),      new WorkspaceEventCodec(),
                new TabOpened(),           new TabOpenedCodec(),
                new TabBecame(),           new TabBecameCodec(),
                new TabRenamed(),          new TabRenamedCodec(),
                new TabMoved(),            new TabMovedCodec(),
                new TabShown(),            new TabShownCodec(),
                new TabClosed(),           new TabClosedCodec(),
                new RegionParted(),        new RegionPartedCodec(),
                new RegionRemoved(),       new RegionRemovedCodec(),
                new TracksChanged(),       new TracksChangedCodec(),
                new FloatOpened(),         new FloatOpenedCodec(),
                new FloatMoved(),          new FloatMovedCodec(),
                new FloatResized(),        new FloatResizedCodec(),
                new FloatRaised(),         new FloatRaisedCodec(),
                new FloatClosed(),         new FloatClosedCodec(),
                new LoggedEvent(),         new LoggedEventCodec(),
                new LogHeader(),           new LogHeaderCodec()));
    }
}
