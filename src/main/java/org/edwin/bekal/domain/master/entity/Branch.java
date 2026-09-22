package org.edwin.bekal.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.edwin.bekal.enums.BranchStatus;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "MST_BRANCHES", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class Branch extends BaseAuditEntity {
    @Column(name = "code", length = 20)
    private String branchCode;

    @Column(name = "name", length = 100)
    private String branchName;

    @Nationalized
    @Column(name = "address", length = 100)
    private String branchAddress;

    @Column(name = "city", length = 100)
    private String branchCity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BranchStatus branchStatus = BranchStatus.ACTIVE;
}
