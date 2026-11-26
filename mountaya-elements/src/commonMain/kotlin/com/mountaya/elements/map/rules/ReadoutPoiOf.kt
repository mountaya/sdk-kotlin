package com.mountaya.elements

import com.mountaya.geo.ReadoutPoint
import kotlinx.serialization.json.JsonPrimitive

/**
 * readoutPoiOf answers the point of interest a readout states for [feature], the topmost the map draws at the point
 * pressed (`map.readout.open`): its name, the feature's `name`, and its category, its OpenMapTiles `subclass`, else its
 * `class`. A property that holds no text reads as the empty text, which the readout's rows rule then words.
 */
internal fun readoutPoiOf(feature: QueriedFeature): ReadoutPoint.Poi {
    val subclass = textOf(feature, "subclass")
    return ReadoutPoint.Poi(
        name = textOf(feature, "name"),
        category = subclass.ifEmpty { textOf(feature, "class") },
    )
}

/** The text [feature] holds under [key], or the empty text where it holds none there or holds no text. */
private fun textOf(
    feature: QueriedFeature,
    key: String,
): String = (feature.properties[key] as? JsonPrimitive)?.takeIf { it.isString }?.content.orEmpty()
