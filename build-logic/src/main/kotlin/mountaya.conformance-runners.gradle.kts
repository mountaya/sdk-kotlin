import com.mountaya.build.contract.ConformanceRunnersExtension
import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.contract.GenerateConformanceTests
import com.mountaya.build.contract.ManifestTask
import com.mountaya.build.contract.MergeConformanceResults
import com.mountaya.build.contract.configure

// A module that runs conformance fixtures. Which registry entries are its own is the root build's
// table, read by the module's project name, and one test function per fixture case is generated
// over each runner it holds. A runner in the common tests runs on every target; one in the jvm
// tests runs its entry on the jvm alone.

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val conformanceRunners = extensions.create<ConformanceRunnersExtension>("conformanceRunners")
conformanceRunners.statesScopes.convention(false)

// The tests are printed into a package of the build's own: the runners' topic, with `generated` in
// place of their kind, which no hand-written file declares.
val testsPackage =
    conformanceRunners.runnerDirectory.map { "${it.substringBeforeLast('/').replace('/', '.')}.generated" }

fun runnersIn(sourceSet: String) =
    conformanceRunners.runnerDirectory.map { directory ->
        fileTree("src/$sourceSet/kotlin/$directory") { include("*Runner.kt") }
    }

val generateConformanceTests =
    tasks.register<GenerateConformanceTests>("generateConformanceTests") {
        description = "Generates one test per fixture case for each capability this module holds a runner for."
        ContractCheckout(project).configure(this, project)
        module.set(project.name)
        runnerPackage.set(conformanceRunners.runnerPackage)
        generatedPackage.set(testsPackage)
        statesScopes.set(conformanceRunners.statesScopes)
        runnerFiles.from(runnersIn("commonTest"))
        jvmRunnerFiles.from(runnersIn("jvmTest"))
        output.set(layout.buildDirectory.dir("generated/mountaya/conformanceTests/kotlin"))
        jvmOutput.set(layout.buildDirectory.dir("generated/mountaya/conformanceJvmTests/kotlin"))
        manifest.set(layout.buildDirectory.file("generated/mountaya/conformance-manifest.json"))
    }

kotlin {
    sourceSets {
        commonTest {
            kotlin.srcDir(generateConformanceTests.flatMap { it.output })
            dependencies {
                // The harness. The module that IS the harness already has it.
                if (project.path != ":conformance") implementation(project(":conformance"))
            }
        }
        matching { it.name == "jvmTest" }.configureEach {
            kotlin.srcDir(generateConformanceTests.flatMap { it.jvmOutput })
        }
    }
}

// The report comes from the jvm target, so that is the run the merge reads. It is a finaliser
// rather than a dependent, so a failing case still reaches the report as a `fail`.
val jvmResults = layout.buildDirectory.dir("test-results/jvmTest")

rootProject.tasks.withType<ManifestTask>().configureEach {
    manifests.from(generateConformanceTests.flatMap { it.manifest })
}
rootProject.tasks.named<MergeConformanceResults>("mergeConformanceResults") {
    results.from(jvmResults)
    mustRunAfter(tasks.named("jvmTest"))
}
tasks.named("jvmTest") {
    finalizedBy(rootProject.tasks.named("mergeConformanceResults"))
}
rootProject.tasks.named("conformance") {
    dependsOn(tasks.named("jvmTest"))
}

// The report is written from a run, never from whatever results an earlier run left behind:
// asking for it runs the jvm tests, and a case that fails there is a `fail` in the report rather
// than the end of the run that writes it.
rootProject.tasks.named("conformanceReport") {
    dependsOn(tasks.named("jvmTest"))
}
gradle.taskGraph.whenReady {
    if (hasTask(":conformanceReport")) {
        tasks.named<Test>("jvmTest") { ignoreFailures = true }
    }
}
