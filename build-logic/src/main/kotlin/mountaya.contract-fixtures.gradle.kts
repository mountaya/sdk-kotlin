import com.mountaya.build.contract.ContractCheckout
import com.mountaya.build.contract.GenerateContractFixtures
import com.mountaya.build.contract.configure

// Compiles the contract checkout's registry and fixtures into this module's common code, so the
// same harness runs the same fixtures on every target, a simulator and a device included.

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val generateContractFixtures =
    tasks.register<GenerateContractFixtures>("generateContractFixtures") {
        description = "Embeds sdk/registry.yaml and sdk/fixtures of the contract checkout."
        ContractCheckout(project).configure(this, project)
        output.set(layout.buildDirectory.dir("generated/mountaya/contract/kotlin"))
    }

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(generateContractFixtures.flatMap { it.output })
        }
    }
}
