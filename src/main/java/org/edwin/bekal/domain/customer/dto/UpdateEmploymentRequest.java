package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class UpdateEmploymentRequest {
    private String customerEmploymentType;
    private String customerCompanyName;
    private String customerJobTitle;
    private String customerIndustry;
    private BigDecimal customerDeclaredIncome;
    private BigDecimal customerOtherIncome;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date customerEmploymentStartDate;

    // Verification Fields
    private BigDecimal customerVerifiedIncome;
    private UUID incomeVerifiedById;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date customerIncomeVerifiedAt;
}