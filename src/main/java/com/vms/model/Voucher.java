package com.vms.model;

import java.time.LocalDate;

public class Voucher {
    private int id;
    private String code;
    private int demandeId;
    private String demandeReference;
    private int clientId;
    private String clientNom;
    private int magasinId;
    private String magasinNom;
    private double valeur;
    private String statut; // GENERE, EMIS, UTILISE, EXPIRE, ANNULE
    private LocalDate dateEmission;
    private LocalDate dateExpiration;
    private LocalDate dateUtilisation;
    private String qrCode;
    private String remarques;

    // Constructeurs
    public Voucher() {
        this.statut = "GENERE";
    }

    public Voucher(String code, double valeur) {
        this();
        this.code = code;
        this.valeur = valeur;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(int demandeId) {
        this.demandeId = demandeId;
    }

    public String getDemandeReference() {
        return demandeReference;
    }

    public void setDemandeReference(String demandeReference) {
        this.demandeReference = demandeReference;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public int getMagasinId() {
        return magasinId;
    }

    public void setMagasinId(int magasinId) {
        this.magasinId = magasinId;
    }

    public String getMagasinNom() {
        return magasinNom;
    }

    public void setMagasinNom(String magasinNom) {
        this.magasinNom = magasinNom;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public LocalDate getDateUtilisation() {
        return dateUtilisation;
    }

    public void setDateUtilisation(LocalDate dateUtilisation) {
        this.dateUtilisation = dateUtilisation;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    public boolean isExpire() {
        if (dateExpiration == null) return false;
        return LocalDate.now().isAfter(dateExpiration);
    }

    public boolean isUtilisable() {
        return "EMIS".equals(statut) && !isExpire();
    }

    @Override
    public String toString() {
        return code + " - " + valeur + " Rs";
    }
}