package net.geekmc.athena.script.repo

import net.geekmc.athena.Athena
import net.geekmc.athena.script.OnScriptLoadListener
import net.geekmc.athena.script.entity.BaseEntity

inline fun <reified T: BaseEntity> BaseRepo<T>.loaderListener() = object : OnScriptLoadListener {
    override fun onScriptLoaded(obj: BaseEntity) {
        if (obj is T) {
            Athena.INSTANCE.logger.info("Loaded ${T::class.java.simpleName} ${obj.id}")
            entities[obj.id] = obj
        }
    }
}