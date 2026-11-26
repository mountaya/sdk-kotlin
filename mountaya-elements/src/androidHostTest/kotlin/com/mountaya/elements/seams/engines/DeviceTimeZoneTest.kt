package com.mountaya.elements

import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceTimeZoneTest {
    @Test
    fun theReadoutsZoneIsTheDevicesAsItIsWhenRead() {
        val before = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Chatham"))
            assertEquals("Pacific/Chatham", deviceTimeZone().id)
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
            assertEquals("Europe/Paris", deviceTimeZone().id)
        } finally {
            TimeZone.setDefault(before)
        }
    }
}
