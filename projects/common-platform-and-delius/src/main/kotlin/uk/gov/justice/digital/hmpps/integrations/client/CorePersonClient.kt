package uk.gov.justice.digital.hmpps.integrations.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PutExchange
import java.time.LocalDate

interface CorePersonClient {
    @GetExchange(value = "/person/commonplatform/{defendantId}")
    fun findByDefendantId(@PathVariable defendantId: String): CorePersonRecord

    @PutExchange(value = "/person/probation/{defendantId}")
    fun createPersonRecord(@PathVariable defendantId: String, @RequestBody person: CreateCorePersonRequest)
}

data class CorePersonRecord(
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

data class CreateCorePersonRequest(
    val title: CodeValue?,
    val name: Name,
    val dateOfBirth: LocalDate,
    val identifiers: NewIdentifiers,
    val addresses: List<Address>,
    val sentences: List<Sentence>,
    val nationality: String?,
    val gender: CodeValue
)

data class CodeValue(
    val code: String
)

data class Name(
    val firstName: String,
    val middleNames: String?,
    val lastName: String
)

data class NewIdentifiers(
    val crn: String? = null,
    val pnc: String? = null,
    val cro: String? = null,
    val prisonerNumber: String? = null,
    @JsonProperty("ni")
    val nationalInsuranceNumber: String? = null,
)

data class Address(
    val fullAddress: String,
    val postcode: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val noFixedAbode: Boolean
)

data class Sentence(
    val date: LocalDate
)