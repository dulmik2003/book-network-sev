package com.dulmika.book.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Async
    public void sendEmail(
            String to,
            String fullUsername,
            EmailTemplateName emailTemplate,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName;

        if (emailTemplate == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplate.getName();
        }

        // todo
        //  configure the mail sender
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED,
                UTF_8.name()
        );

        // todo
        //  set variables for the html template
        HashMap<String, Object> templateVars = new HashMap<>();
        templateVars.put("fullName", fullUsername);
        templateVars.put("confirmationUrl", activationUrl);
        templateVars.put("activationCode", activationCode);

        Context context = new Context();
        context.setVariables(templateVars);

        // todo
        //  set templateVars for the emails
        helper.setFrom("contact@dulmika.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String template = templateEngine.process(templateName, context);
        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
