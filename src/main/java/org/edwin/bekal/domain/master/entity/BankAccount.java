package org.edwin.bekal.domain.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mst_customer_bank_accounts", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class BankAccount extends BaseFullEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Size(max = 30)
    @NotNull
    @Column(name = "bank_account_number", nullable = false, length = 30)
    private String bankAccountNumber;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "bank_account_holder", nullable = false, length = 100)
    private String bankAccountHolder;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @ColumnDefault("'active'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;


}