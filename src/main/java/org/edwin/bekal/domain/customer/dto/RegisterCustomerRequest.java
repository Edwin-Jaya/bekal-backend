package org.edwin.bekal.domain.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegisterCustomerRequest {
    // Personal Details
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String nik;

    @NotNull(message = "Tanggal lahir wajib diisi")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth; // Spring Boot otomatis mengurai format YYYY-MM-DD
    private String gender;        // "MALE" / "FEMALE"
    private String address;

    // Employment Details (Optional)
    private String employmentType;
    private String companyName;
    private String jobTitle;
    private String industry;
    private BigDecimal declaredIncome;
    private BigDecimal otherIncome;

    @NotNull(message = "Tanggal lahir wajib diisi")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate employmentStartDate;
}