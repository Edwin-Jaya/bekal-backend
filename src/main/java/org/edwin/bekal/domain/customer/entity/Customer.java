package org.edwin.bekal.domain.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.edwin.bekal.enums.BranchStatus;
import org.edwin.bekal.enums.CreditTier;
import org.edwin.bekal.enums.CustomerGender;
import org.edwin.bekal.enums.CustomerStatus;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="MST_CUSTOMERS", schema="dbo")
@EntityListeners(AuditingEntityListener.class)
public class Customer extends BaseFullEntity {

    @Column(name="full_name", length = 150)
    private String customerFullName;

    @Column(name="email", length = 150)
    private String customerEmail;

    @Column(name="password_hash", length = 255)
    private String customerPasswordHash;

    @Column(name="phone_number", length = 20)
    private String customerPhoneNumber;

    @Column(name="nik", length = 20)
    private String customerNik;

    @Column(name="date_of_birth")
    private Date customerDateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name="gender", length=10)
    private CustomerGender customerGender;

    @Nationalized
    @Column(name="address", length = 255)
    private String customerAddress;

    @Enumerated(EnumType.STRING)
    @Column(name="status", length = 20)
    private CustomerStatus customerStatus=CustomerStatus.ACTIVE;

    @Column(name="last_login_at")
    private Instant customerLastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_tier", nullable = false)
    private CreditTier creditTier = CreditTier.TIER_1;

    @Column(name = "successful_loans_count", nullable = false)
    private Integer successfulLoansCount = 0;

}
