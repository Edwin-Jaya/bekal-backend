package org.edwin.bekal.domain.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.edwin.bekal.domain.master.entity.InternalUser;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="mst_customer_employments", schema="dbo")
@EntityListeners(AuditingEntityListener.class)
public class Employment extends BaseFullEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Nationalized
    @Column(name="employment_type", length = 30)
    private String customerEmploymentType;

    @Nationalized
    @Column(name="company_name", length = 150)
    private String customerCompanyName;

    @Nationalized
    @Column(name="job_title", length = 100)
    private String customerJobTitle;

    @Nationalized
    @Column(name="industry", length = 100)
    private String customerIndustry;

    @Column(name="declared_income", precision = 15, scale = 2)
    private BigDecimal customerDeclaredIncome;

    @Column(name="verified_income", precision = 15, scale = 2)
    private BigDecimal customerVerifiedIncome;

    @Column(name="other_income", precision = 15, scale = 2)
    private BigDecimal customerOtherIncome;

    // Perubahan: nullable diubah menjadi true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_verified_by", nullable = true)
    private InternalUser internalUser;

    @Column(name="income_verified_at")
    private Instant customerIncomeVerifiedAt;

    @Column(name="employment_start_date")
    private Date customerEmploymentStartDate;

    @Column(name="is_current")
    private Boolean customerIsCurrent;

}