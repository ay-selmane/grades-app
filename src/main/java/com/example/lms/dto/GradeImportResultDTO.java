package com.example.lms.dto;

import java.util.ArrayList;
import java.util.List;

public class GradeImportResultDTO {
    private int totalRows;
    private int successCount;
    private int errorCount;
    private int skippedCount;
    private List<GradeImportRowDTO> successfulImports;
    private List<GradeImportRowDTO> errors;
    private List<GradeImportRowDTO> skipped;

    public GradeImportResultDTO() {
        this.successfulImports = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.skipped = new ArrayList<>();
    }

    // Getters and Setters
    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<GradeImportRowDTO> getSuccessfulImports() {
        return successfulImports;
    }

    public void setSuccessfulImports(List<GradeImportRowDTO> successfulImports) {
        this.successfulImports = successfulImports;
    }

    public List<GradeImportRowDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<GradeImportRowDTO> errors) {
        this.errors = errors;
    }

    public List<GradeImportRowDTO> getSkipped() {
        return skipped;
    }

    public void setSkipped(List<GradeImportRowDTO> skipped) {
        this.skipped = skipped;
    }
}
