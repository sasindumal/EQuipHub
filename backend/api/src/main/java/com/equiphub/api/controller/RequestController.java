package com.equiphub.api.controller;

import com.equiphub.api.dto.request.*;
import com.equiphub.api.model.Request;
import com.equiphub.api.model.User;
import com.equiphub.api.security.CustomUserDetails;
import com.equiphub.api.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
// Bug fix #1: Removed /api/v1 prefix — context-path /api/v1 is already set in application.properties,
// matching the same pattern used by EquipmentController, DepartmentController, and AdminController.
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Equipment Request Management",
     description = "Endpoints for creating, submitting, tracking, and managing equipment requests")
@SecurityRequirement(name = "bearerAuth")
public class RequestController {

    private final RequestService requestService;

    // Bug fix #2: Whitelist of allowed sort fields to prevent PropertyReferenceException (500)
    // when an invalid field name is passed as a ?sortBy= query parameter.
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "status", "requestDate", "requestType");

    // ─────────────────────────────────────────────────────────────
    //  RESPONSE HELPERS (matching EquipmentController pattern)
    // ─────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> ok(Object data, String message) {
        return build(HttpStatus.OK, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> created(Object data, String message) {
        return build(HttpStatus.CREATED, data, message, true);
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        return build(HttpStatus.FORBIDDEN, null, message, false);
    }

    private ResponseEntity<Map<String, Object>> bad(String message) {
        return build(HttpStatus.BAD_REQUEST, null, message, false);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, Object data,
                                                       String message, boolean success) {
        Map<String, Object> body = new HashMap<>();
        body.put("success",   success);
        body.put("message",   message);
        body.put("timestamp", LocalDateTime.now());
        if (data != null) body.put("data", data);
        return ResponseEntity.status(status).body(body);
    }

    // ── Department guard ────────────────────────────────────────
    private boolean hasRequestAccess(CustomUserDetails user, UUID targetDeptId) {
        if (user.getRole() == User.Role.SYSTEMADMIN) return true;
        if (user.getDepartmentId() == null) return false;
        return targetDeptId.toString().equals(user.getDepartmentId());
    }

    private UUID callerDeptId(CustomUserDetails user) {
        if (user.getDepartmentId() == null)
            throw new RuntimeException("Your account has no department assigned");
        return UUID.fromString(user.getDepartmentId());
    }

    // ═════════════════════════════════════════════════════════════
    //  1. CREATE REQUEST
    //     STUDENT, LECTURER, INSTRUCTOR, APPOINTEDLECTURER
    // ═════════════════════════════════════════════════════════════
    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Create a new equipment request",
        description = "Creates a request in DRAFT status. Must include at least one equipment item. " +
                      "Students can only create requests for themselves. Staff can create on behalf of students."
    )
    public ResponseEntity<Map<String, Object>> createRequest(
            @Valid @RequestBody CreateRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Students can only create requests for themselves
        if (currentUser.getRole() == User.Role.STUDENT
                && !request.getStudentId().equals(currentUser.getUserId())) {
            return forbidden("Students can only create requests for themselves");
        }

        // Non-SYSTEMADMIN can only create for their own department
        if (currentUser.getRole() != User.Role.SYSTEMADMIN
                && !hasRequestAccess(currentUser, request.getDepartmentId())) {
            return forbidden("You can only create requests for your own department");
        }

        RequestResponseDTO created = requestService.createRequest(request, currentUser.getUserId());
        log.info("[REQ_CREATE] {} by {}", created.getRequestId(), currentUser.getEmail());
        return created(created, "Equipment request created successfully in DRAFT status");
    }

    // ═════════════════════════════════════════════════════════════
    //  2. SUBMIT REQUEST (DRAFT → PENDING)
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/{requestId}/submit")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Submit a draft request for approval",
        description = "Transitions request from DRAFT to PENDINGAPPROVAL or PENDINGRECOMMENDATION " +
                      "based on request type. Validates equipment availability."
    )
    public ResponseEntity<Map<String, Object>> submitRequest(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        RequestResponseDTO submitted = requestService.submitRequest(requestId, currentUser.getUserId());
        log.info("[REQ_SUBMIT] {} submitted by {}", requestId, currentUser.getEmail());
        return ok(submitted, "Request submitted for approval. Status: " + submitted.getStatus());
    }

    // ═════════════════════════════════════════════════════════════
    //  3. GET REQUEST BY ID
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/{requestId}")
    @Operation(summary = "Get request details by ID",
               description = "Returns full request details including items and approval history")
    public ResponseEntity<Map<String, Object>> getRequestById(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        RequestResponseDTO request = requestService.getRequestById(requestId);

        // Students can only view their own requests
        if (currentUser.getRole() == User.Role.STUDENT
                && !request.getStudentId().equals(currentUser.getUserId())) {
            return forbidden("You can only view your own requests");
        }

        // Non-admin, non-student users can only see requests in their department
        if (!currentUser.isAdmin() && currentUser.getRole() != User.Role.STUDENT
                && !hasRequestAccess(currentUser, request.getDepartmentId())) {
            return forbidden("You can only view requests in your department");
        }

        return ok(request, "Request retrieved successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  4. GET MY REQUESTS (current user's requests)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my")
    @Operation(summary = "Get current user's requests",
               description = "Returns paginated list of requests submitted by or for the current user")
    public ResponseEntity<Map<String, Object>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Bug fix #2: Validate sortBy against whitelist to prevent 500 on invalid field names
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return bad("Invalid sortBy field '" + sortBy + "'. Allowed values: " + ALLOWED_SORT_FIELDS);
        }

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<RequestResponseDTO> requests = requestService.getMyRequests(currentUser.getUserId(), pageable);
        return ok(
            Map.of("requests", requests.getContent(),
                   "totalElements", requests.getTotalElements(),
                   "totalPages", requests.getTotalPages(),
                   "currentPage", requests.getNumber(),
                   "pageSize", requests.getSize()),
            "Your requests retrieved successfully"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  5. GET DEPARTMENT REQUESTS  (explicit UUID path)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','SYSTEMADMIN')")
    @Operation(summary = "Get all requests in a department",
               description = "Paginated. TO, DEPTADMIN, HOD see all. Lecturers/Instructors see their dept only.")
    public ResponseEntity<Map<String, Object>> getDepartmentRequests(
            @PathVariable UUID departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasRequestAccess(currentUser, departmentId)) {
            return forbidden("You can only view requests in your own department");
        }

        // Bug fix #2: Validate sortBy against whitelist to prevent 500 on invalid field names
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return bad("Invalid sortBy field '" + sortBy + "'. Allowed values: " + ALLOWED_SORT_FIELDS);
        }

        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<RequestResponseDTO> requests = requestService.getDepartmentRequests(departmentId, pageable);
        return ok(
            Map.of("requests", requests.getContent(),
                   "totalElements", requests.getTotalElements(),
                   "totalPages", requests.getTotalPages(),
                   "currentPage", requests.getNumber(),
                   "departmentId", departmentId),
            "Department requests retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  5a. GET DEPARTMENT REQUESTS  (JWT-resolved — no UUID in path)
    //      Frontend: GET /requests/department
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','SYSTEMADMIN')")
    @Operation(summary = "Get all requests in the caller's department (JWT-resolved)",
               description = "Resolves department from the authenticated user's JWT. " +
                             "Identical response shape to GET /requests/department/{departmentId}.")
    public ResponseEntity<Map<String, Object>> getDepartmentRequestsForCaller(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return bad("Invalid sortBy field '" + sortBy + "'. Allowed values: " + ALLOWED_SORT_FIELDS);
        }

        UUID deptId = callerDeptId(currentUser);
        Sort sort = direction.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        Page<RequestResponseDTO> requests = requestService.getDepartmentRequests(deptId, pageable);
        log.debug("[REQ_DEPT] {} fetched dept {} requests (page {})",
                currentUser.getEmail(), deptId, page);
        return ok(
            Map.of("requests",      requests.getContent(),
                   "totalElements", requests.getTotalElements(),
                   "totalPages",    requests.getTotalPages(),
                   "currentPage",   requests.getNumber(),
                   "departmentId",  deptId),
            "Department requests retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  5b. GET DEPARTMENT REQUEST STATS  (JWT-resolved — no UUID)
    //      Frontend: GET /requests/department/stats
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Request statistics for the caller's department (JWT-resolved)",
               description = "Returns request counts by status. " +
                             "Identical response to GET /requests/department/{id}/stats.")
    public ResponseEntity<Map<String, Object>> getDepartmentStatsForCaller(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        Map<String, Object> stats = requestService.getDepartmentRequestStats(deptId);
        return ok(stats, "Department request statistics retrieved");
    }

    // ═════════════════════════════════════════════════════════════
    //  5c. GET PENDING COUNT FOR DEPARTMENT  (badge endpoint)
    //      Frontend: GET /requests/department/{departmentId}/pending
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/pending")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Count of pending-approval requests in a department",
               description = "Lightweight count endpoint used by the Requests page badge. " +
                             "Returns {count, departmentId}.")
    public ResponseEntity<Map<String, Object>> getDepartmentPendingCount(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasRequestAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());
        Page<RequestResponseDTO> pending = requestService.getRequestsByStatusAndDepartment(
                Request.RequestStatus.PENDINGAPPROVAL, departmentId, pageable);
        return ok(
            Map.of("count",        pending.getTotalElements(),
                   "departmentId", departmentId),
            "Pending request count retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  6. GET REQUESTS BY STATUS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get requests by status",
               description = "Valid: DRAFT, PENDINGAPPROVAL, PENDINGRECOMMENDATION, APPROVED, REJECTED, " +
                             "MODIFICATIONPROPOSED, CANCELLED, INUSE, RETURNED, COMPLETED, OVERDUE, PENALTYASSESSED")
    public ResponseEntity<Map<String, Object>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Request.RequestStatus requestStatus;
        try {
            requestStatus = Request.RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return bad("Invalid status '" + status + "'. Valid: " +
                       Arrays.toString(Request.RequestStatus.values()));
        }

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<RequestResponseDTO> requests = requestService.getRequestsByStatus(requestStatus, pageable);
        return ok(
            Map.of("requests",      requests.getContent(),
                   "totalElements", requests.getTotalElements(),
                   "totalPages",    requests.getTotalPages(),
                   "status",        requestStatus),
            "Requests filtered by status"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  7. UPDATE REQUEST (DRAFT only)
    // ═════════════════════════════════════════════════════════════
    @PutMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Update a draft request",
        description = "Only requests in DRAFT status can be updated. " +
                      "Pass only the fields you want to change."
    )
    public ResponseEntity<Map<String, Object>> updateRequest(
            @PathVariable String requestId,
            @Valid @RequestBody UpdateRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Students can only update their own requests
        RequestResponseDTO existing = requestService.getRequestById(requestId);
        if (currentUser.getRole() == User.Role.STUDENT
                && !existing.getStudentId().equals(currentUser.getUserId())) {
            return forbidden("You can only update your own requests");
        }

        RequestResponseDTO updated = requestService.updateRequest(requestId, request, currentUser.getUserId());
        return ok(updated, "Request updated successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  8. CANCEL REQUEST
    // ═════════════════════════════════════════════════════════════
    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER','TECHNICALOFFICER','DEPARTMENTADMIN','SYSTEMADMIN')")
    @Operation(
        summary = "Cancel a request",
        description = "Can cancel DRAFT, PENDINGAPPROVAL, PENDINGRECOMMENDATION, or MODIFICATIONPROPOSED requests. " +
                      "Cancels all pending items."
    )
    public ResponseEntity<Map<String, Object>> cancelRequest(
            @PathVariable String requestId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Students can only cancel their own requests
        RequestResponseDTO existing = requestService.getRequestById(requestId);
        if (currentUser.getRole() == User.Role.STUDENT
                && !existing.getStudentId().equals(currentUser.getUserId())) {
            return forbidden("You can only cancel your own requests");
        }

        RequestResponseDTO cancelled = requestService.cancelRequest(requestId, currentUser.getUserId());
        log.info("[REQ_CANCEL] {} cancelled by {}", requestId, currentUser.getEmail());
        return ok(cancelled, "Request cancelled successfully");
    }

    // ═════════════════════════════════════════════════════════════
    //  9. EMERGENCY REQUESTS (department)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/emergency")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get active emergency requests in a department",
               description = "Returns all emergency requests in PENDING, APPROVED, or INUSE status")
    public ResponseEntity<Map<String, Object>> getEmergencyRequests(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasRequestAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        List<RequestResponseDTO> emergencies = requestService.getEmergencyRequests(departmentId);
        return ok(
            Map.of("requests", emergencies, "count", emergencies.size()),
            emergencies.isEmpty() ? "No active emergency requests" :
                    emergencies.size() + " active emergency request(s)"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  10. SLA-BREACHED REQUESTS
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/sla-breached")
    @PreAuthorize("hasAnyRole('DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Get SLA-breached requests",
               description = "Returns all pending requests that have exceeded their SLA deadline")
    public ResponseEntity<Map<String, Object>> getSlaBreachedRequests() {
        List<RequestResponseDTO> breached = requestService.getSlaBreachedRequests();
        return ok(
            Map.of("requests", breached, "count", breached.size()),
            breached.isEmpty() ? "No SLA breaches" :
                    breached.size() + " request(s) have breached SLA"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  11. DEPARTMENT REQUEST STATS  (explicit UUID path)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/department/{departmentId}/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','SYSTEMADMIN')")
    @Operation(summary = "Request statistics for a department",
               description = "Returns request counts by status for the department dashboard")
    public ResponseEntity<Map<String, Object>> getDepartmentStats(
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (!hasRequestAccess(currentUser, departmentId)) {
            return forbidden("Access restricted to your own department");
        }

        Map<String, Object> stats = requestService.getDepartmentRequestStats(departmentId);
        return ok(stats, "Department request statistics retrieved");
    }

    // ═════════════════════════════════════════════════════════════
    //  12. MY DEPARTMENT REQUESTS (shortcut)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my-department")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT','LECTURER','INSTRUCTOR','APPOINTEDLECTURER')")
    @Operation(summary = "Shortcut: get all requests in my department",
               description = "Returns paginated requests for the current user's department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<RequestResponseDTO> requests = requestService.getDepartmentRequests(deptId, pageable);
        return ok(
            Map.of("requests",      requests.getContent(),
                   "totalElements", requests.getTotalElements(),
                   "totalPages",    requests.getTotalPages(),
                   "departmentId",  deptId),
            "Your department's requests retrieved"
        );
    }

    // ═════════════════════════════════════════════════════════════
    //  13. MY DEPARTMENT STATS (shortcut)
    // ═════════════════════════════════════════════════════════════
    @GetMapping("/my-department/stats")
    @PreAuthorize("hasAnyRole('TECHNICALOFFICER','DEPARTMENTADMIN','HEADOFDEPARTMENT')")
    @Operation(summary = "Shortcut: request stats for my department")
    public ResponseEntity<Map<String, Object>> getMyDepartmentStats(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        UUID deptId = callerDeptId(currentUser);
        Map<String, Object> stats = requestService.getDepartmentRequestStats(deptId);
        return ok(stats, "Your department's request statistics retrieved");
    }
}
