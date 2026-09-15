package org.edwin.bekal.domain.customer.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CreateCustomerRequest {
    private String customerFullName;
    private String customerEmail;
    private String customerPasswordHash;
    private String customerPhoneNumber;
    private String customerNik;
    private Date customerDateOfBirth;
    private String customerGender;
    private String customerAddress;
}
