package com.mountaya.build.licence

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LicenceCheckTest {
    private val pom =
        "<licenses><license><name>${LicenceCheck.NAME}</name><url>${LicenceCheck.URL}</url>" +
            "<comments>LICENSE.txt, with a French translation in LICENCE.fr.txt.</comments></license></licenses>"

    @Test
    fun bothTextsStandAtTheRootAndAreNamedByTheReadmeAndThePublishedMetadata() {
        val root = repository()
        val metadata = File(root, "pom.xml").apply { writeText(pom) }

        assertEquals(emptyList(), LicenceCheck.problems(root, listOf(metadata), null))

        File(root, "README.md").writeText("[LICENSE.txt](LICENSE.txt)")
        File(root, "LICENCE.fr.txt").delete()
        metadata.writeText("<licenses><license><name>MIT</name></license></licenses>")
        val problems = LicenceCheck.problems(root, listOf(metadata), null)
        assertTrue(problems.any { "LICENCE.fr.txt is missing" in it }, "$problems")
        assertTrue(problems.any { "does not name the licence." in it }, "$problems")
        assertTrue(problems.any { "does not name the licence's address, ${LicenceCheck.URL}." in it }, "$problems")
        assertTrue(problems.any { "does not name LICENSE.txt" in it }, "$problems")
        assertTrue(LicenceCheck.problems(root, emptyList(), null).any { "No published metadata" in it })
    }

    @Test
    fun eachTextIsTheTypeScriptPackagesOwnByteForByteWhereThatCheckoutIsBeside() {
        val root = repository()
        val metadata = File(root, "pom.xml").apply { writeText(pom) }
        val typescript = repository()

        assertEquals(emptyList(), LicenceCheck.problems(root, listOf(metadata), typescript))

        File(typescript, "LICENCE.fr.txt").writeText("LICENCE MOUNTAYA SDK\nVersion 1.0, retyped\n")
        assertEquals(
            listOf("LICENCE.fr.txt differs from sdk-typescript's, which it carries byte for byte."),
            LicenceCheck.problems(root, listOf(metadata), typescript),
        )
    }

    @Test
    fun theThirdPartyNoticesStandAtTheRootCarryingBothLicencesAndTheReadmeLinksThem() {
        val root = repository()
        val metadata = File(root, "pom.xml").apply { writeText(pom) }

        File(root, "THIRD-PARTY-NOTICES.txt").writeText("Lucide\n\nISC License\n")
        File(root, "README.md").writeText(
            "[LICENSE.txt](LICENSE.txt), [LICENCE.fr.txt](LICENCE.fr.txt), [examples](examples/LICENSE)",
        )
        assertEquals(
            listOf(
                "THIRD-PARTY-NOTICES.txt does not carry `The MIT License`.",
                "README.md does not link THIRD-PARTY-NOTICES.txt.",
            ),
            LicenceCheck.problems(root, listOf(metadata), null),
        )
        File(root, "THIRD-PARTY-NOTICES.txt").delete()
        assertTrue(
            LicenceCheck.problems(root, listOf(metadata), null).any { "is missing from the repository root" in it },
        )
    }

    /**
     * Trail and every example after it are MIT licensed, as sdk-typescript's are, so a developer copies their code
     * freely: the licence stands beside them, the README links it, and it is sdk-typescript's own byte for byte.
     */
    @Test
    fun theExamplesLicenceIsMitLinkedByTheReadmeAndTheTypeScriptExamplesOwnByteForByte() {
        val root = repository()
        val metadata = File(root, "pom.xml").apply { writeText(pom) }
        val typescript = repository()

        assertEquals(emptyList(), LicenceCheck.problems(root, listOf(metadata), typescript))

        File(typescript, "examples/LICENSE").writeText("MIT License\n\nCopyright (c) 2025 Mountaya SAS\n")
        assertEquals(
            listOf("examples/LICENSE differs from sdk-typescript's, which it carries byte for byte."),
            LicenceCheck.problems(root, listOf(metadata), typescript),
        )

        File(root, "examples/LICENSE").writeText("Apache License\n")
        File(root, "README.md").writeText(
            "[LICENSE.txt](LICENSE.txt), [LICENCE.fr.txt](LICENCE.fr.txt), [notices](THIRD-PARTY-NOTICES.txt)",
        )
        assertEquals(
            listOf("examples/LICENSE is not the MIT licence.", "README.md does not link examples/LICENSE."),
            LicenceCheck.problems(root, listOf(metadata), null),
        )

        File(root, "examples/LICENSE").delete()
        assertEquals(
            listOf("examples/LICENSE is missing: the examples are MIT licensed, as sdk-typescript's are."),
            LicenceCheck.problems(root, listOf(metadata), null),
        )
    }

    /** A directory holding both texts, the third-party notices, the examples' licence, and a README that links all four. */
    private fun repository(): File {
        val root = Files.createTempDirectory("licence").toFile()
        File(root, "LICENSE.txt").writeText("MOUNTAYA SDK LICENSE\nVersion 1.0\n")
        File(root, "LICENCE.fr.txt").writeText("LICENCE MOUNTAYA SDK\nVersion 1.0\n")
        File(root, "THIRD-PARTY-NOTICES.txt").writeText("ISC License\n\nThe MIT License (MIT)\n")
        File(root, "examples").mkdirs()
        File(root, "examples/LICENSE").writeText("MIT License\n\nCopyright (c) 2026 Mountaya SAS\n")
        File(root, "README.md").writeText(
            "[LICENSE.txt](LICENSE.txt), [LICENCE.fr.txt](LICENCE.fr.txt), [notices](THIRD-PARTY-NOTICES.txt), " +
                "[examples](examples/LICENSE)",
        )
        return root
    }
}
