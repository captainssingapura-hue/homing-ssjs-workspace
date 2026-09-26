package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;

/**
 * One child of a split and its share of the split's room: an exact number of
 * millionths, {@link #SCALE} decimal places, above zero.
 *
 * @param node  what the track holds: a cell or another split
 * @param share its share of the split
 */
public record Track(Layout node, Scaled share) {

    /** The shares' scale: millionths. */
    public static final int SCALE = 6;

    public Track {
        Objects.requireNonNull(node, "Track.node");
        Objects.requireNonNull(share, "Track.share");
        if (share.scale() != SCALE) throw new IllegalArgumentException("Track.share " + share + " — at scale " + SCALE);
        if (share.units() <= 0) throw new IllegalArgumentException("Track.share " + share + " — above zero");
    }

    public static Track of(Layout node, long millionths) { return new Track(node, Scaled.of(millionths, SCALE)); }
}
