package com.executive_documentation.workings.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageDto<T> {
    // Геттеры и сеттеры
    private List<T> content;
    private PageMetadata metadata;

    // Конструктор
    public PageDto(Page<T> page) {
        this.content = page.getContent();
        this.metadata = new PageMetadata(
                page.getSize(),
                page.getNumber(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Data
    public static class PageMetadata {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;

        // Конструктор, геттеры и сеттеры
        public PageMetadata(int size, int number, long totalElements, int totalPages) {
            this.size = size;
            this.number = number;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
        // Геттеры...
    }
}
