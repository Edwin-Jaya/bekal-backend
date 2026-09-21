package org.edwin.bekal.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file";
            String fileName = UUID.randomUUID() + "_" + originalFileName;

            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Hapus file fisik berdasarkan URL yang dikembalikan oleh store()
     * (format: "/uploads/{namafile}"). Dipakai saat rollback registrasi gagal.
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.replaceFirst("^/?uploads/", "");
            Path targetPath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileUrl, e);
        }
    }
}