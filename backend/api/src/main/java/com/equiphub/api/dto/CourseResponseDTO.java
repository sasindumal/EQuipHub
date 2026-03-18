package com.equiphub.api.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {

    private String courseId;
    private String courseCode;
    private String courseName;
    private UUID departmentId;
    private String departmentName;
    private String departmentCode;
    private Integer semesterOffered;
    private Double credits;
    private Boolean labRequired;
}
