import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.generator.ClientPrinter
import com.mountaya.build.generator.ClientUnit
import com.mountaya.build.generator.GenerateClient

// The generated surface of one unit of the client: printed from the contract checkout's `sdk/model.json` by
// `generateClient`, and held to it by `checkGeneratedClient`, which `check` runs, so a model that moved without a
// reprint fails the gate. The model states the unit of every REST operation, webhook event and component, and each
// unit is a module of its own (SPEC.md section 2): `mountaya-client` prints the core, and `mountaya-admin` the admin
// unit.

val checkoutDirectory = ContractCheckout(project).directory
val modelFile = checkoutDirectory.resolve("sdk/model.json")
val printedUnit =
    when (project.name) {
        "mountaya-client" -> ClientUnit.Core
        "mountaya-admin" -> ClientUnit.Admin
        else -> throw GradleException("${project.path} is no unit of the client, and prints no generated surface.")
    }
val generatedPackage = layout.projectDirectory.dir("src/commonMain/kotlin/${ClientPrinter.directoryOf(printedUnit)}")

fun GenerateClient.configureFrom(project: Project) {
    if (modelFile.isFile) model.set(modelFile)
    checkout.set(checkoutDirectory)
    generated.set(generatedPackage)
    unit.set(printedUnit.wire)
    ci.set(ContractCheckout.isCi(project))
}

tasks.register<GenerateClient>("generateClient") {
    group = "build"
    description =
        "Prints this unit's generated surface from sdk/model.json of the contract checkout. --check compares instead."
    configureFrom(project)
}

val checkGeneratedClient =
    tasks.register<GenerateClient>("checkGeneratedClient") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Fails where the generated package differs from what sdk/model.json prints."
        configureFrom(project)
        check.set(true)
    }

tasks.named("check") { dependsOn(checkGeneratedClient) }

// The facts test reads `sdk/model.json` and `VERSION` from the contract checkout on the jvm, the one
// target that reads a file there, and the elements' tables beside the model, whose digest the printed
// `MODEL_DIGEST` is. Each is a declared input, so a moved model or table is never replayed green.
val versionFile = checkoutDirectory.resolve("VERSION")
val elementsDirectory = checkoutDirectory.resolve("sdk/elements")
tasks.matching { it.name == "jvmTest" }.withType<Test>().configureEach {
    systemProperty("mountaya.model", modelFile.absolutePath)
    systemProperty("mountaya.version", versionFile.absolutePath)
    if (modelFile.isFile) inputs.file(modelFile).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("model")
    if (elementsDirectory.isDirectory) {
        inputs.dir(elementsDirectory).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("elements")
    }
    if (versionFile.isFile) {
        inputs
            .file(
                versionFile,
            ).withPathSensitivity(PathSensitivity.RELATIVE)
            .withPropertyName("version")
    }
}
