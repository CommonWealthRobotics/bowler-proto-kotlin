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
    id("com.jfrog.bintray") version Versions.bintrayPlugin
    `maven-publish`
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

val publicationName = "publication-${Metadata.projectName}-${name.toLowerCase()}"

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            artifactId = Metadata.projectName
            from(components["java"])
            artifact(tasks["sourcesJar"])
            try {
                artifact(tasks.named("shadowJar"))
            } catch (ex: UnknownTaskException) {
            }
        }
    }
}

configureBintrayPkg(publicationName)

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = Versions.gradleWrapper
}

fun Project.configureBintrayPkg(publicationName: String?) {
    bintray {
        val bintrayApiUser = properties["bintray.api.user"] ?: System.getenv("BINTRAY_USER")
        val bintrayApiKey = properties["bintray.api.key"] ?: System.getenv("BINTRAY_API_KEY")
        user = bintrayApiUser as String?
        key = bintrayApiKey as String?

        publicationName?.let { setPublications(it) }

        with(pkg) {
            repo = Metadata.Bintray.repo
            name = Metadata.projectName
            userOrg = Metadata.organization
            publish = true
            setLicenses(Metadata.license)
            vcsUrl = Metadata.Bintray.vcsUrl
            githubRepo = Metadata.Bintray.githubRepo
            with(version) {
                name = Versions.projectVersion
                desc = Metadata.projectDescription
            }
        }
    }
}
