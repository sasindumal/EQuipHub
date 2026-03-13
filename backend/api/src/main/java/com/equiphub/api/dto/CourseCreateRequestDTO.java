package com.equiphub.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateRequestDTO {

    @NotBlank
    @Size(max = 20)
    private String courseId;

    @NotBlank
    @Size(max = 20)
    private String courseCode;

    @NotBlank
    @Size(max = 255)
    private String courseName;

    @NotBlank
    private String departmentId;

    @NotNull
    @Min(1)
    @Max(8)
    private Integer semesterOffered;

    @NotNull
    @Positive
    private Double credits;

    private Boolean labRequired = false;
}
