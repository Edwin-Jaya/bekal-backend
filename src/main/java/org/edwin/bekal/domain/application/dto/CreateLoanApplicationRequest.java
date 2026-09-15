package org.edwin.bekal.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.edwin.bekal.domain.application.entity.Plafond;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.Branch;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreateLoanApplicationRequest implements Serializable {
    @NotNull
    Customer customer;
    @NotNull
    Branch branch;
    @NotNull
    Plafond plafond;
    @NotNull
    BigDecimal amountRequested;
    @NotNull
    Integer tenorMonths;
    @Size(max = 150)
    String purpose;
}