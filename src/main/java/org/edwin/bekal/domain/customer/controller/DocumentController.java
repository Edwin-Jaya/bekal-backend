package org.edwin.bekal.domain.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.config.OpenApiConfig;
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
@Tag(name = "Customer Documents", description = "Endpoints for managing customer verification and KYC documents (KTP, NPWP, KK, salary slips)")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Upload Customer Document",
            description = "Uploads a KYC document file (e.g. KTP, KK, NPWP, Payslip) for a specific customer.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or unsupported format")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @Parameter(description = "Customer UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam("customerId") UUID customerId,
            @Parameter(description = "Document type (e.g. KTP, NPWP, KK, SLIP_GAJI)", example = "KTP")
            @RequestParam("documentType") String documentType,
            @Parameter(description = "Multipart file to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file
    ) {
        DocumentResponse response = documentService.uploadDocument(customerId, documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Uploaded Successfully", response));
    }

    @Operation(
            summary = "Create Document Metadata",
            description = "Stores document metadata manually without file upload.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document metadata created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Created Successfully", response));
    }

    @Operation(
            summary = "Get All Documents",
            description = "Fetches a list of all uploaded customer documents.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents fetched successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocument() {
        List<DocumentResponse> responses = documentService.getAllDocument();
        return ResponseEntity.ok(ApiResponse.success("Document Fetched Successfully", responses));
    }

    @Operation(
            summary = "Update Document",
            description = "Updates document metadata or verification status by document ID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @Parameter(description = "Document UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document Updated Successfully", response));
    }

    @Operation(
            summary = "Delete Document",
            description = "Deletes a customer document by ID.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "Document UUID to delete", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}