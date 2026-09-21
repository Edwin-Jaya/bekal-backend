package org.edwin.bekal.domain.customer.service.impl;

import org.edwin.bekal.domain.customer.dto.CreateEmploymentRequest;
import org.edwin.bekal.domain.customer.dto.EmploymentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateEmploymentRequest;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EmploymentServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmploymentServiceImpl employmentService;

    @Nested
    @DisplayName("createEmployment Tests")
    class CreateEmploymentTests {

        @Test
        @DisplayName("Should create employment successfully when customer exists")
        void createEmployment_success() {
            UUID customerId = UUID.randomUUID();
            CreateEmploymentRequest request = new CreateEmploymentRequest();
            request.setCustomer(customerId);
            request.setCustomerCompanyName("PT Bekal Mandiri");
            request.setCustomerJobTitle("Software Engineer");

            Customer customer = new Customer();
            customer.setId(customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(employmentRepository.saveAndFlush(any(Employment.class))).willAnswer(inv -> inv.getArgument(0));

            EmploymentResponse response = employmentService.createEmployment(request);

            assertThat(response).isNotNull();
            assertThat(response.getCustomerCompanyName()).isEqualTo("PT Bekal Mandiri");
            assertThat(response.getCustomerJobTitle()).isEqualTo("Software Engineer");
            assertThat(response.getCustomerIsCurrent()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when customer is not found")
        void createEmployment_customerNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            CreateEmploymentRequest request = new CreateEmploymentRequest();
            request.setCustomer(customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> employmentService.createEmployment(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Customer not found with ID: " + customerId);
        }
    }

    @Nested
    @DisplayName("updateEmployment Tests")
    class UpdateEmploymentTests {

        @Test
        @DisplayName("Should update employment successfully")
        void updateEmployment_success() {
            UUID employmentId = UUID.randomUUID();
            UpdateEmploymentRequest request = new UpdateEmploymentRequest();
            request.setCustomerCompanyName("PT Updated Company");
            request.setCustomerJobTitle("Senior Engineer");

            Employment employment = new Employment();
            employment.setId(employmentId);
            employment.setCustomerCompanyName("PT Old Company");

            given(employmentRepository.findById(employmentId)).willReturn(Optional.of(employment));
            given(employmentRepository.saveAndFlush(any(Employment.class))).willAnswer(inv -> inv.getArgument(0));

            EmploymentResponse response = employmentService.updateEmployment(employmentId, request);

            assertThat(response).isNotNull();
            assertThat(response.getCustomerCompanyName()).isEqualTo("PT Updated Company");
            assertThat(response.getCustomerJobTitle()).isEqualTo("Senior Engineer");
        }

        @Test
        @DisplayName("Should throw exception when employment is not found for update")
        void updateEmployment_notFound_throwsException() {
            UUID employmentId = UUID.randomUUID();
            UpdateEmploymentRequest request = new UpdateEmploymentRequest();

            given(employmentRepository.findById(employmentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> employmentService.updateEmployment(employmentId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Employment not found with ID" + employmentId);
        }
    }

    @Nested
    @DisplayName("deleteEmployment Tests")
    class DeleteEmploymentTests {

        @Test
        @DisplayName("Should soft delete employment by setting customerIsCurrent to false")
        void deleteEmployment_success() {
            UUID employmentId = UUID.randomUUID();
            Employment employment = new Employment();
            employment.setId(employmentId);
            employment.setCustomerIsCurrent(true);

            given(employmentRepository.findById(employmentId)).willReturn(Optional.of(employment));

            employmentService.deleteEmployment(employmentId);

            assertThat(employment.getCustomerIsCurrent()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when employment is not found for deletion")
        void deleteEmployment_notFound_throwsException() {
            UUID employmentId = UUID.randomUUID();

            given(employmentRepository.findById(employmentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> employmentService.deleteEmployment(employmentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Employment not found with ID" + employmentId);
        }
    }

    @Nested
    @DisplayName("getAllEmployment Tests")
    class GetAllEmploymentTests {

        @Test
        @DisplayName("Should return list of all employment records")
        void getAllEmployment_success() {
            Employment emp1 = new Employment();
            emp1.setCustomerCompanyName("Company A");
            Employment emp2 = new Employment();
            emp2.setCustomerCompanyName("Company B");

            given(employmentRepository.findAll()).willReturn(List.of(emp1, emp2));

            List<EmploymentResponse> result = employmentService.getAllEmployment();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCustomerCompanyName()).isEqualTo("Company A");
            assertThat(result.get(1).getCustomerCompanyName()).isEqualTo("Company B");
        }
    }
}