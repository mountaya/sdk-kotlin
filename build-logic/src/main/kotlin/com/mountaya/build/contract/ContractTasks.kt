package com.mountaya.build.contract

import com.mountaya.build.generator.ModelDigest
import com.mountaya.build.generator.ModelException
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * ContractTask is what every task that reads the contract checkout declares.
 *
 * The checkout is a declared input, by relative path, so Gradle never replays a green result
 * against a registry or a fixture that moved under it.
 */
abstract class ContractTask : DefaultTask() {
    /** `sdk/registry.yaml`, unset when the checkout is absent. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val registry: RegularFileProperty

    /** `sdk/fixtures`, unset when the checkout is absent. */
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val fixtures: DirectoryProperty

    /** Whether an absent checkout is refused. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** The scoped layers, each with the title of its `SPEC.md` section 11 bullet. */
    @get:Input
    abstract val scopedLayers: MapProperty<String, String>

    /** The scoped capabilities, each with the title of its section 11 bullet. */
    @get:Input
    abstract val scopedCapabilities: MapProperty<String, String>

    /** The module that runs each layer's `api: true` entries, from the root build's table. */
    @get:Input
    abstract val apiRunnerModules: MapProperty<String, String>

    /** The module that runs each layer's `api: false` entries, from the root build's table. */
    @get:Input
    abstract val internalRunnerModules: MapProperty<String, String>

    /** Returns the registry, or null when the checkout is absent and that is tolerated. */
    protected fun readRegistry(): ContractRegistry? {
        val file = registry.orNull?.asFile
        if (file == null) {
            if (ci.get()) {
                throw GradleException(
                    "The contract checkout is absent. A green run here has to mean the fixtures ran.",
                )
            }
            return null
        }
        return ContractRegistry.read(file)
    }

    /** Returns the scope table. */
    protected fun scopes(): Scopes = Scopes(scopedLayers.get(), scopedCapabilities.get())

    /** Returns the ownership table. */
    protected fun ownership(): Ownership = Ownership(apiRunnerModules.get(), internalRunnerModules.get())

    /** Returns the fixture a capability names. */
    protected fun fixtureOf(capability: Capability): File {
        val file = File(fixtures.get().asFile, capability.fixture)
        if (!file.isFile) {
            throw GradleException(
                "`${capability.id}` names the fixture ${capability.fixture}, which is not in the checkout.",
            )
        }
        return file
    }
}

/** GenerateContractFixtures compiles the registry and every fixture into the conformance harness. */
abstract class GenerateContractFixtures : ContractTask() {
    /** The source directory the generated file is written under. */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** Writes the embedded contract. */
    @TaskAction
    fun generate() {
        val contract = readRegistry()
        val texts = contract?.capabilities.orEmpty().associate { it.fixture to fixtureOf(it).readText() }
        val directory = File(output.get().asFile, EmbeddedContract.PACKAGE.replace('.', '/'))
        output.get().asFile.deleteRecursively()
        directory.mkdirs()
        File(directory, EmbeddedContract.FILE).writeText(EmbeddedContract.render(contract, texts))
    }
}

/** GenerateConformanceTests generates a module's conformance tests and the manifest that describes them. */
abstract class GenerateConformanceTests : ContractTask() {
    /** The module the tests are generated for, by the name the ownership table knows it by. */
    @get:Input
    abstract val module: Property<String>

    /** The package the module's runners are declared in, the module's own, which a generated test imports them from. */
    @get:Input
    abstract val runnerPackage: Property<String>

    /** The package the tests are generated into. */
    @get:Input
    abstract val generatedPackage: Property<String>

    /** Whether this module states the scoped capabilities as tests. */
    @get:Input
    abstract val statesScopes: Property<Boolean>

    /**
     * The runner files of the module's common tests, whose entries run on every target. A runner
     * is found by its file, so no list of them is kept.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val runnerFiles: ConfigurableFileCollection

    /** The runner files of the module's jvm tests, whose entries run on the jvm alone. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val jvmRunnerFiles: ConfigurableFileCollection

    /** The source directory the common tests are written under. */
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    /** The source directory the jvm tests are written under. */
    @get:OutputDirectory
    abstract val jvmOutput: DirectoryProperty

    /** The manifest of what was generated. */
    @get:OutputFile
    abstract val manifest: RegularFileProperty

    /** Writes the tests and the manifest. */
    @TaskAction
    fun generate() {
        val generated =
            ConformanceTests.generate(
                ConformanceTests.Request(
                    module = module.get(),
                    ownership = ownership(),
                    registry = readRegistry(),
                    scopes = scopes(),
                    casesOf = { capability -> caseNames(fixtureOf(capability)) },
                    runnerFiles = runnerNames(runnerFiles),
                    jvmRunnerFiles = runnerNames(jvmRunnerFiles),
                    runnerPackage = runnerPackage.get(),
                    generatedPackage = generatedPackage.get(),
                    statesScopes = statesScopes.get(),
                ),
            )
        write(output, generated.files)
        write(jvmOutput, generated.jvmFiles)
        manifest.get().asFile.apply {
            parentFile.mkdirs()
            writeText(generated.manifest.encode())
        }
    }

    private fun runnerNames(files: ConfigurableFileCollection): List<String> =
        files.files.filter { it.name.endsWith("Runner.kt") }.map { it.nameWithoutExtension }

    private fun write(
        root: DirectoryProperty,
        sources: Map<String, String>,
    ) {
        val directory = File(root.get().asFile, generatedPackage.get().replace('.', '/'))
        root.get().asFile.deleteRecursively()
        directory.mkdirs()
        for ((name, source) in sources) File(directory, name).writeText(source)
    }

    private fun caseNames(fixture: File): List<String> {
        val root = JsonSlurper().parse(fixture)
        val cases =
            (root as? Map<*, *>)?.get("cases") as? List<*>
                ?: throw GradleException("${fixture.name}: no `cases` list.")
        return cases.map { case ->
            (case as? Map<*, *>)?.get("name") as? String
                ?: throw GradleException("${fixture.name}: a case with no `name`.")
        }
    }
}

/** ManifestTask is what the tasks that read every module's manifest declare. */
abstract class ManifestTask : ContractTask() {
    /** The manifest of every module that runs conformance tests. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifests: ConfigurableFileCollection

    /** Returns the manifests, decoded. */
    protected fun readManifests(): List<ConformanceManifest> =
        manifests.files.sortedBy { it.path }.map(ConformanceManifest::decode)
}

/**
 * CheckConformanceCoverage is the coverage gate: every capability has a runner or a scope, nothing answers to an id the
 * registry dropped, and every name a capability this SDK carries is published under is one a module declares.
 */
abstract class CheckConformanceCoverage : ManifestTask() {
    /** `SPEC.md`, whose section 11 every scope is held to. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val spec: RegularFileProperty

    /** Every published module's committed ABI dumps, the jvm's, Android's and the klib's. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dumps: ConfigurableFileCollection

    /** Runs the gate, prints its line, and fails with what it found. */
    @TaskAction
    fun check() {
        val result =
            CoverageGate.run(
                readRegistry(),
                scopes(),
                ownership(),
                readManifests(),
                spec.get().asFile.readText(),
                ci.get(),
                ApiDeclarations.read(dumps.files.sortedBy { it.path }.map { it.readText() }),
            )
        logger.lifecycle(result.summary)
        if (result.violations.isNotEmpty()) throw GradleException(result.report())
    }
}

/** MergeConformanceResults merges every module's jvm test results into one conformance report. */
abstract class MergeConformanceResults : ManifestTask() {
    /** The test result directories of every module that runs conformance tests. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val results: ConfigurableFileCollection

    /**
     * Every published module's committed ABI dumps, the jvm's, Android's and the klib's, which say what
     * the report's `published` holds.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dumps: ConfigurableFileCollection

    /** The contract checkout's root, whose files the report's `modelDigest` is the digest of. */
    @get:Internal
    abstract val contractRoot: DirectoryProperty

    /** The files of the checkout [ModelDigest.INPUTS] names, so a moved model is never replayed into a report. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val digestInputs: ConfigurableFileCollection

    /**
     * The core's printed `SchemaVersion.kt`, whose `MODEL_DIGEST` names the files the surface under test was printed
     * from, unset where the module is not in this build.
     */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val printedVersion: RegularFileProperty

    /** Where the merged report is written, inside this build. */
    @get:OutputFile
    abstract val report: RegularFileProperty

    /** Writes the report. */
    @TaskAction
    fun merge() {
        val file = report.get().asFile
        val contract = readRegistry()
        if (contract == null) {
            file.delete()
            logger.lifecycle("mergeConformanceResults: the contract checkout is absent, so there is no report.")
            return
        }
        // The report names the files on disk, and its ticks were earned by a surface printed from files of its own:
        // a report is written only where the two are one (`sdk/SPEC.md` section 11).
        val digest =
            try {
                ModelDigest.of(contractRoot.get().asFile)
            } catch (refused: ModelException) {
                throw GradleException("mergeConformanceResults: ${refused.message}", refused)
            }
        val printed =
            printedVersion.orNull
                ?.asFile
                ?.takeIf { it.isFile }
                ?.readText()
                ?.let { PRINTED_DIGEST.find(it)?.groupValues?.get(1) }
        if (printed != digest) {
            throw GradleException(
                "mergeConformanceResults: the surface under test was printed from files whose modelDigest is " +
                    "${printed ?: "unknown"}, and the contract checkout's files are $digest. " +
                    "Run `mise run generate`, then the conformance again.",
            )
        }
        val statuses =
            ConformanceReport.statuses(
                contract,
                scopes(),
                readManifests(),
                ConformanceReport.passedTests(results.files),
            )
        val api = ApiDeclarations.read(dumps.files.sortedBy { it.path }.map { it.readText() })
        file.parentFile.mkdirs()
        file.writeText(
            ConformanceReport.render(statuses, contract.version, digest, ConformanceReport.published(contract, api)),
        )
        val counts =
            statuses.values
                .groupingBy { it }
                .eachCount()
                .toSortedMap()
        logger.lifecycle("mergeConformanceResults: ${counts.entries.joinToString(", ") { "${it.value} ${it.key}" }}")
    }
}

/** The digest the core's printed `SchemaVersion.kt` publishes as `MODEL_DIGEST`. */
private val PRINTED_DIGEST = Regex("""MODEL_DIGEST: kotlin\.String = "([0-9a-f]{64})"""")

/**
 * WriteConformanceReport copies the merged report into the contract checkout.
 *
 * This is the only task that writes outside this repository, and it runs only when asked for by
 * name: a plain test run never writes into another repository.
 */
abstract class WriteConformanceReport : DefaultTask() {
    /** The merged report. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val merged: RegularFileProperty

    /** `sdk/reports/kotlin.json` in the contract checkout. */
    @get:Internal
    abstract val destination: RegularFileProperty

    /** Writes the report. */
    @TaskAction
    fun write() {
        val target = destination.get().asFile
        if (!target.parentFile.isDirectory) {
            throw GradleException("${target.parentFile} is not there. The report is written into a contract checkout.")
        }
        target.writeText(merged.get().asFile.readText())
        logger.lifecycle("conformanceReport: wrote $target")
    }
}
