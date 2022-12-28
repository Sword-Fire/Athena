package net.geekmc.athena.script.dsl

import net.geekmc.athena.script.entity.Group

fun group(block: Group.() -> Unit): Group = Group().apply(block)