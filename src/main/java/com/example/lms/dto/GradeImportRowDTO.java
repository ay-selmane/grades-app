package com.example.lms.dto;

public class GradeImportRowDTO {
    private int rowNumber;
    private String matricule;
    private String nom;
    private String prenom;
    private Double note;
    private String section;
    private String groupe;
    private String status; // "success", "error", "skipped"
    private String message;

    public GradeImportRowDTO() {
    }

    public GradeImportRowDTO(int rowNumber, String matricule, String nom, String prenom, 
                            Double note, String section, String groupe, String status, String message) {
        this.rowNumber = rowNumber;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.note = note;
        this.section = section;
        this.groupe = groupe;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
