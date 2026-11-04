
package com.kaisha.payroll.workjournal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkJournalResponse {

    private Long id;

    private String employeeId;

    private String companyId;

    private LocalDate workDate;

    private String workDescription;

    private boolean highlight;

    private String highlightCategory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
