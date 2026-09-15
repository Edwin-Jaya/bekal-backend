package org.edwin.bekal.domain.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "MST_MENUS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class Menu extends BaseAuditEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu menuParent;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String menuName;

    @Size(max = 150)
    @Column(name = "path", length = 150)
    private String menuPath;

    @Size(max = 50)
    @Column(name = "icon", length = 50)
    private String menuIcon;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "sort_order", nullable = false)
    private Integer menuSortOrder;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "is_active", nullable = false)
    private Boolean menuIsActive;


}