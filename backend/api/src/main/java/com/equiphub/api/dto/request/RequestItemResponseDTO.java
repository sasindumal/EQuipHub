package com.equiphub.api.dto.request;

import com.equiphub.api.model.RequestItem;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RequestItemResponseDTO {

    private Integer requestItemId;
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentSerialNumber;
    private Integer quantityRequested;
    private Integer quantityApproved;
    private Integer quantityIssued;
    private Integer quantityReturned;
    private RequestItem.ItemStatus status;
    private String notes;
}
