package com.mountaya.elements

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * outsideLinkIntent answers the intent that opens [link] outside the application, in the app the device opens a link
 * in, its browser for a web address: `ACTION_VIEW` on the address, in a task of its own, so a context that is no
 * activity starts it as an activity does.
 */
internal fun outsideLinkIntent(link: Uri): Intent =
    Intent(Intent.ACTION_VIEW, link).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

/**
 * openLinkOutside opens [link] outside the application through the platform's own opener, [Context.startActivity] with
 * [outsideLinkIntent], and never through a handler the composition provides, Compose's `LocalUriHandler` among them,
 * so no app redirects or swallows a link the map opens, its watermark's above all (`geo.organization.watermark`). A
 * device holding no app that opens the link opens nothing.
 */
internal fun openLinkOutside(
    context: Context,
    link: Uri,
) {
    try {
        context.startActivity(outsideLinkIntent(link))
    } catch (none: ActivityNotFoundException) {
        // No app on the device opens the link, so the press opens nothing, as a press on a dead link does.
    }
}
