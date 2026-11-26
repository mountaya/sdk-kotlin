package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsSearchListShownTest {
    @Test
    fun theListShowsWhileTheFocusStandsOverAQueryNotDismissed() {
        assertTrue(isSearchListShown(hasFocus = true, query = "Mont", isDismissed = false))
    }

    @Test
    fun noListShowsWithoutTheFocusOrOnceTheReaderDismissedIt() {
        assertFalse(isSearchListShown(hasFocus = false, query = "Mont", isDismissed = false))
        assertFalse(isSearchListShown(hasFocus = true, query = "Mont", isDismissed = true))
    }

    @Test
    fun aQueryOfWhiteSpaceAloneShowsNoList() {
        for (query in listOf("", "   ", "\t\n", "  ﻿")) {
            assertFalse(isSearchListShown(hasFocus = true, query = query, isDismissed = false), "`$query`")
        }
    }
}
