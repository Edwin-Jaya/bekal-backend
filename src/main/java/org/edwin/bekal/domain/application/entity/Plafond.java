package org.edwin.bekal.domain.application.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseEntity;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "TRX_PLAFONDS", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Plafond extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(name = "plafond_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal plafondAmount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "used_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal usedAmount;

    @NotNull
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @NotNull
    @Column(name = "max_tenor_months", nullable = false)
    private Integer maxTenorMonths;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private InternalUser createdBy;


}