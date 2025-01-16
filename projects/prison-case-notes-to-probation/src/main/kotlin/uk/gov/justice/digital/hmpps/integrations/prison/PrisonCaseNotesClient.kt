package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange
import java.net.URI
import java.time.LocalDateTime

interface PrisonCaseNotesClient {
    @GetExchange
    fun getCaseNote(baseUrl: URI): PrisonCaseNote?

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
) {
    companion object {
        val TYPES_OF_INTEREST = setOf(
            TypeSubTypeRequest("PRISON", setOf("RELEASE")),
            TypeSubTypeRequest("TRANSFER", setOf("FROMTOL")),
            TypeSubTypeRequest("GEN", setOf("OSE")),
            TypeSubTypeRequest("ALERT"),
            TypeSubTypeRequest("OMIC"),
            TypeSubTypeRequest("OMIC_OPD"),
            TypeSubTypeRequest("KA"),
        )
    }
}

data class TypeSubTypeRequest(val type: String, val subTypes: Set<String> = setOf())

data class CaseNotesResults(val content: List<PrisonCaseNote>)