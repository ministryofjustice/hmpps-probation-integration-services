package uk.gov.justice.digital.hmpps.audit

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    INSERT_ADDRESS("OIBI029"),
    UPDATE_ADDRESS("OIBI032"),
    DELETE_ADDRESS("OIBI034"),
}
