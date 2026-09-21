package org.edwin.bekal.domain.application.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CacheablePage<T> implements Serializable {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;

    // ✅ Convert from Spring Page
    public static <T> CacheablePage<T> from(Page<T> page) {
        return new CacheablePage<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    // ✅ Convert back to Spring Page
    public Page<T> toPage(org.springframework.data.domain.Pageable pageable) {
        return new org.springframework.data.domain.PageImpl<>(content, pageable, totalElements);
    }
}
