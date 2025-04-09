package uk.gov.justice.digital.hmpps.model

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import kotlin.reflect.KClass

@ValidSearchRequest
data class SearchRequest(
    val firstName: String? = null,
    val surname: String? = null,
    val dateOfBirth: LocalDate? = null,
    val pncNumber: String? = null,
    val crn: String? = null,
    val nomsNumber: String? = null,
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SearchRequestValidator::class])
annotation class ValidSearchRequest(
    val message: String = "At least one decision field or at least one action must be provided",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)

class SearchRequestValidator : ConstraintValidator<ValidSearchRequest, SearchRequest> {
    override fun isValid(request: SearchRequest, context: ConstraintValidatorContext): Boolean = with(request) {
        listOfNotNull(
            firstName,
            surname,
            crn,
            nomsNumber,
            pncNumber,
            dateOfBirth,
        ).isNotEmpty()
    }
}