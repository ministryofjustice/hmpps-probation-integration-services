package uk.gov.justice.digital.hmpps.api.model.appointment

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit.SECONDS
import kotlin.reflect.KClass

interface AppointmentRequest {
    val date: LocalDate
    val startTime: LocalTime
    val endTime: LocalTime

    fun isInFuture() = LocalDate.now().let { now -> date > now || (date == now && startTime > LocalTime.now()) }

    fun changesDateOrTime(date: LocalDate, startTime: LocalTime?, endTime: LocalTime?): Boolean {
        return !this.date.isEqual(date) ||
            this.startTime.truncatedTo(SECONDS).isDifferentTo(startTime?.truncatedTo(SECONDS)) ||
            this.endTime.truncatedTo(SECONDS).isDifferentTo(endTime?.truncatedTo(SECONDS))
    }

    private fun LocalTime.isDifferentTo(other: LocalTime?) =
        other == null || this.isBefore(other) || this.isAfter(other)
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AppointmentValidator::class])
annotation class ValidAppointment(
    val message: String = DEFAULT_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
) {
    companion object {
        const val DEFAULT_MESSAGE = "End time must be after start time."
    }
}

class AppointmentValidator : ConstraintValidator<ValidAppointment, AppointmentRequest> {
    override fun isValid(appointment: AppointmentRequest, context: ConstraintValidatorContext): Boolean =
        with(appointment) {
            endTime.isAfter(startTime)
        }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FutureAppointmentValidator::class])
annotation class FutureAppointment(
    val message: String = DEFAULT_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
) {
    companion object {
        const val DEFAULT_MESSAGE = "Appointment must be in the future."
    }
}

class FutureAppointmentValidator : ConstraintValidator<FutureAppointment, AppointmentRequest> {
    override fun isValid(appointment: AppointmentRequest, context: ConstraintValidatorContext): Boolean =
        with(appointment) {
            val today = LocalDate.now()
            appointment.date.isAfter(today) || (date.isEqual(today) && appointment.startTime.isAfter(LocalTime.now()))
        }
}