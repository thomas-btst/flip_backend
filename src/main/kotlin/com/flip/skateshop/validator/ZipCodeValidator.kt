package com.flip.skateshop.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@Constraint(validatedBy = [ZipCodeValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ZipCodeFormat(
    val message: String = "Invalid zip code.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)

class ZipCodeValidator : ConstraintValidator<ZipCodeFormat, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = value?.matches(Regex("[0-9]{5}")) == true
}
