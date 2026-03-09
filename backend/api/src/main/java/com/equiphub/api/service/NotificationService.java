package com.equiphub.api.service;

import com.equiphub.api.model.*;
import com.equiphub.api.model.Penalty.PenaltyType;
import com.equiphub.api.model.RequestApproval.ApprovalDecision;
import com.equiphub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.name:EQuipHub}")
    private String appName;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ═══════════════════════════════════════════════════════════
    //  1. REQUEST SUBMITTED — notify approver chain
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyRequestSubmitted(Request request) {
        try {
            String studentName = request.getStudent().getFirstName() + " "
                                 + request.getStudent().getLastName();
            String subject = appName + " — New Equipment Request " + request.getRequestId();
            String html = buildHtmlWrapper("New Equipment Request",
                    "<p>A new <strong>" + request.getRequestType() + "</strong> request has been submitted.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Request ID", request.getRequestId())
                    + row("Student", studentName)
                    + row("Department", request.getDepartment().getName())
                    + row("Period", FMT.format(request.getFromDateTime())
                                     + " → " + FMT.format(request.getToDateTime()))
                    + row("Priority", String.valueOf(request.getPriorityLevel()))
                    + row("Emergency", Boolean.TRUE.equals(request.getEmergency()) ? "⚠️ YES" : "No")
                    + "</table>"
                    + "<p>Please review this request in the EQuipHub portal.</p>");

            // Notify instructor if assigned
            if (request.getInstructor() != null) {
                emailService.sendEmail(request.getInstructor().getEmail(), subject, html);
            }
            // Notify supervisor if assigned
            if (request.getSupervisor() != null) {
                emailService.sendEmail(request.getSupervisor().getEmail(), subject, html);
            }

            log.info("[NOTIFY] Request submitted notification sent for {}", request.getRequestId());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send request submitted notification: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  2. APPROVAL DECISION — notify requester
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyApprovalDecision(Request request, RequestApproval approval) {
        try {
            String decision = approval.getDecision().name();
            String emoji = approval.getDecision() == ApprovalDecision.APPROVED ? "✅"
                    : approval.getDecision() == ApprovalDecision.REJECTED ? "❌" : "📝";

            String subject = appName + " — Request " + request.getRequestId()
                             + " " + decision;
            String html = buildHtmlWrapper("Approval Decision: " + emoji + " " + decision,
                    "<p>Your equipment request has received a decision.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Request ID", request.getRequestId())
                    + row("Stage", approval.getApprovalStage().name())
                    + row("Decision", emoji + " " + decision)
                    + row("By", approval.getActorRole())
                    + row("Reason", approval.getReason() != null ? approval.getReason() : "—")
                    + "</table>"
                    + (approval.getComments() != null
                        ? "<p><strong>Comments:</strong> " + approval.getComments() + "</p>" : ""));

            emailService.sendEmail(request.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Approval decision sent to {} for {}",
                    request.getStudent().getEmail(), request.getRequestId());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send approval decision: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  3. AUTO-APPROVAL — notify student
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyAutoApproved(Request request) {
        try {
            String subject = appName + " — Request " + request.getRequestId()
                             + " Auto-Approved ✅";
            String html = buildHtmlWrapper("Auto-Approved ✅",
                    "<p>Your coursework equipment request has been <strong>automatically approved</strong>.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Request ID", request.getRequestId())
                    + row("Period", FMT.format(request.getFromDateTime())
                                     + " → " + FMT.format(request.getToDateTime()))
                    + "</table>"
                    + "<p>You may proceed to collect the equipment from the Technical Officer.</p>");

            emailService.sendEmail(request.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Auto-approval notification sent for {}", request.getRequestId());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send auto-approval notification: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  4. CHECKOUT — notify student & supervisor
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyCheckout(Request request, String equipmentName) {
        try {
            String subject = appName + " — Equipment Checked Out: " + request.getRequestId();
            String html = buildHtmlWrapper("Equipment Checked Out 📦",
                    "<p>Equipment has been checked out for your request.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Request ID", request.getRequestId())
                    + row("Equipment", equipmentName)
                    + row("Return By", FMT.format(request.getToDateTime()))
                    + "</table>"
                    + "<p><strong>Important:</strong> Please return the equipment on time to avoid penalty points.</p>");

            emailService.sendEmail(request.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Checkout notification sent for {}", request.getRequestId());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send checkout notification: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  5. RETURN COMPLETED — notify student
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyReturnCompleted(Request request, boolean hasDamage) {
        try {
            String emoji = hasDamage ? "⚠️" : "✅";
            String subject = appName + " — Equipment Returned " + emoji + ": "
                             + request.getRequestId();
            String html = buildHtmlWrapper("Equipment Returned " + emoji,
                    "<p>Equipment return has been processed.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Request ID", request.getRequestId())
                    + row("Status", hasDamage ? "⚠️ Damage detected — penalty may apply" : "✅ Good condition")
                    + "</table>"
                    + (hasDamage ? "<p>A penalty assessment will be conducted. You will be notified of the outcome.</p>"
                                 : "<p>Thank you for returning the equipment in good condition.</p>"));

            emailService.sendEmail(request.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Return notification sent for {}", request.getRequestId());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send return notification: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  6. PENALTY ISSUED — notify student
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyPenaltyIssued(Penalty penalty) {
        try {
            String subject = appName + " — Penalty Issued: " + penalty.getPoints() + " points";
            String html = buildHtmlWrapper("Penalty Issued ⚠️",
                    "<p>A penalty has been issued against your account.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Penalty ID", String.valueOf(penalty.getPenaltyId()))
                    + row("Type", penalty.getPenaltyType().name())
                    + row("Points", String.valueOf(penalty.getPoints()))
                    + row("Reason", penalty.getReason())
                    + row("Status Level", penalty.getStatusLevel())
                    + "</table>"
                    + "<p>You may appeal this penalty within 7 days through the EQuipHub portal.</p>");

            emailService.sendEmail(penalty.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Penalty notification sent to {}", penalty.getStudent().getEmail());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send penalty notification: {}", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  7. APPEAL DECISION — notify student
    // ═══════════════════════════════════════════════════════════
    @Async
    public void notifyAppealDecision(PenaltyAppeal appeal) {
        try {
            String decision = appeal.getDecision().name();
            String subject = appName + " — Appeal Decision: " + decision;
            String html = buildHtmlWrapper("Appeal Decision: " + decision,
                    "<p>Your penalty appeal has been decided.</p>"
                    + "<table style='width:100%;border-collapse:collapse;'>"
                    + row("Penalty ID", String.valueOf(appeal.getPenalty().getPenaltyId()))
                    + row("Decision", decision)
                    + row("Points Waived", appeal.getPointsWaived() != null
                            ? String.valueOf(appeal.getPointsWaived()) : "0")
                    + row("Reason", appeal.getDecisionReason() != null
                            ? appeal.getDecisionReason() : "—")
                    + "</table>");

            emailService.sendEmail(appeal.getStudent().getEmail(), subject, html);
            log.info("[NOTIFY] Appeal decision sent to {}", appeal.getStudent().getEmail());
        } catch (Exception e) {
            log.error("[NOTIFY] Failed to send appeal decision notification: {}", e.getMessage());
        }
    }

    // ───────────────────────────────────────────────────────────
    //  HTML HELPERS
    // ───────────────────────────────────────────────────────────
    private String buildHtmlWrapper(String title, String body) {
        return """
            <!DOCTYPE html><html><head><meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                .content { padding: 30px; }
                .footer { text-align: center; padding: 15px; background: #f8f9fa; color: #666; font-size: 12px; }
                td { padding: 8px 12px; border-bottom: 1px solid #eee; }
                td:first-child { font-weight: bold; color: #555; width: 40%%; }
            </style></head>
            <body><div class="container">
                <div class="header"><h2 style="margin:0;">%s</h2></div>
                <div class="content">%s</div>
                <div class="footer"><p>University of Jaffna — Faculty of Engineering</p><p>© 2026 EQuipHub</p></div>
            </div></body></html>
            """.formatted(title, body);
    }

    private String row(String label, String value) {
        return "<tr><td>" + label + "</td><td>" + value + "</td></tr>";
    }
}
