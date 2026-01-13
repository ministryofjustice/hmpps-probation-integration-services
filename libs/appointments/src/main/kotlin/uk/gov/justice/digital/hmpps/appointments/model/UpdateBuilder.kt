package uk.gov.justice.digital.hmpps.appointments.model

import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact

class UpdateBuilder<T> {
    var reference: ((T) -> String)? = null
    var amendDateTime: ((T) -> UpdateAppointment.Schedule)? = null
    var recreate: ((T) -> UpdateAppointment.RecreateAppointment)? = null
    var reschedule: ((T) -> UpdateAppointment.RecreateAppointment)? = null
    var reassign: ((T) -> UpdateAppointment.Assignee)? = null
    var applyOutcome: ((T) -> UpdateAppointment.Outcome)? = null
    var appendNotes: ((T) -> String?)? = null
    var flagAs: ((T) -> UpdateAppointment.Flags)? = null
}

typealias UpdatePipeline<T> = List<Pair<T, AppointmentContact>>
typealias ConfigProvider<T, R> = (T) -> R

fun <T, R> UpdatePipeline<T>.withConfig(
    configProvider: ConfigProvider<T, R>?,
    fn: UpdatePipeline<T>.(ConfigProvider<T, R>) -> UpdatePipeline<T>
): UpdatePipeline<T> {
    if (configProvider == null) return this
    return fn(configProvider)
}
