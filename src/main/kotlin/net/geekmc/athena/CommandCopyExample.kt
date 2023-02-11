package net.geekmc.athena

import net.geekmc.athena.di.PathTags
import net.geekmc.athena.di.athenaDi
import net.geekmc.turingcore.library.framework.AutoRegister
import net.geekmc.turingcore.util.extender.onlyOp
import net.geekmc.turingcore.util.lang.ExtensionLang
import net.geekmc.turingcore.util.lang.sendLang
import org.kodein.di.instance
import world.cepi.kstom.command.kommand.Kommand
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.div

@AutoRegister
object CommandCopyExample : Kommand({
    val dataFolder by athenaDi.instance<Path>(tag = PathTags.EXTENSION_FOLDER)
    val lang by athenaDi.instance<ExtensionLang>()
    val targetFolder = dataFolder / "examples"

    syntax {
        runCatching {
            Path.of(this::class.java.getResource("/examples")?.path ?: error("Example folder does not exist."))
                .copyTo(targetFolder)
        }.fold(
            onSuccess = {
                sender.sendLang(lang, "command-copy-example-succ")
            },
            onFailure = {
                sender.sendLang(lang, "command-copy-example-fail", it.localizedMessage)
            }
        )
    }.onlyOp()
}, "athenacopyexample")