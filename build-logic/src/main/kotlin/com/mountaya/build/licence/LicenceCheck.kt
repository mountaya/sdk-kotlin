package com.mountaya.build.licence

import java.io.File

/**
 * LicenceCheck is the licence of SPEC section 10: the Mountaya SDK License 1.0, in English and in French at the
 * repository root, carried verbatim from the TypeScript packages and named by the README and by every module's
 * published metadata, by its name, both texts and the address it is published at.
 */
object LicenceCheck {
    /** The two texts, at the repository root. */
    val TEXTS: List<String> = listOf("LICENSE.txt", "LICENCE.fr.txt")

    /** The name every module's published metadata gives the licence. */
    const val NAME = "Mountaya SDK License, version 1.0"

    /**
     * The address of the English text, which every module's published metadata names the licence by:
     * Mountaya's page of it, a copy of the text the TypeScript packages ship.
     */
    const val URL = "https://www.mountaya.com/en/legal/sdk-license/"

    /** The address of the French translation, which every published licence's comment names. */
    const val FRENCH_URL = "https://www.mountaya.com/fr/legal/sdk-license/"

    /**
     * The third-party notices at the root: the licences of the glyph markup `mountaya-geo` publishes,
     * printed from the contract's notice, which the README links.
     */
    const val THIRD_PARTY_NOTICES = "THIRD-PARTY-NOTICES.txt"

    /** The licences the third-party notices carry, by the line each opens with. */
    private val THIRD_PARTY_LICENCES = listOf("ISC License", "The MIT License")

    /**
     * The examples' licence, beside them: MIT, so a developer copies Trail's code into an app of their own freely, the
     * text sdk-typescript's examples carry, byte for byte.
     */
    const val EXAMPLES = "examples/LICENSE"

    /** The words the examples' licence opens with. */
    private const val MIT = "MIT License"

    /** The words both texts carry on their second line. */
    private const val VERSION = "Version 1.0"

    /**
     * Returns every way [root] falls short: a text missing or of another version, a README that
     * does not link a text or the third-party notices, notices missing or without the ISC and MIT
     * licences, the examples' licence missing, not MIT's or not linked, a published metadata file in
     * [poms] that does not name the licence, its address or both texts, and, where [typescript] is the
     * TypeScript checkout beside this one, a text or the examples' licence that is not its own byte
     * for byte.
     */
    fun problems(
        root: File,
        poms: List<File>,
        typescript: File?,
    ): List<String> {
        val problems = mutableListOf<String>()
        val readme = File(root, "README.md").takeIf { it.isFile }?.readText()
        if (readme == null) problems += "README.md is missing."
        for (text in TEXTS) {
            val file = File(root, text)
            if (!file.isFile) {
                problems += "$text is missing from the repository root."
                continue
            }
            if (VERSION !in file.readText()) problems += "$text is not version 1.0 of the licence."
            if (readme != null && "($text)" !in readme) problems += "README.md does not link $text."
            val theirs = typescript?.let { File(it, text) }
            if (theirs != null && !(theirs.isFile && theirs.readBytes().contentEquals(file.readBytes()))) {
                problems += "$text differs from sdk-typescript's, which it carries byte for byte."
            }
        }
        val notices = File(root, THIRD_PARTY_NOTICES).takeIf { it.isFile }?.readText()
        if (notices == null) {
            problems += "$THIRD_PARTY_NOTICES is missing from the repository root."
        } else {
            for (licence in THIRD_PARTY_LICENCES) {
                if (licence !in notices) problems += "$THIRD_PARTY_NOTICES does not carry `$licence`."
            }
        }
        if (readme != null &&
            "($THIRD_PARTY_NOTICES)" !in readme
        ) {
            problems += "README.md does not link $THIRD_PARTY_NOTICES."
        }
        val examples = File(root, EXAMPLES)
        if (!examples.isFile) {
            problems += "$EXAMPLES is missing: the examples are MIT licensed, as sdk-typescript's are."
        } else {
            if (!examples.readText().startsWith(MIT)) problems += "$EXAMPLES is not the MIT licence."
            if (readme != null && "($EXAMPLES)" !in readme) problems += "README.md does not link $EXAMPLES."
            val theirs = typescript?.let { File(it, EXAMPLES) }
            if (theirs != null && !(theirs.isFile && theirs.readBytes().contentEquals(examples.readBytes()))) {
                problems += "$EXAMPLES differs from sdk-typescript's, which it carries byte for byte."
            }
        }
        if (poms.isEmpty()) problems += "No published metadata was handed to the check."
        for (pom in poms) {
            val text = pom.readText()
            if ("<name>$NAME</name>" !in text) problems += "${pom.path} does not name the licence."
            if ("<url>$URL</url>" !in text) problems += "${pom.path} does not name the licence's address, $URL."
            for (licence in TEXTS) if (licence !in text) problems += "${pom.path} does not name $licence."
        }
        return problems
    }
}
