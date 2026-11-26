package com.mountaya.client

/**
 * Returns the digest a persisted-query request sends for [document]: SHA-256 over the document's
 * exact UTF-8 bytes, in lowercase hexadecimal.
 *
 * The server recomputes the digest from the bytes it stores and compares the two strings, so
 * any other digest is not slower but wrong. Whitespace is part of the document: two documents
 * that differ only in it are two documents, with two digests.
 */
internal fun persistedQueryDigest(document: String): String = sha256(document.encodeToByteArray()).toHexString()
