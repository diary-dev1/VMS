package com.vms.model;

import java.time.LocalDate;

/**
 * Classe modèle pour une Demande de bons cadeau
 */
public class Demande {
    
    private int id;
    private String reference;  // Ex: VR0048-200
    private int clientId;
    private String clientNom;
    private int nombreBons;
    private double valeurUnitaire;
    private double montantTotal;
    private String statut;  // EN_ATTENTE_PAIEMENT, PAYE, APPROUVE, GENERE
    private LocalDate dateCreation;
    private LocalDate datePaiement;
    private LocalDate dateApprobation;
    private String creePar;
    private String remarques;

    // Constructeur vide
    public Demande() {
        this.dateCreation = LocalDate.now();
        this.statut = "EN_ATTENTE_PAIEMENT";
    }

    // Constructeur complet
    public Demande(int id, String reference, int clientId, String clientNom, 
                   int nombreBons, double valeurUnitaire, String statut, 
                   LocalDate dateCreation) {
        this.id = id;
        this.reference = reference;
        this.clientId = clientId;
        this.clientNom = clientNom;
        this.nombreBons = nombreBons;
        this.valeurUnitaire = valeurUnitaire;
        this.montantTotal = nombreBons * valeurUnitaire;
        this.statut = statut;
        this.dateCreation = dateCreation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    public int getNombreBons() {
        return nombreBons;
    }

    public void setNombreBons(int nombreBons) {
        this.nombreBons = nombreBons;
        calculerMontantTotal();
    }

    public double getValeurUnitaire() {
        return valeurUnitaire;
    }

    public void setValeurUnitaire(double valeurUnitaire) {
        this.valeurUnitaire = valeurUnitaire;
        calculerMontantTotal();
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public LocalDate getDateApprobation() {
        return dateApprobation;
    }

    public void setDateApprobation(LocalDate dateApprobation) {
        this.dateApprobation = dateApprobation;
    }

    public String getCreePar() {
        return creePar;
    }

    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }

    // Méthodes utilitaires
    private void calculerMontantTotal() {
        this.montantTotal = this.nombreBons * this.valeurUnitaire;
    }

    /**
     * Génère une référence unique pour la demande
     * Format: VR0001-NB (VR + numéro séquentiel + nombre de bons)
     */
    public void genererReference(int numeroSequence) {
        this.reference = String.format("VR%04d-%d", numeroSequence, this.nombreBons);
    }

    /**
     * Retourne le statut formaté pour l'affichage
     */
    public String getStatutFormate() {
        switch (statut) {
            case "EN_ATTENTE_PAIEMENT":
                return "En attente de paiement";
            case "PAYE":
                return "Payé";
            case "APPROUVE":
                return "Approuvé";
            case "GENERE":
                return "Bons générés";
            default:
                return statut;
        }
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", clientNom='" + clientNom + '\'' +
                ", nombreBons=" + nombreBons +
                ", valeurUnitaire=" + valeurUnitaire +
                ", montantTotal=" + montantTotal +
                ", statut='" + statut + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
