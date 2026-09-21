package oracle.sql

import java.time.ZonedDateTime

class TIMESTAMPTZ(
    private val value: ZonedDateTime,
    private val shouldThrow: Boolean = false,
) {
    fun toZonedDateTime(): ZonedDateTime {
        if (shouldThrow) throw IllegalStateException("boom")
        return value
    }
}

