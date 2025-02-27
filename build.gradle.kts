import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(compose.desktop.linux_x64)
    implementation(compose.desktop.linux_arm64)
    // https://youtrack.jetbrains.com/issue/KTIJ-22262/Compose-IDE-False-positive-Cannot-access-class-androidxcomposeuigeometrySize-error#focus=Comments-27-6447983.0-0
    implementation("org.jetbrains.compose.ui:ui-graphics-desktop:${project.extra["compose.version"] as String}")
    implementation("org.jetbrains.compose.ui:ui-geometry-desktop:${project.extra["compose.version"] as String}")
    implementation("org.jetbrains.compose.foundation:foundation-desktop:${project.extra["compose.version"] as String}")
    //
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.slf4j:slf4j-nop:2.0.7")
    implementation("org.jetbrains.exposed:exposed-core:${project.extra["exposed.version"] as String}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${project.extra["exposed.version"] as String}")
    implementation("org.jetbrains.exposed:exposed-java-time:${project.extra["exposed.version"] as String}")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("cc.ekblad:4koma:1.2.0")
    implementation("cc.ekblad.konbini:konbini:0.1.2")
    implementation("net.time4j:time4j-base:5.9.3")
    implementation("org.jetbrains.compose.material:material-icons-extended:${project.extra["compose.version"] as String}")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Xmx1G")
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
        val dateFilePath = "$projectDir/src/main/resources"
        val dateFileName = "buildDate.txt"

        File("$dateFilePath/$dateFileName").writeText(dateString)
        println(dateString)
    }
}
