package uk.gov.justice.digital.hmpps.config

import tools.jackson.dataformat.csv.CsvMapper
import tools.jackson.module.kotlin.kotlinModule

object CsvMapperConfig {
    val csvMapper: CsvMapper = CsvMapper.builder().run { addModule(kotlinModule()) }.build()
}
