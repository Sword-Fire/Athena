package net.geekmc.athena.script.repo

import net.geekmc.athena.script.entity.BaseEntity

interface BaseRepo<T : BaseEntity> {
    val entities: MutableMap<String, T>
}