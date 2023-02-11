package net.geekmc.athena

import net.geekmc.athena.di.PathTags
import net.geekmc.athena.di.athenaDi
import net.geekmc.athena.script.ScriptLoader
import net.geekmc.turingcore.library.framework.AutoRegister
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import org.kodein.di.instance
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.script.experimental.host.toScriptSource

@AutoRegister
object CommandRun : Command("athenarun") {
    val dataFolder by athenaDi.instance<Path>(tag = PathTags.EXTENSION_FOLDER)

    init {
        val ktsName = ArgumentType.String("kts");

        addSyntax({ sender, context ->
            val kts = context.get(ktsName)
            val ktsPath = dataFolder / kts

            val script = ktsPath.toFile().toScriptSource()

            ScriptLoader.loadSync(script)

            sender.sendMessage("Hello, world!")
        }, ktsName)
    }
}