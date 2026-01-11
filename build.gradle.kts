import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mockkVersion = "1.13.13"
val junitVersion = "5.11.4"
val kotlinxSerializationJson = "1.8.0"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.10.5"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
    id("org.jetbrains.changelog") version "2.5.0"

    kotlin("plugin.serialization") version "2.3.0"
}

group = "com.kozhun"
version = providers.gradleProperty("pluginVersion").get()

sourceSets {
    main {
        java.srcDirs("src/main/gen")
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
        intellijIdeaCommunity("2023.1.5")
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
    pluginConfiguration {
        version.set(project.version.toString())

        ideaVersion {
            sinceBuild.set("231")
            untilBuild.set("253.*")
        }

        changeNotes.set(provider {
            changelog.renderItem(
                changelog.get(project.version.toString())
                    .withHeader(false)
                    .withEmptySections(false),
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
