package com.equiphub.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseUpdateRequestDTO {

    @Size(max = 20)
    private String courseCode;

    @Size(max = 255)
    private String courseName;

    @Min(1)
    @Max(8)
    private Integer semesterOffered;

    @Positive
    private Double credits;

    private Boolean labRequired;
}
