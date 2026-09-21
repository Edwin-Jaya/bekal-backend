package org.edwin.bekal.domain.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FileController fileController;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileController, "uploadDir", tempDir.toString());
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    @DisplayName("GET /api/v1/files/{filename} - Should return 200 OK and PDF content type")
    void serveFile_Pdf_Success() throws Exception {
        String filename = "sample.pdf";
        Files.createFile(tempDir.resolve(filename));

        mockMvc.perform(get("/api/v1/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\""));
    }

    @Test
    @DisplayName("GET /api/v1/files/{filename} - Should return 200 OK and JPEG content type")
    void serveFile_Jpg_Success() throws Exception {
        String filename = "image.jpg";
        Files.createFile(tempDir.resolve(filename));

        mockMvc.perform(get("/api/v1/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    @DisplayName("GET /api/v1/files/{filename} - Should return 200 OK and PNG content type")
    void serveFile_Png_Success() throws Exception {
        String filename = "image.png";
        Files.createFile(tempDir.resolve(filename));

        mockMvc.perform(get("/api/v1/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));
    }

    @Test
    @DisplayName("GET /api/v1/files/{filename} - Should return fallback content type for unknown extensions")
    void serveFile_UnknownExtension_FallbackOctetStream() throws Exception {
        String filename = "document.docx";
        Files.createFile(tempDir.resolve(filename));

        mockMvc.perform(get("/api/v1/files/{filename}", filename))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));
    }

    @Test
    @DisplayName("GET /api/v1/files/{filename} - Should return 404 Not Found when file does not exist")
    void serveFile_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/files/nonexistent.pdf"))
                .andExpect(status().isNotFound());
    }
}