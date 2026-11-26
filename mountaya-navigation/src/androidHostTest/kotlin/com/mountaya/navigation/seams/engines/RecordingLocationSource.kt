package com.mountaya.navigation

import com.mountaya.geo.FollowedRoute

/**
 * RecordingLocationSource keeps what a navigation asks of it, and states a position when the test says,
 * as the platform would.
 */
class RecordingLocationSource : LocationSource {
    var started = 0
        private set
    var stopped = 0
        private set
    val followed = mutableListOf<FollowedRoute?>()
    private var deliver: ((List<Double>) -> Unit)? = null

    override fun start(deliver: (position: List<Double>) -> Unit) {
        started += 1
        this.deliver = deliver
    }

    override fun stop() {
        stopped += 1
        deliver = null
    }

    override fun follow(route: FollowedRoute?) {
        followed += route
    }

    /** Hands [position] to the navigation, as a fix the platform reports, and answers false where the source is not started. */
    fun state(position: List<Double>): Boolean {
        val deliver = deliver ?: return false
        deliver(position)
        return true
    }
}
