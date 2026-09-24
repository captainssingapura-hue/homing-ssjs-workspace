package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;

/**
 * Hand-written codec class for {@link LayoutNode} — sealed (Leaf | Split),
 * <b>recursive</b> in the Split variant. Mirrors WidgetLocation's
 * tagged-union shape, with the structural extension that Split children
 * are themselves LayoutNode values; the matching codec
 * ({@link LayoutNodeJsFunctions}) is self-referential.
 */
public final class LayoutNodeJsDefinition implements DefinitionCodeGen {

    public static final LayoutNodeJsDefinition INSTANCE = new LayoutNodeJsDefinition();

    private LayoutNodeJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != LayoutNode.class) {
            throw new IllegalArgumentException(
                    "LayoutNodeJsDefinition only handles LayoutNode; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    private static final String SOURCE = """
            const LayoutNode = {
                Leaf: class Leaf {
                    constructor(paneId) {
                        if (!(paneId instanceof PaneId)) {
                            throw new TypeError("LayoutNode.Leaf.paneId: expected PaneId");
                        }
                        this.paneId = paneId;
                        Object.freeze(this);
                    }
                },
                Child: class Child {
                    constructor(node, ratio) {
                        if (!(node instanceof LayoutNode.Leaf) && !(node instanceof LayoutNode.Split)) {
                            throw new TypeError("LayoutNode.Child.node: expected LayoutNode");
                        }
                        if (typeof ratio !== number || !(ratio > 0.0) || !isFinite(ratio)) {
                            throw new RangeError(
                                "LayoutNode.Child.ratio must be a positive finite number, got " + ratio);
                        }
                        this.node = node;
                        this.ratio = ratio;
                        Object.freeze(this);
                    }
                },
                Split: class Split {
                    constructor(orientation, children) {
                        if (orientation !== Orientation.HORIZONTAL && orientation !== Orientation.VERTICAL) {
                            throw new TypeError("LayoutNode.Split.orientation: expected Orientation");
                        }
                        if (!Array.isArray(children) || children.length < 2) {
                            throw new RangeError(
                                "LayoutNode.Split.children: a split needs two or more, got "
                                + (Array.isArray(children) ? children.length : typeof children));
                        }
                        var sum = 0.0;
                        for (var i = 0; i < children.length; i++) {
                            if (!(children[i] instanceof LayoutNode.Child)) {
                                throw new TypeError("LayoutNode.Split.children[" + i + "]: expected LayoutNode.Child");
                            }
                            sum += children[i].ratio;
                        }
                        if (!(sum > 0.0) || !isFinite(sum)) {
                            throw new RangeError("LayoutNode.Split.children: shares must sum to a positive finite number");
                        }
                        var out = [];
                        for (var j = 0; j < children.length; j++) {
                            out.push(new LayoutNode.Child(children[j].node, children[j].ratio / sum));
                        }
                        this.orientation = orientation;
                        this.children = Object.freeze(out);
                        Object.freeze(this);
                    }
                    /** The two-child split schema 1 could express; ratio is the first child's share. */
                    static of(orientation, ratio, first, second) {
                        if (typeof ratio !== number || !(ratio > 0.0 && ratio < 1.0)) {
                            throw new RangeError(
                                "LayoutNode.Split.of: ratio must be strictly between 0.0 and 1.0, got " + ratio);
                        }
                        return new LayoutNode.Split(orientation, [
                            new LayoutNode.Child(first, ratio),
                            new LayoutNode.Child(second, 1.0 - ratio)]);
                    }
                }
            };
            Object.freeze(LayoutNode);
            """;
}
