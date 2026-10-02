package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;
import java.util.Objects;

/**
 * The flow, a content type: a slot's parts, in order - each a widget's type and the params it
 * is made with. What a flow widget asks for, to make a column of widgets. Knows nothing of
 * what the parts are, nor of docs: any tree's slot may hold one.
 */
public sealed interface FlowContent {

    /** One part: the type of the widget made for it, and its params. */
    record Part(String type, List<Param> params) {
        public Part {
            Objects.requireNonNull(type, "Part.type");
            params = List.copyOf(Objects.requireNonNull(params, "Part.params"));
        }
    }

    /** A flow's content: its parts, in order. */
    record Flow(List<Part> parts) {
        public Flow { parts = List.copyOf(Objects.requireNonNull(parts, "Flow.parts")); }
    }

    /** A widget wants its flow. */
    record Wanted(List<Param> params) implements FlowContent {}

    /** The party asks its steward, or the party above, for flows it does not hold. */
    record Fetch(List<Item> items) implements FlowContent {}

    /** A flow fetched. */
    record Loaded(List<Param> params, Flow content) implements FlowContent {}

    /** A flow that could not be fetched, and why. */
    record Failed(List<Param> params, String why) implements FlowContent {}

    /** The party answers a widget: its flow. */
    record Content(List<Param> params, Flow content) implements FlowContent {}

    /** The party answers a widget: no flow, and why. */
    record Unavailable(List<Param> params, String why) implements FlowContent {}

    /**
     * The type: {@code flow}, served as {@code FLOW}, its secretary the content secretary. No
     * steward of its own: a flow is fetched as its host's steward fetches it.
     */
    PartyType<FlowContent> TYPE = ContentParty.type("flow", FlowContent.class)
            .servedFrom(new ModuleImports<>(List.of(new FlowContentModule.FLOW()), FlowContentModule.INSTANCE));
}
