package uk.gov.justice.digital.hmpps.model

import java.time.LocalDateTime

data class WarningDetails(
    val breachReasons: List<CodedDescription>,
    val enforceableContacts: List<EnforceableContact>,
    val unpaidWorkRequirements: List<Requirement>
)

data class EnforceableContact(
    val id: Long,
    val datetime: LocalDateTime,
    val description: String?,
    val type: CodedDescription,
    val outcome: CodedDescription,
    val notes: String?,
    val requirement: Requirement?,
)

data class Requirement(val id: Long, val type: CodedDescription, val subType: CodedDescription?)