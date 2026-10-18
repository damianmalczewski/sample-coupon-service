import com.diffplug.spotless.LineEnding
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("jvm-test-suite")
    alias(libs.plugins.idea.ext)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kover)
    alias(libs.plugins.spotless)
    alias(libs.plugins.spring.boot)
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
    compilerOptions {
        javaParameters = true
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.problem4j.spring.bom))
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.springdoc.openapi.bom))

    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.micrometer.registry.prometheus)

    implementation(libs.problem4j.spring.webmvc)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    implementation(libs.caffeine)
    implementation(libs.flyway.database.postgresql)

    runtimeOnly(libs.postgresql)

    testImplementation(platform(libs.junit.bom))
    testImplementation(platform(libs.kotest.bom))

    testImplementation(libs.spring.boot.starter.actuator.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.starter.data.redis.test)
    testImplementation(libs.spring.boot.starter.flyway.test)
    testImplementation(libs.spring.boot.starter.validation.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.spring.boot.testcontainers)

    testImplementation(libs.testcontainers.postgresql)

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotest.runner.junit6)

    testImplementation(libs.archunit)
    testImplementation(libs.testclock)
    testImplementation(libs.wiremock.spring.boot.standalone)

    testRuntimeOnly(libs.junit.platform.launcher)
}

configurations.configureEach {
    exclude(group = libs.spring.boot.starter.logging.get().group, module = libs.spring.boot.starter.logging.get().name)
}

testing {
    // Integration tests live in their own source set so they can be run - and skipped - independently of the unit
    // tests. They see the main and test outputs, and inherit the test dependencies.
    suites {
        val test = getByName<JvmTestSuite>("test")
        register<JvmTestSuite>("integrationTest") {
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

configurations.getByName("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
}
configurations.getByName("integrationTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
}

sourceSets["integrationTest"].apply {
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = true
    }

    jvmArgs("-XX:+EnableDynamicAgentLoading")

    systemProperty("user.language", "en")
    systemProperty("user.country", "US")
    systemProperty("spring.test.context.cache.maxSize", 1)
}

kover {
    // Kover instruments every `Test` task, so the test and integrationTest suites are merged into a single report under
    // `build/reports/kover` without any extra wiring.
    currentProject {
        sources {
            // Kover only excludes the `test` source set from the measured sources out of the box. The integrationTest
            // suite is test code too, so it is excluded explicitly - otherwise the report would measure coverage of the
            // tests themselves.
            excludedSourceSets.add("integrationTest")
        }
    }
}

tasks.named<Task>("check").configure {
    finalizedBy(tasks.named("koverHtmlReport"), tasks.named("koverXmlReport"))
}

tasks.named<Jar>("jar").configure {
    enabled = true
    archiveClassifier = ""
}

tasks.named<BootJar>("bootJar").configure {
    archiveClassifier = "exec"
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}

idea {
    project {
        settings {
            runConfigurations {
                create<Application>("Run [sample-coupon-service]") {
                    mainClass = "com.example.samplecoupon.ApplicationKt"
                    moduleName = "sample-coupon-service.main"
                    workingDirectory = rootProject.rootDir.absolutePath
                    jvmArgs = "-Dspring.profiles.active=ipapi -Dlogging.level.com.example.samplecoupon=debug"
                    programParameters = ""
                }
                create<Application>("Run [sample-coupon-service|localhost]") {
                    mainClass = "com.example.samplecoupon.ApplicationKt"
                    moduleName = "sample-coupon-service.main"
                    workingDirectory = rootProject.rootDir.absolutePath
                    jvmArgs = "-Dspring.profiles.active=wiremock -Dlogging.level.com.example.samplecoupon=debug"
                    programParameters = ""
                }
                create<Gradle>("Format Code [sample-coupon-service]") {
                    taskNames = listOf("spotlessApply")
                    projectPath = rootProject.rootDir.absolutePath
                }
                create<JUnit>("Test [sample-coupon-service|test]") {
                    moduleName = "sample-coupon-service.test"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "com.example.samplecoupon"
                    vmParameters = "-Dspring.test.context.cache.maxSize=1"
                }
                create<JUnit>("Test [sample-coupon-service|integrationTest]") {
                    moduleName = "sample-coupon-service.integrationTest"
                    workingDirectory = rootProject.rootDir.absolutePath
                    packageName = "com.example.samplecoupon"
                    vmParameters = "-Dspring.test.context.cache.maxSize=1"
                }
            }
        }
    }
}

spotless {
    java {
        target("**/src/**/*.java")
        targetExclude("**/build/**")

        googleJavaFormat()
        forbidWildcardImports()
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    sql {
        target("**/src/main/resources/**/*.sql")
        targetExclude("**/build/**")

        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    kotlin {
        target("**/src/**/*.kt")
        targetExclude("**/build/**")

        ktfmt().metaStyle().configure {
            it.setMaxWidth(100)
            it.setRemoveUnusedImports(true)
        }
        endWithNewline()
        toggleOffOn()
        lineEndings = LineEnding.UNIX
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")

        ktlint().editorConfigOverride(mapOf("max_line_length" to "120"))
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("yaml") {
        target("**/*.yml", "**/*.yaml")
        targetExclude("**/build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
    format("misc") {
        target("**/.gitattributes", "**/.gitignore")
        targetExclude("**/build/**")

        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        lineEndings = LineEnding.UNIX
    }
}

defaultTasks("spotlessApply", "build")
