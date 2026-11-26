package com.mountaya.elements

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageDecodingTest {
    @Test
    fun anElevationTileIsDecodedOnThePlatformsPoolForBusyWorkOffTheMainThread() {
        assertEquals<Any?>(Dispatchers.Default, imageDecoding()[ContinuationInterceptor])
    }
}
