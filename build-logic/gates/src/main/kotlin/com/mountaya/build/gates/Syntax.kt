package com.mountaya.build.gates

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierListOwner

/** The 1-based line [this] element starts on, for a message a reader can jump to. */
internal fun PsiElement.line(): Int = containingFile.text.take(textOffset).count { it == '\n' } + 1

/** Whether [this] declaration is private, and so is no name anyone outside its file reads. */
internal fun KtModifierListOwner.isPrivate(): Boolean = hasModifier(KtTokens.PRIVATE_KEYWORD)

/**
 * Whether a consumer can name [this] declaration: neither it nor anything enclosing it is
 * private, internal or protected.
 */
internal fun PsiElement.isPublished(): Boolean =
    generateSequence(this) { it.parent }.filterIsInstance<KtModifierListOwner>().none { owner ->
        owner.hasModifier(KtTokens.PRIVATE_KEYWORD) ||
            owner.hasModifier(KtTokens.INTERNAL_KEYWORD) ||
            owner.hasModifier(KtTokens.PROTECTED_KEYWORD)
    }

/** Splits an identifier into its camel-case words: `onQueryChange` is `on`, `Query`, `Change`. */
internal fun String.camelWords(): List<String> =
    Regex("[A-Z]+(?![a-z0-9])|[A-Z]?[a-z0-9]+")
        .findAll(this)
        .map { it.value }
        .toList()
