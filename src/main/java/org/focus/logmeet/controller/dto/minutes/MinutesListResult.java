package org.focus.logmeet.controller.dto.minutes;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.focus.logmeet.domain.enums.MinutesType;
import org.focus.logmeet.domain.enums.ProjectColor;
import org.focus.logmeet.domain.enums.Status;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MinutesListResult {
    private Long minutesId;
    private Long projectId;
    private String minutesName;
    private ProjectColor color;
    private MinutesType type;
    private Status status;
    private LocalDateTime createdAt;
}