package com.mountaya.build.gates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoundaryGateTest {
    private fun violations(
        module: String,
        files: Map<String, String>,
    ): String = violations(Trees().add(module, "commonMain", files))

    private fun violations(trees: Trees): String = BoundaryGate.run(trees.build()).violations.joinToString("\n")

    /** The client's declarations a test names, each in the directory of its topic and its kind, all in one package. */
    private val client =
        mapOf(
            "com/mountaya/client/transport/engines/Mountaya.kt" to "package com.mountaya.client\nclass Mountaya",
            "com/mountaya/client/transport/rules/Deadline.kt" to "package com.mountaya.client\nclass Deadline",
            "com/mountaya/client/session/engines/SessionTokenEngine.kt" to
                "package com.mountaya.client\nclass SessionTokenEngine",
            "com/mountaya/client/credentials/contracts/SessionToken.kt" to
                "package com.mountaya.client\nsealed interface SessionToken { class Value : SessionToken }",
            "com/mountaya/client/geojson/contracts/GeoJsonLineString.kt" to
                "package com.mountaya.client\nclass GeoJsonLineString",
            "com/mountaya/client/geojson/rules/GeoJsonLength.kt" to
                "package com.mountaya.client\nfun GeoJsonLineString.length(): Double = 0.0",
        )

    @Test
    fun theClientNamesNoAndroidPackageAndKtorAndOkHttpOnlyInThePlatformsExchange() {
        val found =
            violations(
                "mountaya-client",
                mapOf(
                    "com/mountaya/client/retry/rules/Bad.kt" to
                        "package com.mountaya.client\nimport android.util.Log\nimport io.ktor.client.HttpClient\nfun bad(c: HttpClient) = Unit",
                    "com/mountaya/client/transport/engines/Transport.kt" to
                        "package com.mountaya.client\nimport okhttp3.OkHttpClient\nfun bad(c: OkHttpClient) = Unit",
                    "com/mountaya/client/seams/engines/Good.kt" to
                        "package com.mountaya.client\nimport io.ktor.client.HttpClient\n" +
                        "import okhttp3.OkHttpClient\nfun good(c: HttpClient, o: OkHttpClient) = Unit",
                ),
            )

        assertTrue("Bad.kt:2: names `android`" in found, found)
        assertTrue("Bad.kt:3: names `io.ktor`" in found, found)
        assertTrue("Bad.kt:4: names `HttpClient`" in found, found)
        assertTrue("Transport.kt:2: names `okhttp3`" in found, found)
        assertTrue("Good.kt" !in found, found)
    }

    /**
     * Every file of a module declares the module's package, so the directory alone says where it sits: a file
     * outside the platform's exchange that names Ktor fails, whatever package it declares.
     */
    @Test
    fun aFileIsPlacedByItsDirectoryAndNeverByThePackageItDeclares() {
        val found =
            violations(
                "mountaya-client",
                mapOf(
                    "com/mountaya/client/retry/rules/Claims.kt" to
                        "package com.mountaya.client.seams.engines\nimport io.ktor.client.HttpClient\nfun claims(c: HttpClient) = Unit",
                    "com/mountaya/client/seams/engines/Exchange.kt" to
                        "package com.mountaya.client\nimport io.ktor.client.HttpClient\nfun exchange(c: HttpClient) = Unit",
                ),
            )

        assertTrue("Claims.kt:2: names `io.ktor`" in found, found)
        assertTrue("Claims.kt:3: names `HttpClient`" in found, found)
        assertTrue("Exchange.kt" !in found, found)
    }

    /**
     * The admin unit's calls leave through the core's transport, and it stands on the core alone: no connection of
     * its own, no Android, no renderer, no navigation library and no module but `mountaya-client`.
     */
    @Test
    fun theAdminUnitOpensNoConnectionAndNamesNoModuleButTheCore() {
        val found =
            violations(
                "mountaya-admin",
                mapOf(
                    "com/mountaya/admin/webhooks/rules/Bad.kt" to
                        "package com.mountaya.admin\nimport io.ktor.client.HttpClient\nimport java.net.URL\n" +
                        "import okhttp3.OkHttpClient\nimport android.util.Log\nfun bad(c: HttpClient, o: OkHttpClient) = Unit",
                    "com/mountaya/admin/accessors/rules/Worse.kt" to
                        "package com.mountaya.admin\nimport com.mountaya.geo.formatDistance\n" +
                        "import com.mountaya.elements.MapEngine\n" +
                        "import com.mountaya.navigation.DeviceNavigationEngine\n" +
                        "import org.maplibre.android.maps.MapLibreMap\nfun worse() = Unit",
                    "com/mountaya/admin/accessors/rules/Good.kt" to
                        "package com.mountaya.admin\nimport com.mountaya.client.Mountaya\n" +
                        "import com.mountaya.client.generated.RestSchema\nfun good(m: Mountaya, s: RestSchema.Session) = Unit",
                ),
            )

        for (refused in listOf("io.ktor", "java.net", "okhttp3", "android")) {
            assertTrue("Bad.kt" in found && "names `$refused`" in found, "$refused: $found")
        }
        for (refused in listOf(
            "com.mountaya.geo",
            "com.mountaya.elements",
            "com.mountaya.navigation",
            "org.maplibre",
        )) {
            assertTrue("names `$refused`" in found, "$refused: $found")
        }
        assertTrue("Good.kt" !in found, found)
    }

    @Test
    fun theGeoLayerNeverNamesTheClientOrTheNetwork() {
        val found =
            violations(
                "mountaya-geo",
                mapOf(
                    "com/mountaya/geo/profile/rules/Bad.kt" to
                        "package com.mountaya.geo\nimport java.net.URL\nfun bad(client: Mountaya) = Unit",
                ),
            )

        assertTrue("names `java.net`" in found, found)
        assertTrue("names `Mountaya`" in found, found)
    }

    /**
     * Android's ICU writes an instant from API 24, so Geo's seams name it and the release a device runs; no other
     * Android or AndroidX package anywhere, and neither of those two outside the seams.
     */
    @Test
    fun theGeoLayerNamesAndroidsIcuAndItsReleaseInItsSeamsAlone() {
        val found =
            violations(
                Trees().add(
                    "mountaya-geo",
                    "androidMain",
                    mapOf(
                        "com/mountaya/geo/seams/engines/Icu.kt" to
                            "package com.mountaya.geo\nimport android.icu.text.DateFormat\nimport android.os.Build\n" +
                            "fun icu(f: android.icu.util.ULocale): DateFormat? = if (Build.VERSION.SDK_INT > 23) null else null",
                        "com/mountaya/geo/seams/engines/Device.kt" to
                            "package com.mountaya.geo\nimport android.content.Context\n" +
                            "import androidx.core.os.LocaleListCompat\nfun device(c: Context) = Unit",
                        "com/mountaya/geo/format/rules/Leak.kt" to
                            "package com.mountaya.geo\nimport android.icu.text.DateFormat\nimport android.os.Build\n" +
                            "fun leak(f: DateFormat) = Unit",
                    ),
                ),
            )

        assertTrue("Icu.kt" !in found, found)
        assertTrue("Device.kt:2: names `android`" in found, found)
        assertTrue("Device.kt:3: names `androidx`" in found, found)
        assertTrue("Leak.kt:2: names `android.icu`" in found, found)
        assertTrue("Leak.kt:3: names `android.os.Build`" in found, found)
        assertTrue("Leak.kt:2: names `android`," !in found, found)
    }

    /**
     * The client's names sit in one package, and the geo layer still names none of its transport or its session
     * engine: a name is read where the client declares it, by an import, a star import or a name spelt out in full.
     */
    @Test
    fun theGeoLayerNamesNoTopicOfTheClientItIsRefusedWhereverTheClientDeclaresIt() {
        val found =
            violations(
                Trees()
                    .add("mountaya-client", "commonMain", client)
                    .add(
                        "mountaya-geo",
                        "commonMain",
                        mapOf(
                            "com/mountaya/geo/route/rules/Timed.kt" to
                                "package com.mountaya.geo\nimport com.mountaya.client.Deadline\nfun timed(d: Deadline) = Unit",
                            "com/mountaya/geo/route/rules/Kept.kt" to
                                "package com.mountaya.geo\nfun kept(e: com.mountaya.client.SessionTokenEngine?) = Unit",
                            "com/mountaya/geo/route/rules/Everything.kt" to
                                "package com.mountaya.geo\nimport com.mountaya.client.*",
                            "com/mountaya/geo/route/rules/Wire.kt" to
                                "package com.mountaya.geo\nimport com.mountaya.client.GeoJsonLineString\n" +
                                "import com.mountaya.client.generated.RestSchema\nimport com.mountaya.client.length\n" +
                                "fun wire(l: GeoJsonLineString, r: RestSchema.Itinerary) = l.length()",
                        ),
                    ),
            )

        assertTrue("Timed.kt:2: names `com.mountaya.client.transport`" in found, found)
        assertTrue("Kept.kt:2: names `com.mountaya.client.session`" in found, found)
        assertTrue("Kept.kt:2: names `SessionTokenEngine`" in found, found)
        assertTrue("Everything.kt:2: names `com.mountaya.client.transport`" in found, found)
        assertTrue("Everything.kt:2: names `com.mountaya.client.session`" in found, found)
        assertTrue("Wire.kt" !in found, found)
        assertTrue("mountaya-client/" !in found, found)
    }

    /**
     * The navigation is Geo's machine with its guidance, written here: no package of any module names a
     * navigation library, the engine's own package of the navigation module included.
     */
    @Test
    fun noModuleNamesANavigationLibraryTheNavigationModuleIncluded() {
        val navigation =
            violations(
                "mountaya-navigation",
                mapOf(
                    "com/mountaya/navigation/adapter/engines/Route.kt" to
                        "package com.mountaya.navigation\nimport uniffi.ferrostar.Route\nfun route(r: Route) = Unit",
                    "com/mountaya/navigation/guidance/engines/Speech.kt" to
                        "package com.mountaya.navigation\n" +
                        "fun speak(o: com.stadiamaps.ferrostar.core.AndroidTtsObserver) = Unit",
                ),
            )

        assertTrue("Route.kt:2: names `uniffi.ferrostar`" in navigation, navigation)
        assertTrue("Speech.kt:2: names `com.stadiamaps.ferrostar`" in navigation, navigation)
        for (module in listOf("mountaya-client", "mountaya-geo", "mountaya-elements")) {
            val found =
                violations(
                    module,
                    mapOf(
                        "com/mountaya/x/y/rules/Leak.kt" to
                            "package com.mountaya.${module.removePrefix("mountaya-")}\nimport uniffi.ferrostar.Route",
                    ),
                )
            assertTrue("Leak.kt:2: names `uniffi.ferrostar`" in found, "$module: $found")
        }
    }

    @Test
    fun navigationNamesTheClientsWireVocabularyAloneAndNeverTheClient() {
        val found =
            violations(
                Trees()
                    .add("mountaya-client", "commonMain", client)
                    .add(
                        "mountaya-navigation",
                        "androidMain",
                        mapOf(
                            "com/mountaya/navigation/adapter/engines/Wire.kt" to
                                "package com.mountaya.navigation\n" +
                                "import com.mountaya.client.generated.RestSchema\n" +
                                "import com.mountaya.client.GeoJsonLineString\n" +
                                "fun wire(m: RestSchema.ManeuverType, l: GeoJsonLineString) = Unit",
                            "com/mountaya/navigation/guidance/engines/Calls.kt" to
                                "package com.mountaya.navigation\n" +
                                "import com.mountaya.client.Mountaya\n" +
                                "fun calls(c: Mountaya) = Unit",
                            "com/mountaya/navigation/seams/engines/Token.kt" to
                                "package com.mountaya.navigation\n" +
                                "fun token(t: com.mountaya.client.SessionToken.Value) = Unit",
                            "com/mountaya/navigation/seams/rules/Everything.kt" to
                                "package com.mountaya.navigation\nimport com.mountaya.client.*",
                        ),
                    ),
            )

        assertTrue("Calls.kt:2: names `com.mountaya.client`" in found, found)
        assertTrue("Token.kt:2: names `com.mountaya.client`" in found, found)
        assertTrue("Everything.kt:2: names `com.mountaya.client`" in found, found)
        assertTrue("Wire.kt" !in found, found)
    }

    @Test
    fun theElementsNameAndroidComposeMapLibreAndOkHttpInTheRendererAloneWhichAloneAddsALayer() {
        val found =
            violations(
                "mountaya-elements",
                mapOf(
                    "com/mountaya/elements/map/engines/Leak.kt" to
                        "package com.mountaya.elements\nimport org.maplibre.android.maps.Style\n" +
                        "import okhttp3.OkHttpClient\nimport androidx.compose.ui.Modifier\n" +
                        "fun leak(style: Style, c: OkHttpClient, m: Modifier, p: android.graphics.PointF) = style.addLayerAbove(p, m)",
                    "com/mountaya/elements/renderer/components/Material.kt" to
                        "package com.mountaya.elements\nimport androidx.compose.material3.Text\n" +
                        "fun look(style: org.maplibre.android.maps.Style) = style.addLayer(Text)",
                    "com/mountaya/elements/renderer/engines/Renderer.kt" to
                        "package com.mountaya.elements\nimport org.maplibre.android.maps.Style\n" +
                        "import okhttp3.OkHttpClient\nimport androidx.compose.ui.Modifier\n" +
                        "fun draw(style: Style, c: OkHttpClient, m: Modifier, p: android.graphics.PointF) = style.addLayerBelow(p, m)",
                    "com/mountaya/elements/map/engines/Engine.kt" to
                        "package com.mountaya.elements\nclass Engine { fun addLayer(layer: Any) = Unit }",
                ),
            )

        assertTrue("Leak.kt:2: names `org.maplibre`" in found, found)
        assertTrue("Leak.kt:3: names `okhttp3`" in found, found)
        assertTrue("Leak.kt:4: names `androidx`" in found, found)
        assertTrue("Leak.kt:5: names `android`" in found, found)
        assertTrue("Leak.kt:5: names `addLayerAbove`" in found, found)
        assertTrue("Material.kt:2: names `androidx.compose.material3`" in found, found)
        assertTrue("Material.kt:3: names `addLayer`" in found, found)
        assertTrue("Renderer.kt" !in found, found)
        assertTrue("Engine.kt" !in found, found)
    }

    @Test
    fun theElementsNeverNameTheNavigationModule() {
        val found =
            violations(
                Trees()
                    .add(
                        "mountaya-navigation",
                        "androidMain",
                        mapOf(
                            "com/mountaya/navigation/adapter/engines/DeviceNavigationEngine.kt" to
                                "package com.mountaya.navigation\nclass DeviceNavigationEngine",
                        ),
                    ).add(
                        "mountaya-elements",
                        "androidMain",
                        mapOf(
                            "com/mountaya/elements/renderer/components/Guided.kt" to
                                "package com.mountaya.elements\nimport com.mountaya.navigation.DeviceNavigationEngine",
                        ),
                    ),
            )

        assertTrue("Guided.kt:2: names `com.mountaya.navigation`" in found, found)
    }

    @Test
    fun aModuleWithNoRowIsNotScanned() {
        val result =
            BoundaryGate.run(
                Trees()
                    .add(
                        "conformance",
                        "commonMain",
                        mapOf("com/mountaya/conformance/x/rules/X.kt" to "package x\nimport android.util.Log"),
                    ).build(),
            )

        assertEquals(0, result.checked)
        assertEquals(emptyList(), result.violations)
    }
}
