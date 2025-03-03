package org.kopingenieria.logging.service;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.mail.internet.MimeMessage;
import org.kopingenieria.logging.model.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NotificationServiceImpl implements NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;


    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(LogEvent event) {
        logger.info("Enviando alerta para el evento: {}", event.getEventId());
        sendEmail(event);
        sendSlackNotification(event);
        sendSms(event,"+1234567890");
    }

    private void sendEmail(LogEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo("destinatario@example.com"); // Destinatarios del email
            helper.setSubject("Alerta - " + event.getClassName() + "." + event.getMethodName());

            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Cuerpo del email con formato HTML y metadatos
            // Metadatos
            String htmlContent = new StringBuilder()
                    .append("<html>")
                    .append("<body style='font-family:Arial,sans-serif;'>")
                    .append("<div style='margin-bottom:20px;'>")
                    .append("<img src='https://example.com/logo.png' alt='Company Logo' style='width:150px; height:auto;'>")
                    .append("</div>")
                    .append("<h2 style='color:#333;'>Alerta - Error en la Aplicación</h2>")
                    .append("<p>Estimado equipo,</p>")
                    .append("<p>Se ha detectado un error en la aplicación. A continuación, se detallan los datos relevantes:</p>")
                    .append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:60%;'>")
                    .append("<tr><th style='background-color:#f2f2f2;'>Fecha y Hora</th><td>").append(formattedDate).append("</td></tr>")
                    .append("<tr><th style='background-color:#f2f2f2;'>Clase</th><td>").append(event.getClassName()).append("</td></tr>")
                    .append("<tr><th style='background-color:#f2f2f2;'>Método</th><td>").append(event.getMethodName()).append("</td></tr>")
                    .append("<tr><th style='background-color:#f2f2f2;'>Mensaje de Error</th><td>").append(event.getException()).append("</td></tr>")
                    .append("<tr><th style='background-color:#f2f2f2;'>Metadatos</th><td>").append(event.getMetadata()).append("</td></tr>")
                    .append("</table>")
                    .append("<p>Por favor, tomen las medidas necesarias para abordar este problema a la brevedad.</p>")
                    .append("<footer style='margin-top:20px; font-size:12px; color:#555;'>")
                    .append("<p>Atentamente,<br>Equipo de Soporte</p>")
                    .append("<p style='margin-top:10px;'>Si tiene alguna consulta, comuníquese con nosotros a <a href='mailto:soporte@example.com'>soporte@example.com</a> o llame al +1-800-555-1234.</p>")
                    .append("<p style='font-size:10px; color:#999;'>Este mensaje y cualquier archivo adjunto son confidenciales y se dirigen exclusivamente a su destinatario.</p>")
                    .append("</footer>")
                    .append("</body>")
                    .append("</html>")
                    .toString();

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSlackNotification(LogEvent event) {
        try {
            Slack slack = Slack.getInstance();
            String formattedMessage = String.format(
                    "*Alerta - Error en la aplicación*\n" +
                            "> *Fecha y hora:* %s\n" +
                            "> *Clase:* %s\n" +
                            "> *Método:* %s\n" +
                            "> *Mensaje de error:* %s\n" +
                            "> *Metadatos:* %s",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    event.getClassName(),
                    event.getMethodName(),
                    event.getException(),
                    event.getMetadata()
            );
    
            Payload payload = Payload.builder().text(formattedMessage).build();
            WebhookResponse response = slack.send(slackWebhookUrl, payload);
    
            if (response.getCode() == 200) {
                logger.info("Notificación de Slack enviada correctamente.");
            } else {
                logger.error("Error al enviar la notificación de Slack. Código de respuesta: {}", response.getCode());
            }
        } catch (Exception e) {
            logger.error("Error al enviar la notificación de Slack: ", e);
        }
    }

    private void sendSms(LogEvent event, String tophoneNumber) {
        try {
            // Inicializa el cliente de Twilio
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(tophoneNumber), // Número de teléfono del destinatario
                    new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                    String.format("🔥 [Alerta Crítica] 🔥\n- Clase: %s\n- Método: %s\n- Error: %s", event.getClassName(), event.getMethodName(), event.getException())
            ).create();
            logger.info("SMS enviado correctamente. SID: {}", message.getSid());
        } catch (Exception e) {
            logger.error("Error al enviar el SMS: ", e);
        }
    }
}
