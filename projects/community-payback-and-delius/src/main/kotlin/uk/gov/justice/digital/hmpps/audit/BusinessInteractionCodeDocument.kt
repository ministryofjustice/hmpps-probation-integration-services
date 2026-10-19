package uk.gov.justice.digital.hmpps.audit

enum class BusinessInteractionCodeDocument(override val code: String) : InteractionCode {
    DELETE_DOCUMENT("WPBI005"),
    UPLOAD_DOCUMENT("WPBI006"),
}