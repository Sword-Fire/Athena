package net.geekmc.athena.script

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    fileExtension = "item.kts",
    compilationConfiguration = ItemScriptConfiguration::class
)
abstract class ItemScript

object ItemScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        defaultImports.append(
            "net.geekmc.athena.script.item",
            "net.minestom.server.item.*",
        )
//        dependenciesFromClassContext(Item::class, wholeClasspath = true)
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
})