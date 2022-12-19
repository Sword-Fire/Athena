package net.geekmc.athena.script

import kotlinx.coroutines.*
import net.geekmc.athena.Athena
import net.geekmc.turingcore.util.coroutine.MinestomSync
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

/**
 * @param R The type of the return value
 */
abstract class ScriptLoader<R> {
    companion object {
        const val SCRIPT_CACHE_DIR_NAME = "script_cache"
        val CHECKSUM_CHECK_REGEX = """\b([a-f0-9]{40})\b""".toRegex()
    }

    private val logger = ComponentLogger.logger()

    val scriptCacheDir: Path by lazy {
        Athena.INSTANCE.dataDirectory.resolve(SCRIPT_CACHE_DIR_NAME)
    }

    private val scope = CoroutineScope(Dispatchers.MinestomSync)

    private val host = BasicJvmScriptingHost()

    //  override val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ItemScript>()
    abstract val compilationConfiguration: ScriptCompilationConfiguration

    //  override val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<ItemScript> {
    //      jvm { baseClassLoader(ItemScript::class.java.classLoader) }
    //  }
    abstract val evaluationConfiguration: ScriptEvaluationConfiguration

    private fun createCacheDir() {
        Files.createDirectories(scriptCacheDir)
    }

    fun loadSync(vararg scripts: SourceCode, saveCache: Boolean = true) {
        scope.launch {
            loadAsync(*scripts, saveCache = saveCache)
        }
    }

    private fun ByteArray.checksum(): String = MessageDigest.getInstance("SHA1").digest(this).joinToString("") {
        "%02x".format(it)
    }.lowercase()

    private suspend fun readCacheFromPath(path: Path): CompiledScript = withContext(Dispatchers.IO) {
        val fileInputStream = FileInputStream(path.toFile())
        val objectInputStream = ObjectInputStream(fileInputStream)
        return@withContext (objectInputStream.readObject() as CompiledScript).also {
            fileInputStream.close()
            objectInputStream.close()
        }
    }

    private suspend fun saveCacheToPath(compiled: CompiledScript, path: Path) = withContext(Dispatchers.IO) {
        val fileOutputStream = FileOutputStream(path.toFile())
        val objectOutputStream = ObjectOutputStream(fileOutputStream)
        objectOutputStream.writeObject(compiled)
        fileOutputStream.close()
        objectOutputStream.close()
    }

    private suspend fun compileScript(
        script: SourceCode,
        useCache: Boolean = true,
        saveCache: Boolean = true
    ): CompiledScript {
        if (useCache) {
            val scriptChecksum = script.text.encodeToByteArray().checksum()
            check(scriptChecksum.matches(CHECKSUM_CHECK_REGEX)) { "Invalid checksum: $scriptChecksum" }
            val cacheFile = scriptCacheDir.resolve("$scriptChecksum.compiled")
            runCatching {
                val compiledScript = if (Files.exists(cacheFile)) {
                    readCacheFromPath(cacheFile)
                } else {
                    host.compiler.invoke(script, compilationConfiguration).valueOrThrow()
                }
                return compiledScript.apply {
                    if (saveCache) {
                        saveCacheToPath(compiledScript, cacheFile)
                    }
                }
            }.onFailure { logger.info("Error happened when load script", it) }
        }
        return host.compiler.invoke(script, compilationConfiguration).valueOrThrow()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun loadAsync(vararg scripts: SourceCode, useCache: Boolean = true, saveCache: Boolean = true): List<R> {
        createCacheDir()
        val compileds = withContext(Dispatchers.IO.limitedParallelism(20)) {
            scripts.map { script ->
                async { compileScript(script, useCache = useCache, saveCache = saveCache) }
            }
        }.awaitAll()
        return compileds.map { compiled ->
            val result = host.evaluator.invoke(compiled, evaluationConfiguration).valueOrThrow()
            val ret = result.returnValue
            check(ret is ResultValue.Value)
            checkNotNull(ret.value)
            @Suppress("UNCHECKED_CAST")
            ret.value as R
        }
    }
}