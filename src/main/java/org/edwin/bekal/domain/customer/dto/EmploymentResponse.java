package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class EmploymentResponse {
    private UUID id;
    private Customer customer;
    private String customerEmploymentType;
    private String customerCompanyName;
    private String customerJobTitle;
    private String customerIndustry;
    private BigDecimal customerDeclaredIncome;
    private BigDecimal customerVerifiedIncome;
    private BigDecimal customerOtherIncome;
    private InternalUser internalUser;
    private Instant customerIncomeVerifiedAt;
    private Date customerEmploymentStartDate;
    private Boolean customerIsCurrent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;


}
