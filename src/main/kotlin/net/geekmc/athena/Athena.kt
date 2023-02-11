package net.geekmc.athena

import kotlinx.coroutines.*
import net.geekmc.athena.di.initAthenaDi
import net.geekmc.athena.script.ScriptLoader
import net.geekmc.turingcore.library.framework.AutoRegisterFramework
import net.minestom.server.extensions.Extension

@Suppress("unused")
class Athena : Extension() {
    private val autoRegisterFramework by lazy {
        AutoRegisterFramework.load(
            this::class.java.classLoader,
            "net.geekmc.athena",
            this.logger
        )
    }

    private fun loadAllScripts() {
        runBlocking {
            ScriptLoader.reloadScripts()
        }
    }

    override fun preInitialize() {
        initAthenaDi(this)
    }

    override fun initialize() {
        autoRegisterFramework.registerAll()
        loadAllScripts()

        logger.info("Athena initialized.")
    }

    override fun terminate() {
        autoRegisterFramework.unregisterAll()
        logger.info("Athena terminated.")
    }
}