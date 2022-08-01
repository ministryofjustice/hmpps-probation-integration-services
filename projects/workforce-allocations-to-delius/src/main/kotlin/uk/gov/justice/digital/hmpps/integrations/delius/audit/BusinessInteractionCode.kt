package uk.gov.justice.digital.hmpps.integrations.delius.audit

enum class BusinessInteractionCode(val code: String) {
    ADD_PERSON_ALLOCATION("CABI231"),
    ADD_EVENT_ALLOCATION("CABI041"),
    CREATE_COMPONENT_TRANSFER("CABI038"),
}
