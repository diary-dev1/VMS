package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Utilisateur;

import java.sql.*;

public class UtilisateurDAO {

    /**
     * Créer un nouvel utilisateur
     */
    public int createUtilisateur(Utilisateur utilisateur) throws SQLException {
        String sql = "INSERT INTO utilisateurs (username, password_hash, nom_complet, email, role, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, utilisateur.getUsername());
            pstmt.setString(2, utilisateur.getPasswordHash());
            pstmt.setString(3, utilisateur.getNomComplet());
            pstmt.setString(4, utilisateur.getEmail());
            pstmt.setString(5, utilisateur.getRole());
            pstmt.setBoolean(6, utilisateur.isActif());

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
     * Vérifier les identifiants de connexion
     */
    public Utilisateur authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE username = ? AND password_hash = ? AND actif = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(rs.getInt("id"));
                    utilisateur.setUsername(rs.getString("username"));
                    utilisateur.setNomComplet(rs.getString("nom_complet"));
                    utilisateur.setEmail(rs.getString("email"));
                    utilisateur.setRole(rs.getString("role"));
                    utilisateur.setActif(rs.getBoolean("actif"));
                    return utilisateur;
                }
            }
        }

        return null;
    }
}