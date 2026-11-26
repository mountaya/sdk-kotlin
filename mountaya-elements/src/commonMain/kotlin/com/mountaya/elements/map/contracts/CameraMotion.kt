package com.mountaya.elements

import kotlin.time.Duration

/**
 * CameraMotion is how a renderer moves the camera to a target: at once, along a straight ease, or along
 * the curved flight that pulls out and back in. Where the camera lands is the contract's; how it travels
 * there, and for how long, is the platform's.
 */
public sealed interface CameraMotion {
    /** Jump moves the camera at once. */
    public object Jump : CameraMotion {
        override fun toString(): String = "Jump"
    }

    /**
     * Ease moves the camera along a straight ease, over [duration], or over the renderer's own duration
     * where it is null. Two are equal where their durations are.
     */
    public class Ease(
        /** How long the ease takes, or null for the renderer's own duration. */
        public val duration: Duration? = null,
    ) : CameraMotion {
        override fun equals(other: Any?): Boolean = other is Ease && other.duration == duration

        override fun hashCode(): Int = duration.hashCode()

        override fun toString(): String = "Ease(duration=$duration)"
    }

    /** Fly moves the camera along a curved flight, over the renderer's own duration. */
    public object Fly : CameraMotion {
        override fun toString(): String = "Fly"
    }
}
