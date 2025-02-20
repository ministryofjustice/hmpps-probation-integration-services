package uk.gov.justice.digital.hmpps.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logger {
    fun <T : Any> T.logger(): Logger = LoggerFactory.getLogger(this::class.java)
}
