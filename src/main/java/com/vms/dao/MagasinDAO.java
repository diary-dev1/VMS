package com.vms.dao;

import com.vms.database.DatabaseConnection;
import com.vms.model.Magasin;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MagasinDAO {

    public List<Magasin> getAllMagasins() throws SQLException {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM magasins ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }
        }

        return magasins;
    }

    public Magasin getMagasinById(int id) throws SQLException {
        String query = "SELECT * FROM magasins WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMagasinFromResultSet(rs);
            }
        }

        return null;
    }

    public Magasin getMagasinByCode(String code) throws SQLException {
        String query = "SELECT * FROM magasins WHERE code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMagasinFromResultSet(rs);
            }
        }

        return null;
    }

    public int createMagasin(Magasin magasin) throws SQLException {
        String query = "INSERT INTO magasins (code, nom, adresse, ville, telephone, " +
                "responsable, type_magasin, heure_ouverture, heure_fermeture, actif, date_ouverture) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, magasin.getCode());
            pstmt.setString(2, magasin.getNom());
            pstmt.setString(3, magasin.getAdresse());
            pstmt.setString(4, magasin.getVille());
            pstmt.setString(5, magasin.getTelephone());
            pstmt.setString(6, magasin.getResponsable());
            pstmt.setString(7, magasin.getTypeMagasin());
            pstmt.setObject(8, magasin.getHeureOuverture());
            pstmt.setObject(9, magasin.getHeureFermeture());
            pstmt.setBoolean(10, magasin.isActif());
            pstmt.setDate(11, Date.valueOf(magasin.getDateOuverture()));

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

    public boolean updateMagasin(Magasin magasin) throws SQLException {
        String query = "UPDATE magasins SET nom = ?, adresse = ?, ville = ?, telephone = ?, " +
                "responsable = ?, type_magasin = ?, heure_ouverture = ?, " +
                "heure_fermeture = ?, actif = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, magasin.getNom());
            pstmt.setString(2, magasin.getAdresse());
            pstmt.setString(3, magasin.getVille());
            pstmt.setString(4, magasin.getTelephone());
            pstmt.setString(5, magasin.getResponsable());
            pstmt.setString(6, magasin.getTypeMagasin());
            pstmt.setObject(7, magasin.getHeureOuverture());
            pstmt.setObject(8, magasin.getHeureFermeture());
            pstmt.setBoolean(9, magasin.isActif());
            pstmt.setInt(10, magasin.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMagasin(int id) throws SQLException {
        String query = "DELETE FROM magasins WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Magasin> searchMagasins(String keyword) throws SQLException {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM magasins WHERE nom LIKE ? OR code LIKE ? OR ville LIKE ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }
        }

        return magasins;
    }

    public List<Magasin> getMagasinsByVille(String ville) throws SQLException {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM magasins WHERE ville = ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, ville);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }
        }

        return magasins;
    }

    public List<Magasin> getMagasinsActifs() throws SQLException {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM magasins WHERE actif = true ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }
        }

        return magasins;
    }

    private Magasin extractMagasinFromResultSet(ResultSet rs) throws SQLException {
        Magasin magasin = new Magasin();
        magasin.setId(rs.getInt("id"));
        magasin.setCode(rs.getString("code"));
        magasin.setNom(rs.getString("nom"));
        magasin.setAdresse(rs.getString("adresse"));
        magasin.setVille(rs.getString("ville"));
        magasin.setTelephone(rs.getString("telephone"));
        magasin.setResponsable(rs.getString("responsable"));
        magasin.setTypeMagasin(rs.getString("type_magasin"));

        Time heureOuverture = rs.getTime("heure_ouverture");
        if (heureOuverture != null) {
            magasin.setHeureOuverture(heureOuverture.toLocalTime());
        }

        Time heureFermeture = rs.getTime("heure_fermeture");
        if (heureFermeture != null) {
            magasin.setHeureFermeture(heureFermeture.toLocalTime());
        }

        magasin.setActif(rs.getBoolean("actif"));

        Date dateOuverture = rs.getDate("date_ouverture");
        if (dateOuverture != null) {
            magasin.setDateOuverture(dateOuverture.toLocalDate());
        }

        return magasin;
    }
}