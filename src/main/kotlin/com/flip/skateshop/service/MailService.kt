package com.flip.skateshop.service

import com.flip.skateshop.config.SpringProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val scope: CoroutineScope,
    private val templateEngine: TemplateEngine,
    springProperties: SpringProperties,
) {
    private val mailProperties = springProperties.mail

    companion object {
        const val MAIL_RESOURCE_DIR = "mails"
    }

    private fun getContext(
        subject: String,
        firstName: String,
        lastName: String,
        verificationKey: String
    ) = Context().apply {
        setVariable("title", subject)
        setVariable("firstName", firstName)
        setVariable("lastName", lastName)
        setVariable("verificationKey", verificationKey.toList())
    }

    suspend fun sendMail(to: String, subject: String, body: String) {
        scope.launch {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom("Flip Skateshop <${mailProperties.username}>")
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body, true)
            javaMailSender.send(message)
        }
    }

    suspend fun sendActivationKey(email: String, firstName: String, lastName: String, activationKey: String) {
        val subject = "Activation de ton compte Flip Skateshop"
        sendMail(
            email,
            subject,
            templateEngine.process(
                "$MAIL_RESOURCE_DIR/activateAccountEmail.html",
                getContext(subject, firstName, lastName, activationKey)
            )
        )
    }

    suspend fun sendResetPasswordKey(email: String, firstName: String, lastName: String, key: String) {
        val subject = "Réinitialisation de ton mot de passe Flip Skateshop"
        sendMail(
            email,
            subject,
            templateEngine.process(
                "$MAIL_RESOURCE_DIR/resetPasswordEmail.html",
                getContext(subject, firstName, lastName, key)
            )
        )
    }
}