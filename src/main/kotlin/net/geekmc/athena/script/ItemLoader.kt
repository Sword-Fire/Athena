package net.geekmc.athena.script

import kotlinx.coroutines.*
import net.geekmc.turingcore.util.coroutine.MinestomSync
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object ItemLoader {
    private val scope = CoroutineScope(Dispatchers.MinestomSync)

    private val host = BasicJvmScriptingHost()

    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ItemScript>()
    private val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<ItemScript> {
        jvm { baseClassLoader(ItemScript::class.java.classLoader) }
    }

    fun loadSync(vararg scripts: SourceCode) {
        scope.launch {
            loadAsync(*scripts)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun loadAsync(vararg scripts: SourceCode) {
        val compileds = withContext(Dispatchers.IO.limitedParallelism(20)) {
            scripts.map { script ->
                async { host.compiler.invoke(script, compilationConfiguration).valueOrThrow() }
            }
        }.awaitAll()
        compileds.forEach { compiled ->
            val result = host.evaluator.invoke(compiled, evaluationConfiguration).valueOrThrow()
            val ret = result.returnValue
            check(ret is kotlin.script.experimental.api.ResultValue.Value)
            val item = ret.value as Item
            println(item)
        }
    }
}