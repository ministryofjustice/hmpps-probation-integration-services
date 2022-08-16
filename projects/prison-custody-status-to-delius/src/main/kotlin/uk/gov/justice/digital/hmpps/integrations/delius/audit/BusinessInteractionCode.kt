package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    // Add any Delius interaction codes used by the service here...
    // e.g. UPLOAD_DOCUMENT("WPBI006"),
}
