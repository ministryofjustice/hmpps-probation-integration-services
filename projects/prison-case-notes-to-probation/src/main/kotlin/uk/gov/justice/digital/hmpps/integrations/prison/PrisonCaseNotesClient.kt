package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import java.net.URI
import java.time.LocalDateTime

interface PrisonCaseNotesClient {
    @PostExchange
    fun searchCaseNotes(
        uri: URI,
        @RequestBody searchCaseNotes: SearchCaseNotes
    ): CaseNotesResults
}

data class SearchCaseNotes(
    val typeSubTypes: Set<TypeSubTypeRequest>,
    val occurredFrom: LocalDateTime? = null,
    val occurredTo: LocalDateTime? = null,
    val includeSensitive: Boolean = true,
    val page: Int = 1,
    val size: Int = Int.MAX_VALUE,
    val sort: String = "occurredAt,desc",
)

data class TypeSubTypeRequest(val type: String, val subTypes: Set<String> = setOf())

data class CaseNotesResults(val content: List<PrisonCaseNote>)