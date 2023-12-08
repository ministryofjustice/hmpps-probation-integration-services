package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown

enum class TransferStatusCode(val code: String) {
    PENDING("PN"),
    REJECTED("TR"),
}
