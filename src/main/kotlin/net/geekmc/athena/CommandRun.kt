package net.geekmc.athena

import net.geekmc.athena.script.ItemLoader
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import kotlin.script.experimental.host.toScriptSource

object CommandRun : Command("athenarun") {
    init {
        val ktsName = ArgumentType.String("kts");

        addSyntax({ sender, context ->
            val kts = context.get(ktsName)
            val ktsPath = Athena.INSTANCE.dataDirectory.resolve(kts)

            val script = ktsPath.toFile().toScriptSource()

            ItemLoader.loadSync(script)

            sender.sendMessage("Hello, world!")
        }, ktsName)
    }
}