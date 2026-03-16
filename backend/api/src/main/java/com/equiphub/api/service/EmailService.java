package com.equiphub.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:EQuipHub}")
    private String appName;

    /**
     * Send verification code email (HTML version)
     */
    @Async
    public void sendVerificationEmail(String toEmail, String userName, String verificationCode) {
        try {
            log.info("Attempting to send verification email to: {}", toEmail);
            log.debug("From email configured as: {}", fromEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(appName + " - Email Verification Code");

            // Generate HTML content
            String htmlContent = generateVerificationEmailHtml(userName, verificationCode);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);
            
            log.info("Verification email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            log.error("Full error: ", e);
            
            // Fallback to simple text email
            sendSimpleVerificationEmail(toEmail, userName, verificationCode);
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage());
            log.error("Full error: ", e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Fallback: Send simple text email
     */
    public void sendSimpleVerificationEmail(String toEmail, String userName, String verificationCode) {
        try {
            log.info("Attempting fallback: sending simple text email to: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(appName + " - Email Verification Code");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your verification code is: %s\n\n" +
                "This code will expire in 15 minutes.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n%s Team",
                userName, verificationCode, appName
            ));

            mailSender.send(message);
            log.info("Simple verification email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Even simple email failed: {}", e.getMessage());
            throw new RuntimeException("Failed to send any email format", e);
        }
    }

    /**
     * Generate verification email HTML
     */
    private String generateVerificationEmailHtml(String userName, String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 20px auto; 
                        background: white;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        color: white; 
                        padding: 40px 30px; 
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .content { 
                        padding: 40px 30px;
                    }
                    .code-box { 
                        background: #667eea; 
                        color: white; 
                        font-size: 36px; 
                        font-weight: bold; 
                        padding: 25px; 
                        text-align: center; 
                        border-radius: 8px; 
                        letter-spacing: 10px; 
                        margin: 30px 0;
                        font-family: 'Courier New', monospace;
                    }
                    .warning { 
                        background: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning strong {
                        color: #856404;
                    }
                    .footer { 
                        text-align: center; 
                        padding: 20px; 
                        background: #f8f9fa;
                        color: #666; 
                        font-size: 12px;
                    }
                    ul {
                        margin: 10px 0;
                        padding-left: 20px;
                    }
                    li {
                        margin: 5px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Email Verification</p>
                    </div>
                    <div class="content">
                        <h2 style="color: #333; margin-top: 0;">Hello %s,</h2>
                        <p>Thank you for registering with <strong>%s</strong> Equipment Request Management System.</p>
                        <p>Please use the following verification code to complete your registration:</p>
                        
                        <div class="code-box">%s</div>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>This code will expire in <strong>15 minutes</strong></li>
                                <li>Do not share this code with anyone</li>
                                <li>If you didn't request this, please ignore this email</li>
                                <li>You can request a new code if this one expires</li>
                            </ul>
                        </div>
                        
                        <p style="margin-top: 30px;">If you have any questions, please contact your department administrator.</p>
                    </div>
                    <div class="footer">
                        <p style="margin: 5px 0;"><strong>University of Jaffna</strong></p>
                        <p style="margin: 5px 0;">Faculty of Engineering</p>
                        <p style="margin: 15px 0 5px 0;">&copy; 2026 All rights reserved</p>
                        <p style="margin: 5px 0;">This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, userName, appName, verificationCode);
    }

    /**
     * Test email connection
     */
    public boolean testEmailConnection() {
        try {
            log.info("Testing email connection...");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Send to self
            message.setSubject("EQuipHub - Email Test");
            message.setText("Email configuration is working correctly!");

            mailSender.send(message);
            
            log.info("Email test successful!");
            return true;
            
        } catch (Exception e) {
            log.error("Email test failed: {}", e.getMessage());
            log.error("Full error: ", e);
            return false;
        }
    }

    /**
     * Send generic email
     */
    @Async
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            log.info("Sending email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            log.error("Full error: ", e);
        }
    }
    
}
