package uk.gov.justice.digital.hmpps.model

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@ValidCode
data class Code(
    val code: String
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidCodeValidator::class])
annotation class ValidCode(
    val message: String = "must not be blank",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ValidCodeValidator : ConstraintValidator<ValidCode, Code> {
    override fun isValid(value: Code?, context: ConstraintValidatorContext): Boolean =
        value?.code?.isNotBlank() == true
}

data class CodeDescription(
    val code: String,
    val description: String
)

data class CodeName(
    val name: String,
    val code: String
)
