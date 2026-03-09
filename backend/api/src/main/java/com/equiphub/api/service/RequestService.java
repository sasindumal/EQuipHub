package com.equiphub.api.service;

import com.equiphub.api.dto.request.*;
import com.equiphub.api.model.*;
import com.equiphub.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final RequestRepository         requestRepository;
    private final RequestItemRepository     requestItemRepository;
    private final RequestApprovalRepository approvalRepository;
    private final UserRepository            userRepository;
    private final DepartmentRepository      departmentRepository;
    private final CourseRepository           courseRepository;
    private final EquipmentRepository       equipmentRepository;

    // Active statuses for overlap detection
    private static final List<Request.RequestStatus> ACTIVE_STATUSES = List.of(
            Request.RequestStatus.PENDINGAPPROVAL,
            Request.RequestStatus.PENDINGRECOMMENDATION,
            Request.RequestStatus.APPROVED,
            Request.RequestStatus.INUSE
    );

    // ─────────────────────────────────────────────────────────────
    //  REQUEST ID GENERATION: REQ-YYYY-NNNNN
    // ─────────────────────────────────────────────────────────────
    private String generateRequestId() {
        String prefix = "REQ-" + Year.now().getValue() + "-";
        Optional<String> lastId = requestRepository.findLastRequestIdByPrefix(prefix);

        int nextNumber = 1;
        if (lastId.isPresent()) {
            String lastNum = lastId.get().substring(prefix.length());
            try {
                nextNumber = Integer.parseInt(lastNum) + 1;
            } catch (NumberFormatException e) {
                log.warn("Could not parse last request ID number: {}", lastId.get());
            }
        }
        return String.format("%s%05d", prefix, nextNumber);
    }

    // ─────────────────────────────────────────────────────────────
    //  CREATE REQUEST
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public RequestResponseDTO createRequest(CreateRequestDTO dto, UUID submitterId) {
        // Validate date range
        if (!dto.getToDateTime().isAfter(dto.getFromDateTime())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Resolve entities
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + dto.getStudentId()));

        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("Submitter not found: " + submitterId));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found: " + dto.getDepartmentId()));

        // Emergency validation
        if (Boolean.TRUE.equals(dto.getIsEmergency())
                && (dto.getEmergencyJustification() == null || dto.getEmergencyJustification().isBlank())) {
            throw new RuntimeException("Emergency justification is required for emergency requests");
        }

        // Resolve optional references
        Course course = null;
        if (dto.getCourseId() != null) {
            course = courseRepository.findById(dto.getCourseId().toString())
                    .orElseThrow(() -> new RuntimeException("Course not found: " + dto.getCourseId()));
        }

        User supervisor = null;
        if (dto.getSupervisorId() != null) {
            supervisor = userRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found: " + dto.getSupervisorId()));
        }

        User instructor = null;
        if (dto.getInstructorId() != null) {
            instructor = userRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new RuntimeException("Instructor not found: " + dto.getInstructorId()));
        }

        // Validate request type requirements
        validateRequestTypeRequirements(dto);

        // Build request
        String requestId = generateRequestId();
        Request request = Request.builder()
                .requestId(requestId)
                .requestType(dto.getRequestType())
                .student(student)
                .submitter(submitter)
                .department(department)
                .fromDateTime(dto.getFromDateTime())
                .toDateTime(dto.getToDateTime())
                .status(Request.RequestStatus.DRAFT)
                .course(course)
                .supervisor(supervisor)
                .instructor(instructor)
                .description(dto.getDescription())
                .priorityLevel(dto.getPriorityLevel())
                .slaHours(dto.getSlaHours())
                .emergency(Boolean.TRUE.equals(dto.getIsEmergency()))
                .emergencyJustification(dto.getEmergencyJustification())
                .extensionCount(0)
                .maxExtensions(2)
                .build();

        Request savedRequest = requestRepository.save(request);
        log.info("[REQUEST_CREATE] {} type={} by student={} submitter={}",
                requestId, dto.getRequestType(), student.getEmail(), submitter.getEmail());

        // Create request items
        List<RequestItem> savedItems = createRequestItems(savedRequest, dto.getItems());

        return mapToResponse(savedRequest, savedItems, Collections.emptyList());
    }

    // ─────────────────────────────────────────────────────────────
    //  SUBMIT REQUEST (DRAFT → PENDING)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public RequestResponseDTO submitRequest(String requestId, UUID submitterId) {
        Request request = findRequest(requestId);

        if (request.getStatus() != Request.RequestStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT requests can be submitted. Current status: " + request.getStatus());
        }

        // Validate equipment availability for all items
        List<RequestItem> items = requestItemRepository.findByRequestRequestId(requestId);
        for (RequestItem item : items) {
            validateEquipmentAvailability(item.getEquipment(), request.getFromDateTime(), request.getToDateTime());
        }

        // Determine initial status based on request type
        Request.RequestStatus nextStatus = determineSubmitStatus(request.getRequestType());
        request.setStatus(nextStatus);
        request.setSubmittedAt(LocalDateTime.now());

        Request updated = requestRepository.save(request);
        log.info("[REQUEST_SUBMIT] {} status → {} by {}", requestId, nextStatus, submitterId);

        List<RequestApproval> approvals = approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId);
        return mapToResponse(updated, items, approvals);
    }

    // ─────────────────────────────────────────────────────────────
    //  GET REQUEST BY ID
    // ─────────────────────────────────────────────────────────────
    public RequestResponseDTO getRequestById(String requestId) {
        Request request = findRequest(requestId);
        List<RequestItem> items = requestItemRepository.findByRequestRequestId(requestId);
        List<RequestApproval> approvals = approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId);
        return mapToResponse(request, items, approvals);
    }

    // ─────────────────────────────────────────────────────────────
    //  GET MY REQUESTS (paginated)
    // ─────────────────────────────────────────────────────────────
    public Page<RequestResponseDTO> getMyRequests(UUID studentId, Pageable pageable) {
        return requestRepository.findByStudentUserId(studentId, pageable)
                .map(request -> {
                    List<RequestItem> items = requestItemRepository.findByRequestRequestId(request.getRequestId());
                    List<RequestApproval> approvals = approvalRepository
                            .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());
                    return mapToResponse(request, items, approvals);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  GET DEPARTMENT REQUESTS (paginated)
    // ─────────────────────────────────────────────────────────────
    public Page<RequestResponseDTO> getDepartmentRequests(UUID departmentId, Pageable pageable) {
        return requestRepository.findByDepartmentDepartmentId(departmentId, pageable)
                .map(request -> {
                    List<RequestItem> items = requestItemRepository.findByRequestRequestId(request.getRequestId());
                    List<RequestApproval> approvals = approvalRepository
                            .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());
                    return mapToResponse(request, items, approvals);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  GET REQUESTS BY STATUS
    // ─────────────────────────────────────────────────────────────
    public Page<RequestResponseDTO> getRequestsByStatus(Request.RequestStatus status, Pageable pageable) {
        return requestRepository.findByStatus(status, pageable)
                .map(request -> {
                    List<RequestItem> items = requestItemRepository.findByRequestRequestId(request.getRequestId());
                    List<RequestApproval> approvals = approvalRepository
                            .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());
                    return mapToResponse(request, items, approvals);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE REQUEST (only DRAFT)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public RequestResponseDTO updateRequest(String requestId, UpdateRequestDTO dto, UUID updatedBy) {
        Request request = findRequest(requestId);

        if (request.getStatus() != Request.RequestStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT requests can be updated. Current status: " + request.getStatus());
        }

        // Update fields if provided
        if (dto.getFromDateTime() != null)           request.setFromDateTime(dto.getFromDateTime());
        if (dto.getToDateTime() != null)             request.setToDateTime(dto.getToDateTime());
        if (dto.getDescription() != null)            request.setDescription(dto.getDescription());
        if (dto.getPriorityLevel() != null)          request.setPriorityLevel(dto.getPriorityLevel());
        if (dto.getIsEmergency() != null)            request.setEmergency(dto.getIsEmergency());
        if (dto.getEmergencyJustification() != null) request.setEmergencyJustification(dto.getEmergencyJustification());

        // Resolve optional references
        if (dto.getCourseId() != null) {
            Course course = courseRepository.findById(dto.getCourseId().toString())
                    .orElseThrow(() -> new RuntimeException("Course not found: " + dto.getCourseId()));
            request.setCourse(course);
        }
        if (dto.getSupervisorId() != null) {
            User supervisor = userRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("Supervisor not found: " + dto.getSupervisorId()));
            request.setSupervisor(supervisor);
        }
        if (dto.getInstructorId() != null) {
            User instructor = userRepository.findById(dto.getInstructorId())
                    .orElseThrow(() -> new RuntimeException("Instructor not found: " + dto.getInstructorId()));
            request.setInstructor(instructor);
        }

        // Update items if provided
        List<RequestItem> items;
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            requestItemRepository.deleteByRequestRequestId(requestId);
            items = createRequestItems(request, dto.getItems());
        } else {
            items = requestItemRepository.findByRequestRequestId(requestId);
        }

        // Validate date range
        if (request.getToDateTime() != null && request.getFromDateTime() != null
                && !request.getToDateTime().isAfter(request.getFromDateTime())) {
            throw new RuntimeException("End date must be after start date");
        }

        Request updated = requestRepository.save(request);
        log.info("[REQUEST_UPDATE] {} updated by {}", requestId, updatedBy);

        List<RequestApproval> approvals = approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId);
        return mapToResponse(updated, items, approvals);
    }

    // ─────────────────────────────────────────────────────────────
    //  CANCEL REQUEST
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public RequestResponseDTO cancelRequest(String requestId, UUID cancelledBy) {
        Request request = findRequest(requestId);

        // Can only cancel DRAFT, PENDING, or PENDINGRECOMMENDATION
        List<Request.RequestStatus> cancellableStatuses = List.of(
                Request.RequestStatus.DRAFT,
                Request.RequestStatus.PENDINGAPPROVAL,
                Request.RequestStatus.PENDINGRECOMMENDATION,
                Request.RequestStatus.MODIFICATIONPROPOSED
        );

        if (!cancellableStatuses.contains(request.getStatus())) {
            throw new RuntimeException("Cannot cancel request in status: " + request.getStatus());
        }

        request.setStatus(Request.RequestStatus.CANCELLED);

        // Cancel all pending items
        List<RequestItem> items = requestItemRepository.findByRequestRequestId(requestId);
        items.forEach(item -> {
            if (item.getStatus() == RequestItem.ItemStatus.PENDING
                    || item.getStatus() == RequestItem.ItemStatus.APPROVED) {
                item.setStatus(RequestItem.ItemStatus.CANCELLED);
            }
        });
        requestItemRepository.saveAll(items);

        Request updated = requestRepository.save(request);
        log.info("[REQUEST_CANCEL] {} cancelled by {}", requestId, cancelledBy);

        List<RequestApproval> approvals = approvalRepository.findByRequestRequestIdOrderByDecidedAtAsc(requestId);
        return mapToResponse(updated, items, approvals);
    }

    // ─────────────────────────────────────────────────────────────
    //  GET EMERGENCY REQUESTS
    // ─────────────────────────────────────────────────────────────
    public List<RequestResponseDTO> getEmergencyRequests(UUID departmentId) {
        List<Request> emergencyRequests = requestRepository.findEmergencyByDepartment(
                departmentId, ACTIVE_STATUSES);
        return emergencyRequests.stream().map(request -> {
            List<RequestItem> items = requestItemRepository.findByRequestRequestId(request.getRequestId());
            List<RequestApproval> approvals = approvalRepository
                    .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());
            return mapToResponse(request, items, approvals);
        }).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  GET SLA-BREACHED REQUESTS
    // ─────────────────────────────────────────────────────────────
    public List<RequestResponseDTO> getSlaBreachedRequests() {
        List<Request.RequestStatus> pendingStatuses = List.of(
                Request.RequestStatus.PENDINGAPPROVAL,
                Request.RequestStatus.PENDINGRECOMMENDATION
        );
        List<Request> breached = requestRepository.findSlaBreachedRequests(
                pendingStatuses, LocalDateTime.now());
        return breached.stream().map(request -> {
            List<RequestItem> items = requestItemRepository.findByRequestRequestId(request.getRequestId());
            List<RequestApproval> approvals = approvalRepository
                    .findByRequestRequestIdOrderByDecidedAtAsc(request.getRequestId());
            return mapToResponse(request, items, approvals);
        }).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  DASHBOARD STATS
    // ─────────────────────────────────────────────────────────────
    public Map<String, Object> getDepartmentRequestStats(UUID departmentId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("departmentId", departmentId);
        stats.put("totalRequests", requestRepository.countByDepartmentDepartmentId(departmentId));

        for (Request.RequestStatus status : Request.RequestStatus.values()) {
            stats.put("count_" + status.name(),
                    requestRepository.countByDepartmentDepartmentIdAndStatus(departmentId, status));
        }

        stats.put("generatedAt", LocalDateTime.now());
        return stats;
    }

    // ═════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═════════════════════════════════════════════════════════════

    private Request findRequest(String requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
    }

    private void validateRequestTypeRequirements(CreateRequestDTO dto) {
        switch (dto.getRequestType()) {
            case LABSESSION:
                if (dto.getInstructorId() == null) {
                    throw new RuntimeException("Instructor is required for LABSESSION requests");
                }
                break;
            case COURSEWORK:
                if (dto.getCourseId() == null) {
                    throw new RuntimeException("Course is required for COURSEWORK requests");
                }
                break;
            case RESEARCH:
                if (dto.getSupervisorId() == null) {
                    throw new RuntimeException("Supervisor is required for RESEARCH requests");
                }
                break;
            case EXTRACURRICULAR:
            case PERSONAL:
                break;
        }
    }

    private Request.RequestStatus determineSubmitStatus(Request.RequestType type) {
        return switch (type) {
            case LABSESSION    -> Request.RequestStatus.PENDINGRECOMMENDATION;
            case COURSEWORK    -> Request.RequestStatus.PENDINGRECOMMENDATION;
            case RESEARCH      -> Request.RequestStatus.PENDINGRECOMMENDATION;
            case EXTRACURRICULAR -> Request.RequestStatus.PENDINGAPPROVAL;
            case PERSONAL      -> Request.RequestStatus.PENDINGAPPROVAL;
        };
    }

    private void validateEquipmentAvailability(Equipment equipment, LocalDateTime from, LocalDateTime to) {
        if (equipment.getStatus() != Equipment.EquipmentStatus.AVAILABLE) {
            throw new RuntimeException("Equipment '" + equipment.getName() +
                    "' is not available. Current status: " + equipment.getStatus());
        }
        if (equipment.getRetired() != null && equipment.getRetired()) {
            throw new RuntimeException("Equipment '" + equipment.getName() + "' is retired");
        }

        // Check for overlapping requests
        List<Request> overlapping = requestRepository.findOverlappingRequests(
                equipment.getEquipmentId(), ACTIVE_STATUSES, from, to);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Equipment '" + equipment.getName() +
                    "' has conflicting reservations in the requested time range");
        }
    }

    private List<RequestItem> createRequestItems(Request request, List<RequestItemDTO> itemDTOs) {
        List<RequestItem> items = new ArrayList<>();
        for (RequestItemDTO dto : itemDTOs) {
            Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found: " + dto.getEquipmentId()));

            RequestItem item = RequestItem.builder()
                    .request(request)
                    .equipment(equipment)
                    .quantityRequested(dto.getQuantityRequested())
                    .status(RequestItem.ItemStatus.PENDING)
                    .notes(dto.getNotes())
                    .build();
            items.add(item);
        }
        return requestItemRepository.saveAll(items);
    }

    private boolean isSlaBreached(Request request) {
        if (request.getSubmittedAt() == null || request.getSlaHours() == null) return false;
        LocalDateTime deadline = request.getSubmittedAt().plusHours(request.getSlaHours());
        return LocalDateTime.now().isAfter(deadline)
                && ACTIVE_STATUSES.contains(request.getStatus());
    }

    // ─────────────────────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────────────────────
    public RequestResponseDTO mapToResponse(Request r, List<RequestItem> items,
                                             List<RequestApproval> approvals) {
        // Map items
        List<RequestItemResponseDTO> itemResponses = items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        // Map approvals
        List<RequestApprovalResponseDTO> approvalResponses = approvals.stream()
                .map(this::mapApprovalToResponse)
                .collect(Collectors.toList());

        // Calculate SLA deadline
        LocalDateTime slaDeadline = null;
        if (r.getSubmittedAt() != null && r.getSlaHours() != null) {
            slaDeadline = r.getSubmittedAt().plusHours(r.getSlaHours());
        }

        return RequestResponseDTO.builder()
                .requestId(r.getRequestId())
                .requestType(r.getRequestType())
                .status(r.getStatus())
                // Student
                .studentId(r.getStudent() != null ? r.getStudent().getUserId() : null)
                .studentName(r.getStudent() != null
                        ? r.getStudent().getFirstName() + " " + r.getStudent().getLastName() : null)
                .studentEmail(r.getStudent() != null ? r.getStudent().getEmail() : null)
                // Submitter
                .submitterId(r.getSubmitter() != null ? r.getSubmitter().getUserId() : null)
                .submitterName(r.getSubmitter() != null
                        ? r.getSubmitter().getFirstName() + " " + r.getSubmitter().getLastName() : null)
                // Department
                .departmentId(r.getDepartment() != null ? r.getDepartment().getDepartmentId() : null)
                .departmentName(r.getDepartment() != null ? r.getDepartment().getName() : null)
                .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null)
                // Schedule
                .fromDateTime(r.getFromDateTime())
                .toDateTime(r.getToDateTime())
                // Details
                .description(r.getDescription())
                .courseId(r.getCourse() != null ? r.getCourse().getCourseId() : null)
                .courseName(r.getCourse() != null ? r.getCourse().getCourseName() : null)
                .supervisorId(r.getSupervisor() != null ? r.getSupervisor().getUserId() : null)
                .supervisorName(r.getSupervisor() != null
                        ? r.getSupervisor().getFirstName() + " " + r.getSupervisor().getLastName() : null)
                .instructorId(r.getInstructor() != null ? r.getInstructor().getUserId() : null)
                .instructorName(r.getInstructor() != null
                        ? r.getInstructor().getFirstName() + " " + r.getInstructor().getLastName() : null)
                // Priority & SLA
                .priorityLevel(r.getPriorityLevel())
                .slaHours(r.getSlaHours())
                .slaDeadline(slaDeadline)
                .slaBreached(isSlaBreached(r))
                // Emergency
                .emergency(Boolean.TRUE.equals(r.getEmergency()))
                .emergencyJustification(r.getEmergencyJustification())
                // Extensions
                .extensionCount(r.getExtensionCount())
                .maxExtensions(r.getMaxExtensions())
                // Rejection
                .rejectionReason(r.getRejectionReason())
                // Timestamps
                .submittedAt(r.getSubmittedAt())
                .approvedAt(r.getApprovedAt())
                .returnedAt(r.getReturnedAt())
                .completedAt(r.getCompletedAt())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                // Nested
                .items(itemResponses)
                .approvals(approvalResponses)
                .build();
    }

    private RequestItemResponseDTO mapItemToResponse(RequestItem item) {
        return RequestItemResponseDTO.builder()
                .requestItemId(item.getRequestItemId())
                .equipmentId(item.getEquipment() != null ? item.getEquipment().getEquipmentId() : null)
                .equipmentName(item.getEquipment() != null ? item.getEquipment().getName() : null)
                .equipmentSerialNumber(item.getEquipment() != null ? item.getEquipment().getSerialNumber() : null)
                .quantityRequested(item.getQuantityRequested())
                .quantityApproved(item.getQuantityApproved())
                .quantityIssued(item.getQuantityIssued())
                .quantityReturned(item.getQuantityReturned())
                .status(item.getStatus())
                .notes(item.getNotes())
                .build();
    }

    private RequestApprovalResponseDTO mapApprovalToResponse(RequestApproval approval) {
        // Resolve actor name
        String actorName = null;
        try {
            User actor = userRepository.findById(approval.getActorId()).orElse(null);
            if (actor != null) {
                actorName = actor.getFirstName() + " " + actor.getLastName();
            }
        } catch (Exception e) {
            log.warn("Could not resolve actor name for approval {}", approval.getApprovalId());
        }

        return RequestApprovalResponseDTO.builder()
                .approvalId(approval.getApprovalId())
                .approvalStage(approval.getApprovalStage())
                .actorId(approval.getActorId())
                .actorName(actorName)
                .actorRole(approval.getActorRole())
                .action(approval.getAction())
                .decision(approval.getDecision())
                .reason(approval.getReason())
                .comments(approval.getComments())
                .decidedAt(approval.getDecidedAt())
                .build();
    }
}
