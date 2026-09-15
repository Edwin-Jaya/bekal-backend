package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.edwin.bekal.domain.master.entity.Menu;
import org.edwin.bekal.domain.master.entity.Role;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "REL_ROLE_MENU_ACCESS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class RoleMenuAccess extends BaseAuditEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "can_view", nullable = false)
    private Boolean roleMenuCanView = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "can_create", nullable = false)
    private Boolean roleMenuCanCreate = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "can_edit", nullable = false)
    private Boolean roleMenuCanEdit = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "can_delete", nullable = false)
    private Boolean roleMenuCanDelete = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "can_approve", nullable = false)
    private Boolean roleMenuCanApprove = false;

}