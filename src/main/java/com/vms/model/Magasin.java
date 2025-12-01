package com.vms.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Magasin {
    private int id;
    private String code;
    private String nom;
    private String adresse;
    private String ville;
    private String telephone;
    private String email;
    private String responsable;
    private String typeMagasin; // Supermarché, Boutique, Restaurant, etc.
    private LocalTime heureOuverture;
    private LocalTime heureFermeture;
    private boolean actif;
    private LocalDate dateOuverture;
    private int nombreRedemptions;
    private double montantTotalRedemptions;

    // Constructeurs
    public Magasin() {
        this.actif = true;
        this.nombreRedemptions = 0;
        this.montantTotalRedemptions = 0.0;
    }

    public Magasin(String code, String nom, String ville) {
        this();
        this.code = code;
        this.nom = nom;
        this.ville = ville;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getTypeMagasin() {
        return typeMagasin;
    }

    public void setTypeMagasin(String typeMagasin) {
        this.typeMagasin = typeMagasin;
    }

    public LocalTime getHeureOuverture() {
        return heureOuverture;
    }

    public void setHeureOuverture(LocalTime heureOuverture) {
        this.heureOuverture = heureOuverture;
    }

    public LocalTime getHeureFermeture() {
        return heureFermeture;
    }

    public void setHeureFermeture(LocalTime heureFermeture) {
        this.heureFermeture = heureFermeture;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDate getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(LocalDate dateOuverture) {
        this.dateOuverture = dateOuverture;
    }

    public int getNombreRedemptions() {
        return nombreRedemptions;
    }

    public void setNombreRedemptions(int nombreRedemptions) {
        this.nombreRedemptions = nombreRedemptions;
    }

    public double getMontantTotalRedemptions() {
        return montantTotalRedemptions;
    }

    public void setMontantTotalRedemptions(double montantTotalRedemptions) {
        this.montantTotalRedemptions = montantTotalRedemptions;
    }

    public String getHoraires() {
        if (heureOuverture != null && heureFermeture != null) {
            return heureOuverture + " - " + heureFermeture;
        }
        return "Non défini";
    }

    @Override
    public String toString() {
        return nom + " (" + code + ")";
    }
}