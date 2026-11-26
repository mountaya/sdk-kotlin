import com.mountaya.build.licence.CheckLicenceTask
import com.mountaya.build.licence.LicenceCheck
import com.mountaya.build.publication.CheckPublicationsTask
import com.mountaya.build.publication.CheckReleaseRepositoryTask
import com.mountaya.build.publication.Publication
import com.mountaya.build.publication.ReleaseRepository
import com.mountaya.build.targets.MountayaTargets
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

// A module a consumer depends on: its coordinates, the metadata Maven Central requires of each of
// its publications, their signatures, the release repository they are published into, and its
// public API held to the dumps committed beside it (SPEC.md sections 7 and 10).
//
// No task of the gate publishes, and nothing writes outside this repository: `mise run release`
// publishes every module into the release repository, signed, and hands the Central Portal that
// repository as one bundle. The consumer build resolves the modules through the composite build,
// never through `~/.m2`.

plugins {
    `maven-publish`
    signing
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

// One version across the repository, and one group: `com.mountaya:<module>` (SPEC.md section 10).
group = Publication.GROUP
version = catalog.findVersion("mountaya").get().requiredVersion

// Maven Central requires a javadoc jar beside every artifact, and a Kotlin module has no Javadoc:
// each publication carries a jar of its own holding a README that says where the API is documented,
// the KDoc in its sources jar, as Maven Central's requirements allow. A jar of its own, since one
// file shared by every publication would be signed by each of them into the same signature.
val javadocReadme = layout.buildDirectory.file("release/javadoc/README.md")
val writeJavadocReadme =
    tasks.register("writeJavadocReadme") {
        description = "Writes the README every javadoc jar of this module holds."
        val text = Publication.javadocReadme(project.name, version.toString())
        inputs.property("text", text)
        outputs.file(javadocReadme)
        doLast { javadocReadme.get().asFile.writeText(text) }
    }

publishing {
    publications.withType<MavenPublication>().configureEach {
        val publication = name
        val javadocJar =
            tasks.register<Jar>("${publication}JavadocJar") {
                description = "The javadoc jar of the $publication publication."
                archiveBaseName.set("${project.name}-$publication")
                archiveClassifier.set("javadoc")
                from(writeJavadocReadme)
            }
        artifact(javadocJar)
        pom {
            name.set(project.name)
            description.set(project.provider { project.description })
            url.set(Publication.PAGE)
            licenses {
                license {
                    // The text the TypeScript packages ship, at the repository root in English and in
                    // French, and published at its address for every consumer of a POM.
                    name.set(LicenceCheck.NAME)
                    url.set(LicenceCheck.URL)
                    distribution.set("repo")
                    comments.set(
                        "LICENSE.txt, with a French translation in LICENCE.fr.txt (${LicenceCheck.FRENCH_URL}). " +
                            "Where the two differ, the English text prevails.",
                    )
                }
            }
            developers {
                developer {
                    id.set(Publication.DEVELOPER_ID)
                    name.set(Publication.DEVELOPER_NAME)
                    email.set(Publication.DEVELOPER_EMAIL)
                    organization.set(Publication.DEVELOPER_NAME)
                    organizationUrl.set(Publication.URL)
                }
            }
            scm {
                url.set(Publication.SCM_URL)
                connection.set(Publication.SCM_CONNECTION)
                developerConnection.set(Publication.SCM_CONNECTION)
            }
        }
    }
    // The one repository a module is published into, which only a release asks for.
    repositories {
        maven {
            name = ReleaseRepository.NAME
            url = uri(ReleaseRepository.directory(project))
        }
    }
}

// Every publication is signed with the key the release hands over in the environment, held in
// memory and never written to disk. A dry run with no key asks for none (`-Pmountaya.unsigned`);
// anything else that publishes fails without one.
signing {
    val key = providers.environmentVariable(ReleaseRepository.SIGNING_KEY).orNull
    if (key != null) {
        useInMemoryPgpKeys(
            providers.environmentVariable(ReleaseRepository.SIGNING_KEY_ID).orNull,
            key,
            providers.environmentVariable(ReleaseRepository.SIGNING_PASSWORD).orNull.orEmpty(),
        )
    }
    isRequired = ReleaseRepository.signed(project)
    sign(publishing.publications)
}

// The gates read every publication's metadata: the licence gate for the licence, and the
// publication gate for what else Maven Central requires.
rootProject.tasks.named<CheckLicenceTask>("checkLicence") {
    poms.from(tasks.withType<GenerateMavenPom>())
}
rootProject.tasks.named<CheckPublicationsTask>("checkPublications") {
    poms.from(tasks.withType<GenerateMavenPom>())
}

// The release repository is checked once every module has published into it.
val moduleName = project.name
rootProject.tasks.named<CheckReleaseRepositoryTask>("checkReleaseRepository") {
    modules.add(moduleName)
    dependsOn(tasks.named("publishAllPublicationsToReleaseRepository"))
}

// The public API's baseline: the Kotlin Gradle plugin's own ABI dumps, one for the jvm, one for
// android and one klib dump for the iOS targets, committed under `api/` and compared by `check`.
// `./gradlew updateKotlinAbi` rewrites them after a deliberate change, and the diff is the review.
// What one module reads of another's and no consumer does, `InternalMountayaApi`, is left out of every
// dump: it is public to the compiler alone, and no part of the published API (SPEC.md section 7).
extensions.configure<KotlinMultiplatformExtension> {
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        filters {
            exclude {
                annotatedWith.add("com.mountaya.client.InternalMountayaApi")
            }
        }
    }
}

// The dumps are laid out for every target. With the jvm alone the plugin reads the layout of a
// single target, which the committed dumps are not in, so a jvm-only bring-up run checks no dump;
// any run that builds a second target checks the dumps of the targets it builds. A dump is written
// with every target built, so no run rewrites the baseline of a target it left out. An Android
// module's dump is its one target's, and the module is in the build only where android is.
val targets = MountayaTargets.of(project)
tasks.named("checkKotlinAbi") {
    onlyIf("the dumps are laid out for more than one target") { targets.android || targets.ios }
}
tasks.named("updateKotlinAbi") {
    doFirst {
        if (!targets.all) {
            throw GradleException(
                "The ABI dumps are written with every target built, and -P${MountayaTargets.PROPERTY}=$targets leaves some out.",
            )
        }
    }
}
