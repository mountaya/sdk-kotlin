package com.mountaya.conformance

import com.mountaya.geo.altitudeToZoom
import com.mountaya.geo.groundMetersPerPixel
import com.mountaya.geo.zoomToAltitude
import kotlinx.coroutines.test.TestScope

/**
 * `geo.camera.zoom-altitude`: a case with a `zoom` answers the altitude it is seen from and the
 * ground one pixel spans there, and a case with an `altitudeMeters` the zoom it is seen at, both at
 * the case's `latitude` on the nominal canvas.
 *
 * The nominal canvas height is held to the one the conversion reads where none is named: the
 * altitude is proportional to the height, so it is the altitude at the default over the altitude
 * on a canvas one pixel tall. The field-of-view factor is the altitude on a canvas one pixel tall
 * over the ground one pixel spans, the camera standing that many canvas heights from the screen, and
 * the zoom ceiling and floor are what an altitude far too low and far too high convert to, each read
 * back through the rule, which keeps them `internal`.
 */
object GeoCameraZoomAltitudeRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            mapOf(
                "defaultViewportHeightPx" to
                    zoomToAltitude(0.0, 0.0) / zoomToAltitude(0.0, 0.0, viewportHeightPixels = 1.0),
                "fieldOfViewFactor" to
                    zoomToAltitude(0.0, 0.0, viewportHeightPixels = 1.0) / groundMetersPerPixel(0.0, 0.0),
                "zoomCeiling" to altitudeToZoom(1e-6, 0.0),
                "zoomFloor" to altitudeToZoom(1e15, 0.0),
            ),
        )
        val input = case.fields
        val latitude = input.number("latitude")
        if (input["zoom"] == null) {
            return ConformanceOutcome.Value(
                FixtureValue.Number(altitudeToZoom(input.number("altitudeMeters"), latitude)),
            )
        }
        val zoom = input.number("zoom")
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "altitudeMeters" to FixtureValue.Number(zoomToAltitude(zoom, latitude)),
                    "groundMetersPerPixel" to FixtureValue.Number(groundMetersPerPixel(zoom, latitude)),
                ),
            ),
        )
    }
}
