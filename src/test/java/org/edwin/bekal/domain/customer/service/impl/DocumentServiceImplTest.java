package org.edwin.bekal.domain.customer.service.impl;

import org.edwin.bekal.common.util.FileStorageService;
import org.edwin.bekal.domain.customer.dto.CreateDocumentRequest;
import org.edwin.bekal.domain.customer.dto.DocumentResponse;
import org.edwin.bekal.domain.customer.dto.UpdateDocumentRequest;
import org.edwin.bekal.domain.customer.entity.Customer;
import org.edwin.bekal.domain.customer.entity.Document;
import org.edwin.bekal.domain.customer.repository.CustomerRepository;
import org.edwin.bekal.domain.customer.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Nested
    @DisplayName("Upload Document Tests")
    class UploadDocumentTests {

        @Test
        @DisplayName("Should upload document successfully")
        void uploadDocument_success() {
            UUID customerId = UUID.randomUUID();
            String documentType = "KTP";
            MultipartFile file = new MockMultipartFile("file", "ktp.jpg", "image/jpeg", "content".getBytes());

            Customer customer = new Customer();
            customer.setId(customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(fileStorageService.store(file)).willReturn("uploads/ktp.jpg");
            given(documentRepository.save(any(Document.class))).willAnswer(inv -> inv.getArgument(0));

            DocumentResponse response = documentService.uploadDocument(customerId, documentType, file);

            assertThat(response).isNotNull();
            assertThat(response.getDocumentType()).isEqualTo("KTP");
        }

        @Test
        @DisplayName("Should throw exception when customer not found during upload")
        void uploadDocument_customerNotFound_throwsException() {
            UUID customerId = UUID.randomUUID();
            String documentType = "KTP";
            MultipartFile file = new MockMultipartFile("file", "ktp.jpg", "image/jpeg", "content".getBytes());

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.uploadDocument(customerId, documentType, file))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Create Document Tests")
    class CreateDocumentTests {

        @Test
        @DisplayName("Should create document successfully")
        void createDocument_success() {
            CreateDocumentRequest request = new CreateDocumentRequest();
            request.setCustomerId(UUID.randomUUID());
            request.setDocumentType("NPWP");

            Customer customer = new Customer();
            customer.setId(request.getCustomerId());

            given(customerRepository.findById(request.getCustomerId())).willReturn(Optional.of(customer));
            given(documentRepository.save(any(Document.class))).willAnswer(inv -> inv.getArgument(0));

            DocumentResponse response = documentService.createDocument(request);

            assertThat(response).isNotNull();
            assertThat(response.getDocumentType()).isEqualTo("NPWP");
        }
    }

    @Nested
    @DisplayName("Update Document Tests")
    class UpdateDocumentTests {

        @Test
        @DisplayName("Should update document successfully")
        void updateDocument_success() {
            UUID documentId = UUID.randomUUID();
            UpdateDocumentRequest request = new UpdateDocumentRequest();
            request.setStatus("VERIFIED");

            Document document = new Document();
            document.setId(documentId);
            document.setStatus("PENDING");

            given(documentRepository.findById(documentId)).willReturn(Optional.of(document));
            given(documentRepository.save(any(Document.class))).willAnswer(inv -> inv.getArgument(0));

            DocumentResponse response = documentService.updateDocument(documentId, request);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("VERIFIED");
        }

        @Test
        @DisplayName("Should throw exception when document not found during update")
        void updateDocument_notFound_throwsException() {
            UUID documentId = UUID.randomUUID();
            UpdateDocumentRequest request = new UpdateDocumentRequest();

            given(documentRepository.findById(documentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.updateDocument(documentId, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Delete & Get Document Tests")
    class DeleteAndGetDocumentTests {

        @Test
        @DisplayName("Should delete document successfully")
        void deleteDocument_success() {
            UUID documentId = UUID.randomUUID();
            Document document = new Document();
            document.setId(documentId);

            given(documentRepository.findById(documentId)).willReturn(Optional.of(document));

            documentService.deleteDocument(documentId);

            // Verifikasi bahwa findById dipanggil sebagai bagian dari proses delete
            verify(documentRepository).findById(documentId);
        }

        @Test
        @DisplayName("Should throw exception when document not found during delete")
        void deleteDocument_notFound_throwsException() {
            UUID documentId = UUID.randomUUID();

            given(documentRepository.findById(documentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> documentService.deleteDocument(documentId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should get all documents successfully")
        void getAllDocument_success() {
            Document doc = new Document();
            doc.setDocumentType("SIM");

            given(documentRepository.findAll()).willReturn(List.of(doc));

            List<DocumentResponse> result = documentService.getAllDocument();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDocumentType()).isEqualTo("SIM");
        }
    }
}