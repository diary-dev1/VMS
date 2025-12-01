package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Demande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeDAO {

    /**
     * Créer une nouvelle demande
     */
    public int createDemande(Demande demande) throws SQLException {
        String sql = "INSERT INTO demandes (reference, client_id, nombre_bons, valeur_unitaire, " +
                "montant_total, statut, date_creation, remarques) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, demande.getReference());
            pstmt.setInt(2, demande.getClientId());
            pstmt.setInt(3, demande.getNombreBons());
            pstmt.setDouble(4, demande.getValeurUnitaire());
            pstmt.setDouble(5, demande.getNombreBons() * demande.getValeurUnitaire());
            pstmt.setString(6, demande.getStatut());
            pstmt.setDate(7, java.sql.Date.valueOf(demande.getDateCreation()));
            pstmt.setString(8, demande.getRemarques());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        }
    }

    /**
     * Récupérer toutes les demandes
     */
    public List<Demande> getAllDemandes() throws SQLException {
        List<Demande> demandes = new ArrayList<>();
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "ORDER BY d.date_creation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Demande demande = new Demande();
                demande.setId(rs.getInt("id"));
                demande.setReference(rs.getString("reference"));
                demande.setClientId(rs.getInt("client_id"));
                demande.setClientNom(rs.getString("client_nom"));
                demande.setNombreBons(rs.getInt("nombre_bons"));
                demande.setValeurUnitaire(rs.getDouble("valeur_unitaire"));
                demande.setStatut(rs.getString("statut"));

                Date dateCreation = rs.getDate("date_creation");
                if (dateCreation != null) {
                    demande.setDateCreation(dateCreation.toLocalDate());
                }

                Date datePaiement = rs.getDate("date_paiement");
                if (datePaiement != null) {
                    demande.setDatePaiement(datePaiement.toLocalDate());
                }

                Date dateApprobation = rs.getDate("date_approbation");
                if (dateApprobation != null) {
                    demande.setDateApprobation(dateApprobation.toLocalDate());
                }

                demande.setRemarques(rs.getString("remarques"));

                demandes.add(demande);
            }
        }

        return demandes;
    }

    /**
     * Récupérer une demande par ID
     */
    public Demande getDemandeById(int id) throws SQLException {
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "WHERE d.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Demande demande = new Demande();
                    demande.setId(rs.getInt("id"));
                    demande.setReference(rs.getString("reference"));
                    demande.setClientId(rs.getInt("client_id"));
                    demande.setClientNom(rs.getString("client_nom"));
                    demande.setNombreBons(rs.getInt("nombre_bons"));
                    demande.setValeurUnitaire(rs.getDouble("valeur_unitaire"));
                    demande.setStatut(rs.getString("statut"));

                    Date dateCreation = rs.getDate("date_creation");
                    if (dateCreation != null) {
                        demande.setDateCreation(dateCreation.toLocalDate());
                    }

                    Date datePaiement = rs.getDate("date_paiement");
                    if (datePaiement != null) {
                        demande.setDatePaiement(datePaiement.toLocalDate());
                    }

                    Date dateApprobation = rs.getDate("date_approbation");
                    if (dateApprobation != null) {
                        demande.setDateApprobation(dateApprobation.toLocalDate());
                    }

                    demande.setRemarques(rs.getString("remarques"));

                    return demande;
                }
            }
        }

        return null;
    }

    /**
     * Mettre à jour une demande
     */
    public boolean updateDemande(Demande demande) throws SQLException {
        String sql = "UPDATE demandes SET " +
                "statut = ?, " +
                "date_paiement = ?, " +
                "date_approbation = ?, " +
                "remarques = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, demande.getStatut());
            pstmt.setDate(2, demande.getDatePaiement() != null ?
                    java.sql.Date.valueOf(demande.getDatePaiement()) : null);
            pstmt.setDate(3, demande.getDateApprobation() != null ?
                    java.sql.Date.valueOf(demande.getDateApprobation()) : null);
            pstmt.setString(4, demande.getRemarques());
            pstmt.setInt(5, demande.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Supprimer une demande
     */
    public boolean deleteDemande(int id) throws SQLException {
        String sql = "DELETE FROM demandes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Récupérer les demandes par statut
     */
    public List<Demande> getDemandesByStatut(String statut) throws SQLException {
        List<Demande> demandes = new ArrayList<>();
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "WHERE d.statut = ? " +
                "ORDER BY d.date_creation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, statut);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Demande demande = new Demande();
                    demande.setId(rs.getInt("id"));
                    demande.setReference(rs.getString("reference"));
                    demande.setClientId(rs.getInt("client_id"));
                    demande.setClientNom(rs.getString("client_nom"));
                    demande.setNombreBons(rs.getInt("nombre_bons"));
                    demande.setValeurUnitaire(rs.getDouble("valeur_unitaire"));
                    demande.setStatut(rs.getString("statut"));

                    Date dateCreation = rs.getDate("date_creation");
                    if (dateCreation != null) {
                        demande.setDateCreation(dateCreation.toLocalDate());
                    }

                    demande.setRemarques(rs.getString("remarques"));

                    demandes.add(demande);
                }
            }
        }

        return demandes;
    }
}
