@file:Suppress("MissingPackageDeclaration", "SpellCheckingInspection", "GrazieInspection")

/*
* Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

//  Aufrufe
//  1) Microservice uebersetzen und starten
//        .\gradlew bootRun [--args='--debug']
//        .\gradlew compileJava
//        .\gradlew compileTestJava
//
//  2) Microservice als selbstausfuehrendes JAR oder Docker-Image erstellen und ausfuehren
//        .\gradlew bootJar
//        java -jar build/libs/....jar
//        .\gradlew bootBuildImage
//
//  3) Tests und Codeanalyse
//        .\gradlew test jacocoTestReport [--rerun-tasks]
//        .\gradlew jacocoTestCoverageVerification
//        .\gradlew allureServe
//              EINMALIG>>   .\gradlew downloadAllure
//        .\gradlew checkstyleMain checkstyleTest spotbugsMain spotbugsTest
//        .\gradlew sonar
//        .\gradlew buildHealth
//        .\gradlew reason --id com.fasterxml.jackson.core:jackson-annotations:...
//
//  4) Sicherheitsueberpruefung durch OWASP Dependency Check und Snyk
//        .\gradlew dependencyCheckAnalyze --info
//        .\gradlew snyk-test
//
//  5) "Dependencies Updates"
//        .\gradlew versions
//        .\gradlew dependencyUpdates
//        .\gradlew checkNewVersions
//
//  6) API-Dokumentation erstellen
//        .\gradlew javadoc
//
//  7) Entwicklerhandbuch in "Software Engineering" erstellen
//        .\gradlew asciidoctor asciidoctorPdf
//
//  8) Projektreport erstellen
//        .\gradlew projectReport
//        .\gradlew dependencyInsight --dependency spring-security-rsa
//        .\gradlew dependencies
//        .\gradlew dependencies --configuration runtimeClasspath
//        .\gradlew buildEnvironment
//        .\gradlew htmlDependencyReport
//
//  9) Report ueber die Lizenzen der eingesetzten Fremdsoftware
//        .\gradlew generateLicenseReport
//
//  10) Daemon stoppen
//        .\gradlew --stop
//
//  11) Verfuegbare Tasks auflisten
//        .\gradlew tasks
//
//  12) "Dependency Verification"
//        .\gradlew --write-verification-metadata pgp,sha256 --export-keys
//
//  13) Initialisierung des Gradle Wrappers in der richtigen Version
//      dazu ist ggf. eine Internetverbindung erforderlich
//        gradle wrapper --gradle-version=8.2-rc-2 --distribution-type=bin

// https://github.com/gradle/kotlin-dsl/tree/master/samples
// https://docs.gradle.org/current/userguide/kotlin_dsl.html
// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin

// TODO https://youtrack.jetbrains.com/issue/IDEA-320266 Project.getConvention()

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.nio.file.Paths
import net.ltgt.gradle.errorprone.errorprone
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.pdf.AsciidoctorPdfTask
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

val javaVersion: String = System.getProperty("java") ?: libs.versions.javaVersion.get()
val javaLts = "17"
val enablePreview = if (javaVersion == javaLts) null else "--enable-preview"
val imagePath = project.properties["imagePath"] ?: "juergenzimmermann"

plugins {
    java
    idea
    checkstyle
    jacoco
    `project-report`

    alias(libs.plugins.springBoot)

    // https://spring.io/blog/2022/09/26/native-support-in-spring-boot-3-0-0-m5
    // Kommentar entfernen fuer Spring AOT
    //alias(libs.plugins.graalvmPlugin)

    // https://github.com/tbroyer/gradle-errorprone-plugin
    // https://errorprone.info/docs/installation
    alias(libs.plugins.errorpronePlugin)

    // https://spotbugs.readthedocs.io/en/latest/gradle.html
    alias(libs.plugins.spotbugs)

    // https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle
    //alias(libs.plugins.sonarqubePlugin)

    // https://github.com/diffplug/spotless/tree/main/plugin-gradle
    //alias(libs.plugins.spotless)

    // https://github.com/radarsh/gradle-test-logger-plugin
    alias(libs.plugins.testLogger)

    // https://github.com/allure-framework/allure-gradle
    // https://docs.qameta.io/allure/#_gradle_2
    // TODO "The Project.getConvention() method has been deprecated."
    // io.qameta.allure.gradle.adapter.AllureAdapterBasePlugin, ...AllureAdapterExtension, ...AllureAdapterPlugin
    // io.qameta.allure.gradle.report.AllureReportBasePlugin, ...AllureReportExtension
    // io.qameta.allure.gradle.download.AllureDownloadPlugin
    alias(libs.plugins.allure)

    // https://github.com/boxheed/gradle-sweeney-plugin
    alias(libs.plugins.sweeney)

    // https://github.com/jeremylong/dependency-check-gradle
    alias(libs.plugins.owaspDependencycheck)

    // https://github.com/snyk/gradle-plugin
    alias(libs.plugins.snyk)

    // https://github.com/asciidoctor/asciidoctor-gradle-plugin
    alias(libs.plugins.asciidoctor)
    alias(libs.plugins.asciidoctorPdf)
    // Leanpub als Alternative zu PDF: https://github.com/asciidoctor/asciidoctor-leanpub-converter

    // https://github.com/nwillc/vplugin
    alias(libs.plugins.nwillc)

    // https://github.com/ben-manes/gradle-versions-plugin
    // FIXME https://github.com/ben-manes/gradle-versions-plugin/issues/773: compileClasspathCopy usw. deprecated
    alias(libs.plugins.benManes)

    // https://github.com/markelliot/gradle-versions
    alias(libs.plugins.markelliot)

    // https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
    // FIXME https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/issues/893: Project.getConvention() deprecated
    //alias(libs.plugins.dependencyAnalysis)

    // https://github.com/jk1/Gradle-License-Report
    alias(libs.plugins.licenseReport)

    // https://github.com/gradle-dependency-analyze/gradle-dependency-analyze
    // https://github.com/jaredsburrows/gradle-license-plugin
    // https://github.com/hierynomus/license-gradle-plugin
}

defaultTasks = mutableListOf("compileTestJava")
group = "com.acme"
version = "2023.1.0"
val imageTag = project.properties["imageTag"] ?: project.version.toString()

sweeney {
    enforce(mapOf("type" to "gradle", "expect" to "[8.2,8.2]"))
    // https://www.java.com/releases
    // https://devcenter.heroku.com/articles/java-support#specifying-a-java-version
    enforce(mapOf("type" to "jdk", "expect" to "[20.0.1,21]"))
    validate()
}

// https://docs.gradle.org/current/userguide/java_plugin.html#sec:java-extension
// https://docs.gradle.org/current/dsl/org.gradle.api.plugins.JavaPluginExtension.html
java {
    toolchain {
        // einschl. sourceCompatibility und targetCompatibility
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    // sourceCompatibility = JavaVersion.toVersion(javaVersion)
    // targetCompatibility = sourceCompatibility
}

repositories {
    mavenCentral()

    // https://github.com/spring-projects/spring-framework/wiki/Spring-repository-FAQ
    // https://github.com/spring-projects/spring-framework/wiki/Release-Process
    maven("https://repo.spring.io/milestone") { mavenContent { releasesOnly() } }

    // Snapshots von Spring Framework, Spring Boot, Spring Data, Spring Security, Spring for GraphQL, ...
    // maven("https://repo.spring.io/snapshot") { mavenContent { snapshotsOnly() } }

    // Snapshots von springdoc-openapi
    // maven("https://s01.oss.sonatype.org/content/repositories/snapshots") { mavenContent { snapshotsOnly() } }

    // Snapshots von JaCoCo
    // maven("https://oss.sonatype.org/content/repositories/snapshots") {
    //     mavenContent { snapshotsOnly() }
    //     // https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_dependency_management
    //     content { onlyForConfigurations("jacocoAgent", "jacocoAnt") }
    // }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

/* ktlint-disable comment-spacing */
@Suppress("CommentSpacing")
// https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation
dependencies {
    implementation(platform(libs.micrometerBom))
    implementation(platform(libs.jacksonBom))
    implementation(platform(libs.nettyBom))
    implementation(platform(libs.reactorBom))
    //implementation(platform(libs.springBom))
    //implementation(platform(libs.querydslBom))
    //implementation(platform(libs.springDataBom))
    //implementation(platform(libs.springSecurityBom))
    //implementation(platform(libs.zipkinReporterBom))
    //implementation(platform(libs.assertjBom))
    //implementation(platform(libs.mockitoBom))
    //implementation(platform(libs.junitBom))
    implementation(platform(libs.allureBom))
    implementation(platform(libs.springBootBom))
    // spring-boot-starter-parent als "Parent POM"
    implementation(platform(libs.springdocOpenapiBom))

    // "Starters" enthalten sinnvolle Abhaengigkeiten, die man i.a. benoetigt
    // spring-boot-starter beinhaltet Spring Boot mit Actuator sowie spring-boot-starter-logging mit Logback
    implementation("org.springframework.boot:spring-boot-starter")

    // spring-boot-starter-web enthaelt spring-boot-starter-tomcat
    // ggf. spring-boot-starter-jetty oder spring-boot-starter-undertow
    // BEACHTE: spring-boot-starter-hateoas enthaelt spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-tomcat") {
        exclude(group = "org.apache.tomcat.embed", module = "tomcat-embed-websocket")
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // implementation(libs.crac)

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    runtimeOnly("com.h2database:h2")

    // https://github.com/querydsl/querydsl/issues/2444#issuecomment-489538997
    // https://stackoverflow.com/questions/59950657/querydsl-annotation-processor-and-gradle-plugin#answer-59951292
    // https://github.com/querydsl/querydsl/issues/3436
    // https://discuss.gradle.org/t/annotationprocessor-querydsl-java-lang-noclassdeffounderror/27107/5
    // https://github.com/hibernate/hibernate-orm/tree/main/tooling/hibernate-gradle-plugin fuer "Static Metamodel"-Klassen
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:${libs.versions.jakartaPersistence.get()}")

    // Flyway unterstuetzt nicht Oracle 23: https://documentation.red-gate.com/fd/oracle-184127602.html
    if (project.properties["noFlyway"] != "true") {
        implementation("org.flywaydb:flyway-core")
        // https://flywaydb.org/documentation/database/mysql#java-usage
        runtimeOnly("org.flywaydb:flyway-mysql")
    }

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose
    //developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Tracing und Metriken durch Micrometer sowie Visualisierung durch Zipkin oder Prometheus/Grafana
    // FIXME https://github.com/spring-projects/spring-graphql/issues/547 NullPointerException, wenn Requests von Spring-basierten GraphQL-Clients empfangen werden
    //implementation("io.micrometer:micrometer-observation")
    //implementation("io.micrometer:micrometer-tracing-bridge-brave")
    //implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // https://springdoc.org/v2/#swagger-ui-configuration
    // https://github.com/springdoc/springdoc-openapi
    // https://github.com/springdoc/springdoc-openapi-demos/wiki/springdoc-openapi-2.x-migration-guide
    // https://www.baeldung.com/spring-rest-openapi-documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")

    runtimeOnly(libs.jansi)
    runtimeOnly(libs.bouncycastle) // Argon2

    compileOnly(libs.spotbugsAnnotations)
    // https://github.com/spring-projects/spring-framework/issues/25095
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose
    if (project.properties["dockerCompose"] == "true") {
        developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    }

    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools
    //developmentOnly("org.springframework.boot:spring-boot-devtools:${libs.versions.springBoot.get()}")

    // einschl. JUnit und Mockito
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.hamcrest", module = "hamcrest")
        exclude(group = "org.skyscreamer", module = "jsonassert")
        exclude(group = "org.xmlunit", module = "xmlunit-core")
    }
    testImplementation(libs.junitPlatformSuiteApi)
    testRuntimeOnly(libs.junitPlatformSuiteEngine)
    //testImplementation("org.springframework.security:spring-security-test")
    // mock() fuer record
    testImplementation("org.mockito:mockito-inline")

    // https://github.com/tbroyer/gradle-errorprone-plugin
    errorprone(libs.errorprone)

    constraints {
        implementation(libs.annotations)
        //implementation(libs.springGraphQL)
        //implementation(libs.springHateoas)
        //implementation(libs.jakartaPersistence)
        implementation(libs.hibernate)
        //runtimeOnly(libs.postgres)
        runtimeOnly(libs.mysql)
        //runtimeOnly(libs.oracle)
        if (project.properties["noFlyway"] != "true") {
            implementation(libs.flyway)
            runtimeOnly(libs.flywayMySQL)
        }
        //implementation(libs.hibernateValidator)
        //implementation(libs.bundles.tomcat)
        //implementation(libs.bundles.graphqlJavaBundle)
        implementation(libs.graphqlJava)
        //implementation(libs.graphqlJavaDataloader)
        implementation(libs.angusMail)
        //implementation(libs.snakeyaml)
        //implementation(libs.bundles.slf4jBundle)
        //implementation(libs.logback)
        //implementation(libs.bundles.log4j)
        //implementation(libs.springSecurityRsa)
        //testImplementation(libs.mockitoInline)
    }
}
/* ktlint-enable comment-spacing */

// aktuelle Snapshots laden
// configurations.all {
//    resolutionStrategy { cacheChangingModulesFor(0, "seconds") }
// }

tasks.compileJava {
    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html
    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.CompileOptions.html
    // https://dzone.com/articles/gradle-goodness-enabling-preview-features-for-java
    options.isDeprecation = true
    with(options.compilerArgs) {
        add("-Xlint:unchecked")
        // fuer springdoc-openapi https://github.com/spring-projects/spring-framework/issues/29563
        add("-parameters")
        if (enablePreview != null) {
            add(enablePreview)
            //add("-Xlint:preview")
        }

        // https://github.com/tbroyer/gradle-errorprone-plugin#jdk-16-support
        add("--add-opens")
        add("--add-exports")
    }

    // https://uber.github.io/AutoDispose/error-prone
    // https://errorprone.info/docs/flags
    // https://stackoverflow.com/questions/56975581/how-to-setup-error-prone-with-gradle-getting-various-errors
    with(options.errorprone.errorproneArgs) {
        add("-Xep:MissingSummary:OFF")
    }

    // ohne sourceCompatiblity und targetCompatibility:
    //options.release = javaVersion
    // https://blog.gradle.org/incremental-compiler-avoidance#about-annotation-processors
}

tasks.compileTestJava {
    sourceCompatibility = javaVersion
    options.isDeprecation = true
    with(options.compilerArgs) {
        add("-Xlint:unchecked")
        if (enablePreview != null) {
            add(enablePreview)
        }
    }
    options.errorprone.errorproneArgs.add("-Xep:VariableNameSameAsType:OFF")
}

tasks.named<BootJar>("bootJar") {
    doLast {
        println("")
        println("Aufruf der ausfuehrbaren JAR-Datei:")
        @Suppress("MaxLineLength")
        println(
            "java -D'LOG_PATH=./build/log' -D'javax.net.ssl.trustStore=./src/main/resources/truststore.p12' -D'javax.net.ssl.trustStorePassword=zimmermann' -jar build/libs/${archiveFileName.get()} --spring.profiles.default=dev --spring.profiles.active=dev [--debug]", // ktlint-disable max-line-length
        )
        println("")
    }
}

// https://github.com/paketo-buildpacks/spring-boot
tasks.named<BootBuildImage>("bootBuildImage") {
    // statt "created 43 years ago": https://medium.com/buildpacks/time-travel-with-pack-e0efd8bf05db
    //val createdDate = "now"

    // default:   imageName = "docker.io/${project.name}:${project.version}"
    imageName = "$imagePath/${project.name}:$imageTag"

    // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples.builder-configuration
    // https://github.com/bell-sw/Liberica/releases
    @Suppress("StringLiteralDuplication")
    environment = mapOf(
        // https://github.com/paketo-buildpacks/bellsoft-liberica/releases
        // https://github.com/paketo-buildpacks/bellsoft-liberica#configuration
        // https://github.com/paketo-buildpacks/bellsoft-liberica/blob/main/buildpack.toml
        "BP_JVM_VERSION" to javaVersion, // default: 17
        "BPL_JVM_THREAD_COUNT" to "20", // default: 250 (reactive: 50)
        // https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples.runtime-jvm-configuration
        "BPE_DELIM_JAVA_TOOL_OPTIONS" to " ",
        "BPE_APPEND_JAVA_TOOL_OPTIONS" to enablePreview,
        // https://github.com/paketo-buildpacks/spring-boot#configuration
        // https://github.com/paketo-buildpacks/spring-boot/blob/main/buildpack.toml
        //"BP_SPRING_CLOUD_BINDINGS_DISABLED" to "true",
        //"BPL_SPRING_CLOUD_BINDINGS_DISABLED" to "true",
        //"BPL_SPRING_CLOUD_BINDINGS_ENABLED" to "false", // deprecated
        // https://paketo.io/docs/howto/configuration/#enabling-debug-logging
        //"BP_LOG_LEVEL" to "DEBUG",

        // paketobuildpacks/builder:tiny als Builder fuer "Native Image"
        // https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html
        // https://github.com/spring-projects/spring-framework/blob/main/framework-docs/src/docs/asciidoc/core/core-aot.adoc
        //"BP_NATIVE_IMAGE" to "true",
        //"BP_BOOT_NATIVE_IMAGE_BUILD_ARGUMENTS" to "-H:+ReportExceptionStackTraces",
    )

    // https://paketo.io/docs/howto/java/#use-an-alternative-jvm
    // https://github.com/paketo-buildpacks/adoptium
    // https://github.com/paketo-buildpacks/sap-machine
    // https://github.com/paketo-buildpacks/azul-zulu
    // Nur JDK: Amazon Corretto, Oracle, Microsoft OpenJDK, Alibaba Dragonwell

    // Eclipse Temurin statt Bellsoft Liberica: JRE 8, 11, 17, 20
    //buildpacks = listOf(
    //    //"paketo-buildpacks/ca-certificates",
    //    "gcr.io/paketo-buildpacks/adoptium",
    //    "paketo-buildpacks/java",
    //)

    // SapMachine statt Bellsoft Liberica: JRE 11, 17, 20
    //buildpacks = listOf(
    //    "gcr.io/paketo-buildpacks/sap-machine",
    //    "paketo-buildpacks/java",
    //)

    // Azul Zulu statt Bellsoft Liberica: JRE 8, 11, 17, 20
    //buildpacks = listOf(
    //    "gcr.io/paketo-buildpacks/azul-zulu",
    //    "paketo-buildpacks/java",
    //)

    // Podman statt Docker
    // docker {
    //    host = "unix:///run/user/1000/podman/podman.sock"
    //    isBindHostToBuilder = true
    // }
}

//tasks.??? {
//    removeXmlSupport = false
//}

tasks.named<BootRun>("bootRun") {
    if (enablePreview != "") {
        jvmArgs(enablePreview)
    }

    // "System Properties", z.B. fuer Spring Properties oder fuer logback
    // https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties
    systemProperty("spring.profiles.default", "dev")
    systemProperty("spring.profiles.active", "dev")
    systemProperty("LOG_PATH", "./build/log")
    // Logging der Header-Daten
    systemProperty("REQUEST_RESPONSE_LOGLEVEL", System.getProperty("REQUEST_RESPONSE_LOGLEVEL") ?: "DEBUG")
    systemProperty("APPLICATION_LOGLEVEL", System.getProperty("APPLICATION_LOGLEVEL") ?: "TRACE")
    systemProperty("HIBERNATE_LOGLEVEL", System.getProperty("HIBERNATE_LOGLEVEL") ?: "DEBUG")
    systemProperty("FLYWAY_LOGLEVEL", System.getProperty("FLYWAY_LOGLEVEL") ?: "DEBUG")
}

tasks.test {
    useJUnitPlatform {
        includeTags = when (System.getProperty("test")) {
            "all", null -> setOf("integration", "unit")
            "integration" -> setOf("integration")
            "rest" -> setOf("rest")
            "rest-get" -> setOf("rest-get")
            "rest-write" -> setOf("rest-write")
            "graphql" -> setOf("graphql")
            "query" -> setOf("query")
            "mutation" -> setOf("mutation")
            "unit" -> setOf("unit")
            "service-read" -> setOf("service-read")
            "service-write" -> setOf("service-write")
            else -> throw IllegalArgumentException("Fehler bei -Dtest=...")
        }
    }

    systemProperty("javax.net.ssl.trustStore", "./src/main/resources/truststore.p12")
    systemProperty("javax.net.ssl.trustStorePassword", "zimmermann")
    systemProperty("LOG_PATH", "./build/log")
    systemProperty("APPLICATION_LOGLEVEL", "TRACE")
    systemProperty("HIBERNATE_LOGLEVEL", "DEBUG")
    // systemProperty("HIBERNATE_LOGLEVEL", "TRACE")
    systemProperty("FLYWAY_LOGLEVEL", "DEBUG")
    systemProperty("junit.platform.output.capture.stdout", true)
    systemProperty("junit.platform.output.capture.stderr", true)

    if (enablePreview != null) {
        jvmArgs(enablePreview)
    }

    // https://docs.gradle.org/current/userguide/java_testing.html#sec:debugging_java_tests
    // https://www.jetbrains.com/help/idea/run-debug-configuration-junit.html
    // https://docs.gradle.org/current/userguide/java_testing.html#sec:debugging_java_tests
    // debug = true

    // finalizedBy("jacocoTestReport")
}

// https://docs.qameta.io/allure/#_gradle_2
allure {
    version = libs.versions.allure.get()
    adapter {
        frameworks {
            junit5 {
                adapterVersion = libs.versions.allureJunit.get()
                autoconfigureListeners = true
                enabled = true
            }
        }
        autoconfigure = true
        aspectjWeaver = false
        aspectjVersion = libs.versions.aspectjweaver.get()
    }

    // https://github.com/allure-framework/allure-gradle#customizing-allure-commandline-download
    // commandline {
    //     group = "io.qameta.allure"
    //     module = "allure-commandline"
    //     extension = "zip"
    // }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin/#configuring-tasks
tasks.getByName<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required = true
        html.required = true
    }

    // afterEvaluate gibt es nur bei getByName<> ("eager"), nicht bei named<> ("lazy")
    // https://docs.gradle.org/5.0/release-notes.html#configuration-avoidance-api-disallows-common-configuration-errors
    afterEvaluate {
        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) { exclude("**/config/**", "**/entity/**") }
                },
            ),
        )
    }

    // https://github.com/gradle/gradle/pull/12626
    dependsOn(tasks.test)
}

tasks.getByName<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.7")
            }
        }
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    // https://docs.gradle.org/current/dsl/org.gradle.api.plugins.quality.CheckstyleExtension.html
    configFile = file("extras/checkstyle.xml")
    setConfigProperties(
        "configDir" to "$projectDir/extras",
        // "checkstyleSuppressionsPath" to file("checkstyle-suppressions.xml").absolutePath,
    )
    isIgnoreFailures = false
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required = true
        html.required = true
    }
}

spotbugs {
    // https://github.com/spotbugs/spotbugs/releases
    toolVersion = libs.versions.spotbugs.get()
}
tasks.spotbugsMain {
    reports.create("html") {
        required = true
        outputLocation = file("${layout.buildDirectory.asFile.get()}/reports/spotbugs.html")
    }
    excludeFilter = file("extras/spotbugs-exclude.xml")
}

// https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/#analyzing

// https://github.com/jeremylong/DependencyCheck/blob/master/src/site/markdown/dependency-check-gradle/configuration.md
dependencyCheck {
    scanConfigurations = listOf("runtimeClasspath")
    suppressionFile = "$projectDir/extras/owasp.xml"
    data(
        closureOf<org.owasp.dependencycheck.gradle.extension.DataExtension> {
            directory = "C:/Zimmermann/owasp-dependency-check"
            username = "dc"
            password = "p"
        },
    )

    analyzedTypes = listOf("jar")
    analyzers(
        closureOf<org.owasp.dependencycheck.gradle.extension.AnalyzerExtension> {
            // nicht benutzte Analyzer
            assemblyEnabled = false
            autoconfEnabled = false
            bundleAuditEnabled = false
            cmakeEnabled = false
            cocoapodsEnabled = false
            composerEnabled = false
            golangDepEnabled = false
            golangModEnabled = false
            nodeEnabled = false
            nugetconfEnabled = false
            nuspecEnabled = false
            pyDistributionEnabled = false
            pyPackageEnabled = false
            rubygemsEnabled = false
            swiftEnabled = false

            nodeAudit(closureOf<org.owasp.dependencycheck.gradle.extension.NodeAuditExtension> { enabled = true })
            retirejs(closureOf<org.owasp.dependencycheck.gradle.extension.RetireJSExtension> { enabled = true })
            // ossIndex(closureOf<org.owasp.dependencycheck.gradle.extension.OssIndexExtension> { enabled = true })
        },
    )

    format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL.toString()
}

snyk {
    setArguments("--configuration-matching=implementation|runtimeOnly")
    setSeverity("low")
    setApi("40df2078-e1a3-4f28-b913-e2babbe427fd")
}

tasks.javadoc {
    options {
        showFromPackage()
        // outputLevel = org.gradle.external.javadoc.JavadocOutputLevel.VERBOSE

        if (this is CoreJavadocOptions) {
            // Keine bzw. nur elementare Warnings anzeigen wegen Lombok
            // https://stackoverflow.com/questions/52205209/configure-gradle-build-to-suppress-javadoc-console-warnings
            addStringOption("Xdoclint:none", "-quiet")
            // https://stackoverflow.com/questions/59485464/javadoc-and-enable-preview
            addBooleanOption("-enable-preview", true)
            addStringOption("-release", javaVersion)
        }

        if (this is StandardJavadocDocletOptions) {
            author(true)
        }
    }
}

tasks.getByName<AsciidoctorTask>("asciidoctor") {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())
        // requires("asciidoctor-diagram")

        modules {
            diagram.use()
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
        }
    }

    val docPath = Paths.get("extras", "doc")
    setBaseDir(file(docPath))
    setSourceDir(file(docPath))
    logDocuments = true

    // https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/597#issuecomment-844352804
    inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
    forkOptions {
        @Suppress("StringLiteralDuplication")
        jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
    }

    doLast {
        val outputPath = Paths.get(layout.buildDirectory.asFile.get().absolutePath, "docs", "asciidoc")
        val outputFile = Paths.get(outputPath.toFile().absolutePath, "projekthandbuch.html")
        println("Das Projekthandbuch ist in $outputFile")
    }
}

tasks.getByName<AsciidoctorPdfTask>("asciidoctorPdf") {
    asciidoctorj {
        setVersion(libs.versions.asciidoctorj.get())

        modules {
            diagram.use()
            diagram.setVersion(libs.versions.asciidoctorjDiagram.get())
            pdf.setVersion(libs.versions.asciidoctorjPdf.get())
        }
    }

    val docPath = Paths.get("extras", "doc")
    setBaseDir(file(docPath))
    setSourceDir(file(docPath))
    attributes(mapOf("pdf-page-size" to "A4"))
    logDocuments = true

    // https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/597#issuecomment-844352804
    inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
    forkOptions {
        jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.base/java.io=ALL-UNNAMED")
    }

    doLast {
        val outputPath = Paths.get(layout.buildDirectory.asFile.get().absolutePath, "docs", "asciidocPdf")
        val outputFile = Paths.get(outputPath.toString(), "entwicklerhandbuch.pdf")
        println("Das Entwicklerhandbuch ist in $outputFile")
    }
}

licenseReport {
    configurations = arrayOf("runtimeClasspath")
}

tasks.getByName<DependencyUpdatesTask>("dependencyUpdates") {
    checkConstraints = true
}

idea {
    module {
        isDownloadJavadoc = true
        // https://stackoverflow.com/questions/59950657/querydsl-annotation-processor-and-gradle-plugin
        sourceDirs.add(file("generated/"))
        generatedSourceDirs.add(file("generated/"))
    }
}
