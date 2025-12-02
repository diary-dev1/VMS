package com.vms.model;

import java.time.LocalDate;

/**
 * Classe modèle pour un Client
 */
public class Client {

    private int id;
    private String code;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String contactPersonne;
    private String numeroCompte;
    private LocalDate dateInscription;
    private LocalDate dateNaissance;
    private String typeClient;
    private String statut;
    private boolean actif;
    private String remarques;
    private int nombreDemandesTotal;
    private double montantTotal;

    public Client() {
        this.dateInscription = LocalDate.now();
        this.actif = true;
        this.statut = "Actif";
    }

    public Client(int id, String nom, String email, String telephone,
                  String adresse, String contactPersonne, LocalDate dateInscription) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.contactPersonne = contactPersonne;
        this.dateInscription = dateInscription;
        this.actif = true;
        this.statut = "Actif";
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getContactPersonne() {
        return contactPersonne;
    }

    public void setContactPersonne(String contactPersonne) {
        this.contactPersonne = contactPersonne;
    }

    public String getNumeroCompte() {
        return numeroCompte;
    }

    public void setNumeroCompte(String numeroCompte) {
        this.numeroCompte = numeroCompte;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getStatut() {
        return statut != null ? statut : (actif ? "Actif" : "Inactif");
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    public int getNombreDemandesTotal() {
        return nombreDemandesTotal;
    }

    public void setNombreDemandesTotal(int nombreDemandesTotal) {
        this.nombreDemandesTotal = nombreDemandesTotal;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public void genererNumeroCompte(int sequence) {
        this.numeroCompte = String.format("CLI%05d", sequence);
    }

    @Override
    public String toString() {
        return nom;
    }
}