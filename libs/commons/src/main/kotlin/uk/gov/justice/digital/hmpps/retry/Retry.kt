package uk.gov.justice.digital.hmpps.retry

import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

fun <T> retry(
    maxRetries: Int,
    delay: Duration = Duration.ofMillis(100),
    exceptions: List<KClass<out Exception>> = listOf(Exception::class),
    code: () -> T
): T {
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
            if (throwable == null) {
                TimeUnit.MILLISECONDS.sleep(delay.toMillis() * count * count)
            }else{
                throw throwable!!
            }
        }
    }
    throw RuntimeException("unknown error")
}
