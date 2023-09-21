package uk.gov.justice.digital.hmpps.retry

import kotlin.reflect.KClass

fun <T> retry(maxRetries: Int, exceptions: List<KClass<out Exception>> = listOf(Exception::class), code: () -> T): T {
    var throwable: Throwable?
    (1..maxRetries).forEach { count ->
        try {
            return code()
        } catch (e: Throwable) {
            val matchedException = exceptions.firstOrNull { it.isInstance(e) }
            throwable = if (matchedException != null && count < maxRetries) {
                null
            } else {
                e
            }

            if (throwable != null) {
                throw throwable!!
            }
        }
    }
    throw RuntimeException("unknown error")
}
