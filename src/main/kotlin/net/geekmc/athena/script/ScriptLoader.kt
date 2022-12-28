package net.geekmc.athena.script

import kotlinx.coroutines.*
import net.geekmc.athena.Athena
import net.geekmc.athena.script.entity.BaseEntity
import net.geekmc.athena.script.entity.Group
import net.geekmc.athena.script.repo.ItemRepo
import net.geekmc.athena.script.repo.loaderListener
import net.geekmc.turingcore.util.coroutine.MinestomSync
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

interface OnScriptLoadListener {
    fun onScriptLoaded(obj: BaseEntity)
}

object ScriptLoader {
    const val SCRIPT_CACHE_DIR_NAME = "script_cache"
    val CHECKSUM_CHECK_REGEX = """\b([a-f0-9]{40})\b""".toRegex()

    const val ITEMS_DIR_NAME = "script"
    const val ITEMS_POSTFIX = ".kts"

    private val loadListeners = mutableListOf<OnScriptLoadListener>()

    private val logger = ComponentLogger.logger()

    val scriptCacheDir: Path by lazy {
        Athena.INSTANCE.dataDirectory.resolve(SCRIPT_CACHE_DIR_NAME)
    }

    private val scope = CoroutineScope(Dispatchers.MinestomSync)

    private val host = BasicJvmScriptingHost()

    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<AthenaScript>()

    private val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<AthenaScript> {
        jvm { baseClassLoader(AthenaScript::class.java.classLoader) }
    }

    private val scriptsDir = Athena.INSTANCE.dataDirectory.resolve(ITEMS_DIR_NAME)

    init {
        addOnScriptLoadListener(ItemRepo.loaderListener())
    }

    fun addOnScriptLoadListener(listener: OnScriptLoadListener.(obj: BaseEntity) -> Unit) {
        addOnScriptLoadListener(object : OnScriptLoadListener {
            override fun onScriptLoaded(obj: BaseEntity) {
                listener(this, obj)
            }
        })
    }

    fun addOnScriptLoadListener(listener: OnScriptLoadListener) {
        loadListeners.add(listener)
    }

    private fun notifyListeners(obj: BaseEntity) {
        if (obj is Group) {
            obj.entities.forEach { notifyListeners(it) }
        }
        loadListeners.forEach { it.onScriptLoaded(obj) }
    }

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

    suspend fun compileScript(
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
    suspend fun loadAsync(vararg scripts: SourceCode, useCache: Boolean = true, saveCache: Boolean = true): List<BaseEntity> {
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
            (ret.value as BaseEntity).apply {
                notifyListeners(this)
            }
        }
    }

    suspend fun reloadScripts() {
        withContext(Dispatchers.IO) {
            Files.createDirectories(scriptsDir)
        }
        val items = scriptsDir.listDirectoryEntries().filter {
            it.isReadable() && it.toString().endsWith(ITEMS_POSTFIX)
        }
        loadAsync(*items.map {
            it.readText().toScriptSource()
        }.toTypedArray())
    }
}