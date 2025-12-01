package com.vms.dao;
import com.vms.database.DatabaseConnection;
import com.vms.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class ClientDAO {
    public List<Client> getAllClients() throws SQLException {
        List<Client> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clients WHERE actif = TRUE ORDER BY nom")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }
    public int createClient(Client c) throws SQLException {
        String sql = "INSERT INTO clients (numero_compte, nom, email, telephone, adresse, contact_personne, remarques) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNumeroCompte());
            ps.setString(2, c.getNom());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getTelephone());
            ps.setString(5, c.getAdresse());
            ps.setString(6, c.getContactPersonne());
            ps.setString(7, c.getRemarques());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }
    public boolean updateClient(Client c) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE clients SET nom=?, email=?, telephone=?, adresse=?, contact_personne=?, remarques=? WHERE id=?")) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getTelephone());
            ps.setString(4, c.getAdresse());
            ps.setString(5, c.getContactPersonne());
            ps.setString(6, c.getRemarques());
            ps.setInt(7, c.getId());
            return ps.executeUpdate() > 0;
        }
    }
    private Client map(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setNumeroCompte(rs.getString("numero_compte"));
        c.setNom(rs.getString("nom"));
        c.setEmail(rs.getString("email"));
        c.setTelephone(rs.getString("telephone"));
        c.setAdresse(rs.getString("adresse"));
        c.setContactPersonne(rs.getString("contact_personne"));
        c.setRemarques(rs.getString("remarques"));
        c.setActif(rs.getBoolean("actif"));
        Date dt = rs.getDate("date_inscription");
        if (dt != null) c.setDateInscription(dt.toLocalDate());
        return c;
    }
}
