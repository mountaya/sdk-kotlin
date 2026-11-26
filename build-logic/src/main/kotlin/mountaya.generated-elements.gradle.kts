import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.generator.ElementsPrinter
import com.mountaya.build.generator.GenerateElements
import com.mountaya.build.publication.CheckPublicationsTask
import com.mountaya.build.targets.MountayaTargets
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// The elements' contract data `mountaya-geo` publishes: printed from the contract checkout's
// `sdk/elements/` by `generateElements`, with the third-party notices of the glyph markup at the
// repository root, and held to it by `checkGeneratedElements`, which `check` runs, so a table that
// moved without a reprint fails the gate (SPEC.md section 3).

val checkoutDirectory = ContractCheckout(project).directory
val elementsDirectory = checkoutDirectory.resolve("sdk/elements")
val modelFile = checkoutDirectory.resolve("sdk/model.json")
val generatedPackage = layout.projectDirectory.dir("src/commonMain/kotlin/${ElementsPrinter.DIRECTORY}")
val noticesFile = rootProject.layout.projectDirectory.file(ElementsPrinter.NOTICES_FILE)

fun GenerateElements.configureFrom(project: Project) {
    if (elementsDirectory.isDirectory) elements.set(elementsDirectory)
    if (modelFile.isFile) model.set(modelFile)
    checkout.set(checkoutDirectory)
    generated.set(generatedPackage)
    notices.set(noticesFile)
    ci.set(ContractCheckout.isCi(project))
}

tasks.register<GenerateElements>("generateElements") {
    group = "build"
    description = "Prints the elements' data from sdk/elements/ of the contract checkout. --check compares instead."
    configureFrom(project)
}

val checkGeneratedElements =
    tasks.register<GenerateElements>("checkGeneratedElements") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Fails where the elements' data or the notices differ from what sdk/elements/ prints."
        configureFrom(project)
        check.set(true)
    }

tasks.named("check") { dependsOn(checkGeneratedElements) }

// Every archive mountaya-geo publishes for the jvm and Android carries the licences of the markup its
// glyph sets hold, at one path no other library's notices take, from the one file at the root.
val noticesResources = layout.buildDirectory.dir("generated/thirdPartyNotices")
val syncThirdPartyNotices =
    tasks.register<Sync>("syncThirdPartyNotices") {
        description = "Copies the third-party notices to where the published archives pack them."
        from(noticesFile) { into(ElementsPrinter.NOTICES_ENTRY.substringBeforeLast('/')) }
        into(noticesResources)
    }
extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.named("commonMain") { resources.srcDir(syncThirdPartyNotices) }
}

// The publication gate opens each such archive for the notices.
val targets = MountayaTargets.of(project)
rootProject.tasks.named<CheckPublicationsTask>("checkPublications") {
    noticeArchives.from(tasks.named("jvmJar"))
    if (targets.android) noticeArchives.from(tasks.named("bundleAndroidMainAar"))
}

// The data test reads `sdk/elements/` from the contract checkout on the jvm, the one target that reads
// a file there. The tables are a declared input, so a moved table is never replayed green.
tasks.matching { it.name == "jvmTest" }.withType<Test>().configureEach {
    systemProperty("mountaya.elements", elementsDirectory.absolutePath)
    if (elementsDirectory.isDirectory) {
        inputs.dir(elementsDirectory).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("elements")
    }
}
