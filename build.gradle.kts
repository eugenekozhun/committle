import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val mockkVersion = "1.13.13"
val junitVersion = "5.11.4"
val kotlinxSerializationJson = "1.8.0"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.19.0"
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
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

configurations.named("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2023.3.8")
        bundledPlugin("Git4Idea")
        testFramework(TestFrameworkType.Platform)
        testFramework(
            TestFrameworkType.Starter,
            version = "262.11879.100",
            configurationName = "integrationTestImplementation"
        )
    }

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJson")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:$mockkVersion")

    add("integrationTestImplementation", "org.kodein.di:kodein-di-jvm:7.20.2")
    add("integrationTestImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    "integrationTestRuntimeOnly"("org.jetbrains.teamcity:serviceMessages:2024.07")
}

intellijPlatformTesting.testIdeUi.register("integrationTest") {
    task {
        val integrationTestSourceSet = sourceSets.getByName("integrationTest")
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath
        javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(25)) })
        systemProperty("e2e.ide.version", "2026.2.4")
        System.getenv("E2E_IDE_PATH")?.let { systemProperty("e2e.ide.path", it) }
        useJUnitPlatform()
    }
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
            sinceBuild.set("233")
            untilBuild.set("262.*")
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
    prepareJarSearchableOptions {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        options.release.set(17)
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
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
