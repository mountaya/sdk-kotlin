package com.mountaya.navigation

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

/** The tag a device seam's failure no `onError` was given for is logged under. */
internal const val DROPPED_FAILURE_TAG = "MountayaNavigation"

/**
 * Whether an app whose `ApplicationInfo.flags` are [flags] logs a device seam's failure that no `onError` was given
 * for: a debuggable build alone, so a release build stays silent, as `sdk-dart` prints one under `kDebugMode` and
 * `sdk-swift` under `DEBUG`.
 */
internal fun logsDroppedFailures(flags: Int): Boolean = flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

/** The line a debuggable app logs for [failure], which [seam] dropped since it was made with no `onError`. */
internal fun droppedFailureLine(
    seam: String,
    failure: Throwable,
): String = "$seam was made with no onError, so it dropped this failure: ${failure.message}"

/**
 * Logs [failure], which [seam] dropped since it was made with no `onError`, once, as a warning, where the app
 * [context] belongs to is debuggable, and nothing otherwise. A dropped failure is never thrown and never a crash.
 */
internal fun logDroppedFailure(
    context: Context,
    seam: String,
    failure: Throwable,
) {
    if (!logsDroppedFailures(context.applicationInfo.flags)) return
    Log.w(DROPPED_FAILURE_TAG, droppedFailureLine(seam, failure), failure)
}
