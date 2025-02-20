package uk.gov.justice.digital.hmpps.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LazyLogger {
    fun <T : Any> T.logger(): Lazy<Logger> {
        return lazy { LoggerFactory.getLogger(this::class.java) }
    }
}