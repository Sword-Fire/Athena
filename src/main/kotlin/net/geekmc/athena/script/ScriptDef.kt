package net.geekmc.athena.script

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = AthenaScriptConfiguration::class
)
abstract class AthenaScript

object AthenaScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        defaultImports.append(
            "net.geekmc.athena.script.dsl.*",
            "net.minestom.server.item.*",
        )
        dependenciesFromClassContext(AthenaScript::class, wholeClasspath = true)
    }
})