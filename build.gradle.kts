import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("com.diffplug.spotless") version Versions.spotlessPlugin
    kotlin("jvm") version Versions.kotlin
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlintPlugin
    `maven-publish`
    signing
    java
    id("com.google.protobuf") version Versions.protobufPlugin
}

group = "com.commonwealthrobotics"
version = Versions.projectVersion

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api(group = "com.google.protobuf", name = "protobuf-java", version = Versions.protobufJava)
    api(group = "io.grpc", name = "grpc-all", version = Versions.grpc)
    api(group = "io.grpc", name = "grpc-kotlin-stub", version = Versions.grpcKotlin)
    api(group = "javax.annotation", name = "javax.annotation-api", version = Versions.javaxAnnotationAPI)

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = Versions.kotlin)
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = Versions.kotlinCoroutines)
}

sourceSets {
    main {
        proto {
            srcDir("$projectDir/bowler-proto/src/proto")
        }
        java {
            srcDir("$buildDir/generated/source/proto/main/grpc")
            srcDir("$buildDir/generated/source/proto/main/java")
            srcDir("$buildDir/generated/source/proto/main/grpckt")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protobufJava}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${Versions.grpc}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${Versions.grpcKotlin}:jdk7@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            // Disable caching because of https://github.com/google/protobuf-gradle-plugin/issues/180
            it.doFirst { delete(it.outputs) }
            it.outputs.upToDateWhen { false }

            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

spotless {
    kotlinGradle {
        ktlint(Versions.ktlint)
        trimTrailingWhitespace()
    }
    freshmark {
        target("src/**/*.md")
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
    }
    format("extraneous") {
        target("src/**/*.fxml")
        trimTrailingWhitespace()
        indentWithSpaces(2)
        endWithNewline()
    }
    java {
        targetExclude("**/*")
    }
    kotlin {
        ktlint(Versions.ktlint)
        licenseHeaderFile(rootProject.rootDir.toPath().resolve("config").resolve("spotless").resolve("license.txt"))
        targetExclude("**/*")
        // Generated proto sources
        targetExclude(project(":proto").buildDir.walkTopDown().toList())
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

// Always run ktlintFormat after spotlessApply
tasks.named("spotlessApply").configure {
    finalizedBy(tasks.named("ktlintFormat"))
}

ktlint {
    version.set(Versions.ktlint)
    enableExperimentalRules.set(true)
    additionalEditorconfigFile.set(file(rootProject.rootDir.toPath().resolve("config").resolve("ktlint").resolve(".editorconfig")))
    filter {
        exclude {
            it.file.path.contains("generated/")
        }
    }
}

task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    archiveBaseName.set(Metadata.projectName)
    from(sourceSets.main.get().allSource)
}

task<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    archiveBaseName.set(Metadata.projectName)
    from(tasks.named("javadoc"))
}

val publicationName = "bowler-proto-kotlin"

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            artifactId = Metadata.projectName
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            try {
                artifact(tasks.named("shadowJar"))
            } catch (ex: UnknownTaskException) {
            }

            pom {
                name.set(Metadata.projectName)
                description.set(Metadata.projectDescription)
                url.set(Metadata.githubRepo)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("rbenasutti")
                        name.set("Ryan Benasutti")
                        email.set("ryanbenasutti@gmail.com")
                    }
                }

                scm {
                    connection.set(Metadata.scmConnection)
                    developerConnection.set(Metadata.developerConnection)
                    url.set(Metadata.githubRepo)
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                if (System.getenv("OSSRH_USERNAME")?.isNotEmpty() == true) {
                    println("Using environment variables `OSSRH_USERNAME` and `OSSRH_PASSWORD` for publishing credentials.")
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                } else {
                    println("Assuming publishing credentials are configured through project properties `OSSRH_USERNAME` and `OSSRH_PASSWORD`.")
                    username = findProperty("OSSRH_USERNAME") as String?
                    password = findProperty("OSSRH_PASSWORD") as String?
                }
            }
        }
    }
}

signing {
    if (System.getenv("OSSRH_SIGNING_KEY")?.isNotEmpty() == true) {
        val signingKey = System.getenv("OSSRH_SIGNING_KEY")
        val signingPassword = System.getenv("OSSRH_SIGNING_PASSWORD")
        val signingKeyId = System.getenv("OSSRH_SIGNING_KEY_ID")
        if (signingKeyId.isEmpty()) {
            println("Using an in-memory OpenPGP key for signing.")
            useInMemoryPgpKeys(signingKey, signingPassword)
        } else {
            println("Using an in-memory OpenPGP subkey for signing.")
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        }
    } else {
        println("Assuming signatory credentials are configured through project properties `signing.keyId`, `signing.password`, and `signing.secretKeyRingFile`.")
    }

    sign(publishing.publications[publicationName])
    sign(configurations.archives.get())
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = Versions.gradleWrapper
}
