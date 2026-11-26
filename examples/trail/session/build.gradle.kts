import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Trail's session server: a JVM program on the platform's own HTTP server, `com.sun.net.httpserver`, that mints each
// session token the app asks for under the secret key, through the admin unit, so the secret key never reaches the
// device. It depends on `mountaya-admin` alone, which brings the client, and the app never depends on it.
// `mise run trail:session` runs it.

plugins {
    // On the build's classpath already, at the catalog's Kotlin, which the app's build script puts there.
    id("org.jetbrains.kotlin.jvm")
    application
    id("org.jlleitschuh.gradle.ktlint")
}

val sdkVersion = libs.versions.mountaya.get()

java {
    val target =
        JavaVersion.toVersion(
            libs.versions.jvm.target
                .get(),
        )
    sourceCompatibility = target
    targetCompatibility = target
}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(true)
        jvmTarget.set(
            JvmTarget.fromTarget(
                libs.versions.jvm.target
                    .get(),
            ),
        )
    }
}

dependencies {
    implementation("com.mountaya:mountaya-admin:$sdkVersion")
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.trail.session.MainKt")
}

tasks.test {
    useJUnit()
}

ktlint {
    version.set(libs.versions.ktlint.asProvider())
}
