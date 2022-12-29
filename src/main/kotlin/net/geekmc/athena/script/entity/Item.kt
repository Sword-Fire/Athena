package net.geekmc.athena.script.entity

import net.minestom.server.item.Material

data class ItemColor(
    val red: Int,
    val green: Int,
    val blue: Int
)

data class Item(
    override val id: String,
    val material: Material?,
    val name: String?,
    val lore: List<String>,
    val color: ItemColor?
): BaseEntity