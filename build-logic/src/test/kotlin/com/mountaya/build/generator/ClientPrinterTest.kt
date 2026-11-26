package com.mountaya.build.generator

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The printer over a miniature model holding one of every kind it knows, against golden files.
 *
 * A change to the printer shows up here as a diff of the files a reviewer reads, before it moves
 * a byte of the real surface. `MOUNTAYA_UPDATE_GOLDEN=1` rewrites the golden files from the
 * printer, for a change that is meant.
 */
class ClientPrinterTest {
    private val model = ModelReader.read(resource("generator/model.json"))
    private val printed = ClientPrinter.print(model, DIGEST)

    /** The admin unit's files, which `mountaya-admin` holds. */
    private val admin = ClientPrinter.print(model, ClientUnit.Admin, DIGEST)

    @Test
    fun theMiniatureModelPrintsItsGoldenFiles() {
        for ((directory, files) in listOf("golden" to printed, "admin-golden" to admin)) {
            val golden = File("src/test/resources/generator/$directory")
            if (System.getenv("MOUNTAYA_UPDATE_GOLDEN") == "1") {
                golden.deleteRecursively()
                golden.mkdirs()
                for ((name, text) in files) File(golden, "$name.txt").writeText(text)
            }
            val expected = golden.listFiles().orEmpty().associate { it.name.removeSuffix(".txt") to it.readText() }
            assertEquals(expected.keys.sorted(), files.keys.toList(), directory)
            for ((name, text) in files) assertEquals(expected[name], text, "$directory/$name")
        }
    }

    /**
     * An error's code is one open vocabulary on every surface (`sdk/SPEC.md` section 3): the union of the codes the REST
     * API's and the Tile API's `ErrorExtensionsCode` and the Data API's `ErrorCode` declare, each once, in the order
     * the REST API, then the Tile API, then the Data API first names it, printed in the core alone.
     */
    @Test
    fun anErrorsCodeIsOneOpenVocabularyTheUnionOfEverySurfacesCodes() {
        val text = printed.getValue("ApiErrorCode.kt")
        assertTrue("public value class ApiErrorCode(\n" in text)
        val constants =
            Regex("public val (\\w+): ApiErrorCode = ApiErrorCode\\(\"(\\w+)\"\\)")
                .findAll(text)
                .map { it.groupValues[1] to it.groupValues[2] }
                .toList()
        assertEquals(
            listOf(
                "BadRequest" to "BAD_REQUEST",
                "LimitReached" to "LIMIT_REACHED",
                "NotFound" to "NOT_FOUND",
                "TileTooDeep" to "TILE_TOO_DEEP",
                "GatewayTimeout" to "GATEWAY_TIMEOUT",
            ),
            constants,
        )
        assertTrue("The `NOT_FOUND` code, which the REST API, the Tile API and the Data API declare." in text)
        assertTrue("The `LIMIT_REACHED` code, which the REST API declares." in text)
        assertFalse("ApiErrorCode.kt" in admin)
    }

    @Test
    fun printingTwiceGivesTheSameBytes() {
        assertEquals(printed, ClientPrinter.print(ModelReader.read(resource("generator/model.json")), DIGEST))
        assertEquals(
            admin,
            ClientPrinter.print(ModelReader.read(resource("generator/model.json")), ClientUnit.Admin, DIGEST),
        )
    }

    @Test
    fun everyFileNamesTheContractAndTheDigestOfTheFilesItWasPrintedFrom() {
        for ((name, text) in printed + admin) {
            assertTrue(
                "contract\n// v0.0.0-test, model digest $DIGEST.\n" in text,
                name,
            )
        }
        assertTrue(
            "public const val MODEL_DIGEST: kotlin.String = \"$DIGEST\"\n" in printed.getValue("SchemaVersion.kt"),
        )
    }

    @Test
    fun aDigestNotWrittenAsTheContractWritesOneIsRefused() {
        for (digest in listOf("", DIGEST.uppercase(), DIGEST.dropLast(1), DIGEST + "0", "g" + DIGEST.drop(1))) {
            val refused = assertFailsWith<ModelException> { ClientPrinter.print(model, digest) }
            assertTrue("modelDigest" in refused.message.orEmpty(), digest)
        }
    }

    /**
     * The model states the unit of every REST operation, webhook event and component, and the printer splits the
     * client by it and by no list of its own (`sdk/SPEC.md`, "The three layers, in every language"): the core keeps
     * the `studio` and `agent` groups and every component the admin unit does not reach alone, and the admin unit
     * prints the `admin` group, the webhook events and the components only those reach, naming the core's in full.
     */
    @Test
    fun theAdminUnitPrintsItsGroupItsComponentsAndTheWebhookEventsApartFromTheCore() {
        assertEquals(listOf("Admin.kt", "AdminSchema.kt", "OperationTable.kt", "WebhookEvent.kt"), admin.keys.toList())
        assertFalse("Admin.kt" in printed)
        assertFalse("WebhookEvent.kt" in printed)
        val rest = printed.getValue("RestSchema.kt")
        for (name in listOf("Key", "KeyCreateParams", "HutEventObject", "WebhookEvent", "ListKeysParameters")) {
            assertFalse("public class $name " in rest, name)
        }
        assertTrue("public class Hut private constructor(" in rest)
        val schema = admin.getValue("AdminSchema.kt")
        assertTrue("\npackage com.mountaya.admin.generated\n" in schema)
        assertTrue("\npublic object AdminSchema {\n" in schema)
        assertTrue("public class Key private constructor(" in schema)
        assertTrue("public class ListKeysParameters internal constructor()" in schema)
        assertFalse("public class Hut private constructor(" in schema)
        // A component of the core is named in full, and an alias of the core's is read through the core's types.
        assertTrue("public val season: com.mountaya.client.generated.RestSchema.Season," in schema)
        assertTrue("public val labels: com.mountaya.client.generated.RestSchema.Labels? = null," in schema)
        assertTrue("public val where: com.mountaya.client.generated.RestSchema.GeoJSONPoint? = null," in schema)
        assertTrue(
            "buildClassSerialDescriptor(\"com.mountaya.admin.generated.AdminSchema.WebhookEventObject\")" in schema,
        )
        val calls = admin.getValue("Admin.kt")
        assertTrue(
            "public class Admin internal constructor(\n    sender: com.mountaya.client.OperationSender," in calls,
        )
        assertTrue(
            "): com.mountaya.client.Response<kotlin.collections.List<AdminSchema.Key>, " +
                "com.mountaya.client.generated.RestSchema.ListMetadata> {" in calls,
        )
        assertTrue("com.mountaya.client.generated.RestSchema.ListMetadata.serializer())" in calls)
        assertTrue("given.slot2?.let { add(\"season\" to it.value) }" in calls)
        val coreTable = printed.getValue("OperationTable.kt")
        val adminTable = admin.getValue("OperationTable.kt")
        assertFalse("val listKeys:" in coreTable)
        assertTrue("val listKeys:" in adminTable && "val postKey:" in adminTable)
        assertFalse("val getHut:" in adminTable)
        assertFalse("object Tiles" in adminTable || "object Data" in adminTable || "dataEndpoint" in adminTable)
        assertTrue("            Rest.listKeys,\n            Rest.postKey,\n        )\n}\n" in adminTable)
        assertTrue(
            "buildClassSerialDescriptor(\"com.mountaya.admin.generated.WebhookEvent\")" in
                admin.getValue("WebhookEvent.kt"),
        )
    }

    /**
     * A file of the admin unit reads the core's seam where each of its methods hands its call over, and opts in to
     * it there; no file of the core does, since the core reads its own (SPEC.md section 7).
     */
    @Test
    fun everyFileOfTheAdminUnitOptsInToTheCoresSeamAndNoFileOfTheCoreDoes() {
        val optIn = "\n@file:kotlin.OptIn(com.mountaya.client.InternalMountayaApi::class)\n"
        for ((name, text) in admin) assertTrue(optIn in text, name)
        for ((name, text) in printed) assertFalse("OptIn" in text, name)
    }

    /**
     * A paged builder's cursor and limit are the walk's, which `PagedParameters` declares for a module beside the
     * core to override, and each carries the marker that keeps it out of the published API.
     */
    @Test
    fun aPagedBuildersCursorAndLimitCarryTheMarkerOfMountayasOwnApi() {
        for (builder in listOf(
            printed.getValue("RestSchema.kt").substringAfter("public class ListHutsParameters"),
            admin.getValue("AdminSchema.kt").substringAfter("public class ListKeysParameters"),
        )) {
            for (property in listOf("pageCursor: kotlin.String?", "pageLimit: kotlin.Int?")) {
                assertTrue(
                    "@com.mountaya.client.InternalMountayaApi\n        override var $property\n" in builder,
                    property,
                )
            }
        }
    }

    /** A core that named a component of the admin unit would compile only beside its admin unit, which is no core. */
    @Test
    fun aCoreThatNamesAComponentOfTheAdminUnitIsRefused() {
        val site = "\"description\": \"The hut.\" },"
        val key = "{ \"name\": \"key\", \"required\": false, \"type\": { \"kind\": \"ref\", \"name\": \"Key\" } },"
        val text = resource("generator/model.json").replace(site, "$site $key")
        assertTrue(key in text)
        val refusal =
            "RestSchema: `Key` is the admin unit's, and the core unit names nothing another unit declares " +
                "but the core's"
        assertTrue(refusal in printing(text), printing(text))
        assertTrue(refusal in printing(text, ClientUnit.Admin))
    }

    /** The admin unit declares one accessor on the core's client, `admin`, and the core declares none of that name. */
    @Test
    fun anOperationOfTheAdminGroupInTheCoreOrOfAnotherGroupInTheAdminUnitIsRefused() {
        val model = resource("generator/model.json")
        val inCore = model.replace(unitOf("admin.keys", "admin"), unitOf("admin.keys", "core"))
        assertTrue(
            "RestSchema: listKeys is an operation of the `admin` group in the core unit, and the admin unit " +
                "holds the `admin` group and nothing else" in printing(inCore),
        )
        val inAdmin = model.replace(unitOf("studio.huts", "core"), unitOf("studio.huts", "admin"))
        assertTrue(
            "RestSchema: getHut is an operation of the `studio` group in the admin unit, and the admin unit " +
                "holds the `admin` group and nothing else" in printing(inAdmin, ClientUnit.Admin),
        )
    }

    /**
     * Every operation, webhook event and component of the REST API states its unit, and the printer splits by that
     * key alone, so one that states none, or one this printer does not know, is refused where it is read.
     */
    @Test
    fun aUnitTheRestApiLeavesUnsaidOrThePrinterDoesNotKnowIsRefused() {
        val model = resource("generator/model.json")
        val unsaid = model.replace(unitOf("admin.keys", "admin"), "\"namespace\": \"admin.keys\",")
        assertTrue("(listKeys): `unit` is missing" in refusal(unsaid), refusal(unsaid))
        val unknown =
            model.replace(
                "{ \"name\": \"Page\", \"unit\": \"core\",",
                "{ \"name\": \"Page\", \"unit\": \"billing\",",
            )
        assertTrue("(Page).unit: `billing`, which this printer does not know" in refusal(unknown), refusal(unknown))
        val webhook = model.replace(HUT_CREATED + ", \"unit\": \"admin\" },", "$HUT_CREATED },")
        assertTrue("`unit` is missing" in refusal(webhook), refusal(webhook))
    }

    /** Webhook receiving is the admin unit's, and a member extends its union in the module the union is printed in. */
    @Test
    fun aWebhookOfTheCoreAndAMemberOfAnotherUnitThanItsUnionsAreRefused() {
        val model = resource("generator/model.json")
        val webhook = model.replace("$HUT_CREATED, \"unit\": \"admin\" },", "$HUT_CREATED, \"unit\": \"core\" },")
        assertTrue(
            "rest.webhooks.hut.created: the core unit's, and webhook receiving is the admin unit's" in
                printing(
                    webhook,
                ),
        )
        val member =
            model.replace(
                "\"name\": \"HutEventObject\",\n        \"unit\": \"admin\",",
                "\"name\": \"HutEventObject\",\n        \"unit\": \"core\",",
            )
        assertTrue(
            "AdminSchema.WebhookEventObject: its member HutEventObject is the core unit's, and a member extends its " +
                "union in the module the union is printed in" in printing(member),
            printing(member),
        )
    }

    /** The Tile API is the core's whole: the admin unit declares nothing of it. */
    @Test
    fun aTileOperationOfTheAdminUnitIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "\"operationId\": \"getSpec\",",
                "\"operationId\": \"getSpec\",\n        \"unit\": \"admin\",",
            )
        assertTrue("TileSchema: getSpec is the admin unit's, and the Tile API is the core's" in printing(text))
    }

    @Test
    fun theEnvelopeIsNotPrintedAndNoTemplateIsAMethod() {
        assertFalse("class Errors" in printed.getValue("RestSchema.kt"))
        assertFalse("getTile" in printed.getValue("Tiles.kt"))
        assertFalse("getTile" in printed.getValue("OperationTable.kt"))
    }

    @Test
    fun aDeprecationCarriesTheFirstSentenceOfItsDescription() {
        assertTrue("@kotlin.Deprecated(\"Sends a signal.\")" in printed.getValue("Agent.kt"))
        assertTrue("@kotlin.Deprecated(\"Use beds.\")" in printed.getValue("RestSchema.kt"))
        assertTrue("@kotlin.Deprecated(\"Routes between two points.\")" in printed.getValue("Data.kt"))
    }

    /**
     * A member is printed under its words (SPEC.md section 3): its `x-enum-varnames` name less its type's, or its
     * Data API name split at each underscore, in UpperCamelCase, a first word that starts with a digit keeping the
     * type's last word in front. A set the contract writes inline whose model names no member keeps its wire values.
     * Each holds the wire's own value, which is what a decoder reads.
     */
    @Test
    fun anEnumMemberIsPrintedUnderItsWords() {
        val rest = printed.getValue("RestSchema.kt")
        for (line in listOf(
            "public val Summer: Season = Season(\"summer\")",
            "public val AllYear: Season = Season(\"all-year\")",
            "public val Admin: HutScope = HutScope(\"admin\")",
            "public val Size512: TileSize = TileSize(512)",
            "public val low: HutLevel = HutLevel(\"low\")",
            "public val `-name`: ListHutsSort = ListHutsSort(\"-name\")",
        )) {
            assertTrue(line in rest, line)
        }
        assertFalse("SeasonSummer" in rest)
        assertTrue(
            "public val HutCreated: WebhookEventType = WebhookEventType(\"hut.created\")" in
                admin.getValue("AdminSchema.kt"),
        )
        val data = printed.getValue("DataSchema.kt")
        for (line in listOf(
            "public val Hiking: Activity = Activity(\"HIKING\")",
            "public val HikingAndTrail: Activity = Activity(\"HIKING_AND_TRAIL\")",
            "public val Mtb0: Activity = Activity(\"MTB_0\")",
        )) {
            assertTrue(line in data, line)
        }
        assertTrue("ActingUser.Required," in printed.getValue("OperationTable.kt"))
        assertFalse("ActingUser.required" in printed.getValue("OperationTable.kt"))
    }

    /**
     * The model carries no key for a member's words, so a name the rule cannot read, two members printed as
     * one name, and a member printed as a name the value class declares of its own are refused, each by name.
     */
    @Test
    fun anEnumMemberThePrinterCannotNameIsRefused() {
        val model = resource("generator/model.json")
        val cases =
            listOf(
                Triple(
                    "\"name\": \"SeasonSummer\"",
                    "\"name\": \"Summer\"",
                    "Season: the member Summer does not start with its type's name",
                ),
                Triple(
                    "\"name\": \"SeasonSummer\"",
                    "\"name\": \"Seasonsummer\"",
                    "Season: the member Seasonsummer leaves \"summer\" after its type's name, which is no words",
                ),
                Triple(
                    "\"name\": \"SeasonWinter\"",
                    "\"name\": \"SeasonSUMMER\"",
                    "RestSchema.Season: the members SeasonSummer and SeasonSUMMER are printed as Summer and SUMMER",
                ),
                Triple(
                    "\"name\": \"SeasonWinter\"",
                    "\"name\": \"SeasonCompanion\"",
                    "RestSchema.Season: the member SeasonCompanion is printed as Companion, which reads as the " +
                        "`Companion`",
                ),
                Triple(
                    "{ \"name\": \"MTB_0\" }",
                    "{ \"name\": \"VALUE\" }",
                    "DataSchema.Activity: the member VALUE is printed as Value, which reads as the `value`",
                ),
                Triple(
                    "{ \"name\": \"HIKING\" }",
                    "{ \"name\": \"MTB0\" }",
                    "DataSchema.Activity: the members MTB0 and MTB_0 are printed as Mtb0",
                ),
                Triple(
                    "{ \"name\": \"MTB_0\" }",
                    "{ \"name\": \"MTB__0\" }",
                    "Activity: the member MTB__0 is not words joined by single underscores",
                ),
                Triple(
                    "\"enum\": [\"name\", \"-name\"]",
                    "\"enum\": [\"name\", \"Name\"]",
                    "RestSchema.ListHutsSort: the members name and Name are printed as name and Name",
                ),
            )
        for ((site, written, refusal) in cases) {
            assertTrue(model.contains(site), site)
            val message = printing(model.replace(site, written))
            assertTrue(refusal in message, message)
        }
    }

    /**
     * A set the contract writes inline whose `x-enum-varnames` the model carries as `enumNames` is printed under each
     * member's words, as a set the contract names is, its wire value kept: a leading `-` reads `Descending`.
     */
    @Test
    fun anInlineSetTheModelNamesIsPrintedUnderItsMembersWords() {
        val site = "\"enum\": [\"name\", \"-name\"] }"
        val model = resource("generator/model.json")
        assertTrue(model.contains(site), site)
        val names = "\"enumNames\": [\"ListHutsSortName\", \"ListHutsSortNameDescending\"]"
        val named =
            ClientPrinter
                .print(ModelReader.read(model.replace(site, "\"enum\": [\"name\", \"-name\"], $names }")), DIGEST)
                .getValue("RestSchema.kt")

        assertTrue("public val Name: ListHutsSort = ListHutsSort(\"name\")" in named, named)
        assertTrue("public val NameDescending: ListHutsSort = ListHutsSort(\"-name\")" in named, named)
        assertTrue("/** Sent as `-name`. */" in named, named)
        assertFalse("`-name`: ListHutsSort" in named)
        val miscounted =
            model.replace(site, "\"enum\": [\"name\", \"-name\"], \"enumNames\": [\"ListHutsSortName\"] }")
        assertTrue("1 member names for 2 values" in printing(miscounted), printing(miscounted))
    }

    @Test
    fun pathParametersComeInThePathsOwnOrder() {
        val studio = printed.getValue("Studio.kt")
        assertTrue(studio.indexOf("hut_id: kotlin.String,") < studio.indexOf("season: RestSchema.Season,"))
    }

    @Test
    fun aModelVersionThePrinterWasNotWrittenForIsRefused() {
        for (version in listOf(1, 3)) {
            val text = resource("generator/model.json").replace("\"modelVersion\": 2", "\"modelVersion\": $version")
            val refused = assertFailsWith<ModelException> { ModelReader.read(text) }
            assertEquals("sdk/model.json: modelVersion $version, and this printer reads 2.", refused.message)
        }
    }

    /**
     * A form's `encoding` names the media type a file's part is sent as where its caller names none, and the
     * printer sends that one rather than a default of its own; an encoding it cannot send is refused.
     */
    @Test
    fun aFormsFileIsSentAsTheMediaTypeItsEncodingNames() {
        val agent = printed.getValue("Agent.kt")
        assertTrue("MultipartPart.File(\"file\", body.value.file, contentType = \"application/gpx+xml\")" in agent)
        val unstated =
            resource("generator/model.json").replace(
                ", \"encoding\": [ { \"property\": \"file\", \"contentType\": \"application/gpx+xml\" } ]",
                "",
            )
        val plain = ClientPrinter.print(ModelReader.read(unstated), DIGEST).getValue("Agent.kt")
        assertTrue("MultipartPart.File(\"file\", body.value.file))" in plain)

        val twice = "{ \"property\": \"file\", \"contentType\": \"a/b\" }"
        for ((encoding, refusal) in listOf(
            "{ \"property\": \"photo\", \"contentType\": \"image/png\" }" to "names no part of the form",
            "{ \"property\": \"caption\", \"contentType\": \"text/plain\" }" to "names a part that is no file",
            "{ \"property\": \"file\", \"contentType\": \"gpx\" }" to "which no header carries",
            "$twice, $twice" to "is stated twice",
        )) {
            val text =
                resource("generator/model.json").replace(
                    "{ \"property\": \"file\", \"contentType\": \"application/gpx+xml\" }",
                    encoding,
                )
            val message =
                assertFailsWith<ModelException> {
                    ClientPrinter.print(
                        ModelReader.read(text),
                        DIGEST,
                    )
                }.message
            assertTrue(refusal in message.orEmpty(), message)
        }
        val json =
            resource("generator/model.json").replace(
                "{ \"mediaType\": \"application/json\", \"type\": { \"kind\": \"ref\", \"name\": \"Signal\" } }",
                "{ \"mediaType\": \"application/json\", \"type\": { \"kind\": \"ref\", \"name\": \"Signal\" }, " +
                    "\"encoding\": [ { \"property\": \"file\", \"contentType\": \"a/b\" } ] }",
            )
        val message = assertFailsWith<ModelException> { ClientPrinter.print(ModelReader.read(json), DIGEST) }.message
        assertTrue("an `encoding` on a `application/json` body" in message.orEmpty(), message)
    }

    @Test
    fun aKindThePrinterDoesNotKnowIsRefusedNeverSkipped() {
        val schema = resource("generator/model.json").replace("\"kind\": \"alias\"", "\"kind\": \"tuple\"")
        assertTrue("`tuple`" in assertFailsWith<ModelException> { ModelReader.read(schema) }.message.orEmpty())
        val type = resource("generator/model.json").replace("{ \"kind\": \"any\" }", "{ \"kind\": \"bytes\" }")
        assertTrue("`bytes`" in assertFailsWith<ModelException> { ModelReader.read(type) }.message.orEmpty())
    }

    @Test
    fun aNameThePrinterWouldDeclareTwiceIsRefused() {
        val text =
            resource("generator/model.json")
                .replace("\"name\": \"Page\"", "\"name\": \"GetHutParameters\"")
                .replace("\"name\": \"Page\" }", "\"name\": \"GetHutParameters\" }")
        val refused = assertFailsWith<ModelException> { ClientPrinter.print(ModelReader.read(text), DIGEST) }
        assertTrue("GetHutParameters" in refused.message.orEmpty())
    }

    /**
     * Every property that is not required and is nullable, on an object a request sends as JSON,
     * has three states: a merge body, a value object it embeds and a POST body alike. A response
     * alone never does, and neither does a form, whose part is sent or not.
     */
    @Test
    fun threeStatesReachEveryObjectARequestSendsAndNothingElse() {
        val schema = printed.getValue("RestSchema.kt")

        for (name in listOf("Hut", "HutContact", "PingSignal")) {
            val declaration =
                schema
                    .substringAfter(
                        "public class $name private constructor(",
                    ).substringBefore("public class ")
            assertTrue("Settable<kotlin.String?>" in declaration, name)
        }
        for (name in listOf("Page", "HutSummary", "PostSignalMultipartBody")) {
            val declaration =
                schema
                    .substringAfter(
                        "public class $name private constructor(",
                    ).substringBefore("public class ")
            assertFalse("Settable" in declaration, name)
        }
        val answered =
            resource(
                "generator/model.json",
            ).replace(
                "\"usage\": [\"request\", \"response\"],\n        \"description\": \"A mountain hut.\"",
                "\"usage\": [\"response\"],\n        \"description\": \"A mountain hut.\"",
            )
        val reprinted = ClientPrinter.print(ModelReader.read(answered), DIGEST).getValue("RestSchema.kt")
        assertFalse(
            "Settable" in
                reprinted
                    .substringAfter(
                        "public class Hut private constructor(",
                    ).substringBefore("public class HutSummary"),
        )
    }

    @Test
    fun theFactsAWriteDeclaresArePrintedAndItsBuilderTakesAKeyAndATag() {
        val table = printed.getValue("OperationTable.kt")
        val delete = table.substringAfter("val deleteHut:").substringBefore("val getExport:")
        assertTrue(
            "actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.Required" in
                delete,
        )
        assertTrue("timeoutMs = 30000," in delete)
        assertTrue("declaresIdempotencyKey = true," in delete)
        assertTrue("isConditional = true," in delete)
        assertTrue("$SUCCESS(200, kotlin.collections.listOf(\"Idempotent-Replayed\"))," in delete, delete)
        assertTrue("errorStatuses = kotlin.collections.listOf(404, 409)," in delete)

        val studio = printed.getValue("Studio.kt")
        val call = studio.substringAfter("public suspend fun deleteHut(").substringBefore("public suspend fun ")
        assertTrue("idempotencyKey = given.idempotencyKeySlot," in call)
        assertTrue("ifMatch = given.ifMatchSlot," in call)
        val builder = printed.getValue("RestSchema.kt").substringAfter("public class DeleteHutParameters")
        assertTrue("public var idempotencyKey: kotlin.String" in builder)
        assertTrue("public var ifMatch: kotlin.String" in builder)
    }

    @Test
    fun theSuccessStatusesKeepTheModelsOrderEachWithTheHeadersItDeclaresAndTheErrorStatusesAscend() {
        val table = printed.getValue("OperationTable.kt")
        val signal = table.substringAfter("val postSignal:").substringBefore("errorStatuses")
        assertTrue(
            "successResponses =\n" +
                "                    kotlin.collections.listOf(\n" +
                "                        $SUCCESS(201, kotlin.collections.listOf(\"Location\")),\n" +
                "                        $SUCCESS(200, kotlin.collections.emptyList()),\n" +
                "                    ),\n" in signal,
            signal,
        )
        assertTrue("errorStatuses = kotlin.collections.listOf(404, 412)," in table.substringAfter("val patchHut:"))
        // A Data API operation's statuses are the model's, as a REST operation's are: no case of its own.
        val data =
            table
                .substringAfter("object Data")
                .substringAfter("successResponses =")
                .substringBefore("errorStatuses")
        assertTrue("$SUCCESS(200, kotlin.collections.emptyList())," in data, data)
    }

    @Test
    fun aPagedOperationPrintsItsSortsAndAReadThatAnswersNotModifiedSaysSo() {
        val table = printed.getValue("OperationTable.kt")
        assertTrue(
            "sort = kotlin.collections.listOf(\"name\", \"-name\"),\n" in table.substringAfter("val listHuts:"),
        )
        assertTrue("defaultSort = \"name\"," in table.substringAfter("val listHuts:"))
        assertTrue("answersNotModified = true," in table.substringAfter("val getHut:").substringBefore("val listHuts:"))
        assertTrue("pendingInvitee = true," in table.substringAfter("val getHut:").substringBefore("val listHuts:"))
    }

    @Test
    fun aPagedOperationIsWalkedBesideItsMethodAtThePageSizeItsLimitStates() {
        val studio = printed.getValue("Studio.kt")
        val walk = studio.substringAfter("public fun walkListHuts(").substringBefore("\n        /**")

        assertTrue(studio.indexOf("public suspend fun listHuts(") < studio.indexOf("public fun walkListHuts("))
        assertTrue("pageSize: kotlin.Int = 50,\n" in walk)
        assertTrue("maxPages: kotlin.Int? = null,\n" in walk)
        assertTrue("parameters: RestSchema.ListHutsParameters.() -> kotlin.Unit = {},\n" in walk)
        assertTrue("): kotlinx.coroutines.flow.Flow<RestSchema.HutSummary> =\n" in walk)
        assertTrue(
            "com.mountaya.client.PageWalk(pageSize, maxPages) { paged ->\n" +
                "                listHuts(\n" +
                "                    options = options,\n" +
                "                ) {\n" +
                "                    parameters()\n" +
                "                    paged()\n" in walk,
        )
        assertFalse("walkGetHut" in studio, "an operation that does not page has no walk")
    }

    @Test
    fun aPagedOperationNoWalkCouldReadIsRefused() {
        val model = resource("generator/model.json")
        val noDefault =
            model.replace(
                "\"type\": { \"kind\": \"integer\" }, \"default\": 50 }",
                "\"type\": { \"kind\": \"integer\" } }",
            )
        assertTrue("listHuts: it pages by cursor, and its `limit` states no whole number" in printing(noDefault))

        val listing =
            "\"name\": \"HutSummary\" } } },\n          \"metadata\": { \"required\": true, \"type\": { \"kind\": \"ref\", \"name\": \""
        val paged = model.replace(listing + "ListMetadata\"", listing + "Page\"")
        assertTrue("listHuts: it pages by cursor, and its metadata is not the `ListMetadata`" in printing(paged))

        val clash = model.replace("\"operationId\": \"patchHut\"", "\"operationId\": \"walkListHuts\"")
        assertTrue("Studio.Huts: walkListHuts would be both an operation and a walk" in printing(clash))
    }

    @Test
    fun theTemplatesArePrintedAsUrlTemplatesOfTheirSurfaceAndNoneWhereItListsNone() {
        val templates = printed.getValue("TileTemplates.kt")

        assertTrue("public object TileTemplates {" in templates)
        assertTrue(
            "operationId = \"getTile\",\n" +
                "            path = \"/v1/{z}/{x}/{y}\",\n" +
                "            pathParameters = kotlin.collections.listOf(\"z\", \"x\", \"y\"),\n" in templates,
        )
        assertTrue("mediaTypes = kotlin.collections.listOf(\"application/json\")," in templates)
        assertTrue("kotlin.collections.listOf(\n            getTile,\n            getTileJson,\n        )" in templates)
        assertFalse("RestTemplates.kt" in printed, "the REST surface lists no template")
    }

    @Test
    fun aTemplateNamedAllOrWhosePathAndParametersDisagreeIsRefused() {
        val model = resource("generator/model.json")
        val all = model.replace("\"operationId\": \"getTile\"", "\"operationId\": \"all\"")
        assertTrue("TileTemplates: a route named `all`" in printing(all))

        val declared =
            model.replace(
                "\"operationId\": \"getTile\", \"method\": \"GET\", \"path\": \"/v1/{z}/{x}/{y}\",",
                "\"operationId\": \"getTile\", \"method\": \"GET\", \"path\": \"/v1/{z}/{x}/{y}\", " +
                    "\"pathParameters\": [ { \"name\": \"z\", \"type\": { \"kind\": \"integer\" } } ],",
            )
        assertTrue("getTile: its path names z, x, y, and it declares z" in printing(declared))
    }

    @Test
    fun aSuccessWhoseDataIsAlwaysNullAnswersWithTheJsonItSent() {
        val call = printed.getValue("Studio.kt").substringAfter("public suspend fun deleteHut(")
        assertTrue(
            "): com.mountaya.client.Response<kotlinx.serialization.json.JsonElement, " in call,
        )
    }

    @Test
    fun aPropertyOneWayAloneAndAScopeThatIncludesOthersAreDocumented() {
        val schema = printed.getValue("RestSchema.kt")
        assertTrue("The server writes it, and a request never sends it." in schema)
        assertTrue("A request sends it, and the server never answers it." in schema)
        assertTrue("It includes `read`: a key carrying it reaches what that scope reaches." in schema)
        assertTrue("It includes `read` and `write`: a key carrying it reaches what those scopes reach." in schema)
        assertTrue("A key carrying it carries `read` and `write` beside it." in schema)
    }

    @Test
    fun aDescriptionIsAStringAndAMapWhereOneStandsIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "\"description\": \"June to September.\"",
                "\"description\": { \"June\": \"to September.\" }",
            )
        assertTrue("`description` is not a string" in refusal(text))
    }

    @Test
    fun anActingUserThePrinterDoesNotKnowIsRefused() {
        val text = resource("generator/model.json").replace("\"actingUser\": \"optional\"", "\"actingUser\": \"maybe\"")
        assertTrue("`maybe`, which this printer does not know" in refusal(text))
    }

    @Test
    fun aTemplateThatIsNotBinaryIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "\"success\": { \"kind\": \"binary\", \"mediaTypes\": [\"application/x-protobuf\"]",
                "\"success\": { \"kind\": \"envelope\", \"mediaTypes\": [\"application/x-protobuf\"]",
            )
        assertTrue("a template that is not binary" in refusal(text))
    }

    @Test
    fun aPropertyInheritedFromAParentTheComponentDoesNotExtendIsRefused() {
        val text = resource("generator/model.json").replace("\"from\": \"Hut\"", "\"from\": \"Page\"")
        assertTrue("inherited from `Page`, which it does not extend" in refusal(text))
    }

    @Test
    fun aPropertyOnTheWrongSideOfItsComponentOrBothWaysIsRefused() {
        val onRequest =
            resource(
                "generator/model.json",
            ).replace(
                "\"name\": \"note\", \"required\": false,",
                "\"name\": \"note\", \"readOnly\": true, \"required\": false,",
            )
        assertTrue("written by the server alone, on a component a request sends" in refusal(onRequest))
        val both =
            resource(
                "generator/model.json",
            ).replace("\"writeOnly\": true", "\"writeOnly\": true, \"readOnly\": true")
        assertTrue("a property both read-only and write-only" in refusal(both))
    }

    @Test
    fun anObjectBothClosedAndOpenToOtherKeysIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "\"name\": \"HutContact\", \"closed\": true,",
                "\"name\": \"HutContact\", \"closed\": true, \"additional\": { \"kind\": \"any\" },",
            )
        assertTrue("an object both closed and open to other keys" in refusal(text))
    }

    @Test
    fun anOperationThatPagesByCursorAndTakesNoCursorIsRefused() {
        val text =
            resource(
                "generator/model.json",
            ).replace("{ \"name\": \"cursor\", \"required\": false,", "{ \"name\": \"after\", \"required\": false,")
        assertTrue("it pages by cursor and takes no `cursor`" in refusal(text))
    }

    @Test
    fun aHeaderTheClientSendsItselfIsRefusedAmongTheCallersAndAnyOtherIsAnArgument() {
        assertTrue("public var `X-Trace`: kotlin.String" in printed.getValue("RestSchema.kt"))
        for (header in listOf("Idempotency-Key", "if-match", "Accept-Language", "Last-Event-ID")) {
            val text = resource("generator/model.json").replace("\"name\": \"X-Trace\"", "\"name\": \"$header\"")
            assertTrue("`$header` among the caller's headers, where the client sends it" in refusal(text), header)
        }
    }

    @Test
    fun aTypeNoMethodPrintsIsStillHeldToTheComponentsItNames() {
        for (site in listOf(
            "\"name\": \"Season\" } } ] } ]",
            "\"type\": { \"kind\": \"ref\", \"name\": \"Page\" } } ] } } } ]",
        )) {
            val text =
                resource(
                    "generator/model.json",
                ).replace(site, site.replace("Season", "Gone").replace("Page", "Gone"))
            assertTrue(
                "`Gone` names no component" in
                    assertFailsWith<ModelException> {
                        ClientPrinter.print(
                            ModelReader.read(text),
                            DIGEST,
                        )
                    }.message.orEmpty(),
                site,
            )
        }
        assertFalse("DeleteHutConflict" in printed.getValue("RestSchema.kt"))
    }

    @Test
    fun aScalarIsMappedByItsWireAndNeverByItsName() {
        val both =
            resource(
                "generator/model.json",
            ).replace(
                "{ \"name\": \"ID\", \"builtin\": true }",
                "{ \"name\": \"ID\", \"builtin\": true, \"wire\": { \"kind\": \"string\" } }",
            )
        assertTrue("a scalar is built in or travels in a wire, one of the two" in refusal(both))
        val neither =
            resource(
                "generator/model.json",
            ).replace("{ \"name\": \"ID\", \"builtin\": true }", "{ \"name\": \"ID\" }")
        assertTrue("a scalar is built in or travels in a wire, one of the two" in refusal(neither))
        val format =
            resource(
                "generator/model.json",
            ).replace(
                "\"wire\": { \"kind\": \"string\", \"format\": \"uri\" }",
                "\"wire\": { \"kind\": \"string\", \"format\": \"uuid\" }",
            )
        assertTrue("a string wire of format `uuid`" in refusal(format))
    }

    @Test
    fun aBodyTakenInTwoMediaTypesIsOneClassWithACasePerType() {
        val agent = printed.getValue("Agent.kt")
        assertTrue("body: RestSchema.PostSignalBody," in agent)
        assertTrue("is RestSchema.PostSignalBody.Json ->" in agent)
        assertTrue("is RestSchema.PostSignalBody.Multipart ->" in agent)
    }

    @Test
    fun aBodyClassNamedLikeASchemaIsRefused() {
        val text =
            resource("generator/model.json")
                .replace("{ \"name\": \"Labels\",", "{ \"name\": \"PostSignalBody\",")
        val refused = assertFailsWith<ModelException> { ClientPrinter.print(ModelReader.read(text), DIGEST) }
        assertTrue("PostSignalBody" in refused.message.orEmpty())
    }

    @Test
    fun anOperationDeclaringNoDataAnswersWithTheJsonItSent() {
        assertTrue("Response<kotlinx.serialization.json.JsonElement, " in printed.getValue("Agent.kt"))
    }

    /**
     * The model states every Data API operation's credentials, as it states a REST operation's, and the
     * printer reads them: no fact of the Data API is the printer's own.
     */
    @Test
    fun aDataApiOperationTakesTheCredentialsTheModelStates() {
        val data = printed.getValue("OperationTable.kt").substringAfter("object Data").substringAfter("val route:")
        assertTrue("actingUser = com.mountaya.client.CredentialDeclaration.ActingUser.None," in data, data)
        assertTrue("requiresSession = true," in data)
        assertTrue("acceptsShareToken = false," in data)
    }

    @Test
    fun aDataApiOperationWithNoCredentialsOrNoSuccessIsRefused() {
        val text = resource("generator/model.json")
        val stated =
            listOf(
                "        \"credentials\": { \"actingUser\": \"none\", \"acceptsShareToken\": false, " +
                    "\"requiresSession\": true, \"requiresSecretKey\": false, \"takesNoCredential\": false, " +
                    "\"pendingInvitee\": false },\n",
                "        \"success\": { \"responses\": [ { \"status\": 200, \"headers\": [] } ] },\n",
            )
        for (line in stated) {
            assertTrue(line in text, line)
            assertFailsWith<ModelException>(line) { ModelReader.read(text.replace(line, "")) }
        }
    }

    /**
     * A web address a class a request sends holds is written by the serializer that refuses one with no
     * scheme; read, and on a class no request sends, it is the text the wire sent.
     */
    @Test
    fun aWebAddressARequestSendsIsWrittenByTheSerializerThatRefusesOneWithNoScheme() {
        val hut =
            printed
                .getValue(
                    "RestSchema.kt",
                ).substringAfter("public class Hut ")
                .substringBefore("public class Builder")
        val sent = "@kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class)"
        assertTrue("com.mountaya.client.Settable<$sent com.mountaya.client.Url?>" in hut, hut)
        assertTrue("public val website: com.mountaya.client.Url?" in hut, hut)
    }

    /** A secret the model marks is printed by no `toString`, and stays readable. */
    @Test
    fun aSecretIsPrintedRedacted() {
        val signal = printed.getValue("RestSchema.kt").substringAfter("public class PingSignal ")
        val toString = signal.substringAfter("override fun toString()").substringBefore("public class Builder")
        assertTrue("\"<redacted>\"" in toString, toString)
        assertFalse("this.secret" in toString, toString)
        assertTrue("prints it as `<redacted>`" in signal)
    }

    @Test
    fun aReferenceToTheEnvelopeIsRefused() {
        val text =
            resource(
                "generator/model.json",
            ).replace("\"ref\", \"name\": \"Page\"", "\"ref\", \"name\": \"Errors\"")
        assertTrue(
            "envelope" in
                assertFailsWith<ModelException> {
                    ClientPrinter.print(
                        ModelReader.read(text),
                        DIGEST,
                    )
                }.message.orEmpty(),
        )
    }

    @Test
    fun theTasksAWriteRunsArePrintedLastAndAReadAndTheDataApiRunNone() {
        val table = printed.getValue("OperationTable.kt")
        val signal = table.substringAfter("val postSignal:").substringBefore("val getSignals:")
        assertTrue(
            "errorStatuses = kotlin.collections.listOf(409),\n" +
                "                tasks = kotlin.collections.listOf(\"signal.post\", \"signal.record\"),\n" +
                "            )" in signal,
            signal,
        )
        assertTrue(
            "tasks = kotlin.collections.listOf(\"hut.delete\")," in table.substringAfter("val deleteHut:"),
        )
        assertTrue(
            "tasks = kotlin.collections.emptyList()," in
                table.substringAfter("val getHut:").substringBefore("val listHuts:"),
        )
        assertTrue(
            "tasks = kotlin.collections.emptyList()," in
                table.substringAfter("object Data").substringBefore("val restOperations"),
        )
    }

    @Test
    fun anEventStreamIsReturnedAsTheClientsEventStream() {
        val agent = printed.getValue("Agent.kt").substringAfter("public fun getSignals(")
        assertTrue("): com.mountaya.client.EventStream<RestSchema.Signal> {" in agent, agent)
    }

    @Test
    fun aWebhookTypeIsAClassCarryingItsObjectAndItsPreviousAttributesOrNothing() {
        val event = admin.getValue("WebhookEvent.kt")
        assertTrue("public abstract class WebhookEvent internal constructor() {" in event)
        assertTrue(
            "    public class HutCreated(\n" +
                "        payload: WebhookEvent.Payload<AdminSchema.HutEventObject, kotlin.Nothing>,\n" +
                "    ) : WebhookEvent.Typed<AdminSchema.HutEventObject, kotlin.Nothing>(payload) {\n" +
                "        override val type: AdminSchema.WebhookEventType\n" +
                "            get() = AdminSchema.WebhookEventType(\"hut.created\")\n" in event,
        )
        assertTrue(
            "payload: WebhookEvent.Payload<AdminSchema.HutEventObject, WebhookEvent.HutUpdatedPreviousAttributes>," in
                event,
        )
        assertTrue("/** `hut.updated`: A hut's count, update time, season or former name changed. */" in event)
        assertTrue("    public class Unknown(\n" in event)
        assertTrue("public val event: AdminSchema.WebhookEvent," in event)
        val previous = event.substringAfter("public class HutUpdatedPreviousAttributes private constructor(")
        assertTrue("public val count: kotlin.Long? = null," in previous)
        assertTrue(
            "public val updated_at: com.mountaya.client.Stated<kotlin.time.Instant?>?" in previous,
        )
        assertTrue("get() = settable1.stated" in previous)
        assertTrue("public val season: com.mountaya.client.generated.RestSchema.Season? = null," in previous)
        assertFalse("HutCreatedPreviousAttributes" in event)
        // The webhook envelope's own components are printed as every component is.
        assertTrue("public class WebhookEvent private constructor(" in admin.getValue("AdminSchema.kt"))
    }

    @Test
    fun everyMemberOfTheEnvelopeIsReadOffEveryEventTheDeprecatedOneDeprecated() {
        val event = admin.getValue("WebhookEvent.kt")
        val base = event.substringBefore("public abstract class Typed")
        for (member in listOf(
            "id: kotlin.String",
            "`object`: kotlin.String",
            "created_at: kotlin.time.Instant",
            "request_id: kotlin.String?",
            "livemode: kotlin.Boolean?",
        )) {
            assertTrue("public abstract val $member\n" in base, member)
        }
        assertTrue(
            "@kotlin.Deprecated(\"The former identifier of the event.\")\n    public abstract val legacy_id" in base,
        )
        assertFalse("val data" in base)
        val typed = event.substringAfter("public abstract class Typed").substringBefore("public class HutCreated")
        assertTrue("override val created_at: kotlin.time.Instant\n            get() = payload.created_at" in typed)
        assertTrue("public val data: WebhookEvent.PayloadData<Object, PreviousAttributes>" in typed)
        assertTrue(
            "@kotlin.Deprecated(\"The former identifier of the event.\")\n" +
                "        @kotlin.Suppress(\"DEPRECATION\")\n" +
                "        override val legacy_id: kotlin.String?\n" +
                "            get() = payload.legacy_id" in typed,
        )
        val unknown = event.substringAfter("public class Unknown(").substringBefore("public class Payload")
        assertTrue("override val type: AdminSchema.WebhookEventType\n            get() = event.type" in unknown)
        assertTrue("override val livemode: kotlin.Boolean?\n            get() = event.livemode" in unknown)
    }

    @Test
    fun aPayloadItsTypesDoNotReadAndATypeTheBuildDoesNotKnowAreReadAsTheEnvelope() {
        val serializer = admin.getValue("WebhookEvent.kt").substringAfter("internal object Serializer")
        assertTrue("\"hut.created\" ->\n                        readOrNull {" in serializer)
        assertTrue(
            "decodeFromJsonElement(WebhookEvent.Payload.serializer(AdminSchema.HutEventObject.serializer(), " +
                "Unread), unread(json))" in
                serializer,
        )
        assertTrue("return typed ?: WebhookEvent.Unknown(untyped(input, json, type))" in serializer)
        assertTrue("return input.json.decodeFromJsonElement(AdminSchema.WebhookEvent.serializer(), json)" in serializer)
        assertTrue(
            "AdminSchema.WebhookEventData(`object` = AdminSchema.WebhookEventObject.Unknown(arrived))" in serializer,
        )
        assertTrue("payload.data.previous_attributes?.let { this.previous_attributes = it }" in serializer)
        assertTrue("type = AdminSchema.WebhookEventType(type)," in serializer)
        assertTrue("payload.livemode?.let { this.livemode = it }" in serializer)
        assertTrue("@kotlin.Suppress(\"DEPRECATION\")\n        private fun untyped(" in serializer)
        assertTrue(
            "kotlin.collections.listOf(\"id\", \"object\", \"type\", \"created_at\", \"data\", \"request_id\", " +
                "\"livemode\", \"legacy_id\")" in
                serializer,
        )
    }

    @Test
    fun aWebhookTheModelDoesNotDeclareAsTheEnvelopeAndItsObjectSayIsRefused() {
        val envelope =
            printing(
                resource("generator/model.json").replace(
                    "\"type\": \"hut.created\", \"envelope\": \"WebhookEvent\"",
                    "\"type\": \"hut.created\", \"envelope\": \"Page\"",
                ),
            )
        assertTrue(
            "rest.webhooks.hut.created: delivered in `Page`, and this printer prints the `WebhookEvent` " +
                "envelope alone" in
                envelope,
        )
        val objectName =
            printing(
                resource("generator/model.json").replace(
                    "\"envelope\": \"WebhookEvent\", \"object\": \"HutEventObject\", \"summary\": \"A hut was created.\"",
                    "\"envelope\": \"WebhookEvent\", \"object\": \"Gone\", \"summary\": \"A hut was created.\"",
                ),
            )
        assertTrue("rest.webhooks.hut.created: `Gone` names no object component" in objectName)
        val field =
            printing(
                resource(
                    "generator/model.json",
                ).replace("\"previousAttributes\": [\"count\",", "\"previousAttributes\": [\"beds\","),
            )
        assertTrue("rest.webhooks.hut.updated: the previous attribute `beds` is no field of `HutEventObject`" in field)
        val twice =
            printing(
                resource(
                    "generator/model.json",
                ).replace("\"previousAttributes\": [\"count\",", "\"previousAttributes\": [\"old\","),
            )
        assertTrue("rest.webhooks.hut.updated: a previous attribute named twice" in twice)
    }

    @Test
    fun aSetOfTypesTheEnvelopesTypeDisagreesWithIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "{ \"value\": \"hut.created\", \"name\": \"WebhookEventTypeHutCreated\" }",
                "{ \"value\": \"hut.opened\", \"name\": \"WebhookEventTypeHutOpened\" }",
            )
        assertEquals(
            "rest.webhooks and `AdminSchema.WebhookEventType` disagree: no webhook for hut.opened; no member for " +
                "hut.created; each type is one webhook and one member",
            printing(text),
        )
    }

    @Test
    fun twoTypesReadAsOneClassOrAsTheEventsOwnAreRefused() {
        val folded =
            resource("generator/model.json")
                .replace("{ \"type\": \"hut.created\",", "{ \"type\": \"hut_updated\",")
                .replace("{ \"value\": \"hut.created\",", "{ \"value\": \"hut_updated\",")
        assertTrue(
            "rest.webhooks.hut.updated: read as `HutUpdated`, the class `hut_updated` is read as" in printing(folded),
        )
        val own =
            resource("generator/model.json")
                .replace("{ \"type\": \"hut.created\",", "{ \"type\": \"unknown\",")
                .replace("{ \"value\": \"hut.created\",", "{ \"value\": \"unknown\",")
        assertTrue("rest.webhooks.unknown: read as `Unknown`, a class the event declares of its own" in printing(own))
        val listed =
            resource("generator/model.json").replace("{ \"type\": \"hut.created\",", "{ \"type\": \"hut.updated\",")
        assertTrue("rest.webhooks.hut.updated: a type listed twice" in printing(listed))
    }

    @Test
    fun aWebhookOnTheTileApiIsRefused() {
        val text =
            resource("generator/model.json").replace(
                "\"tiles\": {\n    \"origin\": \"https://tiles.example.com\",",
                "\"tiles\": {\n    \"origin\": \"https://tiles.example.com\",\n    \"webhooks\": [ " +
                    "{ \"type\": \"tile.made\", \"envelope\": \"WebhookEvent\", \"object\": \"Spec\" } ],",
            )
        assertEquals("tiles.webhooks: webhooks on a surface this printer prints none for", printing(text))
    }

    @Test
    fun anEnvelopeNoEventCouldBeHeldInIsRefused() {
        val member =
            printing(resource("generator/model.json").replace("{ \"name\": \"livemode\",", "{ \"name\": \"payload\","))
        assertTrue("AdminSchema.WebhookEvent.payload: read as a member an event holds of its own" in member)
        val event =
            printing(resource("generator/model.json").replace("{ \"name\": \"livemode\",", "{ \"name\": \"event\","))
        assertTrue("AdminSchema.WebhookEvent.event: read as a member an event holds of its own" in event)
        val objectComponent =
            printing(
                resource("generator/model.json").replace(
                    "{ \"name\": \"object\", \"required\": true, \"type\": { \"kind\": \"ref\", \"name\": \"WebhookEventObject\" }",
                    "{ \"name\": \"object\", \"required\": true, \"type\": { \"kind\": \"ref\", \"name\": \"HutEventObject\" }",
                ),
            )
        assertTrue("AdminSchema.WebhookEvent.data.object: neither a union nor any value" in objectComponent)
        val previous =
            printing(
                resource("generator/model.json").replace(
                    "\"type\": { \"kind\": \"map\", \"values\": { \"kind\": \"any\" } }, \"description\": \"The fields of",
                    "\"type\": { \"kind\": \"map\", \"values\": { \"kind\": \"string\" } }, \"description\": \"The fields of",
                ),
            )
        assertTrue("AdminSchema.WebhookEvent.data.previous_attributes: no map of any value" in previous)
        val type =
            printing(
                resource("generator/model.json").replace(
                    "{ \"name\": \"type\", \"required\": true, \"type\": { \"kind\": \"ref\", \"name\": \"WebhookEventType\" }",
                    "{ \"name\": \"type\", \"required\": true, \"type\": { \"kind\": \"integer\" }",
                ),
            )
        assertTrue("AdminSchema.WebhookEvent.type: neither a string nor an enum of strings" in type)
        val untyped =
            printing(
                resource("generator/model.json").replace(
                    "{ \"name\": \"type\", \"required\": true, \"type\": { \"kind\": \"ref\", \"name\": \"WebhookEventType\" }",
                    "{ \"name\": \"type\", \"required\": false, \"type\": { \"kind\": \"ref\", \"name\": \"WebhookEventType\" }",
                ),
            )
        assertTrue("AdminSchema.WebhookEvent: an envelope with no required `type`" in untyped)
    }

    /** Returns why the printer refuses [text]. */
    private fun printing(
        text: String,
        unit: ClientUnit = ClientUnit.Core,
    ): String =
        assertFailsWith<ModelException> {
            ClientPrinter.print(ModelReader.read(text), unit, DIGEST)
        }.message.orEmpty()

    /** The lines of the miniature model that place an operation of [namespace] in [unit]. */
    private fun unitOf(
        namespace: String,
        unit: String,
    ): String = "\"namespace\": \"$namespace\",\n        \"unit\": \"$unit\","

    private fun refusal(text: String): String =
        assertFailsWith<ModelException> { ModelReader.read(text) }.message.orEmpty()

    private fun resource(path: String): String =
        assertNotNull(javaClass.classLoader.getResource(path), "no test resource $path").readText()

    private companion object {
        /** The digest the miniature is printed beside, which no file of it is the digest of. */
        const val DIGEST = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
    }
}

/** The end of the miniature model's `hut.created` webhook, before the unit it states. */
private const val HUT_CREATED = "`data.object` is a `HutEventObject`.\""

/** The printed name of a success status and the headers it declares. */
private const val SUCCESS = "com.mountaya.client.SuccessResponse"
