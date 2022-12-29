package net.geekmc.athena.script.repo

import net.geekmc.athena.script.entity.Item

object ItemRepo: BaseRepo<Item> {
    override val entities: MutableMap<String, Item> = mutableMapOf()
}