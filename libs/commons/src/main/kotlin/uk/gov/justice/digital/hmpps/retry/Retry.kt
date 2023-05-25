package uk.gov.justice.digital.hmpps.retry

fun <T> retry(maxRetries: Int, code: () -> T): T {
    var throwable: Throwable? = null
    (1..maxRetries).forEach { _ ->
        try {
            return code()
        } catch (e: Throwable) {
            throwable = e
        }
    }
    throw throwable!!
}
