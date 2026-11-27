package uk.gov.justice.digital.hmpps.audit

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    DELETE_CONTACT("CLBT050"),
}

interface InteractionCode {
    val code: String
}
