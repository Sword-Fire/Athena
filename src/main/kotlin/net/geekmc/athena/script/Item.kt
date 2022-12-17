package net.geekmc.athena.script

import net.minestom.server.item.Material

data class ItemColor(
    val red: Int,
    val green: Int,
    val blue: Int
)

data class Item(
    val id: String,
    val material: Material?,
    val name: String?,
    val lore: List<String>,
    val color: ItemColor?
)

class ItemBuilder(private val id: String) {
    var material: Material? = null
    var name: String? = null
    var lore = mutableListOf<String>()
    var color: ItemColor? = null

    inner class Lore {
        operator fun String.unaryMinus() {
            lore.add(this)
        }
    }

    fun lore(block: Lore.() -> Unit) {
        Lore().block()
    }

    fun color(red: Int, green: Int, blue: Int) {
        color = ItemColor(red, green, blue)
    }

    fun build(): Item = Item(id, material, name, lore, color)
}

fun item(id: String, block: ItemBuilder.() -> Unit): Item =
    ItemBuilder(id).apply(block).build()
