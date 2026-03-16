package com.equiphub.api.service;

import com.equiphub.api.model.EmailVerification;
import com.equiphub.api.model.User;
import com.equiphub.api.repository.EmailVerificationRepository;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate and send verification code
     */
    @Transactional
    public void generateAndSendVerificationCode(User user) {
        // Delete any existing unverified codes
        verificationRepository.deleteByUser(user);

        // Generate 6-digit code
        String verificationCode = String.format("%06d", random.nextInt(1000000));

        // Create verification record
        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setVerificationCode(verificationCode);
        verification.setVerificationType("REGISTRATION");
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        verificationRepository.save(verification);

        // Send email
        String userName = user.getFirstName() + " " + user.getLastName();
        emailService.sendVerificationEmail(user.getEmail(), userName, verificationCode);

        log.info("Verification code generated and sent to: {}", user.getEmail());
    }

    /**
     * Verify code
     */
    @Transactional
    public boolean verifyCode(User user, String code) {
        Optional<EmailVerification> verificationOpt = 
            verificationRepository.findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(user);

        if (verificationOpt.isEmpty()) {
            log.warn("No verification code found for user: {}", user.getEmail());
            return false;
        }

        EmailVerification verification = verificationOpt.get();

        // Check if expired
        if (verification.isExpired()) {
            log.warn("Verification code expired for user: {}", user.getEmail());
            return false;
        }

        // Check attempt count
        if (!verification.canRetry()) {
            log.warn("Max verification attempts exceeded for user: {}", user.getEmail());
            return false;
        }

        // Increment attempt count
        verification.setAttemptCount(verification.getAttemptCount() + 1);
        verificationRepository.save(verification);

        // Verify code
        if (!verification.getVerificationCode().equals(code)) {
            log.warn("Invalid verification code for user: {}", user.getEmail());
            return false;
        }

        // Mark as verified
        verification.setVerified(true);
        verification.setVerifiedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        // Update user
        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
        return true;
    }

    /**
     * Resend verification code
     */
    @Transactional
    public void resendVerificationCode(User user) {
        Optional<EmailVerification> existingOpt = 
            verificationRepository.findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(user);

        if (existingOpt.isPresent()) {
            EmailVerification existing = existingOpt.get();
            
            // Check if last code was sent recently (prevent spam)
            if (existing.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(2))) {
                throw new RuntimeException("Please wait 2 minutes before requesting a new code");
            }
        }

        // Generate new code
        generateAndSendVerificationCode(user);
    }

    /**
     * Clean up expired verification codes (runs daily)
     */
    @Scheduled(cron = "0 0 0 * * *") // Midnight daily
    @Transactional
    public void cleanupExpiredCodes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
        verificationRepository.deleteByExpiresAtBeforeAndVerifiedFalse(cutoffTime);
        log.info("Expired verification codes cleaned up");
    }
}
