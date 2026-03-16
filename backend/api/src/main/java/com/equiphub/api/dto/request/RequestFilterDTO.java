package com.equiphub.api.dto.request;

import com.equiphub.api.model.Request;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequestFilterDTO {

    private Request.RequestStatus status;
    private Request.RequestType requestType;
    private UUID departmentId;
    private UUID studentId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Boolean isEmergency;
    private Integer priorityLevel;
}
