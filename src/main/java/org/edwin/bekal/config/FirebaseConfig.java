package org.edwin.bekal.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration // 👈 Ubah dari @Component ke @Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        // 👈 Gunakan try-with-resources agar InputStream otomatis ditutup
        try (InputStream serviceAccount = new ClassPathResource("firebase/firebase-credentials.json").getInputStream()) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setHttpTransport(new NetHttpTransport()) // 👈 Tambahkan ini untuk cegah error GZIP/Transport
                    .setConnectTimeout(30000)
                    .setReadTimeout(30000)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Gagal inisialisasi Firebase Admin SDK", e);
        }
    }
}