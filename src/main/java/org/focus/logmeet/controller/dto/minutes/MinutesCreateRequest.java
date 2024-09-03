package org.focus.logmeet.controller.dto.minutes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinutesCreateRequest {
    private String base64FileData;
    private String minutesName;
    private String fileName;
    private Long projectId;
    private String textContent; // textContent는 직접 작성한 회의록 업로드에 사용됨.
}