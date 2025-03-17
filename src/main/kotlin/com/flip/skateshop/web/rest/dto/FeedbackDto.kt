package com.flip.skateshop.web.rest.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.Instant

class FeedbackDto(
    val user: FeedbackUserDto?,
    val rate: Double,
    val comment: String,
    val date: Instant,
)

class CreateFeedbackDto(
    @field:Min(value = 0)
    @field:Max(value = 5)
    val rate: Double,
    val comment: String,
)
