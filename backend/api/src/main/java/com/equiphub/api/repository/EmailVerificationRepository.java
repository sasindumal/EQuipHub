package com.equiphub.api.repository;

import com.equiphub.api.model.EmailVerification;
import com.equiphub.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    
    Optional<EmailVerification> findByUserAndVerificationCodeAndVerifiedFalse(
        User user, 
        String verificationCode
    );
    
    Optional<EmailVerification> findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(User user);
    
    void deleteByExpiresAtBeforeAndVerifiedFalse(LocalDateTime dateTime);
    
    void deleteByUser(User user);
}
