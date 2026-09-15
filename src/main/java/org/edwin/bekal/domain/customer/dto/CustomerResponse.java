package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class CustomerResponse {
    private UUID id;

    private String customerFullName;

    private String customerEmail;

    private String customerPasswordHash;

    private String customerPhoneNumber;

    private String customerNik;

    private Date customerDateOfBirth;

    private String customerGender;

    private String customerAddress;

    private String customerStatus;

    private Instant customerEmailVerifiedAt;

    private String customerDeviceToken;

    private Instant customerLastLoginAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Instant updatedAt;

}
