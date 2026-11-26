package com.mountaya.build.generator

import java.io.File
import java.security.MessageDigest

/**
 * ModelDigest is the digest of what this repository prints from, `modelDigest` (`sdk/SPEC.md` section 11): the files of
 * the contract checkout every printer here reads, the model and the elements' data, named exactly, since the tag
 * `contract` names cannot tell a print of a tag from one of a commit between two.
 *
 * The definition is the contract's, and any language reproduces it with a plain SHA-256: for each input in the
 * order [INPUTS] gives, its path from the checkout's root, `/`-separated as written there, a line feed, its length
 * in bytes in decimal digits, a line feed, then its bytes exactly as the checkout holds them, all fed to one
 * SHA-256, written as 64 lowercase hexadecimal characters. The length frames each file, so no file's bytes read as
 * the next one's path.
 */
object ModelDigest {
    /** The inputs of the digest, each a path from the checkout's root, in the order it reads them. */
    val INPUTS: List<String> =
        listOf(
            "sdk/model.json",
            "sdk/elements/words.json",
            "sdk/elements/tokens.json",
            "sdk/elements/glyphs.json",
            "sdk/elements/NOTICE",
        )

    /**
     * Returns the digest of the inputs under [checkout], the contract checkout's root, refusing one that is missing.
     */
    fun of(checkout: File): String =
        of(
            INPUTS.map { path ->
                val file = File(checkout, path)
                if (!file.isFile) throw ModelException("$path: not in the contract checkout, and the digest reads it")
                path to file.readBytes()
            },
        )

    /** Returns the digest of [inputs], each a path and its bytes, in the order given. */
    fun of(inputs: List<Pair<String, ByteArray>>): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        for ((path, bytes) in inputs) {
            sha256.update("$path\n${bytes.size}\n".toByteArray(Charsets.UTF_8))
            sha256.update(bytes)
        }
        return sha256.digest().joinToString("") { "%02x".format(it) }
    }

    /** Answers whether [digest] is written as the definition writes one: 64 lowercase hexadecimal characters. */
    fun isWellFormed(digest: String): Boolean = digest.length == 64 && digest.all { it in '0'..'9' || it in 'a'..'f' }
}
