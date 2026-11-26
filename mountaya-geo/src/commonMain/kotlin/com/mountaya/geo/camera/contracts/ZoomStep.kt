package com.mountaya.geo

/**
 * ZoomStep is one of a map's two zoom controls, the step it takes, carrying the contract's spelling
 * (`geo.camera.zoom-limit-reached`).
 */
public enum class ZoomStep(
    /** The control as `geo/zoom-limit-reached.json` spells it. */
    public val id: String,
) {
    /** The control that steps the camera one zoom in. */
    ZoomIn("zoom-in"),

    /** The control that steps the camera one zoom out. */
    ZoomOut("zoom-out"),
}
