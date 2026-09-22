package org.edwin.bekal.domain.customer.service.impl;

import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.util.FileStorageService;
import org.edwin.bekal.domain.customer.dto.CreateDocumentRequest;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateDocumentRequest;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.edwin.bekal.domain.customer.service.DocumentService;
import org.edwin.bekal.domain.master.repository.InternalUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final CustomerRepository customerRepository;
    private final DocumentRepository documentRepository;
    private final InternalUserRepository internalUserRepository;
    private final FileStorageService fileStorageService;

    @Override
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId()));

        Document document = new Document();
        document.setCustomer(customer);
        document.setDocumentType(request.getDocumentType());
        document.setFileUrl(request.getFileUrl() != null ? request.getFileUrl() : "");
        document.setIsLatest(true);

        Document saved = documentRepository.save(document);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocument() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentResponse uploadDocument(UUID customerId, String documentType, MultipartFile file) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));

        // Simpan file fisik — sesuaikan dengan strategi storage lo (local disk, S3, dsb)
        String storedFileUrl = fileStorageService.store(file); // lo perlu punya service ini

        Document document = new Document();
        document.setCustomer(customer);
        document.setDocumentType(documentType);
        document.setFileUrl(storedFileUrl);
        document.setIsLatest(true);

        Document saved = documentRepository.save(document);
        return mapToResponse(saved);
    }

    @Override
    public DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        if (request.getDocumentType() != null) document.setDocumentType(request.getDocumentType());
        if (request.getFileUrl() != null) document.setFileUrl(request.getFileUrl());
        if (request.getIsLatest() != null) document.setIsLatest(request.getIsLatest());

        Document updated = documentRepository.save(document);
        return mapToResponse(updated);
    }

    @Override
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));

        document.setIsLatest(false);
        documentRepository.save(document);
    }

    public DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .customerId(document.getCustomer() != null ? document.getCustomer().getId() : null)
                .documentType(document.getDocumentType())
                .fileUrl(document.getFileUrl())
                .isLatest(document.getIsLatest())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}