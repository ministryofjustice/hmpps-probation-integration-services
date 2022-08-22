package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    ADD_RELEASE("TCBI002"),
    ADD_RECALL("TCBI006"),
}
