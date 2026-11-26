package com.mountaya.conformance

import com.mountaya.client.DataOperation
import com.mountaya.client.FieldSelection
import com.mountaya.client.MountayaException
import kotlinx.coroutines.test.TestScope

/**
 * `transport.data.selection`: the case's operation is built from its name, its input's type and its field
 * tree, each as the case's input names it, and the case is answered with the document a call sends for its
 * selection, `FULL` standing for the operation's own full document, or with the class it is refused with.
 *
 * The tree is written as `sdk/model.json` writes an operation's selection, an array of fields in the tree's
 * order, each its `name` and, where it holds fields, its `fields`. The selection is an object whose key order
 * says nothing, `true` selecting a field whole.
 */
object TransportDataSelectionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val operation =
            DataOperation(
                input.text("operation"),
                input.text("inputType"),
                FieldSelection { tree(input.items("fields")) },
            )
        val selecting = input.objectOrNull("select")?.let { FieldSelection { select(it) } }
        return try {
            val document = operation.document(selecting)
            val isFull = selecting == null && document == operation.document
            ConformanceOutcome.Value(FixtureValue.Text(if (isFull) "FULL" else document))
        } catch (refusal: MountayaException) {
            ConformanceOutcome.Refused(refusal.name)
        }
    }

    /** A field tree as the model writes one: each field its `name`, and its `fields` where it holds any. */
    private fun FieldSelection.Builder.tree(level: List<FixtureValue>) {
        for (item in level) {
            val node = item as? FixtureValue.Object ?: throw AssertionError("a field of the tree is not an object")
            val name = node.text("name")
            if (node["fields"] == null) field(name) else field(name) { tree(node.items("fields")) }
        }
    }

    /** A selection written as the fixture writes one: an object per level, `true` selecting a field whole. */
    private fun FieldSelection.Builder.select(level: FixtureValue.Object) {
        for ((name, value) in level.entries) {
            when (value) {
                is FixtureValue.Bool -> if (value.value) field(name)
                is FixtureValue.Object -> field(name) { select(value) }
                else -> throw AssertionError("`$name` is neither a boolean nor an object")
            }
        }
    }
}
