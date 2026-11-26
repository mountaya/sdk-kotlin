package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `transport.request.form-parts`: the case's parts written through the transport's own
 * `multipart/form-data` writer, then read back off the bytes it wrote, part by part, as a server
 * reads them: each part's `Content-Disposition` as written, keyed by the form field its `name`
 * names, and its `Content-Type`, null where it carries none.
 *
 * A text part is a [MultipartPart.Text]; a file part is a [MultipartPart.File] holding the file's text as its
 * bytes, with the file name and the media type the case gives it, null where it gives none.
 */
object TransportRequestFormPartsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val parts =
            case.fields.items("parts").map { written ->
                val part = written as? FixtureValue.Object ?: throw AssertionError("a part is not an object")
                val text = part.textOrNull("text")
                if (text != null) {
                    MultipartPart.Text(part.text("name"), text)
                } else {
                    MultipartPart.File(
                        part.text("name"),
                        part.text("file").encodeToByteArray(),
                        filename = part.textOrNull("filename"),
                        contentType = part.textOrNull("contentType"),
                    )
                }
            }
        val written = FormBody.write(parts)
        val boundary =
            written.contentType.substringAfter("boundary=", "").ifEmpty {
                throw AssertionError("the body's `Content-Type` names no boundary: ${written.contentType}")
            }
        val read =
            readParts(written.bytes.decodeToString(), boundary).map { headers ->
                val disposition = headers["content-disposition"] ?: throw AssertionError("a part names no field")
                val field =
                    FIELD.find(disposition)?.groupValues?.get(1)
                        ?: throw AssertionError("the disposition `$disposition` names no form field")
                field to
                    FixtureValue.Object(
                        listOf(
                            "contentType" to (headers["content-type"]?.let(FixtureValue::Text) ?: FixtureValue.Null),
                            "disposition" to FixtureValue.Text(disposition),
                        ),
                    )
            }
        if (read.map { it.first }.toSet().size != read.size) throw AssertionError("two parts fill one form field")
        return ConformanceOutcome.Value(
            FixtureValue.Object(listOf("parts" to FixtureValue.Object(read.sortedBy { it.first }))),
        )
    }

    /** The field a disposition names, as `name="…"` writes it. */
    private val FIELD = Regex("""^form-data; name="([^"]*)"""")

    /**
     * The headers of each part [body] holds between the delimiters of [boundary], each name in ASCII
     * lowercase, failing where the body is not the parts its boundary frames and its close.
     */
    private fun readParts(
        body: String,
        boundary: String,
    ): List<Map<String, String>> {
        val delimiter = "--$boundary"
        if (!body.startsWith("$delimiter\r\n")) throw AssertionError("the body does not open on its boundary")
        if (!body.endsWith("\r\n$delimiter--\r\n")) throw AssertionError("the body does not close on its boundary")
        val inside = body.removePrefix("$delimiter\r\n").removeSuffix("\r\n$delimiter--\r\n")
        return inside.split("\r\n$delimiter\r\n").map { part ->
            val head = part.substringBefore("\r\n\r\n", missingDelimiterValue = "")
            if (head.isEmpty()) throw AssertionError("a part holds no headers before its content")
            head.split("\r\n").associate { line ->
                val name = line.substringBefore(':', missingDelimiterValue = "")
                if (name.isEmpty()) throw AssertionError("the part's header line `$line` is no header")
                HeaderLookup.asciiLowercase(name) to line.substringAfter(':').trim()
            }
        }
    }
}
