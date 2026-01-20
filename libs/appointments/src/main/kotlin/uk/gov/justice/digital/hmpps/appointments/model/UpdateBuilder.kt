package uk.gov.justice.digital.hmpps.appointments.model

import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.model.UpdateAppointment.*

class UpdateBuilder<T> {
    var id: ValueProvider<T, Long>? = null
    var reference: ValueProvider<T, String>? = null
    var amendDateTime: UpdateProvider<T, Schedule>? = null
    var recreate: UpdateProvider<T, Recreate>? = null
    var reschedule: UpdateProvider<T, Recreate>? = null
    var reassign: UpdateProvider<T, Assignee>? = null
    var applyOutcome: UpdateProvider<T, Outcome>? = null
    var appendNotes: UpdateProvider<T, String?>? = null
    var flagAs: UpdateProvider<T, Flags>? = null
}

typealias ValueProvider<T, Values> = (T) -> Values
typealias UpdateProvider<T, Values> = Values.(T) -> Values

internal typealias UpdatePipeline<T> = List<Pair<T, AppointmentContact>>

internal fun <RequestType, Values> UpdatePipeline<RequestType>.applyUpdates(
    provider: UpdateProvider<RequestType, Values>?,
    fn: UpdatePipeline<RequestType>.(UpdateProvider<RequestType, Values>) -> UpdatePipeline<RequestType>
): UpdatePipeline<RequestType> {
    if (provider == null) return this
    return this.fn(provider)
}
