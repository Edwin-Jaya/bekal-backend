package org.edwin.bekal.domain.customer.service;

import org.edwin.bekal.domain.customer.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    DocumentResponse createDocument(CreateDocumentRequest request);
    List<DocumentResponse> getAllDocument();
    DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request);
    void deleteDocument(UUID id);
    DocumentResponse uploadDocument(UUID customerId, String documentType, MultipartFile file);
}
