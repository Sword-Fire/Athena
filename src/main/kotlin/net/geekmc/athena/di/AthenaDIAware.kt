package net.geekmc.athena.di

import org.kodein.di.DI
import org.kodein.di.DIAware

interface AthenaDIAware : DIAware {
    override val di: DI get() = athenaDi
}