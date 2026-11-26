import com.mountaya.build.contract.ContractCheckout

plugins {
    id("mountaya.multiplatform-library")
    id("mountaya.contract-fixtures")
    id("mountaya.conformance-runners")
}

// The conformance harness. It is never published: it depends on the other modules as a
// consumer does, so a capability whose runner lives here is proven reachable from outside.

kotlin {
    sourceSets {
        commonMain.dependencies {
            // A runner takes the test's scope, for virtual time, so the type crosses.
            api(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(project(":mountaya-client"))
            // The admin unit, whose operations a transport fixture names and which receives webhooks.
            implementation(project(":mountaya-admin"))
            implementation(project(":mountaya-geo"))
            implementation(project(":mountaya-elements"))
        }
    }
}

conformanceRunners {
    // The root build's table gives this module the `api: true` entries of transport, geo and the
    // map, which a consumer calls, and they run here, outside every module they prove. This module
    // also states each scoped entry as a test of its own, so a scope is reported and never
    // skipped; a scoped entry needs no module to run it, and the coverage gate asks for none.
    runnerPackage.set("com.mountaya.conformance")
    runnerDirectory.set("com/mountaya/conformance/runners/rules")
    statesScopes.set(true)
}

// The model test reads `sdk/model.json`, and the runners' token test the elements' tokens table, from the
// contract checkout on the jvm, the one target that reads a file there. Both are declared inputs, so a moved
// document is never replayed green.
val model = ContractCheckout(project).directory.resolve("sdk/model.json")
val tokens = ContractCheckout(project).directory.resolve("sdk/elements/tokens.json")
tasks.named<Test>("jvmTest") {
    systemProperty("mountaya.model", model.absolutePath)
    systemProperty("mountaya.tokens", tokens.absolutePath)
    if (model.isFile) {
        inputs.file(model).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("model")
    }
    if (tokens.isFile) {
        inputs.file(tokens).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("tokens")
    }
}
