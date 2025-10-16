package uk.gov.justice.digital.hmpps.api.model.appointment

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

interface AppointmentRequest {
    val date: LocalDate
    val startTime: LocalTime
    val endTime: LocalTime

    fun changesDateOrTime(date: LocalDate, startTime: LocalTime, endTime: LocalTime): Boolean {
        return !this.date.isEqual(date) ||
            this.startTime.isDifferentTo(startTime) || this.endTime.isDifferentTo(endTime)
    }

    private fun LocalTime.isDifferentTo(other: LocalTime) = this.isBefore(other) || this.isAfter(other)
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