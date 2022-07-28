package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.annotation.JsonValue
import java.time.ZonedDateTime

data class AllocationEvent(
    val eventType: EventType,
    val description: String,
    val detailUrl: String,
    val occurredAt: ZonedDateTime,
    val additionalInformation: AdditionalInformation,
    val personReference: PersonReference
)

enum class EventType(@JsonValue val value: String) {
    PERSON_ALLOCATED("person.community.manager.allocated"),
    EVENT_ALLOCATED("event.manager.allocated"),
    REQUIREMENT_ALLOCATED("requirement.manager.allocated")
}

data class AdditionalInformation(val allocationId: String)

data class PersonReference(val identifiers: List<PersonIdentifier>)

data class PersonIdentifier(val type: String, val value: String)