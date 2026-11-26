package com.mountaya.navigation

import com.mountaya.geo.FollowedRoute

/**
 * EagerLocationSource states [position] the moment it is told of a route, as an app's own source that
 * reads where the traveller stands at once may.
 */
class EagerLocationSource(
    private val position: List<Double>,
) : LocationSource {
    private var deliver: ((List<Double>) -> Unit)? = null

    override fun start(deliver: (position: List<Double>) -> Unit) {
        this.deliver = deliver
    }

    override fun stop() {
        deliver = null
    }

    override fun follow(route: FollowedRoute?) {
        if (route == null) return
        deliver?.invoke(position)
    }
}
