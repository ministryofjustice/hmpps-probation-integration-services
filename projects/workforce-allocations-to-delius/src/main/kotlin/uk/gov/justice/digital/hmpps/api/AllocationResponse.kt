package uk.gov.justice.digital.hmpps.api

import java.time.LocalDate

data class AllocationResponse(
    val crn: String,
    val event: Event,
    val name: Name,
    val tier: String,
    val sentence: Sentence,
    val initialAppointment: InitialAppointment,
    val probationStatus: String
)

data class Event(val number: String, val manager: EventManager)
data class EventManager(val code: String, val name: Name, val teamCode: String)
data class InitialAppointment(val date: LocalDate)
data class Sentence(val type: String, val date: LocalDate, val length: String)
data class Name(
    val forename: String,
    val surname: String,
    val middleName: String?,
)

data class AllocationDemandResponse(val cases: List<AllocationResponse>)