import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "de.shintaikan"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

plugins.withId("org.jetbrains.kotlin.multiplatform") {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

kotlin {
    jvm { withJava() }
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                // https://youtrack.jetbrains.com/issue/KTIJ-22262/Compose-IDE-False-positive-Cannot-access-class-androidxcomposeuigeometrySize-error#focus=Comments-27-6447983.0-0
                implementation("org.jetbrains.compose.ui:ui-graphics-desktop:${extra["compose.version"] as String}")
                implementation("org.jetbrains.compose.ui:ui-geometry-desktop:${extra["compose.version"] as String}")
                implementation("org.jetbrains.compose.foundation:foundation-desktop:${extra["compose.version"] as String}")
                //
                implementation("org.slf4j:slf4j-nop:2.0.3")
                implementation("org.jetbrains.exposed:exposed-core:${extra["exposed.version"] as String}")
                implementation("org.jetbrains.exposed:exposed-jdbc:${extra["exposed.version"] as String}")
                implementation("org.jetbrains.exposed:exposed-java-time:${extra["exposed.version"] as String}")
                implementation("org.postgresql:postgresql:42.5.1")
                implementation("org.apache.commons:commons-csv:1.9.0")
                implementation("cc.ekblad:4koma:1.1.0")
            }

        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.AppImage, TargetFormat.Rpm)
            packageName = "shintaikandesktop"
            packageVersion = "1.0.0"
            includeAllModules = true
        }

    }
}

tasks.register<WriteDateFile>("writeDateFile") {
    projectDir = getProjectDir()
}

tasks.register<WriteDateFileAndBuild>("writeDateFileAndBuild") {
    dependsOn("writeDateFile")
    dependsOn("packageReleaseUberJarForCurrentOS")
    tasks.findByName("packageReleaseUberJarForCurrentOS")?.mustRunAfter("writeDateFile")
}

abstract class WriteDateFileAndBuild : DefaultTask()

abstract class WriteDateFile : DefaultTask() {

    @InputDirectory
    var projectDir: File? = null

    @TaskAction
    fun createBuildDateFile() {
        val formatter = SimpleDateFormat("EEEE, dd.MM.yyyy HH:mm:ss")
        val dateString = formatter.format(Date()).toString()
        val dateFilePath = "$projectDir/src/jvmMain/resources"
        val dateFileName = "buildDate.txt"

        File("$dateFilePath/$dateFileName").writeText(dateString)
        println(dateString)
    }
}
