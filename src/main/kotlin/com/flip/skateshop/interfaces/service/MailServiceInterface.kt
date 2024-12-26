package com.flip.skateshop.interfaces.service

interface MailServiceInterface {
    suspend fun sendActivationKey(
        email: String,
        firstName: String,
        lastName: String,
        activationKey: String,
    )

    suspend fun sendResetPasswordKey(
        email: String,
        firstName: String,
        lastName: String,
        key: String,
    )

    suspend fun sendCommandConfirmation(
        email: String,
        firstName: String,
        lastName: String,
        commandId: String,
        invoice: String,
    )
}
