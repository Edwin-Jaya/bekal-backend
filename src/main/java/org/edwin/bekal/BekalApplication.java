package org.edwin.bekal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "org.edwin.bekal")
@EnableJpaAuditing
public class BekalApplication {

    public static void main(String[] args) {
        SpringApplication.run(BekalApplication.class, args);
    }

}
