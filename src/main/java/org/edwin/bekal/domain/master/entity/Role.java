package org.edwin.bekal.domain.master.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@Table(name = "MST_ROLES", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class Role extends BaseAuditEntity {
    //name, description, isactive(bit)
    @Column(name = "name", length = 50)
    private String roleName;

    @Nationalized
    @Column(name = "description", length = 255)
    private String roleDescription;

    @Column(name = "is_active")
    private Boolean roleIsActive = true;

}
