package com.mountaya.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.WordOverrides
import com.mountaya.geo.scopeOf
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MountayaScopeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aScopeStatedNestsInTheOneAroundItEverySettingItLeavesOutInherited() {
        val french = ElementsScopeStatement(language = RestSchema.Language.Fr, locale = "fr-CH")
        val words =
            ElementsScopeStatement(
                words =
                    WordOverrides.BySection(
                        JsonObject(
                            mapOf("controls" to JsonObject(mapOf("zoomOut" to JsonPrimitive("Moins")))),
                        ),
                    ),
            )
        var seen: ElementsScope? = null
        compose.setContent { MountayaScope(french) { MountayaScope(words) { seen = LocalMountayaScope.current } } }

        compose.runOnIdle {
            val scope = assertNotNull(seen, "the nested scope is read")
            assertEquals(scopeOf(scopeOf(ElementsScope.standard, french), words), scope)
            assertEquals("fr-CH", scope.locale)
            assertEquals("Zoom avant", scope.word("controls.zoomIn"))
            assertEquals("Moins", scope.word("controls.zoomOut"))
        }
    }
}
