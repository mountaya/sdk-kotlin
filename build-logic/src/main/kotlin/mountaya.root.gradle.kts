import com.mountaya.build.MountayaBuildExtension
import com.mountaya.build.changelog.CheckChangelogTask
import com.mountaya.build.contract.CheckConformanceCoverage
import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.contract.CoverageGate
import com.mountaya.build.contract.MergeConformanceResults
import com.mountaya.build.contract.TrailScenario
import com.mountaya.build.contract.WriteConformanceReport
import com.mountaya.build.contract.configure
import com.mountaya.build.device.DeviceTestResults
import com.mountaya.build.gates.BoundaryGate
import com.mountaya.build.gates.DeterminismGate
import com.mountaya.build.gates.DocumentationGate
import com.mountaya.build.gates.FileStructureGate
import com.mountaya.build.gates.GateTask
import com.mountaya.build.gates.HotPathBudgetGate
import com.mountaya.build.gates.NamingGate
import com.mountaya.build.gates.SuppressionGate
import com.mountaya.build.generator.ClientPrinter
import com.mountaya.build.generator.ModelDigest
import com.mountaya.build.licence.CheckLicenceTask
import com.mountaya.build.licence.LicenceCheck
import com.mountaya.build.publication.CheckPublicationsTask
import com.mountaya.build.publication.CheckReleaseRepositoryTask
import com.mountaya.build.publication.Publication
import com.mountaya.build.publication.ReleaseRepository
import com.mountaya.build.readme.DocSnippets
import com.mountaya.build.readme.DocSnippetsTask
import com.mountaya.build.readme.Floors
import com.mountaya.build.readme.ReadmeSnippetsTask
import com.mountaya.build.readme.SkillSnippets
import com.mountaya.build.readme.SkillSnippetsTask
import com.mountaya.build.seams.BuildClock
import com.mountaya.build.size.CheckAppSizeTask
import com.mountaya.build.targets.MountayaTargets
import com.mountaya.build.trail.CheckTrailTask
import com.mountaya.build.trail.TrailCheck
import com.mountaya.build.wrapper.CheckWrapperTask

plugins {
    // For the root's own scripts: `settings.gradle.kts` and `build.gradle.kts`.
    id("org.jlleitschuh.gradle.ktlint")
}

// The repository's gates, registered once. Each module's convention plugin hands its own source
// trees, classpaths and test results to the tasks registered here, read from its Gradle model,
// so no task below keeps a list of modules.

val mountaya = extensions.create<MountayaBuildExtension>("mountaya")
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val checkout = ContractCheckout(project)
val targets = MountayaTargets.of(project)

ktlint {
    version.set(catalog.findVersion("ktlint").get().requiredVersion)
}

// The compiler's parser, for the gates' worker process alone.
val gateParser =
    configurations.create("gateParser") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }

dependencies {
    add(
        gateParser.name,
        "org.jetbrains.kotlin:kotlin-compiler-embeddable:${catalog.findVersion("kotlin").get().requiredVersion}",
    )
}

fun gate(
    name: String,
    about: String,
    exemptions: Provider<Map<String, String>>? = null,
) = tasks.register<GateTask>(name) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = about
    gate.set(name)
    parserClasspath.from(gateParser)
    report.set(layout.buildDirectory.file("reports/gates/$name.txt"))
    if (exemptions == null) this.exemptions.set(emptyMap()) else this.exemptions.set(exemptions)
    packageExemptions.set(emptyMap())
    ci.set(ContractCheckout.isCi(project))
}

val lintNaming = gate(NamingGate.NAME, "The naming law of SPEC section 5.", mountaya.namingExemptions)
val lintDocumentation =
    gate(
        DocumentationGate.NAME,
        "Every declaration a consumer reaches carries KDoc, as SPEC section 7 says.",
        mountaya.documentationExemptions,
    )
val lintFileStructure =
    gate(FileStructureGate.NAME, "The layout law of SPEC section 6.", mountaya.untestedFiles).apply {
        configure { packageExemptions.set(mountaya.packageExemptions) }
    }
val checkBoundaries = gate(BoundaryGate.NAME, "The boundary table of SPEC section 7, as a source scan.")
val checkSuppressions =
    gate(SuppressionGate.NAME, "Every suppression, counted against its exemption.", mountaya.suppressions)
val checkDeterminism = gate(DeterminismGate.NAME, "No read of the environment outside seams/.")

// The hot paths the contract lists for this SDK, each held to a live budget test in a test source set, device ones
// included (SPEC.md section 7): the list is read from the contract checkout, whose absence leaves each budget test
// held to its constant alone here and fails under CI.
val checkHotPathBudgets =
    gate(HotPathBudgetGate.NAME, "Every hot path the contract lists for this SDK keeps a live budget test.").apply {
        configure { checkout.hotPaths?.let { hotPaths.set(it) } }
    }

// Trail, `examples/trail`, held to the contract's `sdk/trail.yaml` (SPEC.md section 7): a marker for every step and none
// for a step it does not list, no value shaped as a key or a session token under `examples/`, the demo itinerary a copy
// byte for byte, and the examples' licence beside them. An absent checkout leaves the markers and the copy unchecked
// here and fails under CI.
val examplesDirectory = layout.projectDirectory.dir("examples")
val checkTrail =
    tasks.register<CheckTrailTask>(TrailCheck.NAME) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description =
            "Trail holds to the contract's sdk/trail.yaml: its steps marked, no key committed, its itinerary a copy."
        examples.set(examplesDirectory)
        sources.from(fileTree(examplesDirectory) { exclude(TrailCheck.UNREAD.map { "**/$it/**" }) })
        checkout.trail?.let { scenario.set(it) }
        checkout.trailItinerary?.let { itinerary.set(it) }
        ci.set(ContractCheckout.isCi(project))
    }

val checkWrapper =
    tasks.register<CheckWrapperTask>("checkWrapper") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Holds the Gradle wrapper to the Gradle mise.toml pins."
        miseFile.set(layout.projectDirectory.file("mise.toml"))
        wrapperProperties.set(layout.projectDirectory.file("gradle/wrapper/gradle-wrapper.properties"))
    }

// The names the registry publishes are looked for in the committed ABI dumps of every module, whichever
// targets this run builds, as the changelog's are (SPEC.md section 9), by the coverage gate and by the
// report's `published`, which the contract holds to the registry's `kotlin` column.
val abiDumps = fileTree(layout.projectDirectory) { include("mountaya-*/api/**/*.api") }

val mergeConformanceResults =
    tasks.register<MergeConformanceResults>("mergeConformanceResults") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Merges every module's jvm conformance results into one report."
        checkout.configure(this, project)
        dumps.from(abiDumps)
        contractRoot.set(checkout.directory)
        digestInputs.from(ModelDigest.INPUTS.map { checkout.directory.resolve(it) }.filter { it.isFile })
        val printed =
            layout.projectDirectory.file(
                "mountaya-client/src/commonMain/kotlin/${ClientPrinter.DIRECTORY}/SchemaVersion.kt",
            )
        if (printed.asFile.isFile) printedVersion.set(printed)
        report.set(layout.buildDirectory.file("reports/conformance/kotlin.json"))
    }

val checkConformanceCoverage =
    tasks.register<CheckConformanceCoverage>(CoverageGate.NAME) {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description =
            "Every capability of the registry has a runner or a scope, nothing answers to an id it dropped, " +
            "and every name it publishes is declared."
        checkout.configure(this, project)
        spec.set(layout.projectDirectory.file("SPEC.md"))
        dumps.from(abiDumps)
    }

tasks.register("conformance") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs every capability's fixture against this SDK on the jvm target, then the coverage gate."
    dependsOn(mergeConformanceResults, checkConformanceCoverage)
}

tasks.register<WriteConformanceReport>("conformanceReport") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description =
        "Writes the conformance report into the contract checkout. Nothing else writes outside this repository."
    merged.set(mergeConformanceResults.flatMap { it.report })
    destination.set(checkout.report)
}

// The licence of SPEC.md section 10: both texts at the root, the README linking them, every
// module's published metadata naming them, and each byte for byte the TypeScript packages' own
// where that checkout is beside this one. Each published module hands its metadata in.
val typescriptCheckout = layout.projectDirectory.dir("../sdk-typescript")
val checkLicence =
    tasks.register<CheckLicenceTask>("checkLicence") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "The licence texts, carried verbatim and named by the README and every module's metadata."
        root.set(layout.projectDirectory)
        documents.from((LicenceCheck.TEXTS + LicenceCheck.EXAMPLES).map { layout.projectDirectory.file(it) })
        documents.from(layout.projectDirectory.file("README.md"))
        typescript.set(typescriptCheckout)
        typescriptTexts.from(
            (LicenceCheck.TEXTS + LicenceCheck.EXAMPLES)
                .map {
                    typescriptCheckout.file(
                        it,
                    )
                }.filter { it.asFile.isFile },
        )
    }

// What Maven Central requires of every module's published metadata beside its licence (SPEC.md
// section 10): its name, description and page, its developer and its source repository. Each
// published module hands its metadata in, as it does to the licence gate.
val checkPublications =
    tasks.register<CheckPublicationsTask>("checkPublications") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Every module's published metadata states what Maven Central requires of it."
    }

// The changelog of SPEC.md section 10: its shape, and every name it gives one a module publishes,
// read off the committed ABI dumps of every module, whichever targets this run builds.
val checkChangelog =
    tasks.register<CheckChangelogTask>("checkChangelog") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "The changelog's shape, and every name it gives one the modules publish."
        changelog.set(layout.projectDirectory.file("CHANGELOG.md"))
        dumps.from(fileTree(layout.projectDirectory) { include("mountaya-*/api/**/*.api") })
        groupId.set(Publication.GROUP)
        modules.set(
            layout.projectDirectory.asFile
                .listFiles()
                .orEmpty()
                .filter { it.name.startsWith("mountaya-") && File(it, "api").isDirectory }
                .map { it.name },
        )
    }

// The release of SPEC.md section 10, which `mise run release` runs and the gate never does: every
// module published into the release repository, signed, the repository held to what the Central
// Portal validates, then zipped into the one bundle the Portal takes. Each published module hands
// its name and its publishing task to the check. A release is published with every target built.
val sdkVersion = catalog.findVersion("mountaya").get().requiredVersion
val checkReleaseRepository =
    tasks.register<CheckReleaseRepositoryTask>("checkReleaseRepository") {
        group = "publishing"
        description = "Holds the release repository to what the Central Portal validates."
        repository.set(ReleaseRepository.directory(project))
        groupId.set(Publication.GROUP)
        releaseVersion.set(sdkVersion)
        signed.set(ReleaseRepository.signed(project))
        machinePaths.set(ReleaseRepository.machinePaths(project))
    }
val releaseBundle = ReleaseRepository.bundle(project)
tasks.register<Zip>("centralBundle") {
    group = "publishing"
    description = "Publishes every module into the release repository, checks it, and zips it for the Central Portal."
    dependsOn(checkReleaseRepository)
    from(ReleaseRepository.directory(project)) { exclude(ReleaseRepository.UNREAD) }
    destinationDirectory.set(releaseBundle.parentFile)
    archiveFileName.set(releaseBundle.name)
}
// A publishing task is named after the repository it publishes into, as
// `publish<Publication>PublicationToReleaseRepository`.
val publishesRelease = "To${ReleaseRepository.NAME.replaceFirstChar(Char::uppercaseChar)}Repository"
gradle.taskGraph.whenReady {
    if (!targets.all && allTasks.any { it.name.endsWith(publishesRelease) }) {
        val switch = "-P${MountayaTargets.PROPERTY}=$targets"
        throw GradleException("A release is published with every target built, and $switch leaves some out.")
    }
}

// The floors a consumer's build meets, as the version catalog holds them (SPEC.md section 2): what the README's floors
// table and the developer skill are held to.
val catalogFloors = Floors.keys.associateWith { catalog.findVersion(it).get().requiredVersion }

// The README's Kotlin snippets, printed into the consumer build (SPEC.md section 10): the check is
// the gate's, and `printReadmeSnippets` prints them again after a deliberate change. The check also
// holds the README's floors table, its one prose copy of each floor, to the catalog's (section 2).
fun readmeSnippets(
    name: String,
    writes: Boolean,
    about: String,
) = tasks.register<ReadmeSnippetsTask>(name) {
    group = if (writes) "documentation" else LifecycleBasePlugin.VERIFICATION_GROUP
    description = about
    readme.set(layout.projectDirectory.file("README.md"))
    printed.set(
        layout.projectDirectory.file("consumer/src/commonMain/kotlin/com/example/consumer/readme/ReadmeSnippets.kt"),
    )
    packageName.set("com.example.consumer.readme")
    write.set(writes)
    rewrite.set("mise run readme")
    floors.set(catalogFloors)
}

val checkReadmeSnippets =
    readmeSnippets(
        "checkReadmeSnippets",
        false,
        "Fails where the consumer's printed snippets differ from the README's, or its floors table from the catalog's.",
    )
readmeSnippets("printReadmeSnippets", true, "Prints the README's Kotlin snippets into the consumer build.")

// The developer skill's Kotlin snippets (SPEC.md section 10), read from the `ai` checkout beside this one,
// or the one `-Pmountaya.aiDir` names, and printed into the consumer's generated sources, which it compiles
// beside the README's, never committed: each fence where its imports say, common code or the jvm's or
// Android's. The floors the skill states are held to the catalog's. An absent checkout is a note here and a
// failure under CI.
val namedCheckout = findProperty(SkillSnippetsTask.PROPERTY) as String?
val aiCheckout = rootProject.file(namedCheckout ?: SkillSnippetsTask.DEFAULT)
val checkSkillSnippets =
    tasks.register<SkillSnippetsTask>("checkSkillSnippets") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description =
            "Holds the Kotlin skill's floors to the catalog's and prints its snippets into the consumer, " +
            "which compiles them."
        checkout.set(aiCheckout)
        shownCheckout.set(if (namedCheckout == null) SkillSnippetsTask.DEFAULT else aiCheckout.path)
        documents.from(fileTree(aiCheckout.resolve(SkillSnippets.SKILL)) { include("**/*.md") })
        ci.set(ContractCheckout.isCi(project))
        floors.set(catalogFloors)
        dumps.from(abiDumps)
        packageName.set("com.example.consumer.readme")
        output.set(layout.projectDirectory.dir("consumer/build/generated/skill"))
    }

// The Kotlin fences of the Markdown documents `mise run snippets -- <file.md> [...]` hands in, through
// `-Pmountaya.snippets`, a documentation site's pages among them (SPEC.md section 10): printed into the consumer's build
// directory, never committed, each fence in a file of its own where its imports say, and compiled there against the
// modules' public surface for the jvm and Android, warnings as errors. The build names no document of its own, reads no
// path it is not handed, and no gate runs it: the consumer compiles the fences only where the property names some.
val snippetPaths =
    (findProperty(DocSnippetsTask.PROPERTY) as String?)
        ?.split(File.pathSeparatorChar)
        ?.filter { it.isNotEmpty() }
        .orEmpty()
val docSnippets = layout.projectDirectory.dir("consumer/build/generated/snippets")
val printDocSnippets =
    tasks.register<DocSnippetsTask>("printDocSnippets") {
        group = "documentation"
        description = "Prints the Kotlin fences of the documents -P${DocSnippetsTask.PROPERTY} names into the consumer."
        paths.set(snippetPaths)
        shownFrom.set(findProperty(DocSnippetsTask.SHOWN_FROM) as String? ?: rootDir.path)
        documents.from(snippetPaths.map(::File))
        dumps.from(abiDumps)
        output.set(docSnippets)
    }
tasks.register("checkDocSnippets") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description =
        "Compiles the Kotlin fences of the documents -P${DocSnippetsTask.PROPERTY} names against the modules, " +
        "for the jvm and Android: `mise run snippets -- <file.md> [...]`."
    dependsOn(printDocSnippets)
    gradle.includedBuilds.firstOrNull { it.name == "consumer" }?.let { consumer ->
        dependsOn(consumer.task(":compileCommonMainKotlinMetadata"), consumer.task(":compileKotlinJvm"))
        if (targets.android) dependsOn(consumer.task(":compileAndroidMain"))
    }
    val tally = docSnippets.file(DocSnippets.TALLY)
    doLast { logger.lifecycle(tally.asFile.readText().trimEnd()) }
}

// The outside-in builds of SPEC.md section 7, each an included build that depends on the modules by their coordinates
// and compiles against their public surface alone, warnings as errors: the consumer in `consumer/`, the README's
// snippets with it, and, where the android target is built, Trail in `examples/trail`, the sample app of the contract's
// `sdk/trail.yaml`, whose release build runs R8 over the four modules an app that draws a map and navigates declares and
// everything they carry, whose journey is compiled and packaged for a device, and whose session server's tests run.
// Each is included where this build is the one run; a build that includes this one, the consumer or Trail run on its
// own, builds it there.
val trail = gradle.includedBuilds.firstOrNull { it.name == "trail" }
val checkConsumer =
    tasks.register("checkConsumer") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description =
            "Builds the consumer and Trail against the modules' public surface, the README's and the skill's " +
            "snippets with them."
        dependsOn(checkReadmeSnippets, checkSkillSnippets)
        gradle.includedBuilds.firstOrNull { it.name == "consumer" }?.let { consumer ->
            dependsOn(consumer.task(":assemble"), consumer.task(":ktlintCheck"))
        }
        trail?.let { app ->
            dependsOn(
                app.task(":assembleRelease"),
                app.task(":assembleDebugAndroidTest"),
                app.task(":ktlintCheck"),
                app.task(":session:check"),
            )
        }
    }

// The app-size gate of SPEC.md section 7, where the android target is built: Trail's release build, the one
// `checkConsumer` makes, read package by package with the command-line tools' apkanalyzer, and each module's share and
// each library's the SDK brings held to the baseline beside the app, examples/trail/size/android.json, at the version of
// `sdk/trail.yaml` it records, a library's only at the version the baseline records it at, which the app's build writes
// from its release runtime classpath with that classpath's graph, where a library an SDK module brings that no share
// names fails it. `MOUNTAYA_SIZE_BASELINE=write` records the figures after a deliberate change, for a new version of the
// scenario, a new Android Gradle plugin or Kotlin, or for a library resolved at a new version.
val trailApp = examplesDirectory.dir(TrailCheck.TRAIL)
val checkAppSize =
    if (!targets.android) {
        null
    } else {
        tasks.register<CheckAppSizeTask>(CheckAppSizeTask.NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Holds the SDK's share of Trail's dex code, module by module, to its baseline."
            apk.set(trailApp.file("build/outputs/apk/release/trail-release-unsigned.apk"))
            mapping.set(trailApp.file("build/outputs/mapping/release/mapping.txt"))
            versions.set(trailApp.file("build/outputs/size/release-runtime-versions.txt"))
            graph.set(trailApp.file("build/outputs/size/release-runtime-graph.txt"))
            baseline.set(trailApp.file("size/android.json"))
            shownBaseline.set("examples/trail/size/android.json")
            scenario.set(providers.provider { checkout.trail?.let { TrailScenario.read(it).version } })
            ci.set(ContractCheckout.isCi(project))
            agp.set(catalog.findVersion("agp").get().requiredVersion)
            kotlin.set(catalog.findVersion("kotlin").get().requiredVersion)
            write.set(providers.environmentVariable("MOUNTAYA_SIZE_BASELINE").map { it == "write" }.orElse(false))
            androidHome.set(
                providers.environmentVariable("ANDROID_HOME").orElse(providers.environmentVariable("ANDROID_SDK_ROOT")),
            )
            trail?.let { app ->
                dependsOn(app.task(":assembleRelease"), app.task(":writeReleaseRuntimeVersions"))
            }
            mustRunAfter(checkConsumer)
        }
    }

// Build logic's own tests and formatting, as a task of this build so the order below can name it.
val checkBuildLogic =
    tasks.register("checkBuildLogic") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "The tests and the formatting of the build logic itself."
        dependsOn(gradle.includedBuild("build-logic").task(":check"))
    }

val sourceGates =
    listOf(
        checkWrapper,
        checkLicence,
        checkPublications,
        checkChangelog,
        checkReadmeSnippets,
        checkSkillSnippets,
        lintNaming,
        lintDocumentation,
        lintFileStructure,
        checkBoundaries,
        checkSuppressions,
        checkDeterminism,
        checkHotPathBudgets,
        checkTrail,
        tasks.named("ktlintCheck"),
    )

tasks.register("verify") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Every gate, in order. This is the gate."
    dependsOn(checkBuildLogic)
    dependsOn(sourceGates)
    dependsOn(checkConsumer)
    checkAppSize?.let { dependsOn(it) }
    dependsOn(mergeConformanceResults, checkConformanceCoverage)
}

// Coverage is asked last. It is the gate that is red while capabilities are still being written,
// and every other gate has to have answered before it does.
checkConformanceCoverage.configure {
    mustRunAfter(sourceGates)
    mustRunAfter(mergeConformanceResults, checkBuildLogic, checkConsumer)
    checkAppSize?.let { mustRunAfter(it) }
}

// Trail's journey of `sdk/trail.yaml` on a device, beside each module's device tests: AGP's test engine passes a run whose
// APK never installed, so the run is held to the reports it wrote since this build began, one that names no test failing
// (`DeviceTestResults`), as each module's run is. The build's start is read off its one clock seam, `BuildClock`.
val began = BuildClock.nowMillis()
val checkTrailJourney =
    trail?.let { app ->
        tasks.register("checkTrailJourney") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Trail's journey on a device: its run reports at least one test."
            dependsOn(app.task(":connectedDebugAndroidTest"))
            val reports = trailApp.dir("build/outputs/androidTest-results/connected")
            doLast {
                if (DeviceTestResults.testsRun(reports.asFile, began) == 0) {
                    throw GradleException(
                        "Trail's connectedDebugAndroidTest ran no test: its APK did not install, or the device " +
                            "stopped answering, as the log above says.",
                    )
                }
            }
        }
    }

tasks.register("verifyDevice") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "The tests that need an Android emulator running: each module's device tests and Trail's journey."
    checkTrailJourney?.let { dependsOn(it) }
    doFirst {
        if (!targets.android) {
            throw GradleException(
                "The android target is switched off (-P${MountayaTargets.PROPERTY}=$targets), so there is nothing to run on a device.",
            )
        }
    }
}
