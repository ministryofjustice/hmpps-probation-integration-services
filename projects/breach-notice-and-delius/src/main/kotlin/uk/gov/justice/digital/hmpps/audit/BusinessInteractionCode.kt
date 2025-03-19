package uk.gov.justice.digital.hmpps.audit

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    DELETE_DOCUMENT("WPBI005"),
    UPLOAD_DOCUMENT("WPBI006"),
}
