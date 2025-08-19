package uk.gov.justice.digital.hmpps.model

data class Manager(
    val staff: ProbationPractitioner,
    val team: CodedValue,
    val probationDeliveryUnit: CodedValue,
    val officeLocations: List<CodedValue>,
)