package uk.gov.justice.digital.hmpps.controller.model

import java.time.ZonedDateTime

data class PersonalDetailsAndCircumstances(
    val preferredLanguage: CodeAndDescription? = null,
    val personalCircumstances: List<PersonCircumstance>? = emptyList(),
    val disabilities: List<Disability>? = emptyList(),
    val offenderPersonalityDisorder: OffenderPersonalityDisorder? = null
)

data class OffenderPersonalityDisorder(
    val status: CodeAndDescription
)

data class Disability(
    val type: CodeAndDescription,
    val updatedAt: ZonedDateTime,
)

data class PersonCircumstance(
    val type: CodeAndDescription,
    val subType: CodeAndDescription,
    val updatedAt: ZonedDateTime
)

data class CodeAndDescription(
    val code: String,
    val description: String,
)