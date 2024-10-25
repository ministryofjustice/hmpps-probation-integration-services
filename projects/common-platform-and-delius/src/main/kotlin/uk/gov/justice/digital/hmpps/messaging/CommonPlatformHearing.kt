package uk.gov.justice.digital.hmpps.messaging

import org.openfolder.kotlinasyncapi.annotation.channel.Message
import java.time.LocalDate
import java.time.ZonedDateTime

@Message
data class CommonPlatformHearing(
    val hearing: Hearing
)

data class Hearing(
    val id: String,
    val courtCentre: CourtCentre,
    val type: HearingType,
    val jurisdictionType: String,
    val hearingDays: List<HearingDay>,
    val prosecutionCases: List<ProsecutionCase>
)

data class ProsecutionCase(
    val id: String,
    val initiationCode: String,
    val prosecutionCaseIdentifier: ProsecutionCaseIdentifier,
    val defendants: List<Defendant>,
    val caseStatus: String,
    val caseMarkers: List<Any> = emptyList()
)

data class ProsecutionCaseIdentifier(
    val prosecutionAuthorityCode: String,
    val prosecutionAuthorityId: String,
    val caseURN: String
)

data class Defendant(
    val id: String,
    val offences: List<Offence>,
    val prosecutionCaseId: String,
    val personDefendant: PersonDefendant?,
    val legalEntityDefendant: Any? = null,
    val masterDefendantId: String,
    val pncId: String,
    val croNumber: String
)

data class Offence(
    val id: String,
    val offenceDefinitionId: String,
    val offenceCode: String,
    val offenceTitle: String,
    val wording: String,
    val offenceLegislation: String,
    val listingNumber: Int,
    val judicialResults: List<JudicialResult>,
    val plea: Plea? = null,
    val verdict: Verdict? = null
)

data class JudicialResult(
    val isConvictedResult: Boolean?,
    val label: String?,
    val judicialResultTypeId: String?,
    val resultText: String?
)

data class Plea(
    val pleaValue: String?,
    val pleaDate: LocalDate
)

data class Verdict(
    val verdictDate: ZonedDateTime?,
    val verdictType: VerdictType
)

data class VerdictType(
    val description: String?,
)

data class PersonDefendant(
    val personDetails: PersonDetails
)

data class PersonDetails(
    val gender: String,
    val lastName: String,
    val middleName: String? = null,
    val firstName: String,
    val dateOfBirth: LocalDate,
    val address: Address,
    val contact: Contact?,
    val ethnicity: Ethnicity
)

data class Address(
    val address1: String? = null,
    val address2: String? = null,
    val address3: String? = null,
    val address4: String? = null,
    val address5: String? = null,
    val postcode: String? = null
)

data class Contact(
    val home: String? = null,
    val mobile: String,
    val work: String? = null
)

data class HearingType(
    val id: String,
    val description: String,
    val welshDescription: String? = null
)

data class Ethnicity(
    val observedEthnicityDescription: String,
    val selfDefinedEthnicityDescription: String
)

data class CourtCentre(
    val id: String,
    val code: String,
    val roomId: String,
    val roomName: String
)

data class HearingDay(
    val sittingDay: ZonedDateTime,
    val listedDurationMinutes: Int,
    val listingSequence: Int
)