package com.flip.skateshop.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [PhoneValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class PhoneFormat(
    val message: String = "Invalid phone number format.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PhoneValidator : ConstraintValidator<PhoneFormat, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = value?.matches(Regex("^0[1-9]\\d{8}\$")) == true
}
