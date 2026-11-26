package com.mountaya.build.targets

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MountayaTargetsTest {
    private fun project(targets: String?): Project =
        ProjectBuilder.builder().build().also { project ->
            if (targets != null) project.extensions.extraProperties[MountayaTargets.PROPERTY] = targets
        }

    @Test
    fun everyTargetIsBuiltUnlessTheSwitchNamesFewer() {
        val targets = MountayaTargets.of(project(null))

        assertTrue(targets.all)
        assertEquals("android,ios,jvm", targets.toString())
    }

    @Test
    fun theJvmTargetIsAlwaysBuilt() {
        val jvm = MountayaTargets.of(project("jvm"))
        val ios = MountayaTargets.of(project(" ios "))

        assertFalse(jvm.android)
        assertFalse(jvm.ios)
        assertEquals("jvm", jvm.toString())
        assertEquals("ios,jvm", ios.toString())
    }

    @Test
    fun aTargetThisBuildDoesNotKnowIsRefused() {
        val error = assertFailsWith<IllegalArgumentException> { MountayaTargets.of(project("jvm,wasm")) }

        assertTrue(error.message.orEmpty().contains("names wasm"))
    }
}
