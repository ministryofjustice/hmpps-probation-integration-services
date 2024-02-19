package uk.gov.justice.digital.hmpps.data.generator

import java.util.concurrent.atomic.AtomicLong

object IdGenerator {
    private val id = AtomicLong(1)

    fun getAndIncrement() = id.getAndIncrement()

    fun id() = getAndIncrement()

    fun get() = id.get()
}
