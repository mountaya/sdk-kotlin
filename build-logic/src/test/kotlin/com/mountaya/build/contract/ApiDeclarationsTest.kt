package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiDeclarationsTest {
    /** A klib dump of the shapes the Kotlin plugin writes, one declaration of each kind. */
    private val klib =
        listOf(
            "// Klib ABI Dump",
            "// Targets: [iosArm64, iosSimulatorArm64]",
            "// Library unique name: <com.mountaya:mountaya-geo>",
            "final class com.mountaya.geo/MapPaint { // com.mountaya.geo/MapPaint|null[0]",
            "    constructor <init>(kotlin/String) // com.mountaya.geo/MapPaint.<init>|<init>(kotlin.String){}[0]",
            "    final val background // com.mountaya.geo/MapPaint.background|{}background[0]",
            "        final fun <get-background>(): kotlin/String // com.mountaya.geo/MapPaint.background.<get-background>|<get-background>(){}[0]",
            "    final fun copy(kotlin/String = ...): com.mountaya.geo/MapPaint // com.mountaya.geo/MapPaint.copy|copy(kotlin.String){}[0]",
            "    final enum class Kind : kotlin/Enum<com.mountaya.geo/MapPaint.Kind> { // com.mountaya.geo/MapPaint.Kind|null[0]",
            "        enum entry Fill // com.mountaya.geo/MapPaint.Kind.Fill|null[0]",
            "    }",
            "    final object Companion { // com.mountaya.geo/MapPaint.Companion|null[0]",
            "        final val standard // com.mountaya.geo/MapPaint.Companion.standard|{}standard[0]",
            "    }",
            "}",
            "abstract fun interface com.mountaya.geo/SearchFetch { // com.mountaya.geo/SearchFetch|null[0]",
            "}",
            "final const val com.mountaya.geo/METERS_PER_FOOT // com.mountaya.geo/METERS_PER_FOOT|{}METERS_PER_FOOT[0]",
            "final val com.mountaya.geo/livelyPalette // com.mountaya.geo/livelyPalette|{}livelyPalette[0]",
            "final fun com.mountaya.geo/haversineMeters(kotlin/Double): kotlin/Double // com.mountaya.geo/haversineMeters|haversineMeters(kotlin.Double){}[0]",
            "final suspend fun <#A: kotlin/Any?> (kotlin/String).com.mountaya.geo/searchAll(): kotlin/Int // com.mountaya.geo/searchAll|searchAll@kotlin.String(){0§<kotlin.Any?>}[0]",
        ).joinToString("\n")

    /** A jvm dump of the same shapes, as the JVM spells them. */
    private val jvm =
        listOf(
            "public final class com/mountaya/client/SessionTokenEngine : com/mountaya/client/SessionTokenSource {",
            "\tpublic fun <init> (Lkotlinx/coroutines/CoroutineScope;Lkotlin/jvm/functions/Function1;)V",
            "\tpublic fun getSnapshots ()Lkotlinx/coroutines/flow/StateFlow;",
            "\tpublic final fun refresh ()V",
            "}",
            "",
            "public final class com/mountaya/client/ApiException\$IdempotentAnswer {",
            "\tpublic final fun getStatus ()I",
            "}",
            "",
            "public final class com/mountaya/client/BaseUrls\$Companion {",
            "\tpublic final fun getDefault ()Lcom/mountaya/client/BaseUrls;",
            "}",
            "",
            "public final class com/mountaya/geo/ConvertLengthKt {",
            "\tpublic static final fun convertLength-4iIbNF4 (DLjava/lang/String;)D",
            "\tpublic static synthetic fun formatLength\$default (DILjava/lang/Object;)Ljava/lang/String;",
            "\tpublic static final fun getLivelyPalette ()Ljava/util/List;",
            "\tpublic static final field METERS_PER_FOOT D",
            "}",
        ).joinToString("\n")

    /**
     * Each declaration by its path after its package: a nested type through the type it is nested in,
     * a member through its type, a companion's as its type's, and the companion itself as the type.
     */
    @Test
    fun aKlibDumpDeclaresEachDeclarationByItsPathAfterItsPackageCompanionLeftOut() {
        val api = ApiDeclarations.read(listOf(klib))

        assertEquals(
            setOf(
                "MapPaint",
                "MapPaint.background",
                "MapPaint.copy",
                "MapPaint.Kind",
                "MapPaint.Kind.Fill",
                "MapPaint.standard",
                "SearchFetch",
                "METERS_PER_FOOT",
                "livelyPalette",
                "haversineMeters",
                "searchAll",
            ),
            api.paths,
        )
    }

    /** An accessor and a constructor name nothing of their own, and a header line declares nothing. */
    @Test
    fun aKlibDumpDeclaresNoAccessorConstructorOrHeader() {
        val api = ApiDeclarations.read(listOf(klib))

        assertFalse(api.paths.any { "<" in it || "Companion" in it || "Targets" in it }, api.paths.toString())
    }

    @Test
    fun aJvmDumpDeclaresItsClassesThroughTheClassesTheyAreNestedInAndNoFileFacade() {
        val api = ApiDeclarations.read(listOf(jvm))

        assertTrue(api.declares("SessionTokenEngine"))
        assertTrue(api.declares("ApiException.IdempotentAnswer"))
        assertTrue(api.declares("BaseUrls"))
        assertFalse(api.declares("IdempotentAnswer"))
        assertFalse(api.declares("ConvertLengthKt"))
    }

    /** A class's getter is no property name: what common code declares, a klib dump spells as Kotlin does. */
    @Test
    fun aJvmDumpDeclaresEachMemberAsTheJvmSpellsItThroughItsTypeACompanionsAsItsTypes() {
        val api = ApiDeclarations.read(listOf(jvm))

        assertTrue(api.declares("SessionTokenEngine.getSnapshots"))
        assertTrue(api.declares("SessionTokenEngine.refresh"))
        assertTrue(api.declares("ApiException.IdempotentAnswer.getStatus"))
        assertTrue(api.declares("BaseUrls.getDefault"))
        assertFalse(api.declares("SessionTokenEngine.snapshots"))
        assertFalse(api.paths.any { "<init>" in it || "Companion" in it }, api.paths.toString())
    }

    @Test
    fun aFileFacadeHoldsTopLevelDeclarationsUnmangledAndAPropertyUnderItsOwnNameToo() {
        val api = ApiDeclarations.read(listOf(jvm))

        for (name in listOf("convertLength", "formatLength", "getLivelyPalette", "livelyPalette", "METERS_PER_FOOT")) {
            assertTrue(api.declares(name), name)
        }
        assertFalse(api.declares("convertLength-4iIbNF4"))
    }

    @Test
    fun everyDumpIsReadIntoOneSetAndNoDumpDeclaresNothing() {
        val both = ApiDeclarations.read(listOf(klib, jvm))

        assertTrue(both.declares("SessionTokenEngine") && both.declares("MapPaint"))
        assertTrue(ApiDeclarations.read(emptyList()).isEmpty)
        assertFalse(both.isEmpty)
    }

    @Test
    fun aFacadeAFileRenamesHoldsTopLevelDeclarationsAndIsNoType() {
        val api =
            ApiDeclarations.read(
                listOf(
                    """
                    public final class com/mountaya/client/SecretKeyCredential {
                    	public static final fun Credential (Ljava/lang/String;)Lcom/mountaya/client/Credential;
                    	public static final fun invoke (Lcom/mountaya/client/Credential${'$'}Secret${'$'}Companion;Ljava/lang/String;)Lcom/mountaya/client/Credential${'$'}Secret;
                    }

                    public final class com/mountaya/client/Holder {
                    	public static final field INSTANCE Lcom/mountaya/client/Holder;
                    	public final fun held ()I
                    }
                    """.trimIndent(),
                ),
            )

        assertTrue(api.declares("invoke"))
        assertTrue(api.declares("Credential"))
        assertFalse(api.declares("SecretKeyCredential"))
        assertTrue(api.declares("Holder"))
        assertTrue(api.declares("Holder.held"))
    }
}
