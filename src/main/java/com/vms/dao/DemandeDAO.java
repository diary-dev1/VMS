package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Demande;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeDAO {

    public int createDemande(Demande demande) throws SQLException {
        String sql = "INSERT INTO demandes (reference, client_id, nombre_bons, valeur_unitaire, " +
                "montant_total, statut, date_creation, remarques) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public List<Demande> getAllDemandes() throws SQLException {
        List<Demande> demandes = new ArrayList<>();
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "ORDER BY d.date_creation DESC";

        Connection conn = DatabaseConnection.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                demandes.add(extractDemandeFromResultSet(rs));
            }
        }
        return demandes;
    }

    public Demande getDemandeById(int id) throws SQLException {
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "WHERE d.id = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractDemandeFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean updateDemande(Demande demande) throws SQLException {
        String sql = "UPDATE demandes SET " +
                "statut = ?, " +
                "date_paiement = ?, " +
                "date_approbation = ?, " +
                "remarques = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, demande.getStatut());
            pstmt.setDate(2, demande.getDatePaiement() != null ?
                    java.sql.Date.valueOf(demande.getDatePaiement()) : null);
            pstmt.setDate(3, demande.getDateApprobation() != null ?
                    java.sql.Date.valueOf(demande.getDateApprobation()) : null);
            pstmt.setString(4, demande.getRemarques());
            pstmt.setInt(5, demande.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteDemande(int id) throws SQLException {
        String sql = "DELETE FROM demandes WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Demande> getDemandesByStatut(String statut) throws SQLException {
        List<Demande> demandes = new ArrayList<>();
        String sql = "SELECT d.*, c.nom as client_nom FROM demandes d " +
                "LEFT JOIN clients c ON d.client_id = c.id " +
                "WHERE d.statut = ? " +
                "ORDER BY d.date_creation DESC";

        Connection conn = DatabaseConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statut);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(extractDemandeFromResultSet(rs));
                }
            }
        }
        return demandes;
    }

    // ← méthode extraite pour éviter la duplication de code
    private Demande extractDemandeFromResultSet(ResultSet rs) throws SQLException {
        Demande demande = new Demande();
        demande.setId(rs.getInt("id"));
        demande.setReference(rs.getString("reference"));
        demande.setClientId(rs.getInt("client_id"));
        demande.setClientNom(rs.getString("client_nom"));
        demande.setNombreBons(rs.getInt("nombre_bons"));
        demande.setValeurUnitaire(rs.getDouble("valeur_unitaire"));
        demande.setStatut(rs.getString("statut"));
        demande.setRemarques(rs.getString("remarques"));

        Date dateCreation = rs.getDate("date_creation");
        if (dateCreation != null) demande.setDateCreation(dateCreation.toLocalDate());

        Date datePaiement = rs.getDate("date_paiement");
        if (datePaiement != null) demande.setDatePaiement(datePaiement.toLocalDate());

        Date dateApprobation = rs.getDate("date_approbation");
        if (dateApprobation != null) demande.setDateApprobation(dateApprobation.toLocalDate());

        return demande;
    }
}