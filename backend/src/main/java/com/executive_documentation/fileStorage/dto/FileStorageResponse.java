package com.executive_documentation.fileStorage.dto;

public record FileStorageResponse(String fileName, int pageCount) {
    // Можно добавить дополнительные методы, если нужно
    public String getFullInfo() {
        return "File: " + fileName + " (Pages: " + pageCount + ")";
    }
}
