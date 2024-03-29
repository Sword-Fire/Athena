import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.geekmc.athena"
version = "0.1.0-SNAPSHOT"
val outputName = "${project.name}-$version.jar"

repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

val isInWorkSpace = parent?.name == "swork-fire-workspace"

if (isInWorkSpace) {
    tasks.getByName("build") {
        dependsOn(":kstom:build")
        dependsOn(":kstom:jar")
        dependsOn(":turing-core:build")
    }
}

dependencies {
    if (isInWorkSpace) {
        compileOnly(project(":turing-core"))
    } else {
        // TODO: pin version
        compileOnly("net.geekmc.swork-fire:turing-core:+")
    }

    if (parent?.name == "swork-fire-workspace") {
        compileOnly(project(":kstom"))
    } else {
        // TODO: pin version
        compileOnly("org.ktorm:ktorm-core:${project.ext["version.ktorm-core"]}")
    }

    // TODO: pin version
    compileOnly("com.github.Minestom:Minestom:-SNAPSHOT") {
        exclude(group = "org.tinylog")
    }

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${project.ext["version.kotlinx-coroutines-core"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-common:${project.ext["version.kotlinx-coroutines-core"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-jvm:${project.ext["version.kotlin-scripting-jvm"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-dependencies:${project.ext["version.kotlin-scripting-dependencies"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:${project.ext["version.kotlin-scripting-dependencies-maven"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-jvm-host:${project.ext["version.kotlin-scripting-jvm-host"]}")

    compileOnly("org.kodein.di:kodein-di-jvm:${project.ext["version.kodein-di-jvm"]}")
}

// Process some props in extension.json
@Suppress("UnstableApiUsage")
tasks.withType<ProcessResources> {
    filesMatching("extension.json") {
        filter {

            val extensionVersionPlaceholder = "@!extensionVersion@"
            val dependencyVersionPlaceholder = "@!dependencyVersion@"

            // 替换拓展版本。
            var ret = it
            if (extensionVersionPlaceholder in ret) {
                ret = ret.replace(extensionVersionPlaceholder, version as String)
            }

            // 替换Maven依赖版本。
            @Suppress("UNCHECKED_CAST")
            val dependencyToVersionMap = project.ext["version.dependencyToVersionMap"] as Map<String, String>

            if ("@!dependencyVersion@" in ret) {
                var replaced = false
                for ((dependency, version) in dependencyToVersionMap) {
                    if (dependency in ret) {
                        ret = ret.replace(dependencyVersionPlaceholder, version)
                        replaced = true
                        break
                    }
                }
                if (!replaced) {
                    throw IllegalStateException("Dependency not found in dependencyToVersionMap: $ret")
                }
            }
            return@filter ret
        }
    }
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
    archiveFileName.set(outputName)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Jar> {
    archiveFileName.set(outputName)
}