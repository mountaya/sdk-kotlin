package com.mountaya.build.contract

/**
 * PublishedNames says how a name the registry publishes is looked for in what the modules declare (SPEC.md sections 5
 * and 9).
 *
 * The registry's `kotlin` column records each name exactly as this SDK spells it, a declaration's path
 * after its package in the committed ABI dumps, `Companion` left out: `MapPaint.standard`, and
 * `ApiException`, the suffix of the divergence of SPEC.md section 11. A name is declared where a dump
 * declares that path, and nowhere else: no case is folded and no suffix is read in another's place.
 */
object PublishedNames {
    /** Answers whether [api] declares [name], a spelling of the registry's `kotlin` column. */
    fun isDeclared(
        name: String,
        api: ApiDeclarations,
    ): Boolean = api.declares(name)
}
