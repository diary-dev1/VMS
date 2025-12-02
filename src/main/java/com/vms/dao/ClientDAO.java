package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Client;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM clients ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                clients.add(extractClientFromResultSet(rs));
            }
        }

        return clients;
    }

    public Client getClientById(int id) throws SQLException {
        String query = "SELECT * FROM clients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractClientFromResultSet(rs);
            }
        }

        return null;
    }

    public Client getClientByNumeroCompte(String numeroCompte) throws SQLException {
        String query = "SELECT * FROM clients WHERE numero_compte = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, numeroCompte);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractClientFromResultSet(rs);
            }
        }

        return null;
    }

    public int createClient(Client client) throws SQLException {
        String query = "INSERT INTO clients (numero_compte, nom, telephone, email, adresse, " +
                "contact_personne, date_inscription, actif, remarques) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, client.getNumeroCompte());
            pstmt.setString(2, client.getNom());
            pstmt.setString(3, client.getTelephone());
            pstmt.setString(4, client.getEmail());
            pstmt.setString(5, client.getAdresse());
            pstmt.setString(6, client.getContactPersonne());
            pstmt.setDate(7, Date.valueOf(client.getDateInscription()));
            pstmt.setBoolean(8, client.isActif());
            pstmt.setString(9, client.getRemarques());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public boolean updateClient(Client client) throws SQLException {
        String query = "UPDATE clients SET nom = ?, telephone = ?, email = ?, " +
                "adresse = ?, contact_personne = ?, actif = ?, remarques = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, client.getNom());
            pstmt.setString(2, client.getTelephone());
            pstmt.setString(3, client.getEmail());
            pstmt.setString(4, client.getAdresse());
            pstmt.setString(5, client.getContactPersonne());
            pstmt.setBoolean(6, client.isActif());
            pstmt.setString(7, client.getRemarques());
            pstmt.setInt(8, client.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteClient(int id) throws SQLException {
        String query = "DELETE FROM clients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Client> searchClients(String keyword) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM clients WHERE nom LIKE ? OR numero_compte LIKE ? OR telephone LIKE ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                clients.add(extractClientFromResultSet(rs));
            }
        }

        return clients;
    }

    public List<Client> getClientsActifs() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM clients WHERE actif = true ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                clients.add(extractClientFromResultSet(rs));
            }
        }

        return clients;
    }

    private Client extractClientFromResultSet(ResultSet rs) throws SQLException {
        Client client = new Client();

        // Colonnes qui existent dans ta table
        client.setId(rs.getInt("id"));
        client.setNumeroCompte(rs.getString("numero_compte"));
        client.setNom(rs.getString("nom"));
        client.setEmail(rs.getString("email"));
        client.setTelephone(rs.getString("telephone"));
        client.setAdresse(rs.getString("adresse"));
        client.setContactPersonne(rs.getString("contact_personne"));
        client.setActif(rs.getBoolean("actif"));
        client.setRemarques(rs.getString("remarques"));

        Date dateInscription = rs.getDate("date_inscription");
        if (dateInscription != null) {
            client.setDateInscription(dateInscription.toLocalDate());
        }

        // Générer un code basé sur l'ID (pour compatibilité avec le reste du code)
        client.setCode("CLI-" + client.getId());

        // Statut basé sur actif
        client.setStatut(client.isActif() ? "ACTIF" : "INACTIF");

        return client;
    }
}