import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    jvm {
/*        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }*/
        withJava()
    }
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.slf4j:slf4j-nop:1.7.36")
                implementation("org.jetbrains.exposed:exposed-core:0.39.2")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.39.2")
                implementation("org.jetbrains.exposed:exposed-java-time:0.39.2")
                implementation("org.postgresql:postgresql:42.3.3")
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

