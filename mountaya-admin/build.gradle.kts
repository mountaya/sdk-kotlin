plugins {
    id("mountaya.multiplatform-library")
    id("mountaya.published-module")
    id("mountaya.generated-client")
    id("org.jetbrains.kotlin.plugin.serialization")
}

description =
    "Mountaya's admin unit: the Map Studio API's organization half, a session token's mint and webhook receiving."

kotlin {
    sourceSets {
        commonMain.dependencies {
            // The unit declares `admin` on the core's client and answers its types, so the core is part of its API.
            api(project(":mountaya-client"))
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
