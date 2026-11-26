package com.mountaya.geo

/**
 * NavigationError is why the last route request a navigation acted on failed: a Mountaya value,
 * never the exception of the engine underneath, which a consumer could only match by importing what
 * that engine keeps hidden.
 *
 * A first route that fails leaves a navigation `failed` with one, and a new route that fails leaves
 * it `off-route` on the route it holds, with one. Each failure is its own, identified where the engine
 * records it, so an observer comparing two states tells two failures apart. The kinds below are every reason this build knows.
 * The class is open to more, as the client's exception ladder is, so a `when` over it keeps an `else`
 * branch and a kind a later release adds reaches it rather than breaking the build.
 */
public abstract class NavigationError internal constructor() {
    /**
     * RouteFailed is a route request whose source failed: [cause] is what it threw, a client
     * exception for a request the API refused or a failure of the host's own. A failure is identified
     * where it is recorded: one is equal to itself alone, so two recorded apart are two, whatever
     * their sources threw, the same throwable included, and a state holding one never equals a state
     * holding another.
     */
    public class RouteFailed(
        /** What the route source threw. */
        public val cause: Throwable,
    ) : NavigationError() {
        override fun toString(): String = "NavigationError.RouteFailed($cause)"
    }

    /**
     * EmptyRoute is a route request whose source answered a route whose line holds no vertex, which
     * is no route to follow. It is identified where it is recorded, as every failure is: one is equal
     * to itself alone, so two empty routes recorded apart are two failures.
     */
    public class EmptyRoute : NavigationError() {
        override fun toString(): String = "NavigationError.EmptyRoute"
    }
}
