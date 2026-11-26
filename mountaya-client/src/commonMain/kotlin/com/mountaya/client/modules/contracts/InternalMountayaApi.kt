package com.mountaya.client

/**
 * InternalMountayaApi marks a declaration one of Mountaya's modules reads of another's and no consumer does: public
 * to the compiler, since Kotlin's `internal` stops at its module, and refused to every caller that has not opted in,
 * which no app should. A module reading one opts in where it reads it, `@OptIn(InternalMountayaApi::class)`.
 *
 * The ABI dumps leave out every declaration it marks, so it is no part of the published API and may change in any
 * release. It is what `sdk-swift` writes `package` and `sdk-dart` keeps under `lib/src/` (SPEC.md section 7).
 */
@RequiresOptIn(
    message =
        "This is Mountaya's own API, which its modules read across their boundary: it is no part of the " +
            "published API, and may change in any release.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.TYPEALIAS,
)
@MustBeDocumented
public annotation class InternalMountayaApi
