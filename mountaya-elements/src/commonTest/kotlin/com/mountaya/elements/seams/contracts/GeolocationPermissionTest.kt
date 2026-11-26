package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class GeolocationPermissionTest {
    @Test
    fun aPermissionIsDeniedGrantedToPromptForOrUnknown() {
        assertEquals(listOf("denied", "granted", "prompt", "unknown"), GeolocationPermission.entries.map { it.id })
    }
}
