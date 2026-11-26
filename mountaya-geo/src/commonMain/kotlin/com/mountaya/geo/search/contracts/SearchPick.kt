package com.mountaya.geo

/**
 * SearchPick is what picking a search result does to a map (`geo.search.pick`): how the camera moves, null where
 * it stays, and what the map's search highlight is handed, null where nothing is marked, which takes down the mark
 * of the pick before.
 *
 * Two are equal where both members are.
 */
public class SearchPick(
    /** How the camera moves onto the result, or null where it stays. */
    public val camera: SearchPickCamera?,
    /** What the map's search highlight draws, or null where nothing is marked. */
    public val mark: SearchHighlight?,
) {
    override fun equals(other: Any?): Boolean = other is SearchPick && other.camera == camera && other.mark == mark

    override fun hashCode(): Int = camera.hashCode() * 31 + mark.hashCode()

    override fun toString(): String = "SearchPick(camera=$camera, mark=$mark)"
}
