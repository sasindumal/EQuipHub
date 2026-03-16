package com.equiphub.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class RequestItemDTO {

    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantityRequested;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
