import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    kotlin("jvm") version "1.7.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.geekmc.athena"
version = "0.1.0-SNAPSHOT"

repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

dependencies {
    if (parent?.name == "swork-fire-workspace") {
        implementation(project(":turing-core"))
    } else {
        // TODO: pin version
        implementation("net.geekmc.swork-fire:turing-core:+")
    }

    // TODO: pin version
    compileOnly("com.github.Minestom:Minestom:-SNAPSHOT") {
        exclude(group = "org.tinylog")
    }

    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

// Process some props in extension.json
@Suppress("UnstableApiUsage")
tasks.withType<ProcessResources> {
    filter {
        return@filter if ("!@!version!@!" in it) {
            it.replace("!@!version!@!", version as String)
        } else it
    }
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}