package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.edwin.bekal.domain.application.entity.LoanApplication;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link org.edwin.bekal.domain.application.entity.LoanReview}
 */
@Data
@Builder
public class LoanReviewResponse implements Serializable {
    @NotNull
    private UUID id;
    @NotNull
    LoanApplication loanApplicationId;
    @NotNull
    InternalUser reviewedBy;
    @NotNull
    @Size(max = 20)
    String result;
    String notes;
    @NotNull
    Instant reviewedAt;
}