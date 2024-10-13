package com.flip.skateshop.service

import com.flip.skateshop.config.SpringProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val scope: CoroutineScope,
    springProperties: SpringProperties,
) {
    private val mailProperties = springProperties.mail

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
        val body = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <title>Activation de compte Flip Skateshop</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        width: 100%;
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        padding: 20px;
                        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        border-radius: 8px;
                    }
                    h1 {
                        color: #333333;
                        text-align: center;
                        font-size: 24px;
                        margin-bottom: 20px;
                    }
                    p {
                        font-size: 16px;
                        color: #555555;
                        line-height: 1.6;
                    }
                    .code-container {
                        display: flex;
                        justify-content: center;
                        margin: 20px 0;
                    }
                    .code-box {
                        font-size: 24px;
                        color: #0080ff;
                        font-weight: bold;
                        border: 2px solid #000000;
                        padding: 3px;
                        margin: 0 5px;
                        width: 30px;
                        text-align: center;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #888888;
                    }
                    a {
                        color: #ff5733;
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Activation de ton compte Flip Skateshop</h1>
                    <p>Bonjour <strong>$firstName $lastName</strong>,</p>
                    <p>Bienvenue chez Flip Skateshop ! Nous sommes ravis de te compter parmi nos membres.</p>
                    <p>Pour activer ton compte, voici ton code de vérification :</p>
                    <div class="code-container">
                        ${activationKey.map { "<div class='code-box'>$it</div>" }.joinToString("")}
                    </div>
                    <p>Utilise ce code pour finaliser ton inscription et commencer à profiter de nos offres exclusives.</p>
                    <p>Merci et à très bientôt sur Flip Skateshop !</p>
                    <p class="footer">Cordialement,<br>L'équipe Flip Skateshop</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        sendMail(email, subject, body)
    }

    suspend fun sendResetPasswordKey(email: String, firstName: String, lastName: String, key: String) {
        val subject = "Réinitialisation de ton mot de passe Flip Skateshop"
        val body = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
                <meta charset="UTF-8">
                <title>Réinitialisation de ton mot de passe Flip Skateshop</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        width: 100%;
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        padding: 20px;
                        box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        border-radius: 8px;
                    }
                    h1 {
                        color: #333333;
                        text-align: center;
                        font-size: 24px;
                        margin-bottom: 20px;
                    }
                    p {
                        font-size: 16px;
                        color: #555555;
                        line-height: 1.6;
                    }
                    .code-container {
                        display: flex;
                        justify-content: center;
                        margin: 20px 0;
                    }
                    .code-box {
                        font-size: 24px;
                        color: #0080ff;
                        font-weight: bold;
                        border: 2px solid #000000;
                        padding: 3px;
                        margin: 0 5px;
                        width: 30px;
                        text-align: center;
                        border-radius: 4px;
                    }
                    .footer {
                        margin-top: 20px;
                        text-align: center;
                        font-size: 14px;
                        color: #888888;
                    }
                    a {
                        color: #ff5733;
                        text-decoration: none;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Réinitialisation de ton mot de passe Flip Skateshop</h1>
                    <p>Bonjour <strong>$firstName $lastName</strong>,</p>
                    <p>Nous avons bien reçu ta demande de réinitialisation de mot de passe.</p>
                    <p>Pour créer un nouveau mot de passe, voici ton code de réinitialisation :</p>
                    <div class="code-container">
                        ${key.map { "<div class='code-box'>$it</div>" }.joinToString("")}
                    </div>
                    <p>Utilise ce code pour finaliser la réinitialisation de ton mot de passe.</p>
                    <p> Si tu n'as pas demandé de réinitialisation, tu peux ignorer cet e-mail en toute sécurité.</p>
                    <p>Merci et à très bientôt sur Flip Skateshop !</p>
                    <p class="footer">Cordialement,<br>L'équipe Flip Skateshop</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        sendMail(email, subject, body)
    }
}