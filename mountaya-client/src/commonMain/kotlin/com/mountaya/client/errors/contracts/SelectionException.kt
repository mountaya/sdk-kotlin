package com.mountaya.client

/**
 * SelectionException is a field selection the operation's field tree does not allow, refused at
 * the call rather than by the server.
 *
 * Its message names the field by its [path] and then says what is wrong with it.
 */
public class SelectionException(
    /**
     * The field the selection got wrong, named from the operation's result down, or empty where
     * the selection as a whole is wrong.
     */
    public val path: List<String>,
    message: String,
) : MountayaException(if (path.isEmpty()) message else "`${path.joinToString(".")}`: $message", null, null) {
    override val name: String get() = "SelectionError"
}
