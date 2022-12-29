package net.geekmc.athena

import kotlinx.coroutines.*
import net.geekmc.athena.script.ItemLoader
import net.geekmc.turingcore.framework.TuringFrameWork
import net.geekmc.turingcore.util.color.toComponent
import net.minestom.server.MinecraftServer
import net.minestom.server.extensions.Extension
import java.nio.file.Files
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.script.experimental.host.toScriptSource

@Suppress("unused")
class Athena : Extension() {
    companion object {
        lateinit var INSTANCE: Athena
            private set
        const val ITEMS_DIR_NAME = "items"
    }

    private fun loadAllItemsFromData() {
        val itemsDir = dataDirectory.resolve(ITEMS_DIR_NAME)
        Files.createDirectories(itemsDir)
        val items = itemsDir.listDirectoryEntries().filter {
            it.isReadable() && it.toString().endsWith(".item.kts")
        }
        runBlocking {
            ItemLoader.loadAsync(*items.map {
                it.readText().toScriptSource()
            }.toTypedArray()).forEach { logger.info(it.toString()) }
        }
//        logger.info("脚本数量为 ${items.size}")
//        val times = 20
//        logger.info("每个脚本加载 $times 次")
//        val time = measureTimeMillis {
//            runBlocking {
//                ItemLoader.loadAsync(*buildList {
//                    repeat(times) {
//                        items.forEach {
//                            add(it.toFile().toScriptSource())
//                        }
//                    }
//                }.toTypedArray())
//            }
//        }
//        logger.info("加载完成，耗时 ${time}ms")
    }

    override fun initialize() {
        INSTANCE = this

        registerFramework()
        MinecraftServer.getCommandManager().register(CommandRun)

        loadAllItemsFromData()

        logger.info("Athena initialized.")
    }

    override fun terminate() {
        logger.info("Athena terminated.")
    }

    private fun registerFramework() {
        TuringFrameWork.registerExtension("net.geekmc.athena", this).apply {
            consolePrefix = "[Athena] "
            playerPrefix = "&f[&gAthena&f] ".toComponent()
        }
    }
}