package uk.gov.justice.digital.hmpps.config

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object CsvMapperConfig {
    val csvMapper = CsvMapper().also { it.registerKotlinModule().registerModule(JavaTimeModule()) }
}
