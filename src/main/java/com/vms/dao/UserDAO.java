package com.vms.dao;

import com.vms.model.User;
import com.vms.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==================== CREATE ====================
    /**
     * Créer un nouvel utilisateur
     */
    public boolean create(User user) {
        String sql = "INSERT INTO utilisateurs (username, password_hash, nom_complet, email, role, actif, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getNomComplet());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isActif());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ==================== READ ====================
    /**
     * Récupérer un utilisateur par son ID
     */
    public User findById(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer un utilisateur par son username (pour login)
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM utilisateurs WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer un utilisateur par son email
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de tous les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }


    public List<User> findAllActive() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE actif = TRUE ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs actifs: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Récupérer les utilisateurs par rôle
     */
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs WHERE role = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par rôle: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Compter le nombre total d'utilisateurs
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM utilisateurs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Compter les utilisateurs par rôle
     */
    public int countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage par rôle: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    /**
     * Authentifier un utilisateur
     */
    public User authenticate(String username, String password) throws SQLException {
        User user = findByUsername(username);

        if (user != null && user.isActif()) {
            // ⚠️ Comparaison simple (temporaire - pas sécurisé)
            if (user.getPasswordHash().equals(password)) {
                // Mettre à jour la dernière connexion
                updateLastLogin(user.getId());
                return user;
            }
        }

        return null;
    }
    // ==================== UPDATE ====================
    /**
     * Mettre à jour un utilisateur (sans changer le mot de passe)
     */
    public boolean update(User user) {
        String sql = "UPDATE utilisateurs SET username = ?, nom_complet = ?, email = ?, " +
                "role = ?, actif = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getNomComplet());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setBoolean(5, user.isActif());
            stmt.setInt(6, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour le mot de passe d'un utilisateur
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE utilisateurs SET password_hash = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour la dernière connexion
     */
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE utilisateurs SET derniere_connexion = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la dernière connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Activer/Désactiver un utilisateur
     */
    public boolean toggleActive(int userId) {
        String sql = "UPDATE utilisateurs SET actif = NOT actif, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de statut: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Désactiver un utilisateur
     */
    public boolean deactivate(int userId) {
        String sql = "UPDATE utilisateurs SET actif = FALSE, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ==================== DELETE ====================
    /**
     * Supprimer définitivement un utilisateur (ATTENTION: suppression physique)
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprimer logiquement (désactiver) un utilisateur - RECOMMANDÉ
     */
    public boolean softDelete(int userId) {
        return deactivate(userId);
    }

    // ==================== VALIDATION ====================
    /**
     * Vérifier si un username existe déjà
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du username: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifier si un username existe (en excluant un ID spécifique pour l'édition)
     */
    public boolean usernameExistsExcept(String username, int userId) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE username = ? AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du username: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifier si un email existe déjà
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean emailExistsExcept(String email, int userId) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ? AND id != ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ==================== UTILITAIRE ====================
    /**
     * Mapper un ResultSet vers un objet User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setNomComplet(rs.getString("nom_complet"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setActif(rs.getBoolean("actif"));

        // Gestion des timestamps qui peuvent être NULL
        Timestamp derniereConnexion = rs.getTimestamp("derniere_connexion");
        if (derniereConnexion != null) {
            user.setDerniereConnexion(derniereConnexion.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return user;
    }

}