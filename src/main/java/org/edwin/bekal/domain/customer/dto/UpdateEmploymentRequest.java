package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.InternalUser;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
public class UpdateEmploymentRequest {
    private String customerEmploymentType;
    private String customerCompanyName;
    private String customerJobTitle;
    private String customerIndustry;
    private BigDecimal customerDeclaredIncome;
    private BigDecimal customerOtherIncome;
    private Date customerEmploymentStartDate;
}
