package net.geekmc.athena

import kotlinx.coroutines.*
import net.geekmc.athena.script.ScriptLoader
import net.geekmc.turingcore.framework.TuringFrameWork
import net.geekmc.turingcore.util.color.toComponent
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

        registerFramework()
        MinecraftServer.getCommandManager().register(CommandRun)

        loadAllScripts()

        logger.info("Athena initialized.")
    }

    override fun terminate() {
        logger.info("Athena terminated.")
    }

    private fun registerFramework() {
        TuringFrameWork.registerExtension("net.geekmc.minotaur", this).apply {
            consolePrefix = "[Minotaur] "
            playerPrefix = "&f[&gMinotaur&f] ".toComponent()
        }
    }
}