package org.edwin.bekal.domain.customer.service.impl;

import org.edwin.bekal.common.util.FileStorageService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.entity.Employment;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRollbackServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EmploymentRepository employmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private CustomerRollbackServiceImpl customerRollbackService;

    @Nested
    @DisplayName("rollbackIncompleteRegistration Tests")
    class RollbackIncompleteRegistrationTests {

        @Test
        @DisplayName("Should do nothing when customer is not found (Idempotent)")
        void rollback_customerNotFound_doesNothing() {
            UUID customerId = UUID.randomUUID();
            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            customerRollbackService.rollbackIncompleteRegistration(customerId);

            verify(documentRepository, never()).findByCustomerIdAndIsLatestTrue(any());
            verify(customerRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should cancel rollback if customer email is already verified")
        void rollback_customerVerified_cancelsRollback() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setCustomerEmailVerifiedAt(Instant.now());

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            customerRollbackService.rollbackIncompleteRegistration(customerId);

            verify(documentRepository, never()).findByCustomerIdAndIsLatestTrue(any());
            verify(customerRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should delete documents, physical files, employment, and customer when registration is incomplete")
        void rollback_success() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();
            customer.setCustomerEmailVerifiedAt(null);

            Document doc1 = new Document();
            doc1.setFileUrl("file1.png");
            Document doc2 = new Document();
            doc2.setFileUrl("file2.png");
            List<Document> documents = List.of(doc1, doc2);

            Employment employment = new Employment();

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(documentRepository.findByCustomerIdAndIsLatestTrue(customerId)).willReturn(documents);
            given(employmentRepository.findByCustomer_Id(customerId)).willReturn(Optional.of(employment));

            customerRollbackService.rollbackIncompleteRegistration(customerId);

            verify(fileStorageService).deleteFile("file1.png");
            verify(fileStorageService).deleteFile("file2.png");
            verify(documentRepository).deleteAll(documents);
            verify(employmentRepository).delete(employment);
            verify(customerRepository).delete(customer);
        }

        @Test
        @DisplayName("Should proceed with DB rollback even if deleting physical file throws exception")
        void rollback_fileDeletionFails_continuesRollback() {
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer();

            Document doc = new Document();
            doc.setFileUrl("file1.png");

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(documentRepository.findByCustomerIdAndIsLatestTrue(customerId)).willReturn(List.of(doc));
            doThrow(new RuntimeException("File system error")).when(fileStorageService).deleteFile("file1.png");
            given(employmentRepository.findByCustomer_Id(customerId)).willReturn(Optional.empty());

            customerRollbackService.rollbackIncompleteRegistration(customerId);

            verify(documentRepository).deleteAll(List.of(doc));
            verify(customerRepository).delete(customer);
        }
    }
}