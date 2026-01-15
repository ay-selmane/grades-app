package com.example.lms.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDTO {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<String> errors;
    private List<String> messages;
    private List<ImportRowDTO> validData;
    private boolean success;

    public ImportResultDTO() {
        this.errors = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.validData = new ArrayList<>();
    }

    // Getters and Setters
    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
    
    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    
    public void addMessage(String message) {
        this.messages.add(message);
    }

    public List<ImportRowDTO> getValidData() {
        return validData;
    }

    public void setValidData(List<ImportRowDTO> validData) {
        this.validData = validData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
