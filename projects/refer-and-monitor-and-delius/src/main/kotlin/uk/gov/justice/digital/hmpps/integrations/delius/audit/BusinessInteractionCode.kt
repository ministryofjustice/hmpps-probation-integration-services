package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    MANAGE_NSI("NIBI009"),
    ADD_CONTACT("CLBI003"),
    UPDATE_CONTACT("CLBI007"),
}
