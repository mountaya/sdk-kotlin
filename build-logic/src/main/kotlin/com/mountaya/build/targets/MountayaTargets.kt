package com.mountaya.build.targets

import org.gradle.api.Project

/**
 * MountayaTargets says which targets a multiplatform module builds.
 *
 * Every target is built unless `-Pmountaya.targets` names fewer, as in `-Pmountaya.targets=jvm`.
 * The switch exists for bringing a machine up: the jvm target needs a JDK and nothing else, the
 * android target needs the Android SDK and its licences, and the iOS targets need Xcode. A
 * release never uses it.
 */
class MountayaTargets private constructor(
    private val enabled: Set<String>,
) {
    /** Whether the android target is built. */
    val android: Boolean get() = ANDROID in enabled

    /** Whether the iOS targets are built. */
    val ios: Boolean get() = IOS in enabled

    /** Whether every target is built, which is what a published module needs. */
    val all: Boolean get() = enabled == ALL

    /** Returns the enabled targets, for a message. */
    override fun toString(): String = enabled.sorted().joinToString(",")

    /** The property the targets are read from, and its reading. */
    companion object {
        /** The Gradle property that names the targets a run builds. */
        const val PROPERTY = "mountaya.targets"
        private const val JVM = "jvm"
        private const val ANDROID = "android"
        private const val IOS = "ios"
        private val ALL = setOf(JVM, ANDROID, IOS)

        /** Reads the switch from [project]. The jvm target is always built. */
        fun of(project: Project): MountayaTargets {
            val requested =
                (project.findProperty(PROPERTY) as String?)
                    ?.split(',')
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.toSet()
            val unknown = requested.orEmpty() - ALL
            require(unknown.isEmpty()) {
                "-P$PROPERTY names ${unknown.joinToString(", ")}. The targets are ${ALL.sorted().joinToString(", ")}."
            }
            return MountayaTargets((requested ?: ALL) + JVM)
        }
    }
}
