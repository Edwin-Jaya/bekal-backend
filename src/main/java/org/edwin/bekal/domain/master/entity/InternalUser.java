package org.edwin.bekal.domain.master.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="MST_INTERNAL_USERS", schema="dbo")
@EntityListeners(AuditingEntityListener.class)
public class InternalUser extends BaseFullEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name="employee_code", length = 30)
    private String internalUserEmployeeCode;

    @Column(name="full_name", length = 150)
    private String internalUserFullName;

    @Column(name="email", length = 150)
    private String internalUserEmail;

    @Column(name="password_hash", length = 255)
    private String internalUserPasswordHash;

    @Column(name="phone_number", length = 20)
    private String internalUserPhoneNumber;

    @Column(name="is_active")
    private Boolean internalUserIsActive=true;

    @Column(name="last_login_at")
    private Instant internalUserLastLoginAt;

}
