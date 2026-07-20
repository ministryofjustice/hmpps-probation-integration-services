package uk.gov.justice.digital.hmpps.model

data class PersonalCircumstances(
    val type: CodeDescription,
    val subType: CodeDescription?
)
