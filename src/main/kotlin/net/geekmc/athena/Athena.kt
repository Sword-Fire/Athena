package net.geekmc.athena

import kotlinx.coroutines.*
import net.geekmc.athena.script.ScriptLoader
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension

@Suppress("unused")
class Athena : Extension() {
    companion object {
        lateinit var INSTANCE: Athena
            private set

    }

    private fun loadAllScripts() {
        runBlocking {
            ScriptLoader.reloadScripts()
        }
    }

    override fun initialize() {
        INSTANCE = this

        MinecraftServer.getCommandManager().register(CommandRun)
        loadAllScripts()

        logger.info("Athena initialized.")
    }

    override fun terminate() {
        logger.info("Athena terminated.")
    }
}