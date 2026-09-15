package org.edwin.bekal.domain.customer.dto;

import lombok.Data;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
public class CreateEmploymentRequest {
    private UUID customer;
    private String customerEmploymentType;
    private String customerCompanyName;
    private String customerJobTitle;
    private String customerIndustry;
    private BigDecimal customerDeclaredIncome;
    private BigDecimal customerVerifiedIncome;
    private BigDecimal customerOtherIncome;
    private Date customerEmploymentStartDate;
    private Boolean customerIsCurrent;
}
