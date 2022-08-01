package uk.gov.justice.digital.hmpps.integrations.delius.audit

import uk.gov.justice.digital.hmpps.audit.InteractionCode

enum class BusinessInteractionCode(override val code: String) : InteractionCode {
    ADD_PERSON_ALLOCATION("CABI231"),
    ADD_EVENT_ALLOCATION("CABI041"),
    CREATE_COMPONENT_TRANSFER("CABI038"),
}
