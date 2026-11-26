package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.MotionDurationScale

/**
 * motionRemoved is whether the reader turned animations off where it is composed, Android's "remove animations": the
 * animator duration scale at 0, as Compose reads it for the composition's own animations, its `MotionDurationScale`.
 * A camera move a control starts then jumps instead of animating.
 *
 * It reads the scale as it is composed, so the composition starts following the system's setting, and composes again
 * as the setting changes; a composition whose effects carry no scale moves as the platform's default does.
 */
@Composable
internal fun motionRemoved(): Boolean {
    val scale = rememberCoroutineScope().coroutineContext[MotionDurationScale] ?: return false
    return scale.scaleFactor == 0f
}
