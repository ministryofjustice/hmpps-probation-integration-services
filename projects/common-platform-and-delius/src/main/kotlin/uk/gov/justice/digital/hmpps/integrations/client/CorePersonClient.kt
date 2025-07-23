package uk.gov.justice.digital.hmpps.integrations.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

interface CorePersonClient {
    @GetExchange(value = "/person/commonplatform/{defendantId}")
    fun findByDefendantId(@PathVariable defendantId: String): CorePersonRecord
}

data class CorePersonRecord(
    val cprUUID: String,
    val firstName: String,
    val middleNames: String?,
    val lastName: String,
    val dateOfBirth: LocalDate?,
    val identifiers: Identifiers,
)

data class Identifiers(
    val crns: List<String> = emptyList(),
    val prisonNumbers: List<String> = emptyList(),
    val defendantIds: List<String> = emptyList(),
    val pncs: List<String> = emptyList(),
    val cros: List<String> = emptyList(),
)