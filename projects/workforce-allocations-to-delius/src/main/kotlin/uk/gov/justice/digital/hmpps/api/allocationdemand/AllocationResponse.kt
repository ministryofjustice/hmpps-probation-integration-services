package uk.gov.justice.digital.hmpps.api.allocationdemand

import java.time.LocalDate

data class AllocationResponse(
    val crn: String,
    val name: Name,
    val event: Event,
    val sentence: Sentence?,
    val initialAppointment: InitialAppointment?
)

data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
)

data class Event(val number: String, val manager: EventManager)
data class EventManager(val code: String, val name: Name, val teamCode: String)
data class Sentence(val type: String, val date: LocalDate, val length: String)

data class InitialAppointment(val date: LocalDate)

data class AllocationDemandResponse(val cases: List<AllocationResponse>)
