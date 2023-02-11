package net.geekmc.athena.script.repo

import net.geekmc.athena.di.athenaDi
import net.geekmc.athena.script.OnScriptLoadListener
import net.geekmc.athena.script.entity.BaseEntity
import org.kodein.di.instance
import org.slf4j.Logger

inline fun <reified T: BaseEntity> BaseRepo<T>.loaderListener() = object : OnScriptLoadListener {
    val logger by athenaDi.instance<Logger>()

    override fun onScriptLoaded(obj: BaseEntity) {
        if (obj is T) {
            logger.info("Loaded ${T::class.java.simpleName} ${obj.id}")
            entities[obj.id] = obj
        }
    }
}