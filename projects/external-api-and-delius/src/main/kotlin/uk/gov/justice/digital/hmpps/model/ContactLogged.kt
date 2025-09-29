package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

data class ContactLogged(
    val id: Long,
    val crn: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val contactDate: ZonedDateTime,
    val type: CodedValue,
    val description: String,
    val location: CodedValue?,
    val outcome: CodedValue?,
    val officer: Officer,
    val notes: String?
)

data class Officer(
    val code: String,
    val name: Name,
    val team: OfficerTeam,
)

data class OfficerTeam(
    val code: String,
    val description: String,
    val pdu: OfficerPdu
)

data class OfficerPdu(val code: String, val description: String, val provider: Provider)

data class ContactsLogged(val content: List<ContactLogged>, val totalPages: Int, val totalResults: Long)