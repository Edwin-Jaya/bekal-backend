package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edwin.bekal.common.util.FileStorageService;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.repository.EmploymentRepository;
import org.edwin.bekal.domain.customer.service.CustomerRollbackService;
import org.edwin.bekal.enums.CustomerStatus; // Import enum CustomerStatus
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerRollbackServiceImpl implements CustomerRollbackService {

    private final CustomerRepository customerRepository;
    private final DocumentRepository documentRepository;
    private final EmploymentRepository employmentRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public void rollbackIncompleteRegistration(UUID customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            // Sudah tidak ada / sudah pernah di-rollback — anggap sukses (idempotent).
            return;
        }

        // Cek jika status customer sudah VERIFIED / ACTIVE (batalkan rollback)
        if (CustomerStatus.ACTIVE.equals(customer.getCustomerStatus())) {
            log.info("Rollback dibatalkan karena customerId={} sudah terverifikasi (status={})",
                    customerId, customer.getCustomerStatus());
            return;
        }

        List<Document> documents = documentRepository.findByCustomerIdAndIsLatestTrue(customerId);
        for (Document doc : documents) {
            try {
                fileStorageService.deleteFile(doc.getFileUrl());
            } catch (Exception e) {
                log.warn("Gagal hapus file fisik saat rollback: {}", doc.getFileUrl(), e);
            }
        }
        documentRepository.deleteAll(documents);

        employmentRepository.findByCustomer_Id(customerId)
                .ifPresent(employmentRepository::delete);

        customerRepository.delete(customer);

        log.info("Rollback registrasi selesai untuk customerId={}", customerId);
    }
}