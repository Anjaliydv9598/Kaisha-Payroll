
package com.kaisha.payroll.workjournal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class WorkJournalRequest {

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    @NotBlank(message = "Work description is required")
    @Size(
            max = 5000,
            message = "Work description cannot exceed 5000 characters"
    )
    private String workDescription;

    private boolean highlight = false;

    @Size(
            max = 100,
            message = "Highlight category cannot exceed 100 characters"
    )
    private String highlightCategory;
}
