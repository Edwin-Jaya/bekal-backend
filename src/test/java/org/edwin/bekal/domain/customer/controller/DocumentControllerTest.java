package org.edwin.bekal.domain.customer.controller;

import org.edwin.bekal.common.dto.ApiResponse;
import org.edwin.bekal.domain.customer.dto.CreateDocumentRequest;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateDocumentRequest;
import org.edwin.bekal.domain.customer.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    @Nested
    @DisplayName("uploadDocument Tests")
    class UploadDocumentTests {

        @Test
        @DisplayName("Should upload document successfully")
        void uploadDocument_success() {
            UUID customerId = UUID.randomUUID();
            String documentType = "KTP";
            MultipartFile file = new MockMultipartFile("file", "ktp.jpg", "image/jpeg", "content".getBytes());

            DocumentResponse responseDto = DocumentResponse.builder()
                    .customerId(customerId)
                    .documentType("KTP")
                    .fileUrl("uploads/ktp.jpg")
                    .build();

            given(documentService.uploadDocument(customerId, documentType, file)).willReturn(responseDto);

            ResponseEntity<ApiResponse<DocumentResponse>> response = documentController.uploadDocument(customerId, documentType, file);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getFileUrl()).isEqualTo("uploads/ktp.jpg");
        }
    }

    @Nested
    @DisplayName("CRUD Document Tests")
    class CrudDocumentTests {

        @Test
        @DisplayName("createDocument - Should return CREATED status and DocumentResponse")
        void createDocument_success() {
            CreateDocumentRequest request = new CreateDocumentRequest();
            DocumentResponse responseDto = DocumentResponse.builder().documentType("NPWP").build();

            given(documentService.createDocument(request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<DocumentResponse>> response = documentController.createDocument(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getDocumentType()).isEqualTo("NPWP");
        }

        @Test
        @DisplayName("getAllDocument - Should return list of documents")
        void getAllDocument_success() {
            List<DocumentResponse> responseList = List.of(
                    DocumentResponse.builder().documentType("KTP").build(),
                    DocumentResponse.builder().documentType("SIM").build()
            );

            given(documentService.getAllDocument()).willReturn(responseList);

            ResponseEntity<ApiResponse<List<DocumentResponse>>> response = documentController.getAllDocument();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData()).hasSize(2);
        }

        @Test
        @DisplayName("updateDocument - Should update document and return CREATED status")
        void updateDocument_success() {
            UUID id = UUID.randomUUID();
            UpdateDocumentRequest request = new UpdateDocumentRequest();
            DocumentResponse responseDto = DocumentResponse.builder().id(id).status("VERIFIED").build();

            given(documentService.updateDocument(id, request)).willReturn(responseDto);

            ResponseEntity<ApiResponse<DocumentResponse>> response = documentController.updateDocument(id, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getData().getStatus()).isEqualTo("VERIFIED");
        }

        @Test
        @DisplayName("deleteDocument - Should delete document and return NO_CONTENT")
        void deleteDocument_success() {
            UUID id = UUID.randomUUID();

            ResponseEntity<ApiResponse<Void>> response = documentController.deleteDocument(id);

            verify(documentService).deleteDocument(id);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}