import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mockkVersion = "1.13.13"
val junitVersion = "5.11.4"
val kotlinxSerializationJson = "1.10.0"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
    id("org.jetbrains.changelog") version "2.5.0"

    kotlin("plugin.serialization") version "2.3.20"
}

group = "com.kozhun"
version = providers.gradleProperty("pluginVersion").get()

sourceSets {
    main {
        java.srcDirs("src/main/gen")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.4")
        jetbrainsRuntime()
        bundledPlugin("Git4Idea")
        testFramework(TestFrameworkType.Platform)
    }

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJson")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

changelog {
    version.set(project.version.toString())
    path.set("${project.projectDir}/CHANGELOG.md")
}

intellijPlatform {
    buildSearchableOptions.set(false)

    pluginConfiguration {
        version.set(project.version.toString())

        ideaVersion {
            sinceBuild.set("253")
            untilBuild.set("261.*")
        }

        changeNotes.set(provider {
            val item = changelog.getOrNull(project.version.toString())
                ?: changelog.getUnreleased()

            changelog.renderItem(
                item.withHeader(false).withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })
    }

    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

tasks {
    runIde {
        systemProperty("idea.platform.prefix", "Idea")
    }

    prepareJarSearchableOptions {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        options.release.set(21)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
        }
    }

    generateParser {
        sourceFile.set(file("src/main/kotlin/com/kozhun/commitmessagetemplate/language/grammar/CMT.bnf"))
        targetRootOutputDir.set(file("src/main/gen"))
        pathToParser.set("com/kozhun/commitmessagetemplate/language/parser/CMTParser.java")
        pathToPsiRoot.set("com/kozhun/commitmessagetemplate/language/psi")
        purgeOldFiles.set(true)
    }

    generateLexer {
        sourceFile.set(file("src/main/kotlin/com/kozhun/commitmessagetemplate/language/lexer/CMT.flex"))
        targetOutputDir.set(file("src/main/gen/com/kozhun/commitmessagetemplate/language/lexer"))
        purgeOldFiles.set(true)
    }
}

detekt {
    source.setFrom("src/main/kotlin")
    config.setFrom("detekt-config.yml")
    buildUponDefaultConfig = true
}
