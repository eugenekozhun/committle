import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mockkVersion = "1.13.12"
val junitVersion = "5.11.0"
val kotlinxSerializationJson = "1.7.3"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.intellij") version "1.17.4"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"

    kotlin("plugin.serialization") version "2.1.21"
}

group = "com.kozhun"
version = "2.3.1"

sourceSets["main"].java.srcDirs("src/main/gen")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJson")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

intellij {
    version.set("2023.1.5")
    type.set("IC")

    plugins.set(
        listOf(
            "Git4Idea"
        )
    )
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("253.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

detekt {
    source.setFrom("src/main/kotlin")
    config.setFrom("detekt-config.yml")
    buildUponDefaultConfig = true
}
