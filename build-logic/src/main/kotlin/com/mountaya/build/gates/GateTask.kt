package com.mountaya.build.gates

import com.mountaya.build.contract.HotPaths
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * GateTask runs one source gate over every module's source trees.
 *
 * The trees are handed over by each module's own convention plugin, read from its Gradle model.
 * The gate itself runs in a worker process that is handed the compiler's parser, so the parser
 * never shares a class loader with the build's own Kotlin plugin.
 */
abstract class GateTask : DefaultTask() {
    /** Which gate this task runs: one of the `NAME` constants of the gate objects. */
    @get:Input
    abstract val gate: Property<String>

    /** The source trees, each encoded by [SourceTree.encode]. */
    @get:Input
    abstract val trees: ListProperty<String>

    /** The files under those trees, declared so a moved or edited file reruns the gate. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /** The gate's exemptions: what is exempt, and the sentence that justifies it. */
    @get:Input
    abstract val exemptions: MapProperty<String, String>

    /** The files the layout gate lets declare a package other than their module's, each with its sentence. */
    @get:Input
    abstract val packageExemptions: MapProperty<String, String>

    /**
     * The contract's `sdk/hot-paths.yaml`, which the hot-path gate holds the test sources to: unset for every other
     * gate, and where the contract checkout is absent.
     */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val hotPaths: RegularFileProperty

    /** Whether a gate that reads the contract checkout fails where it is absent, as a run under CI does. */
    @get:Input
    abstract val ci: Property<Boolean>

    /** The compiler's parser, resolved by the root build. */
    @get:Classpath
    abstract val parserClasspath: ConfigurableFileCollection

    /** Where the run's report is written, pass or fail. */
    @get:OutputFile
    abstract val report: RegularFileProperty

    /** The executor the gate's work runs on, in a process of its own. */
    @get:Inject
    abstract val workers: WorkerExecutor

    /** Runs the gate, prints its summary, and fails with every violation it found. */
    @TaskAction
    fun run() {
        // The contract's list is read here, where its reader is, and handed to the worker one entry a line: a list
        // of another shape fails the run, since one misread would hold this SDK to nothing.
        val listed =
            hotPaths.orNull?.asFile?.let { file ->
                try {
                    HotPaths.read(file).map { it.encode() }
                } catch (refused: IllegalArgumentException) {
                    throw GradleException(refused.message ?: "${file.path} is no list of hot paths.", refused)
                }
            }
        val queue = workers.processIsolation { classpath.from(parserClasspath) }
        queue.submit(GateWork::class.java) {
            gate.set(this@GateTask.gate)
            trees.set(this@GateTask.trees)
            exemptions.set(this@GateTask.exemptions)
            packageExemptions.set(this@GateTask.packageExemptions)
            hotPaths.set(listed.orEmpty())
            hotPathsRead.set(listed != null)
            required.set(ci)
            report.set(this@GateTask.report)
        }
        queue.await()

        val lines = report.get().asFile.readLines()
        logger.lifecycle(lines.first())
        if (lines.size > 1) throw GradleException(lines.joinToString("\n"))
    }
}

/** GateParameters is what a gate's worker is handed. */
interface GateParameters : WorkParameters {
    /** Which gate to run. */
    val gate: Property<String>

    /** The encoded source trees. */
    val trees: ListProperty<String>

    /** The gate's exemptions. */
    val exemptions: MapProperty<String, String>

    /** The files the layout gate lets declare a package other than their module's. */
    val packageExemptions: MapProperty<String, String>

    /** The contract's hot paths, each encoded by [HotPath.encode], which the hot-path gate holds the tests to. */
    val hotPaths: ListProperty<String>

    /** Whether the contract's list of hot paths was read, which it is not where the checkout is absent. */
    val hotPathsRead: Property<Boolean>

    /** Whether a list that was not read fails the run, as it does under CI. */
    val required: Property<Boolean>

    /** Where to write the report. */
    val report: RegularFileProperty
}

/** GateWork runs a gate inside the worker process and writes its report. */
abstract class GateWork : WorkAction<GateParameters> {
    override fun execute() {
        val trees =
            parameters.trees
                .get()
                .map(SourceTree::decode)
                .filter { it.directory.isDirectory }
        val exemptions = parameters.exemptions.get()
        val result =
            when (val gate = parameters.gate.get()) {
                NamingGate.NAME -> {
                    NamingGate.run(trees, exemptions)
                }

                DocumentationGate.NAME -> {
                    DocumentationGate.run(trees, exemptions)
                }

                FileStructureGate.NAME -> {
                    FileStructureGate.run(trees, exemptions, parameters.packageExemptions.get())
                }

                BoundaryGate.NAME -> {
                    BoundaryGate.run(trees)
                }

                SuppressionGate.NAME -> {
                    SuppressionGate.run(trees, exemptions)
                }

                DeterminismGate.NAME -> {
                    DeterminismGate.run(trees)
                }

                HotPathBudgetGate.NAME -> {
                    val listed = parameters.hotPaths.get().map(HotPath::decode)
                    HotPathBudgetGate.run(
                        trees,
                        listed.takeIf { parameters.hotPathsRead.get() },
                        parameters.required.get(),
                    )
                }

                else -> {
                    error("No gate is named $gate.")
                }
            }
        parameters.report.get().asFile.apply {
            parentFile.mkdirs()
            writeText(result.report() + "\n")
        }
    }
}
