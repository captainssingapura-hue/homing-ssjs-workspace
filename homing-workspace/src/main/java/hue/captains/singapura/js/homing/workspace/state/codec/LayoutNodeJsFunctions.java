package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;

/**
 * Hand-written codec for {@link LayoutNode}. Tagged-union dispatch on
 * {@code kind} like {@code WidgetLocation}, but with self-referential
 * recursion in the Split variant — the codec calls itself for
 * {@code first} and {@code second} sub-trees.
 */
public final class LayoutNodeJsFunctions implements FunctionsCodeGen {

    public static final LayoutNodeJsFunctions INSTANCE = new LayoutNodeJsFunctions();

    private LayoutNodeJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != LayoutNode.class) {
            throw new IllegalArgumentException(
                    "LayoutNodeJsFunctions only handles LayoutNode; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const LayoutNodeCodec = {
                transformTo(node) {
                    if (node instanceof LayoutNode.Leaf) {
                        return {
                            kind:   'Leaf',
                            paneId: PaneIdCodec.transformTo(node.paneId)
                        };
                    }
                    if (node instanceof LayoutNode.Split) {
                        var children = [];
                        for (var i = 0; i < node.children.length; i++) {
                            children.push({
                                node:  LayoutNodeCodec.transformTo(node.children[i].node),
                                ratio: node.children[i].ratio
                            });
                        }
                        return {
                            kind:        'Split',
                            orientation: OrientationCodec.transformTo(node.orientation),
                            children:    children
                        };
                    }
                    throw new TypeError("LayoutNodeCodec.transformTo: not a LayoutNode variant");
                },
                transformFrom(wire) {
                    if (wire == null || typeof wire.kind !== 'string') {
                        throw new TypeError("LayoutNodeCodec.transformFrom: wire missing 'kind' discriminator");
                    }
                    switch (wire.kind) {
                        case 'Leaf':
                            return new LayoutNode.Leaf(PaneIdCodec.transformFrom(wire.paneId));
                        case 'Split': {
                            // Schema 1 wrote a binary split as orientation + ratio + first + second.
                            // It reads as a two-child split with shares [ratio, 1 - ratio], which is
                            // the whole of the migration and the reason it cannot fail.
                            if (wire.children === undefined && wire.first !== undefined) {
                                return LayoutNode.Split.of(
                                    OrientationCodec.transformFrom(wire.orientation),
                                    wire.ratio,
                                    LayoutNodeCodec.transformFrom(wire.first),
                                    LayoutNodeCodec.transformFrom(wire.second));
                            }
                            if (!Array.isArray(wire.children)) {
                                throw new TypeError("LayoutNodeCodec.transformFrom: Split wire needs children");
                            }
                            var children = [];
                            for (var i = 0; i < wire.children.length; i++) {
                                children.push(new LayoutNode.Child(
                                    LayoutNodeCodec.transformFrom(wire.children[i].node),
                                    wire.children[i].ratio));
                            }
                            return new LayoutNode.Split(
                                OrientationCodec.transformFrom(wire.orientation), children);
                        }
                        default:
                            throw new TypeError(
                                "LayoutNodeCodec.transformFrom: unknown kind '" + wire.kind + "'");
                    }
                }
            };
            """;
}
