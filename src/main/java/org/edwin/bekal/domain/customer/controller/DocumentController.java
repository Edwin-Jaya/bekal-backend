package org.edwin.bekal.domain.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.customer.dto.CreateDocumentRequest;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateDocumentRequest;
import org.edwin.bekal.domain.customer.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("customerId") UUID customerId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file
    ) {
        DocumentResponse response = documentService.uploadDocument(customerId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Uploaded Successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Created Successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocument() {
        List<DocumentResponse> responses = documentService.getAllDocument();
        return ResponseEntity.ok(ApiResponse.success("Document Fetched Successfully", responses));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Updated Successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}