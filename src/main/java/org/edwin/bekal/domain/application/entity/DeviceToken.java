package org.edwin.bekal.domain.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.edwin.bekal.common.base.BaseAuditEntity;
import org.edwin.bekal.common.base.BaseFullEntity;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "TRX_DEVICE_TOKENS", schema = "dbo")
@EntityListeners(AuditingEntityListener.class)
public class DeviceToken extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 255)
    private String fcmToken;

    @Column(name = "device_info", length = 100)
    private String deviceInfo;

}