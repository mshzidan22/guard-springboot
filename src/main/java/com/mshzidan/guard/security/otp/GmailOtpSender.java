package com.mshzidan.guard.security.otp;

import com.mshzidan.guard.security.exception.OtpSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component("gmailSender")
@RequiredArgsConstructor
public class GmailOtpSender implements OtpSender {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${email.from}")
    private String from;
    @Value("${email.otp.subject}")
    private String subject;
    @Value("${email.otp.template}")
    private String template;

    @Override
    public void sendOtp(String recipient, String code) {
        try {
            Context ctx = new Context();
            ctx.setVariable("otp", code);
            String html = templateEngine.process(template, ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true);
            h.setFrom(from);
            h.setTo(recipient);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            //will be handled by Exception Handler.
            throw new OtpSendingException("Failed to send OTP email to " + recipient, e);
        }
    }
}