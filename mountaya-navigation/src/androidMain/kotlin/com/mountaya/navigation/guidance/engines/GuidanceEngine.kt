package com.mountaya.navigation

import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.GuidancePlan
import com.mountaya.geo.NavigationState
import com.mountaya.geo.NavigationStatus
import com.mountaya.geo.duePrompt
import com.mountaya.geo.guidancePlanOf

/**
 * GuidanceEngine is the guidance of one navigation: the route it guides, laid out by Geo's [guidancePlanOf],
 * and the prompts of that route already said, each said once through [voice].
 *
 * It decides nothing the navigation decides. The engine it serves tells it of the route the machine
 * follows at each state, [follow], and hands it that state, [guide], whose own progress Geo's [duePrompt] reads
 * the prompt due from: a prompt is said once per route, and only while the navigation is `navigating`. A
 * new route and a [halt] hush the voice. It reads no clock, since a prompt falls due by where the
 * traveller stands, and it is driven from the thread its engine is.
 */
internal class GuidanceEngine(
    private val voice: NavigationVoice,
) {
    /** The route guided, the one the navigation followed when last told, or null where it follows none. */
    var route: FollowedRoute? = null
        private set

    /** The plan of [route], or null where it follows none, or one whose line names no place. */
    var plan: GuidancePlan? = null
        private set

    /** The prompts of [route] said, by their place in its plan. */
    private val said = HashSet<Int>()

    /**
     * Keeps to [followed], the route the navigation follows, and answers whether it is another than the
     * one guided: the voice is then hushed, and the route laid out afresh with nothing of it said. A route
     * is the one guided where it equals it, as Geo compares a followed route.
     */
    fun follow(followed: FollowedRoute?): Boolean {
        if (followed == route) return false
        route = followed
        plan = followed?.let(::guidancePlanOf)
        said.clear()
        voice.hush()
        return true
    }

    /**
     * Says the prompt due in [state], where the navigation is `navigating` on the route guided and that
     * prompt was not said before.
     */
    fun guide(state: NavigationState) {
        val plan = plan ?: return
        if (state.status != NavigationStatus.Navigating || state.route != route) return
        val due = duePrompt(plan, state.progress) ?: return
        if (said.add(due.id)) voice.say(due.instruction)
    }

    /** Forgets the route guided and what was said along it, and hushes the voice, as the navigation stops. */
    fun halt() {
        route = null
        plan = null
        said.clear()
        voice.hush()
    }
}
