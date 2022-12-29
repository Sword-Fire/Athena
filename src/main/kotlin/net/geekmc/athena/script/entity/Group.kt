package net.geekmc.athena.script.entity

data class Group(
    val entities: MutableList<BaseEntity> = mutableListOf(), override val id: String = ""
): BaseEntity {
    operator fun BaseEntity.unaryPlus() {
        entities.add(this)
    }
}
