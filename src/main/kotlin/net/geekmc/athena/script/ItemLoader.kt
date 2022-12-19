package net.geekmc.athena.script

import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object ItemLoader: ScriptLoader<Item>() {
    override val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ItemScript>()
    override val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<ItemScript> {
        jvm { baseClassLoader(ItemScript::class.java.classLoader) }
    }
}