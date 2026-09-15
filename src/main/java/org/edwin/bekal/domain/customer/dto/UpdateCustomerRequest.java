package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class UpdateCustomerRequest {
    private String customerFullName;

    private String customerEmail;

    private String customerPhoneNumber;

    private String customerNik;

    private Date customerDateOfBirth;

    private String customerGender;

    private String customerAddress;

    private String cutomerPhoneNumber;
}
