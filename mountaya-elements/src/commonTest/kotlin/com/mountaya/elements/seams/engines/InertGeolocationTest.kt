package com.mountaya.elements

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InertGeolocationTest {
    @Test
    fun anInertGeolocationNeverLocatesAndReadsItsPermissionAsDenied() =
        runTest {
            val geolocation = InertGeolocation()

            assertEquals<GeolocationFix>(GeolocationFix.Refused(GeolocationRefusal.NoApi), geolocation.locate())
            assertEquals(GeolocationPermission.Denied, geolocation.permission())
        }
}
