package com.mountaya.build.gates

import org.jetbrains.kotlin.CoreEnvironmentDeprecation
import org.jetbrains.kotlin.cli.extensionsStorage
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File

/**
 * KotlinParser parses Kotlin sources with the compiler's own parser.
 *
 * A gate that read sources with regular expressions would take a keyword inside a string for a
 * declaration and a commented-out import for a real one. The parser reads a file exactly as the
 * compiler does, and nothing here resolves a type: every gate asks a question the syntax answers.
 *
 * The compiler marks its standalone environment, the configuration it is built from and the
 * storage of plugin extensions as APIs it will rework. A gate needs only the parser behind them,
 * and they are the one way to reach it.
 */
@OptIn(CoreEnvironmentDeprecation::class, CompilerConfiguration.Internals::class, ExperimentalCompilerApi::class)
class KotlinParser : AutoCloseable {
    private val disposable = Disposer.newDisposable("mountaya-gates")
    private val factory: KtPsiFactory

    init {
        val configuration = CompilerConfiguration()
        // No compiler plugin is loaded, and the environment still asks where their extensions live.
        configuration.extensionsStorage = CompilerPluginRegistrar.ExtensionStorage()
        val environment =
            KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES,
            )
        factory = KtPsiFactory(environment.project, markGenerated = false)
    }

    /** Parses [file] into its syntax tree. */
    fun parse(file: File): KtFile = factory.createFile(file.name, file.readText().replace("\r\n", "\n"))

    override fun close() {
        Disposer.dispose(disposable)
    }
}
